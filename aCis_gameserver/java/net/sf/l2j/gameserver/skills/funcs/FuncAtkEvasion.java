package net.sf.l2j.gameserver.skills.funcs;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncAtkEvasion extends Func {

	static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

	public static Func getInstance() {
		return _fae_instance;
	}

	private FuncAtkEvasion() {
		super(Stats.Evasion, 0x10, null, null);
	}

	@Override
	public void calc(Env env) {
		env.addValue(Formulas.BASE_EVASION_ACCURACY[env.getCharacter().getDEX()] + env.getCharacter().getLevel());
	}
}
