package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestTargetCanceld extends L2GameClientPacket {

	private int _unselect;

	@Override
	protected void readImpl() {
		_unselect = readH();
	}

	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		if (_unselect == 0) {
			if (activeChar.isCastingNow() && activeChar.canAbortCast()) {
				activeChar.abortCast();
			} else {
				activeChar.setTarget(null);
			}
		} else {
			activeChar.setTarget(null);
		}
	}
}
