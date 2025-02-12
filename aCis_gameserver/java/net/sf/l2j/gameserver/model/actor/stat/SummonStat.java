package net.sf.l2j.gameserver.model.actor.stat;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Summon;

public class SummonStat extends PlayableStat {

	public SummonStat(Summon activeChar) {
		super(activeChar);
	}

	@Override
	public Summon getActiveChar() {
		return (Summon) super.getActiveChar();
	}
}
