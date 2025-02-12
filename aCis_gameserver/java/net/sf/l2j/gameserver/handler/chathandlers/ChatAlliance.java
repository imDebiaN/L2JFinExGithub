package net.sf.l2j.gameserver.handler.chathandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatAlliance implements IChatHandler {

	private static final int[] COMMAND_IDS
			= {
				9
			};

	@Override
	public void handleChat(int type, Player activeChar, String target, String text) {
		if (activeChar.getClan() == null || activeChar.getClan().getAllyId() == 0) {
			return;
		}

		activeChar.getClan().broadcastToOnlineAllyMembers(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
	}

	@Override
	public int[] getChatTypeList() {
		return COMMAND_IDS;
	}
}
