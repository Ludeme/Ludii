package tester;

import java.util.Vector;

import ai.player.Darkboard;
import ai.player.DeepDarkboard10;
import ai.player.Player;
import core.Globals;
import pgn.ExtendedPGNGame;
import umpire.local.LocalUmpire;

/**
 * This is the basic class for comparing the strength of different (CPU) players. Subclass to test.
 * @author Nikola Novarlic
 *
 */
public class Tester {
	
	double trueRatio = 0.8;
	static int parameter = 0;
	
	public class PlayerData
	{
		String playerName = null;
		double elo;
		int scores[][];
	}
	
	Vector<PlayerData> players = new Vector();
	
	/**
	 * 
	 * @param rounds How many test rounds, negative for infinity
	 */
	public void test(int rounds, int printEvery)
	{
		for (int x=0; x<getPlayerNumber(); x++)
		{
			PlayerData pd = new PlayerData();
			pd.elo = getStartingElo();
			pd.scores = new int[getPlayerNumber()][3];
			players.add(pd);
		}
		
		
		for (int k=0; k!=rounds; k++)
		{
			for (int p1=0; p1<getPlayerNumber(); p1++)
				for (int p2=0; p2<getPlayerNumber(); p2++)
				{
					if (p1==p2) continue;
					//if (p1!=0 && p2!=0) continue; //only test against player 0
					match(p1,p2);
					System.out.println("-------");
					for (int p=0; p<getPlayerNumber(); p++)
					{
						System.out.println(""+p+": "+players.get(p).playerName+" ("+(int)players.get(p).elo+")");
						for (int q=0; q<getPlayerNumber(); q++)
						{
							if (q!=p) System.out.println("-> "+q+": "+players.get(p).scores[q][0]+" wins, "+
									players.get(p).scores[q][1]+" draws, "+players.get(p).scores[q][2]+" losses");
						}
					}
					System.out.println("-------");
				}
		}
	}
	
	/**
	 * Matches the player against gnuchess with a variable percentage of random moves
	 * which is adjusted until the two players have the same playing strength.
	 * @param rounds
	 * @param printEvery
	 */
	public void test2(int rounds, int printEvery)
	{
		for (int x=0; x<getPlayerNumber(); x++)
		{
			PlayerData pd = new PlayerData();
			pd.elo = getStartingElo();
			pd.scores = new int[getPlayerNumber()][3];
			players.add(pd);
		}
		
		while (true)
		{
			for (int k=0; k!=rounds; k++)
			{
				//rounds are cyclical with period 2*(PlayerNumber-1) (each player faces all others with black and white)
				//if playerNumber is odd, one player rests on each round
				int period = k % (2*(getPlayerNumber()-1));
				int smallPeriod = period % (getPlayerNumber()-1);
				
				//each player faces the opponent smallPeriod+1 places to its right, swapping around if necessary
				boolean played[] = new boolean[getPlayerNumber()];
				for (int s=0; s<played.length; s++) played[s] = false;
				
				int matchNumber = getPlayerNumber()/2;
				for (int j=0; j<matchNumber; j++)
				{
					int p1 = 0;
					for (int s=0; s<getPlayerNumber(); s++)
					{
						if (!played[s])
						{
							p1 = s; played[p1] = true; break;
						}
					}
					int p2 = (p1+smallPeriod+1)%getPlayerNumber();
					if (period!=smallPeriod)
					{
						//swap
						int swap = p1;
						p1 = p2; p2 = swap;
					}
					match(p1,p2);
					//asymmetricMatch(p1,p2);
				}
				if ((k+1)%printEvery==0)
				{
					System.out.println("-------");
					for (int p=0; p<getPlayerNumber(); p++)
					{
						System.out.println(""+p+": "+players.get(p).playerName+" ("+players.get(p).elo+")");
						for (int q=0; q<getPlayerNumber(); q++)
						{
							if (q!=p) System.out.println("-> "+q+": "+players.get(p).scores[q][0]+" wins, "+
									players.get(p).scores[q][1]+" draws, "+players.get(p).scores[q][2]+" losses");
						}
					}
					System.out.println("-------");
				}
			}
			
			//calculate scores after each round
			PlayerData pd = players.get(0);
			double score1 = pd.scores[1][0] + 0.5*pd.scores[1][1];
			double score2 = pd.scores[1][2] + 0.5*pd.scores[1][1];
			
			pd.scores[1][0] = 0; pd.scores[1][1] = 0; pd.scores[1][2] = 0;
			
			double ratio = score1 / (score1+score2); //if > 0.5, DB won, if < 0.5 gnuchess won
			double correctionFactor = (ratio-0.5)*2.0; //if > 0, trueRatio will be strengthened
			
			trueRatio += (0.05*correctionFactor);
			if (trueRatio<0.0) trueRatio=0.0;
			if (trueRatio>1.0) trueRatio=1.0;
			
			double correctionIntensity = (correctionFactor<0.0? -correctionFactor : correctionFactor);
			rounds += (rounds*1.0*(1.0-correctionIntensity));
			//if (correctionIntensity<(2.0/rounds)) rounds*=2; //increase accuracy;
			
		}
	}

	public void asymmetricTest(int rounds, int printEvery)
	{
		for (int x=0; x<getPlayerNumber(); x++)
		{
			PlayerData pd = new PlayerData();
			pd.elo = getStartingElo();
			pd.scores = new int[getPlayerNumber()][3];
			players.add(pd);
		}
		
		
		for (int k=0; k!=rounds; k++)
		{
			//rounds are cyclical with period 2*(PlayerNumber-1) (each player faces all others with black and white)
			//if playerNumber is odd, one player rests on each round
			int period = k % (2*(getPlayerNumber()-1));
			int smallPeriod = period % (getPlayerNumber()-1);
			
			//each player faces the opponent smallPeriod+1 places to its right, swapping around if necessary
			boolean played[] = new boolean[getPlayerNumber()];
			for (int s=0; s<played.length; s++) played[s] = false;
			
			int matchNumber = getPlayerNumber()/2;
			for (int j=0; j<matchNumber; j++)
			{
				int p1 = 0;
				for (int s=0; s<getPlayerNumber(); s++)
				{
					if (!played[s])
					{
						p1 = s; played[p1] = true; break;
					}
				}
				int p2 = (p1+smallPeriod+1)%getPlayerNumber();
				if (period!=smallPeriod)
				{
					//swap
					int swap = p1;
					p1 = p2; p2 = swap;
				}
				asymmetricMatch(p1,p2);
			}
			if ((k+1)%printEvery==0)
			{
				System.out.println("-------");
				for (int p=0; p<getPlayerNumber(); p++)
				{
					System.out.println(""+p+": "+players.get(p).playerName+" ("+players.get(p).elo+")");
					for (int q=0; q<getPlayerNumber(); q++)
					{
						if (q!=p) System.out.println("-> "+q+": "+players.get(p).scores[q][0]+" wins, "+
								players.get(p).scores[q][1]+" draws, "+players.get(p).scores[q][2]+" losses");
					}
				}
				System.out.println("-------");
			}
		}
	}
	
	public void test3(int rounds, int printEvery)
	{
		for (int x=0; x<getPlayerNumber(); x++)
		{
			PlayerData pd = new PlayerData();
			pd.elo = getStartingElo();
			pd.scores = new int[getPlayerNumber()][3];
			players.add(pd);
		}
		
		
		for (int k=0; k!=rounds; k++)
		{
			//rounds are cyclical with period 2*(PlayerNumber-1) (each player faces all others with black and white)
			//if playerNumber is odd, one player rests on each round
			int period = k % (2*(getPlayerNumber()-1));
			int smallPeriod = period % (getPlayerNumber()-1);
			
			//each player faces the opponent smallPeriod+1 places to its right, swapping around if necessary
			boolean played[] = new boolean[getPlayerNumber()];
			for (int s=0; s<played.length; s++) played[s] = false;
			
			int matchNumber = getPlayerNumber()/2;
			for (int j=0; j<matchNumber; j++)
			{
				int p1 = 0;
				for (int s=0; s<getPlayerNumber(); s++)
				{
					if (!played[s])
					{
						p1 = s; played[p1] = true; break;
					}
				}
				int p2 = (p1+smallPeriod+1)%getPlayerNumber();
				if (period!=smallPeriod)
				{
					//swap
					int swap = p1;
					p1 = p2; p2 = swap;
				}
				match(p1,p2);
			}
			if ((k+1)%printEvery==0)
			{
				System.out.println("-------");
				/*for (int p=0; p<getPlayerNumber(); p++)
				{
					System.out.println(""+p+": "+players.get(p).playerName+" ("+players.get(p).elo+")");
					for (int q=0; q<getPlayerNumber(); q++)
					{
						if (q!=p) System.out.println("-> "+q+": "+players.get(p).scores[q][0]+" wins, "+
								players.get(p).scores[q][1]+" draws, "+players.get(p).scores[q][2]+" losses");
					}
				}*/
				System.out.print(Globals.sampler);
				System.out.println("-------");
			}
		}		
	}
	
	
	public LocalUmpire getUmpire(int player1, int player2)
	{
		
		Player p1 = getPlayer(player1,true);
		Player p2 = getPlayer(player2,false);
		
		if (players.get(player1).playerName==null)
			players.get(player1).playerName = p1.getPlayerName();
		
		if (players.get(player2).playerName==null)
			players.get(player2).playerName = p2.getPlayerName();
		
		return new LocalUmpire(p1,p2);
		
		/*players.get(player1).playerName = p1.getPlayerName();
		players.get(player2).playerName = p2.getPlayerName();
		
		
		PartialInformationLocalUmpire pilu = new PartialInformationLocalUmpire(p1,p2);*/
		
		//pilu.setFreeSquares((player1==0), 32);
		
		//return pilu;
	}
	
	public void match(int a, int b)
	{
		
		double points1 = 0.0;
		double points2 = 0.0;
		
		LocalUmpire lu = getUmpire(a,b);
		lu.autoFinish = true; //fast adjudication
		lu.adjudication = true;
		
		Player p1 = lu.getP1(); //Player p2 = lu.getP2();
		
		lu.verbose = false;
		
		System.gc();
		
		try
		{
			ExtendedPGNGame g = lu.arbitrate();
			//g.saveToFile();
			
			//System.out.println(g);
			
			if (lu.getGameOutcome()==LocalUmpire.OUTCOME_CHECKMATE)
			{
				if (lu.getWinner()==p1)
				{
					points1 = 1.0; points2 = 0.0;
					players.get(a).scores[b][0]++;
					players.get(b).scores[a][2]++;
				} else
				{
					points1 = 0.0; points2 = 1.0;
					players.get(a).scores[b][2]++;
					players.get(b).scores[a][0]++;
				}
			} else
			{
				points1 = points2 = 0.5;
				players.get(a).scores[b][1]++;
				players.get(b).scores[a][1]++;
			}
			
			//update ELO
			double elo1 = players.get(a).elo;
			double elo2 = players.get(b).elo;
			double p1expect = 1.0/(1.0+Math.pow(10,(elo2-elo1)/400.0));
			double p2expect = 1.0/(1.0+Math.pow(10,(elo1-elo2)/400.0));
			double p1delta = (points1 - p1expect)*getEloKFactor();
			double p2delta = (points2 - p2expect)*getEloKFactor();
			players.get(a).elo += (int)p1delta;
			players.get(b).elo += (int)p2delta;
		} catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		
	}
	
	public void asymmetricMatch(int a, int b)
	{
		
		double points1 = 0.0;
		double points2 = 0.0;
		
		LocalUmpire lu = getUmpire(a,b);
		
		Player p1 = lu.getP1(); //Player p2 = lu.getP2();
		
		lu.verbose = false;
		ExtendedPGNGame g = lu.arbitrate((a==0? LocalUmpire.boardLayoutFromFEN("1nb1kbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR") : 
			LocalUmpire.boardLayoutFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/1NB1KBN1")));
		
		System.out.println(g);
		
		if (lu.getGameOutcome()==LocalUmpire.OUTCOME_CHECKMATE)
		{
			if (lu.getWinner()==p1)
			{
				points1 = 1.0; points2 = 0.0;
				players.get(a).scores[b][0]++;
				players.get(b).scores[a][2]++;
			} else
			{
				points1 = 0.0; points2 = 1.0;
				players.get(a).scores[b][2]++;
				players.get(b).scores[a][0]++;
			}
		} else
		{
			points1 = points2 = 0.5;
			players.get(a).scores[b][1]++;
			players.get(b).scores[a][1]++;
		}
		
		//update ELO
		double elo1 = players.get(a).elo;
		double elo2 = players.get(b).elo;
		double p1expect = 1.0/(1.0+Math.pow(10,(elo2-elo1)/400.0));
		double p2expect = 1.0/(1.0+Math.pow(10,(elo1-elo2)/400.0));
		double p1delta = (points1 - p1expect)*getEloKFactor();
		double p2delta = (points2 - p2expect)*getEloKFactor();
		players.get(a).elo += p1delta;
		players.get(b).elo += p2delta;
		
	}
	
	
	/**
	 * Subclass these methods...
	 * @return
	 */
	public int getPlayerNumber()
	{
		return 2;
	}
	
	public Player getPlayer(int index, boolean white)
	{
		//String players[] = { "darkboard","rjay","kilgor","sting-r","tomtomtomtom","meisterzinger","krieg"};
		//OpponentProfile op = OpponentProfile.getProfile(players[index]);
		//return new DeepDarkboard101(white,op.openingBookWhite,op.openingBookBlack,players[index]);
		//return (index==0? new ExperimentalDarkboard(white) : new Darkboard(white) /*new FruitChessPlayer(white,trueRatio)*/);
		//return new Darkboard(white);
		//return new MonteCarloPlayer(new GnuChessPlayer(white,1.0),white);
		/*if (index==0) return new Darkboard(white);
		if (index==1)
		{
			Darkboard db = new ExperimentalDarkboard(white);
			db.genericOppModel=1; //weak
			return db;
		}
		if (index==2)
		{
			Darkboard db = new ExperimentalDarkboard(white);
			db.genericOppModel=2; //strong
			return db;
		}*/
		/*if (index==0)
		{
			DeepDarkboard10 dd = new DeepDarkboard10(white);
			dd.usePlans = true;
			return dd;
		}
		if (index==1)
		{
			DeepDarkboard10 dd = new DeepDarkboard10(white);
			dd.usePlans = false;
			return dd;
		}*/

		//if (index==1) return new OldDarkboard(white);
		
		if (index==0) return new DeepDarkboard10(white);
		//if (index==2) return  new PureMCPlayer(white,3);
		//if (index==3) return  new PureMCPlayer(white,10);
		//if (index==0) return new DeepDarkboard10(white);
		//else return new DeepDarkboard101(white,OpeningTester.WHITE,OpeningTester.BLACK);
		return null;
	}
	
	public double getStartingElo()
	{
		return 1600.0;
	}
	
	public double getEloKFactor()
	{
		return 0.1;
	}
	
	public static void main(String args[])
	{
		String path = System.getProperty("user.home") + "/darkboard_data/";
		// System.out.println(path);
		Darkboard.initialize(path);
		
		if (args.length>0) parameter = Integer.parseInt(args[0]);
		
		Globals.hasGui = false;
		Globals.threadNumber = 2;
		new Tester().test(-1, 1);
		//new Tester().test2(10, 1);
		//new Tester().asymmetricTest(-1, 1);
		//new Tester().test3(-1, 1);
	}

}
