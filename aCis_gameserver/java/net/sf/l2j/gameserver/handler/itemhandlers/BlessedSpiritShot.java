package net.sf.l2j.gameserver.handler.itemhandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

public class BlessedSpiritShot implements IItemHandler {

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		final Player activeChar = (Player) playable;
		final ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final Weapon weaponItem = activeChar.getActiveWeaponItem();
		final int itemId = item.getItemId();

		// Check if bss can be used
		if (weaponInst == null || weaponItem == null || weaponItem.getSpiritShotCount() == 0) {
			if (!activeChar.getAutoSoulShot().contains(itemId)) {
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
			}
			return;
		}

		// Check if bss is already active (it can be charged over SpiritShot)
		if (activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)) {
			return;
		}

		// Check for correct grade.
		if (weaponItem.getCrystalType() != item.getItem().getCrystalType()) {
			if (!activeChar.getAutoSoulShot().contains(itemId)) {
				activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
			}

			return;
		}

		// Consume bss if player has enough of them
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false)) {
			if (!activeChar.disableAutoShot(itemId)) {
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
			}

			return;
		}

		activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
		activeChar.setChargedShot(ShotType.BLESSED_SPIRITSHOT, true);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, item.getItem().getStaticSkills().get(0).getId(), 1, 0, 0), 600);
	}
}
