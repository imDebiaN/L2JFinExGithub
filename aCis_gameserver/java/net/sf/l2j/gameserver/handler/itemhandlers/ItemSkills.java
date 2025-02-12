package net.sf.l2j.gameserver.handler.itemhandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Pet;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.type.EtcItemType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Template for item skills handler.
 */
public class ItemSkills implements IItemHandler {

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
		if (playable instanceof Servitor) {
			return;
		}

		final boolean isPet = playable instanceof Pet;
		final Player activeChar = playable.getPlayer();

		// Pets can only use tradable items.
		if (isPet && !item.isTradable()) {
			activeChar.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}

		final L2Skill itemSkill = item.getEtcItem().getStaticSkills().get(0).getSkill();
		if (itemSkill == null) {
			_log.info(item.getName() + " does not have registered any skill for handler.");
			return;
		}

		if (!itemSkill.checkCondition(playable, playable.getTarget(), false)) {
			return;
		}

		// No message on retail, the use is just forgotten.
		if (playable.isSkillDisabled(itemSkill)) {
			return;
		}

		if (!itemSkill.isPotion() && playable.isCastingNow()) {
			return;
		}

		// Item consumption is setup here.
		if (itemSkill.isPotion() || itemSkill.isSimultaneousCast()) {
			if (!item.isHerb()) {
				// Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
				if (!playable.destroyItem("Consume", item.getObjectId(), (itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsume() > 0) ? itemSkill.getItemConsume() : 1, null, false)) {
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					return;
				}
			}

			playable.doCast(itemSkill, true);
			// Summons should be affected by herbs too, self time effect is handled at L2Effect constructor.
			if (!isPet && item.getItemType() == EtcItemType.HERB && activeChar.hasServitor()) {
				activeChar.getActiveSummon().doCast(itemSkill, true);
			}
		} else {
			// Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
			if (!playable.destroyItem("Consume", item.getObjectId(), (itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsume() > 0) ? itemSkill.getItemConsume() : 1, null, false)) {
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}

			playable.getAI().setIntention(CtrlIntention.IDLE);
			if (!playable.useMagic(itemSkill, forceUse, false)) {
				return;
			}
		}

		// Send message to owner.
		if (isPet) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(itemSkill));
		} else {
			final int skillId = itemSkill.getId();

			// Buff icon for healing potions.
			switch (skillId) {
				case 2031:
				case 2032:
				case 2037:
					final int buffId = activeChar.getShortBuffTaskSkillId();
					if (skillId == 2037) {
						// Greater healing potions.
						activeChar.shortBuffStatusUpdate(skillId, itemSkill.getLevel(), itemSkill.getBuffDuration() / 1000);
					} else if (skillId == 2032 && buffId != 2037) {
						// Healing potions.
						activeChar.shortBuffStatusUpdate(skillId, itemSkill.getLevel(), itemSkill.getBuffDuration() / 1000);
					} else if (buffId != 2037 && buffId != 2032) {
						// Lesser healing potions.
						activeChar.shortBuffStatusUpdate(skillId, itemSkill.getLevel(), itemSkill.getBuffDuration() / 1000);
					}
					break;
			}
		}

		// Reuse.
		int reuseDelay = itemSkill.getReuseDelay();
		if (item.isEtcItem()) {
			if (item.getEtcItem().getReuseDelay() > reuseDelay) {
				reuseDelay = item.getEtcItem().getReuseDelay();
			}

			playable.addTimeStamp(itemSkill, reuseDelay);
			if (reuseDelay != 0) {
				playable.disableSkill(itemSkill, reuseDelay);
			}

			if (!isPet) {
				final int group = item.getEtcItem().getSharedReuseGroup();
				if (group >= 0) {
					activeChar.sendPacket(new ExUseSharedGroupItem(item.getItemId(), group, reuseDelay, reuseDelay));
				}
			}
		} else if (reuseDelay > 0) {
			playable.addTimeStamp(itemSkill, reuseDelay);
			playable.disableSkill(itemSkill, reuseDelay);
		}
	}
}
