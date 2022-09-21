package ai.opponent;

import java.io.File;
import java.util.Vector;

import ai.opening.OpeningTreeNode;
import ai.player.Darkboard;
import pgn.ExtendedPGNGame;
import tester.OpeningTester;

public class ProfileUpdater implements Runnable {
	
	public static ProfileUpdater globalUpdater = new ProfileUpdater();
	public static int DURATION = 30;
	
	public class ProfileTask
	{
		String name;
		boolean white;
		ExtendedPGNGame game;
	}
	
	Vector<ProfileTask> queue = new Vector<ProfileTask>();
	boolean timeCompression = true;
	boolean emptyProfiles = false;
	
	
	public void addTask(String player, ExtendedPGNGame g)
	{
		if (player==null || g==null) return;
		boolean w = (player.toLowerCase().equals(g.getWhite().toLowerCase()));
		addTask(player,w,g);
	}
	
	public synchronized void addTask(String player, boolean w, ExtendedPGNGame g)
	{
		ProfileTask pt = new ProfileTask();
		pt.name = player;
		pt.white = w;
		pt.game = g;
		
		queue.add(pt);
		
		notifyAll();
	}
	
	private void handleTask(ProfileTask pt)
	{
		OpponentProfile op = OpponentProfile.getProfile(pt.name);
		
		OpeningTreeNode otn = (pt.white? op.openingBookWhite : op.openingBookBlack);
		if (pt.game!=null) otn.addGame(pt.game, pt.white, 12, false);
		
		if (otn.visits>=10) //don't run the simulator if you don't have enough samples
		{
		
			OpeningTester ot = new OpeningTester(otn,pt.white);
			
			int duration = DURATION*1000;
			int q = queue.size();
			if (timeCompression)
				for (int k=2; k<q; k++) duration/=2; //hurry it up if the queue is getting too long
			
			OpeningTreeNode strategy = ot.test(duration, 1);
			
			strategy.prune();
			System.out.println("Best strategy for "+pt.name+": "+strategy);
			
			if (pt.white) op.customStrategyBlack = strategy;
			else op.customStrategyWhite = strategy;
		
		}
		
		if (otn.visits>=10 || pt.game!=null) op.save();
		
		if (emptyProfiles) OpponentProfile.profiles.clear();
	}

	public void run() 
	{
		try
		{
			while (true)
			{
				synchronized (this) 
				{
					while (queue.size()==0)
					{
						// System.out.println("Empty queue");
						wait();
					}  
				}
				
				// System.out.println("Queue size :"+queue.size());
				ProfileTask pt = queue.remove(0);
				handleTask(pt);				
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	public static void main(String args[])
	{
		String path = System.getProperty("user.home") + "/darkboard_data/";
		// System.out.println(path);
		Darkboard.initialize(path);
		
		globalUpdater.emptyProfiles = true;
		globalUpdater.timeCompression = false;
		
		File prof = new File(path + "profiles/");
		//String list[] = prof.list();
		String list[] = {"darkboard"};
		for (int k=0; k<list.length; k++)
		{
			if (list[k].startsWith(".")) continue;
			globalUpdater.addTask(list[k], true, null);
			globalUpdater.addTask(list[k], false, null);
		}
		
		
	}

}
