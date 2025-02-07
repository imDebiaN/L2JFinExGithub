package net.sf.l2j.gameserver.skills.effects;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.skills.Effect;
import net.sf.l2j.gameserver.skills.EffectTemplate;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.L2Effect;
import net.sf.l2j.gameserver.templates.skills.L2EffectType;

/**
 * @author demonia
 */
@Effect("ImobilePetBuff")
final class EffectImobilePetBuff extends L2Effect {

	private Summon _pet;

	public EffectImobilePetBuff(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType() {
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onStart() {
		_pet = null;

		if (getEffected() instanceof Summon && getEffector() instanceof Player && ((Summon) getEffected()).getPlayer() == getEffector()) {
			_pet = (Summon) getEffected();
			_pet.setIsImmobilized(true);
			return true;
		}
		return false;
	}

	@Override
	public void onExit() {
		_pet.setIsImmobilized(false);
	}

	@Override
	public boolean onActionTime() {
		return false;
	}
}
