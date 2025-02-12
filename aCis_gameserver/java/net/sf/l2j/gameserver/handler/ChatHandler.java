package net.sf.l2j.gameserver.handler;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.handler.chathandlers.ChatAll;
import net.sf.l2j.gameserver.handler.chathandlers.ChatAlliance;
import net.sf.l2j.gameserver.handler.chathandlers.ChatClan;
import net.sf.l2j.gameserver.handler.chathandlers.ChatHeroVoice;
import net.sf.l2j.gameserver.handler.chathandlers.ChatParty;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPartyMatchRoom;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPartyRoomAll;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPartyRoomCommander;
import net.sf.l2j.gameserver.handler.chathandlers.ChatPetition;
import net.sf.l2j.gameserver.handler.chathandlers.ChatShout;
import net.sf.l2j.gameserver.handler.chathandlers.ChatTell;
import net.sf.l2j.gameserver.handler.chathandlers.ChatTrade;

public class ChatHandler {

	private final Map<Integer, IChatHandler> _datatable = new HashMap<>();

	public static ChatHandler getInstance() {
		return SingletonHolder._instance;
	}

	protected ChatHandler() {
		registerChatHandler(new ChatAll());
		registerChatHandler(new ChatAlliance());
		registerChatHandler(new ChatClan());
		registerChatHandler(new ChatHeroVoice());
		registerChatHandler(new ChatParty());
		registerChatHandler(new ChatPartyMatchRoom());
		registerChatHandler(new ChatPartyRoomAll());
		registerChatHandler(new ChatPartyRoomCommander());
		registerChatHandler(new ChatPetition());
		registerChatHandler(new ChatShout());
		registerChatHandler(new ChatTell());
		registerChatHandler(new ChatTrade());
	}

	public void registerChatHandler(IChatHandler handler) {
		for (int id : handler.getChatTypeList()) {
			_datatable.put(id, handler);
		}
	}

	public IChatHandler getChatHandler(int chatType) {
		return _datatable.get(chatType);
	}

	public int size() {
		return _datatable.size();
	}

	private static class SingletonHolder {

		protected static final ChatHandler _instance = new ChatHandler();
	}
}
