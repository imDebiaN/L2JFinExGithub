package net.sf.l2j.gameserver.handler.admincommandhandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminMenu implements IAdminCommandHandler {

	private static final String[] ADMIN_COMMANDS
			= {
				"admin_char_manage",
				"admin_teleport_character_to_menu"
			};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (command.equals("admin_char_manage")) {
			showMainPage(activeChar);
		} else if (command.startsWith("admin_teleport_character_to_menu")) {
			String[] data = command.split(" ");
			if (data.length == 5) {
				String playerName = data[1];
				Player player = World.getInstance().getPlayer(playerName);
				if (player != null) {
					teleportCharacter(player, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), activeChar);
				}
			}
			showMainPage(activeChar);
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private static void teleportCharacter(Player player, int x, int y, int z, Player activeChar) {
		if (player != null) {
			player.sendMessage("A GM is teleporting you.");
			player.teleToLocation(x, y, z, 0);
		}
		showMainPage(activeChar);
	}

	private static void showMainPage(Player activeChar) {
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
}
