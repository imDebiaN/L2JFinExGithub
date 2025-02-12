package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

/**
 * Format: ch - Trigger packet
 *
 * @author KenM
 */
public class ExShowVariationCancelWindow extends L2GameServerPacket {

	public static final ExShowVariationCancelWindow STATIC_PACKET = new ExShowVariationCancelWindow();

	private ExShowVariationCancelWindow() {
	}

	@Override
	protected void writeImpl() {
		writeC(0xfe);
		writeH(0x51);
	}
}
