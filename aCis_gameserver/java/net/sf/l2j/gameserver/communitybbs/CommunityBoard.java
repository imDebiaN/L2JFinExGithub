package net.sf.l2j.gameserver.communitybbs;

import org.slf4j.LoggerFactory;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.ClanBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.FriendsBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.MailBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.PostBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.TopBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.TopicBBSManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class CommunityBoard {

	protected CommunityBoard() {
	}

	public static CommunityBoard getInstance() {
		return SingletonHolder._instance;
	}

	public void handleCommands(L2GameClient client, String command) {
		final Player activeChar = client.getActiveChar();
		if (activeChar == null) {
			return;
		}

		if (!Config.ENABLE_COMMUNITY_BOARD) {
			activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
			return;
		}

		if (command.startsWith("_bbshome")) {
			TopBBSManager.getInstance().parseCmd(command, activeChar);
		} else if (command.startsWith("_bbsloc")) {
			RegionBBSManager.getInstance().parseCmd(command, activeChar);
		} else if (command.startsWith("_bbsclan")) {
			ClanBBSManager.getInstance().parseCmd(command, activeChar);
		} else if (command.startsWith("_bbsmemo")) {
			TopicBBSManager.getInstance().parseCmd(command, activeChar);
		} else if (command.startsWith("_bbsmail") || command.equals("_maillist_0_1_0_")) {
			MailBBSManager.getInstance().parseCmd(command, activeChar);
		} else if (command.startsWith("_friend") || command.startsWith("_block")) {
			FriendsBBSManager.getInstance().parseCmd(command, activeChar);
		} else if (command.startsWith("_bbstopics")) {
			TopicBBSManager.getInstance().parseCmd(command, activeChar);
		} else if (command.startsWith("_bbsposts")) {
			PostBBSManager.getInstance().parseCmd(command, activeChar);
		} else {
			BaseBBSManager.separateAndSend("<html><body><br><br><center>The command: " + command + " isn't implemented.</center></body></html>", activeChar);
		}
	}

	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5) {
		final Player activeChar = client.getActiveChar();
		if (activeChar == null) {
			return;
		}

		if (!Config.ENABLE_COMMUNITY_BOARD) {
			activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
			return;
		}

		if (url.equals("Topic")) {
			TopicBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		} else if (url.equals("Post")) {
			PostBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		} else if (url.equals("_bbsloc")) {
			RegionBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		} else if (url.equals("_bbsclan")) {
			ClanBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		} else if (url.equals("Mail")) {
			MailBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		} else if (url.equals("_friend")) {
			FriendsBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		} else {
			BaseBBSManager.separateAndSend("<html><body><br><br><center>The command: " + url + " isn't implemented.</center></body></html>", activeChar);
		}
	}

	private static class SingletonHolder {

		protected static final CommunityBoard _instance = new CommunityBoard();
	}
}
