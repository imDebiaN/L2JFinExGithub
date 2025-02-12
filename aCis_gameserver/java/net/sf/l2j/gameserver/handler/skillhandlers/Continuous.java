package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.finex.model.talents.handlers.SonicAssault;
import net.sf.finex.model.talents.handlers.TalentHandler;
import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.ClanHallManagerNpc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Effect;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.templates.skills.L2EffectType;
import net.sf.l2j.gameserver.templates.skills.ESkillType;

public class Continuous implements ISkillHandler {

	private static final ESkillType[] SKILL_IDS = {
		ESkillType.BUFF,
		ESkillType.DEBUFF,
		ESkillType.DOT,
		ESkillType.MDOT,
		ESkillType.POISON,
		ESkillType.BLEED,
		ESkillType.HOT,
		ESkillType.CPHOT,
		ESkillType.MPHOT,
		ESkillType.FEAR,
		ESkillType.CONT,
		ESkillType.WEAKNESS,
		ESkillType.REFLECT,
		ESkillType.UNDEAD_DEFENSE,
		ESkillType.AGGDEBUFF,
		ESkillType.FUSION
	};

	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
		final Player player = activeChar.getPlayer();

		if (skill.getEffectId() != 0) {
			L2Skill sk = SkillTable.getInstance().getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());
			if (sk != null) {
				skill = sk;
			}
		}

		final boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
		final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);

		TalentHandler sonicAssault = null;
		if (activeChar.isPlayer()) {
			if (SonicAssault.validate(skill.getId(), player)) {
				SkillTable.FrequentTalent.SONIC_ASSAULT.getHandler().invoke(player);
			}
		}

		for (WorldObject obj : targets) {
			if (!obj.isCreature()) {
				continue;
			}

			Creature target = ((Creature) obj);
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED) {
				target = activeChar;
			}

			switch (skill.getSkillType()) {
				case BUFF:
					// Target under buff immunity.
					if (target.getFirstEffect(L2EffectType.BLOCK_BUFF) != null) {
						continue;
					}

					// Player holding a cursed weapon can't be buffed and can't buff
					if (!(activeChar instanceof ClanHallManagerNpc) && target != activeChar) {
						if (target.isPlayer()) {
							if (target.getPlayer().isCursedWeaponEquipped()) {
								continue;
							}
						} else if (player != null && player.isCursedWeaponEquipped()) {
							continue;
						}
					}
					break;

				case HOT:
				case CPHOT:
				case MPHOT:
					if (activeChar.isInvul()) {
						continue;
					}
					break;
			}

			// Target under debuff immunity.
			if (skill.isDebuff() && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) != null) {
				continue;
			}

			boolean acted = true;
			byte shld = 0;

			if (skill.isOffensive() || skill.isDebuff()) {
				shld = Formulas.calcShldUse(activeChar, target, skill);
				acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps);
			}

			if (acted) {
				if (skill.isToggle()) {
					target.stopSkillEffects(skill.getId());
				}

				// if this is a debuff let the duel manager know about it so the debuff
				// can be removed after the duel (player & target must be in the same duel)
				if (target instanceof Player && ((Player) target).isInDuel() && (skill.getSkillType() == ESkillType.DEBUFF || skill.getSkillType() == ESkillType.BUFF) && player != null && player.getDuelId() == ((Player) target).getDuelId()) {
					DuelManager dm = DuelManager.getInstance();
					for (L2Effect buff : skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps))) {
						if (buff != null) {
							dm.onBuff(((Player) target), buff);
						}
					}
				} else {
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
				}

				if (skill.getSkillType() == ESkillType.AGGDEBUFF) {
					if (target instanceof Attackable) {
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					} else if (target instanceof Playable) {
						if (target.getTarget() == activeChar) {
							target.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
						} else {
							target.setTarget(activeChar);
						}
					}
				}
			} else {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
			}

			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}

		if (skill.hasSelfEffects()) {
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect()) {
				effect.exit();
			}

			skill.getEffectsSelf(activeChar);
		}

		if (!skill.isPotion()) {
			activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
		}
	}

	@Override
	public ESkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
