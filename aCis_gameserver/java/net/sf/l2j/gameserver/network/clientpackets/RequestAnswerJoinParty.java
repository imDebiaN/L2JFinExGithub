package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.network.serverpackets.ExManagePartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.JoinParty;

public final class RequestAnswerJoinParty extends L2GameClientPacket {

	private int _response;

	@Override
	protected void readImpl() {
		_response = readD();
	}

	@Override
	protected void runImpl() {
		final Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}

		final Player requestor = player.getActiveRequester();
		if (requestor == null) {
			return;
		}

		requestor.sendPacket(new JoinParty(_response));

		Party party = requestor.getParty();
		if (_response == 1) {
			if (party == null) {
				party = new Party(requestor, player, requestor.getLootRule());
			} else {
				party.addPartyMember(player);
			}

			if (requestor.isInPartyMatchRoom()) {
				final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
				if (list != null) {
					final PartyMatchRoom room = list.getPlayerRoom(requestor);
					if (room != null) {
						if (player.isInPartyMatchRoom()) {
							if (list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player)) {
								final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
								for (Player member : room.getPartyMembers()) {
									member.sendPacket(packet);
								}
							}
						} else {
							room.addMember(player);

							final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
							for (Player member : room.getPartyMembers()) {
								member.sendPacket(packet);
							}

							player.setPartyRoom(room.getId());
							player.broadcastUserInfo();
						}
					}
				}
			}
		}

		// Must be kept out of "ok" answer, can't be merged with higher content.
		if (party != null) {
			party.setPendingInvitation(false);
		}

		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
