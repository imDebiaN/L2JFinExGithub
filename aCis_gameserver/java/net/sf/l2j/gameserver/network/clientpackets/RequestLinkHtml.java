package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author zabbix Lets drink to code!
 */
public final class RequestLinkHtml extends L2GameClientPacket
{
	private String _link;
	
	@Override
	protected void readImpl()
	{
		_link = readS();
	}
	
	@Override
	public void runImpl()
	{
		final Player actor = getClient().getActiveChar();
		if (actor == null)
			return;
		
		if (_link.contains("..") || !_link.contains(".htm"))
			return;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(_link);
		sendPacket(html);
	}
}