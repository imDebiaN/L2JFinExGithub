package net.sf.l2j.gameserver.handler.admincommandhandlers;

import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class AdminLevel implements IAdminCommandHandler {

	private static final String[] ADMIN_COMMANDS
			= {
				"admin_addlevel",
				"admin_setlevel"
			};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (activeChar == null) {
			return false;
		}

		WorldObject targetChar = activeChar.getTarget();

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		String val = "";
		if (st.countTokens() >= 1) {
			val = st.nextToken();
		}

		if (actualCommand.equalsIgnoreCase("admin_addlevel")) {
			try {
				if (targetChar instanceof Playable) {
					((Playable) targetChar).getStat().addLevel(Byte.parseByte(val));
				}
			} catch (NumberFormatException e) {
				activeChar.sendMessage("Wrong number format.");
				return false;
			}
		} else if (actualCommand.equalsIgnoreCase("admin_setlevel")) {
			try {
				if (targetChar == null || !(targetChar instanceof Player)) {
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT); // incorrect target!
					return false;
				}
				Player targetPlayer = (Player) targetChar;

				byte lvl = Byte.parseByte(val);
				if (lvl >= 1 && lvl <= Experience.MAX_LEVEL) {
					long pXp = targetPlayer.getExp();
					long tXp = Experience.LEVEL[lvl];

					if (pXp > tXp) {
						targetPlayer.removeExpAndSp(pXp - tXp, 0);
					} else if (pXp < tXp) {
						targetPlayer.addExpAndSp(tXp - pXp, 0);
					}
				} else {
					activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".");
					return false;
				}
			} catch (NumberFormatException e) {
				activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".");
				return false;
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
}
