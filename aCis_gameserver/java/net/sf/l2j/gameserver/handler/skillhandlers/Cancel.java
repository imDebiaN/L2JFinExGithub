package net.sf.l2j.gameserver.handler.skillhandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Effect;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.templates.skills.ESkillType;

/**
 * @author DS
 */
public class Cancel implements ISkillHandler {

	private static final ESkillType[] SKILL_IDS
			= {
				ESkillType.CANCEL,
				ESkillType.MAGE_BANE,
				ESkillType.WARRIOR_BANE
			};

	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
		// Delimit min/max % success.
		final int minRate = (skill.getSkillType() == ESkillType.CANCEL) ? 25 : 40;
		final int maxRate = (skill.getSkillType() == ESkillType.CANCEL) ? 75 : 95;

		// Get skill power (which is used as baseRate).
		final double skillPower = skill.getPower();

		for (WorldObject obj : targets) {
			if (!(obj instanceof Creature)) {
				continue;
			}

			final Creature target = (Creature) obj;
			if (target.isDead()) {
				continue;
			}

			int lastCanceledSkillId = 0;
			int count = skill.getMaxNegatedEffects();

			// Calculate the difference of level between skill level and victim, and retrieve the vuln/prof.
			final int diffLevel = skill.getMagicLevel() - target.getLevel();
			final double skillVuln = Formulas.calcSkillVulnerability(activeChar, target, skill, skill.getSkillType());

			for (L2Effect effect : target.getAllEffects()) {
				// Don't cancel null effects or toggles.
				if (effect == null || effect.getSkill().isToggle()) {
					continue;
				}

				// Mage && Warrior Bane drop only particular stacktypes.
				switch (skill.getSkillType()) {
					case MAGE_BANE:
						if ("casting_time_down".equalsIgnoreCase(effect.getStackType())) {
							break;
						}

						if ("ma_up".equalsIgnoreCase(effect.getStackType())) {
							break;
						}

						continue;

					case WARRIOR_BANE:
						if ("attack_time_down".equalsIgnoreCase(effect.getStackType())) {
							break;
						}

						if ("speed_up".equalsIgnoreCase(effect.getStackType())) {
							break;
						}

						continue;
				}

				// If that skill effect was already canceled, continue.
				if (effect.getSkill().getId() == lastCanceledSkillId) {
					continue;
				}

				// Calculate the success chance following previous variables.
				if (calcCancelSuccess(effect.getPeriod(), diffLevel, skillPower, skillVuln, minRate, maxRate)) {
					// Stores the last canceled skill for further use.
					lastCanceledSkillId = effect.getSkill().getId();

					// Exit the effect.
					effect.exit();
				}

				// Remove 1 to the stack of buffs to remove.
				count--;

				// If the stack goes to 0, then break the loop.
				if (count == 0) {
					break;
				}
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
		activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT) ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
	}

	private static boolean calcCancelSuccess(int effectPeriod, int diffLevel, double baseRate, double vuln, int minRate, int maxRate) {
		double rate = (2 * diffLevel + baseRate + effectPeriod / 120) * vuln;

		if (Config.DEVELOPER) {
			_log.info("calcCancelSuccess(): diffLevel:" + diffLevel + ", baseRate:" + baseRate + ", vuln:" + vuln + ", total:" + rate);
		}

		if (rate < minRate) {
			rate = minRate;
		} else if (rate > maxRate) {
			rate = maxRate;
		}

		return Rnd.get(100) < rate;
	}

	@Override
	public ESkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
