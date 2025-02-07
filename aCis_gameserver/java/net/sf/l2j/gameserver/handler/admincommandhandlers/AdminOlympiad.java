package net.sf.l2j.gameserver.handler.admincommandhandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>endoly : ends olympiads manually.</li>
 * <li>sethero : set the target as a temporary hero.</li>
 * <li>setnoble : set the target as a noble.</li>
 * </ul>
 *
 */
public class AdminOlympiad implements IAdminCommandHandler {

	private static final String[] ADMIN_COMMANDS
			= {
				"admin_endoly",
				"admin_sethero",
				"admin_setnoble"
			};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (command.startsWith("admin_endoly")) {
			Olympiad.getInstance().manualSelectHeroes();
			activeChar.sendMessage("Heroes have been formed.");
		} else if (command.startsWith("admin_sethero")) {
			Player target = null;
			if (activeChar.getTarget() instanceof Player) {
				target = (Player) activeChar.getTarget();
			} else {
				target = activeChar;
			}

			target.setHero(!target.isHero());
			target.broadcastUserInfo();
			activeChar.sendMessage("You have modified " + target.getName() + "'s hero status.");
		} else if (command.startsWith("admin_setnoble")) {
			Player target = null;
			if (activeChar.getTarget() instanceof Player) {
				target = (Player) activeChar.getTarget();
			} else {
				target = activeChar;
			}

			target.setNoble(!target.isNoble(), true);
			activeChar.sendMessage("You have modified " + target.getName() + "'s noble status.");
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
}
