package net.sf.l2j.gameserver.model.manor;

import org.slf4j.LoggerFactory;

public final class CropProcure extends SeedProduction
{
	private final int _rewardType;
	
	public CropProcure(int id, int amount, int type, int startAmount, int price)
	{
		super(id, amount, price, startAmount);
		
		_rewardType = type;
	}
	
	public final int getReward()
	{
		return _rewardType;
	}
}