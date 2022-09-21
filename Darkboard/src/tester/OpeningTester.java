package tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.opening.OpeningTreeNode;
import ai.opponent.OpponentProfile;
import ai.player.Darkboard;
import ai.player.Player;
import core.Globals;
import core.Move;
import database.GameDatabase;
import pgn.ExtendedPGNGame;
import umpire.local.LocalUmpire;

public class OpeningTester {
	
	public static OpeningTreeNode WHITE = null;
	public static OpeningTreeNode BLACK = null;
	
	public class TesterTask implements Runnable
	{
		OpeningTester ot;
		boolean white;
		int number;

		boolean terminated = false;
		
		public TesterTask(OpeningTester o, boolean w, int n)
		{
			ot = o;
			white = w;
			number = n;
		}
		
		public void evaluationRun()
		{
			OpeningTreeNode strat = (white?playerWhite:playerBlack);
		}
		
		public void terminate()
		{
			//player.fcp.destroy();
			terminated = true;
		}

		public void run() 
		{
			// System.out.println("Starting thread...");
			evaluationRun();
			/*while (true)
			{
				try
				{
				MCTSOpeningPlayer player = new MCTSOpeningPlayer(white,(white?playerWhite:playerBlack));
				MimicDarkboard20 opponent = new MimicDarkboard20(!white,ot.profile);
				LocalUmpire lu = (white? new LocalUmpire(player,opponent) : new LocalUmpire(opponent,player));
				lu.autoFinish = true;
				lu.adjudication = true;
				lu.verbose = false;
				
				lu.arbitrate();
				if (lu.getWinner()==player) player.backpropagate(1.0); //victory
				else if (lu.getWinner()==opponent) player.backpropagate(0.0); //defeat
				else player.backpropagate(0.5); //draw
				} catch (Exception e) {}
			}*/
		}
		
	}
	
	OpeningTreeNode playerWhite,playerBlack,opponentWhite,opponentBlack;
	OpponentProfile profile;
	
	static int moveLimit = 12;
	
	public static OpeningTreeNode load(String s)
	{
		File f = new File(s);
		OpeningTreeNode result = null;
		try
		{
			FileInputStream fos = new FileInputStream(f);
			ObjectInputStream oos = new ObjectInputStream(fos);
			result = (OpeningTreeNode) oos.readObject();
			oos.close();
		} catch (Exception e) { e.printStackTrace(); }
		
		return result;
	}
	
	public OpeningTester(OpeningTreeNode otn, boolean w)
	{
		playerWhite = new OpeningTreeNode();
		Vector<Move> mv = new LocalUmpire(new Player(),new Player()).legalMoves(true);
		for (int k=0; k<mv.size(); k++)
		{
			playerWhite.addChild(mv.get(k), true);
		}
		
		playerBlack = new OpeningTreeNode();
		mv = new LocalUmpire(new Player(),new Player()).legalMoves(false);
		for (int k=0; k<mv.size(); k++)
		{
			playerBlack.addChild(mv.get(k), true);
		}
		
		opponentBlack = (!w? otn : null);
		opponentWhite = (w? otn : null);
		
		profile = new OpponentProfile();
		profile.openingBookWhite = opponentWhite;
		profile.openingBookBlack = opponentBlack;
	}
	
	public OpeningTester(GameDatabase gd, String playerName, String opponentName)
	{
		playerWhite = new OpeningTreeNode();
		Vector<Move> mv = new LocalUmpire(new Player(),new Player()).legalMoves(true);
		for (int k=0; k<mv.size(); k++)
		{
			playerWhite.addChild(mv.get(k), true);
		}
		
		playerBlack = new OpeningTreeNode();
		mv = new LocalUmpire(new Player(),new Player()).legalMoves(false);
		for (int k=0; k<mv.size(); k++)
		{
			playerBlack.addChild(mv.get(k), true);
		}
		
		opponentWhite = new OpeningTreeNode();
		opponentBlack = new OpeningTreeNode();
		
		for (int k=0; k<gd.gameNumber(); k++)
		{
			ExtendedPGNGame pgn = gd.getGame(k);
			
			if (playerName==null || playerName.toUpperCase().equals(pgn.getWhite().toUpperCase()))
				playerWhite.addGame(pgn, true, moveLimit, true);
			
			if (playerName==null || playerName.toUpperCase().equals(pgn.getBlack().toUpperCase()))
				playerBlack.addGame(pgn, false, moveLimit, true);
			
			if (opponentName==null || opponentName.toUpperCase().equals(pgn.getWhite().toUpperCase()))
				opponentWhite.addGame(pgn, true, moveLimit, true);
			
			if (opponentName==null || opponentName.toUpperCase().equals(pgn.getBlack().toUpperCase()))
				opponentBlack.addGame(pgn, false, moveLimit, true);
		}
		
		profile = new OpponentProfile();
		profile.openingBookWhite = opponentWhite;
		profile.openingBookBlack = opponentBlack;
	}
	
	public OpeningTreeNode test(int timeLimit, int threads)
	{
		ExecutorService threadManager = Executors.newCachedThreadPool();
		
		Future<?> f[] = new Future[threads];
		TesterTask tt[] = new TesterTask[threads];
		
		for (int k=0; k<threads; k++)
		{
			boolean color = (opponentWhite==null? true : false);
			tt[k] = new TesterTask(this,color,k);
			f[k] = threadManager.submit(tt[k]);
		}
		
		long millis = System.currentTimeMillis();
		long end = millis + timeLimit - 5;

		try
		{
			boolean finish = false;
			while (!finish)
			{
				Thread.sleep(timeLimit);
				long millis2 = System.currentTimeMillis();
				if (millis2>=end) finish = true;
				else timeLimit = (int)(end - millis2);
			}
		} catch (Exception e) {  }

		for (int k=0; k<threads; k++)
		{
			tt[k].terminate();
			f[k].cancel(false);
		}
		
		return (opponentWhite!=null? playerBlack : playerWhite);
			
	}
	
	public void test()
	{
		try
		{
		ExecutorService threadManager = Executors.newCachedThreadPool();
		
		Future<?> f[] = new Future[Globals.threadNumber];
		
		for (int k=0; k<Globals.threadNumber; k++)
		{
			boolean color = k<(Globals.threadNumber/2);
			if (color && opponentWhite==null) color = false;
			else if (!color && opponentBlack==null) color = true;
			f[k] = threadManager.submit(new TesterTask(this,color,k)); //half white, half black
		}
		boolean alt = false;
		
		while (true)
		{
			try
			{
				Thread.sleep(60000);
			} catch (Exception e) {  }
			
			System.out.println("White stats");
			System.out.println(playerWhite);
			for (int k=0; k<playerWhite.moveChildren.size(); k++)
				System.out.println(playerWhite.moveChildren.get(k));
			
			System.out.println("Black stats");
			System.out.println(playerBlack);
			for (int k=0; k<playerBlack.moveChildren.size(); k++)
				System.out.println(playerBlack.moveChildren.get(k));
			
			//playerWhite.save(Globals.PGNPath+"/WHITETREE"+(alt?"_ALT":""));
			//playerBlack.save(Globals.PGNPath+"/BLACKTREE"+(alt?"_ALT":""));
			
			alt = !alt;
			
			for (int k=0; k<Globals.threadNumber; k++)
				if (f[k].isDone()) 
					f[k] = threadManager.submit(new TesterTask(this,k<(Globals.threadNumber/2),k)); //half white, half black
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
		
		/*WHITE = load(Globals.PGNPath+"/WHITETREE");
		BLACK = load(Globals.PGNPath+"/BLACKTREE");
		
		Globals.hasGui = false;
		Globals.threadNumber = 2;
		new Tester().test(-1, 1);*/
		
		/*File f = new File("/Users/giampi/BLACKTREE");
		OpeningTreeNode node = null;
		try
		{
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			node = (OpeningTreeNode)ois.readObject();
			ois.close();
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println(node);
		if (f!=null) return;*/
		
		GameDatabase db = new GameDatabase();
		Globals.threadNumber = 2;
		db.addGames(new File(OpeningTester.class.getResource("/Darkboard/databasecream.pgn").getPath()));
		db.addGames(new File(OpeningTester.class.getResource("/Darkboard/db2.pgn").getPath()));
		
		new OpeningTester(db,"dcencjnwejneih","meisterzinger").test();
		//new OpeningTester(db,null,null).test();
	}
	
}
