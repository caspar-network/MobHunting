package au.com.mineauz.MobHunting.storage;

import java.util.HashSet;

import org.bukkit.entity.Player;

import au.com.mineauz.MobHunting.ExtendedMobType;
import au.com.mineauz.MobHunting.MobHunting;
import au.com.mineauz.MobHunting.achievements.Achievement;
import au.com.mineauz.MobHunting.achievements.ProgressAchievement;

public class DataStoreManager extends Thread
{
	// Accessed on multiple threads
	private HashSet<Object> mWaiting = new HashSet<Object>();
	
	// Accessed only from this thread
	private DataStore mStore;
	
	private HashSet<StatStore> mWaitingStats = new HashSet<StatStore>();
	private HashSet<AchievementStore> mWaitingAchievements = new HashSet<AchievementStore>();
	
	private int mSaveInterval;
	
	private boolean mFlush = false;
	private boolean mExit = false;
	
	public DataStoreManager(DataStore store)
	{
		mStore = store;
		mSaveInterval = MobHunting.config().savePeriod;
		
		start();
	}
	
	public void recordKill(Player player, ExtendedMobType type, boolean bonusMob)
	{
		synchronized(mWaiting)
		{
			mWaiting.add(new StatStore(type.getName() + "_kill", player.getName()));
			mWaiting.add(new StatStore("total_kill", player.getName()));
			
			if(bonusMob)
				mWaiting.add(new StatStore("BonusMob_kill", player.getName()));
		}
	}
	
	public void recordAssist(Player player, Player killer, ExtendedMobType type, boolean bonusMob)
	{
		synchronized(mWaiting)
		{
			mWaiting.add(new StatStore(type.getName() + "_assist", player.getName()));
			mWaiting.add(new StatStore("total_assist", player.getName()));
			
			if(bonusMob)
				mWaiting.add(new StatStore("BonusMob_assist", player.getName()));
		}
	}
	
	public void recordAchievement(Player player, Achievement achievement)
	{
		synchronized(mWaiting)
		{
			mWaiting.add(new AchievementStore(achievement.getID(), player.getName(), -1));
		}
	}
	
	public void recordAchievementProgress(Player player, ProgressAchievement achievement, int progress)
	{
		synchronized(mWaiting)
		{
			mWaiting.add(new AchievementStore(achievement.getID(), player.getName(), progress));
		}
	}
	
	private void queueWaiting()
	{
		synchronized(mWaiting)
		{
			mWaitingStats.clear();
			mWaitingAchievements.clear();
			
			for(Object obj : mWaiting)
			{
				if(obj instanceof StatStore)
					mWaitingStats.add((StatStore)obj);
				if(obj instanceof AchievementStore)
					mWaitingAchievements.add((AchievementStore)obj);
			}
			
			mWaiting.clear();
		}
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			synchronized(this)
			{
				if(mExit)
					break;
			}

			
			queueWaiting();
			
			try
			{
				if(!mWaitingStats.isEmpty())
					mStore.saveStats(mWaitingStats);
				
				if(!mWaitingAchievements.isEmpty())
					mStore.saveAchievements(mWaitingAchievements);
				
				Thread.sleep(mSaveInterval * 50);
			}
			catch(DataStoreException e)
			{
				e.printStackTrace();
			}
			catch(InterruptedException e)
			{
				synchronized(this)
				{
					if(mFlush)
					{
						mFlush = false;
						continue;
					}
					else
						break;
				}
			}
		}
	}
	
	public void flush()
	{
		synchronized(this)
		{
			mFlush = true;
			interrupt();
		}
	}
	
	public void waitForFinish()
	{
		synchronized(this)
		{
			flush();
			mExit = true;
		}
		
		try
		{
			join();
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}	
