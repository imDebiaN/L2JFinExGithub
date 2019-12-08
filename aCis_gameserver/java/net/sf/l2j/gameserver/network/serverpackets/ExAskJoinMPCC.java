package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

/**
 * @author chris_00 Asks the player to join a CC
 */
public class ExAskJoinMPCC extends L2GameServerPacket
{
	private final String _requestorName;
	
	public ExAskJoinMPCC(String requestorName)
	{
		_requestorName = requestorName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x27);
		writeS(_requestorName);
	}
}