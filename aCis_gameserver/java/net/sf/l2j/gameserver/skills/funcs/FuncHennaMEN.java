package net.sf.l2j.gameserver.skills.funcs;

import org.slf4j.LoggerFactory;

import net.sf.finex.model.dye.DyeComponent;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncHennaMEN extends Func {

	static final FuncHennaMEN _fh_instance = new FuncHennaMEN();

	public static Func getInstance() {
		return _fh_instance;
	}

	private FuncHennaMEN() {
		super(Stats.MEN, 0x10, null, null);
	}

	@Override
	public void calc(Env env) {
		final DyeComponent dye = env.getPlayer().getComponent(DyeComponent.class);
		if (dye != null) {
			env.addValue(dye.getDyeMEN());
		}
	}
}
