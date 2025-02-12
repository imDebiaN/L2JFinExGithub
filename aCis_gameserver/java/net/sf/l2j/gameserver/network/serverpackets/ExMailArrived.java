package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

/**
 * Format: (ch) (just a trigger)
 *
 * @author -Wooden-
 */
public class ExMailArrived extends L2GameServerPacket {

	public static final ExMailArrived STATIC_PACKET = new ExMailArrived();

	private ExMailArrived() {
	}

	@Override
	protected void writeImpl() {
		writeC(0xfe);
		writeH(0x2d);
	}
}
