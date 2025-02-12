package net.sf.l2j.gameserver.scripting.scripts.teleports;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.SealType;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;

/**
 * Spawn Gatekeepers at Lilith/Anakim deaths (after a 10sec delay).<BR>
 * Despawn them after 15 minutes.
 */
public class GatekeeperSpirit extends Quest {

	private static final int ENTER_GK = 31111;
	private static final int EXIT_GK = 31112;
	private static final int LILITH = 25283;
	private static final int ANAKIM = 25286;

	public GatekeeperSpirit() {
		super(-1, "teleports");

		addStartNpc(ENTER_GK);
		addFirstTalkId(ENTER_GK);
		addTalkId(ENTER_GK);

		addKillId(LILITH, ANAKIM);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.equalsIgnoreCase("lilith_exit")) {
			addSpawn(EXIT_GK, 184446, -10112, -5488, 0, false, 900000, false);
		} else if (event.equalsIgnoreCase("anakim_exit")) {
			addSpawn(EXIT_GK, 184466, -13106, -5488, 0, false, 900000, false);
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		final CabalType playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
		final CabalType sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SealType.AVARICE);
		final CabalType winningCabal = SevenSigns.getInstance().getCabalHighestScore();

		if (playerCabal == sealAvariceOwner && playerCabal == winningCabal) {
			switch (sealAvariceOwner) {
				case DAWN:
					return "dawn.htm";

				case DUSK:
					return "dusk.htm";
			}
		}

		npc.showChatWindow(player);
		return null;
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		switch (npc.getNpcId()) {
			case LILITH:
				startQuestTimer("lilith_exit", 10000, null, null, false);
				break;

			case ANAKIM:
				startQuestTimer("anakim_exit", 10000, null, null, false);
				break;
		}
		return super.onKill(npc, killer, isPet);
	}
}
