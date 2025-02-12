package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.Config;
import net.sf.finex.data.ManufactureItemData;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.finex.enums.EStoreType;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg;

public final class RequestRecipeShopListSet extends L2GameClientPacket {

	private int _count;
	private int[] _items;

	@Override
	protected void readImpl() {
		_count = readD();
		if (_count < 0 || _count * 8 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET) {
			_count = 0;
		}

		_items = new int[_count * 2];
		for (int x = 0; x < _count; x++) {
			int recipeID = readD();
			_items[x * 2 + 0] = recipeID;
			int cost = readD();
			_items[x * 2 + 1] = cost;
		}
	}

	@Override
	protected void runImpl() {
		final Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}

		if (player.isInDuel()) {
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			return;
		}

		if (player.isInsideZone(ZoneId.NO_STORE)) {
			player.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (_count == 0) {
			player.forceStandUp();
		} else {
			L2ManufactureList createList = new L2ManufactureList();

			for (int x = 0; x < _count; x++) {
				int recipeID = _items[x * 2 + 0];
				int cost = _items[x * 2 + 1];
				createList.add(new ManufactureItemData(recipeID, cost));
			}
			createList.setStoreName(player.getCreateList() != null ? player.getCreateList().getStoreName() : "");
			player.setCreateList(createList);

			player.setStoreType(EStoreType.MANUFACTURE);
			player.sitDown();
			player.broadcastUserInfo();
			player.broadcastPacket(new RecipeShopMsg(player));
		}
	}
}
