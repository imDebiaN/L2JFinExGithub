package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.QuestState;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket {

	String _bypass;

	@Override
	protected void readImpl() {
		_bypass = readS();
	}

	@Override
	protected void runImpl() {
		final Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}

		QuestState qs = player.getQuestState("Tutorial");
		if (qs != null) {
			qs.getQuest().notifyEvent(_bypass, null, player);
		}
	}
}
