package net.sf.l2j.gameserver.scripting.scripts.teleports;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;

public class NoblesseTeleport extends Quest
{
	public NoblesseTeleport()
	{
		super(-1, "teleports");
		
		addStartNpc(30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964);
		addTalkId(30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (player.isNoble())
			return "noble.htm";
		
		return "nobleteleporter-no.htm";
	}
}