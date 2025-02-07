package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

/**
 * Format chS c: (id) 0x39 h: (subid) 0x00 S: the character name (or maybe cmd
 * string ?)
 *
 * @author -Wooden-
 */
public final class SuperCmdCharacterInfo extends L2GameClientPacket {

	@SuppressWarnings("unused")
	private String _characterName;

	@Override
	protected void readImpl() {
		_characterName = readS();
	}

	@Override
	protected void runImpl() {
	}
}
