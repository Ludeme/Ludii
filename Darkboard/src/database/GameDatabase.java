/*
 * Created on 29-set-05
 *
 */
package database;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import ai.opponent.OpponentProfile;
import ai.player.Darkboard;
import ai.player.Player;
import core.Chessboard;
import core.Metaposition;
import core.Move;
import pgn.ExtendedPGNGame;
import pgn.ExtendedPGNGame.PGNMoveData;
import pgn.PGNParser;
import umpire.local.LocalUmpire;
import umpire.local.StepwiseLocalUmpire;

/**
 * @author Nikola Novarlic
 *
 */
public class GameDatabase {
	
	Vector database;
	File dbPath = null;
	
	public class EloComparator implements Comparator<ExtendedPGNGame>
	{

		public int compare(ExtendedPGNGame g1, ExtendedPGNGame g2) {
			// TODO Auto-generated method stub
			
			int s1 = g1.getBestElo();
			int s2 = g2.getBestElo();
			
			int score = s1-s2;
			if (score<0) return -1;
			if (score>0) return 1;
			return 0;
		}
		
	}
	
	public GameDatabase()
	{
		database = new Vector();
	}
	
	public GameDatabase(File path)
	{
		dbPath = path;
		// System.out.println("Loading...");
		try
		{
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			database = (Vector)ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) { e.printStackTrace();}
		
		System.out.println("DB loaded with "+gameNumber()+ " games.");
		
	}
	
	public void saveDatabase(File f)
	{
		dbPath = f;
		saveDatabase();
	}
	
	public void saveDatabase()
	{
		try 
		{
			FileOutputStream fos = new FileOutputStream(dbPath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(database);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public boolean addGame(ExtendedPGNGame game)
	{
		if (game==null) return false;
		boolean acceptable = true;
		String rejectReason = "";
		
		if (game.getResult()=="*")
		{
			acceptable = false;
			rejectReason = "unfinished game"; 
		} //don't include unfinished games.
		if (game.getEvent().indexOf("w16")==-1)
		{ 
			acceptable = false;
			rejectReason = "not a Kriegspiel game"; 
		} //only include kriegspiel games
		if (game.getMove(true,5)==null)
		{
			acceptable = false;
			rejectReason = "must be at least 5 moves long"; 			
		}
		if (!acceptable)
		{
			/*System.out.println("Rejecting "+game.getDate()+","+game.getTime()+","+game.getWhite()+
			","+game.getBlack());
			System.out.println("Reason: "+rejectReason);*/
			return false;
		}
		database.add(game);
		return true;
	}
	
	public int gameNumber()
	{
		return database.size();
	}
	
	public ExtendedPGNGame getGame(int index)
	{
		if (index<0 || index>=gameNumber()) return null;
		return ((ExtendedPGNGame)database.get(index));
	}
	
	public void profile()
	{
		// System.out.println("Evaluating...");
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame pgn = (ExtendedPGNGame)database.get(k);
			OpponentProfile.getProfile(pgn.getWhite()).updateWithNewGame(pgn, 0.0f);
			OpponentProfile.getProfile(pgn.getBlack()).updateWithNewGame(pgn, 0.0f);
		}
		
		Enumeration<OpponentProfile> prof = OpponentProfile.profiles.elements();
		
		// System.out.println("Saving...");
		while (prof.hasMoreElements())
		{
			OpponentProfile op = prof.nextElement();
			if (op.whitesize+op.blacksize>=20) System.out.println(op.name+" "+op.whitesize+" "+op.blacksize);
			op.save();
		}
	}
	
	public void addAllGames(File f)
	{
		FileInputStream is;
		try
		{
			is = new FileInputStream(f);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		PGNParser parser = new PGNParser(is);
		ExtendedPGNGame game=null;
		// System.out.println("Loading games");
		int counter = 0;
		do
		{
			try
			{
			game = parser.parseNextGame();
			if (game!=null) addGame(game);
			if (++counter % 25 == 0) System.out.print(".");
			if (counter%1000 == 0) System.out.println();
			} catch (Exception e)
			{
				e.printStackTrace();
				// System.out.println("Game Number: "+counter);

			}
			//System.out.println(counter);
		} while (game!=null);
		System.out.println("");
		System.out.println("Loaded "+(counter-1)+" games, "+gameNumber()+" in database.");
	}
	
	public void addGames(InputStream is)
	{
		PGNParser parser = new PGNParser(is);
		ExtendedPGNGame game=null;
		// System.out.println("Loading games");
		int counter = 0;
		boolean inserted;
		String pls[] = {"MeisterZinger","Sting-R","rjay","Pasi","Budgie","diswin","CJunk","Kilgor","paoloc",
			"christofischer","kino79","boing","kriegspiel","damir1813"};
		do
		{
			try
			{
			game = parser.parseNextGame();
			boolean keep = true;
			//for (int k=0; k<pls.length; k++) if (game.getWhite().equals(pls[k]) || game.getBlack().equals(pls[k]))
				keep = true;
			if (game!=null && keep) inserted = addGame(game);
			if (++counter % 25 == 0) System.out.print(".");
			if (counter%1000 == 0) System.out.println();
			} catch (Exception e)
			{
				e.printStackTrace();
				// System.out.println("Game Number: "+counter);

			}
			//System.out.println(counter);
		} while (game!=null);
		System.out.println("");
		System.out.println("Loaded "+(counter-1)+" games, "+gameNumber()+" in database.");
	}
	
	public void addGamesAndSave(InputStream is,OutputStream os)
	{
		PGNParser parser = new PGNParser(is);
		parser.recordGameString = true;
		int added = 0;
		
		ExtendedPGNGame game=null;
		// System.out.println("Loading games");
		int counter = 0;
		boolean inserted;
		do
		{
			try
			{
			game = parser.parseNextGame();
			if (game!=null) {  inserted = addGame(game); if (inserted) added++; } else inserted=false;
			if (inserted && game.associatedString!=null) os.write(game.associatedString.getBytes());
			if (++counter % 25 == 0) System.out.print(".");
			if (counter%1000 == 0) System.out.println();
			//this to prevent out of memory...
			if (database.size()>0) database.remove(0);
			} catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Game Number: "+counter);

			}
			//System.out.println(counter);
		} while (game!=null);
		System.out.println("");
		System.out.println("Loaded "+(counter-1)+" games, "+added+" in database.");
	}
	
	public void addGames(File f)
	{
		try
		{
			FileInputStream s = new FileInputStream(f);
			addGames(s);
			s.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void creamOfTheCrop(File input, File output)
	{
		try
		{
			FileInputStream s = new FileInputStream(input);
			FileOutputStream o = new FileOutputStream(output);
			BufferedInputStream bis = new BufferedInputStream(s,2048);
			addGamesAndSave(bis,o);
			bis.close();
			s.close();
			o.close();
			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void printAvgAndStandardDeviation(int sample[])
	{
		double total = 0;
		for (int k=0; k<sample.length; k++)
			total+=sample[k];
			
		double avg = total / sample.length;
		double dev = 0;
		
		for (int k=0; k<sample.length; k++)
		{
			double delta = avg-sample[k];
			dev+=(delta*delta);
		}
		dev = dev / (sample.length-1);
		double sqr = Math.sqrt(dev);
		
		// System.out.println("Average: "+avg);
		// System.out.println("Std Deviation: "+sqr);
	}
	
	public void printAvgAndStandardDeviationWithWeight(int sample[])
	{
		double total = 0;
		int sampleNumber = 0;
		for (int k=0; k<sample.length; k++)
		{
			total+=(k*sample[k]);
			sampleNumber+=sample[k];
		}	
		double avg = total / sampleNumber;
		double dev = 0;
		
		for (int k=0; k<sample.length; k++)
		{
			double delta = avg-k;
			dev+=(delta*delta)*sample[k];
		}
		dev = dev / (sampleNumber-1);
		double sqr = Math.sqrt(dev);
		
		// System.out.println("Average: "+avg);
		// System.out.println("Std Deviation: "+sqr);
	}
	
	public void outputAvgAndStandardDeviationWithWeight(int sample[], float out[])
	{
		double total = 0;
		int sampleNumber = 0;
		for (int k=0; k<sample.length; k++)
		{
			total+=(k*sample[k]);
			sampleNumber+=sample[k];
		}	
		
		if (sampleNumber<1) return;
		
		double avg = total / sampleNumber;
		double dev = 0;
		
		for (int k=0; k<sample.length; k++)
		{
			double delta = avg-k;
			dev+=(delta*delta)*sample[k];
		}
		dev = (sampleNumber>1? dev / (sampleNumber-1) : 0.0); 
		double sqr = Math.sqrt(dev);
		
		out[0] = (float)avg;
		out[1] = (float)sqr;
	}
	
	public void printResultStatistics()
	{
		String outcomes[] = {
		"Black checkmated",
		"Black resigns",
		"Black forfeits on time",
		//"Black got disconnected and forfeits",
		"White checkmated",
		"White resigns",
		"White forfeits on time",
		//"White got disconnected and forfeits",
		"White stalemated",
		"Black stalemated",
		"Game drawn because neither player has mating material",
		"Black ran out of time and White has no material to mate",
		"White ran out of time and Black has no material to mate",
		"Game drawn by mutual agreement",
		"Game drawn by the 50 move rule",
		"Game drawn by repetition"
		};
		int games[] = new int[outcomes.length];
		double ratios[] = new double[outcomes.length];
		
		int statBase = 0;
		
		for (int k=0; k<gameNumber(); k++)
		{
			String out = getGame(k).getICCResult();
			if (out!=null && !out.equals("*"))
				for (int j=0; j<outcomes.length; j++)
				{
					if (outcomes[j].equals(out))
					{
						statBase++;
						games[j]++;
						break;
					}
					//if (j==outcomes.length-1) System.out.println("Unrecognized outcome: "+out);
				}
		}
		
		for (int j=0; j<outcomes.length; j++)
			ratios[j] = 1.0*games[j]/statBase;
			
		double white = ratios[0]+ratios[1]+ratios[2];
		double black = ratios[3]+ratios[4]+ratios[5];
		double draw = ratios[6]+ratios[7]+ratios[8]+ratios[9]+ratios[10]+ratios[11]+ratios[12]+ratios[13];
		/*
		System.out.println("GAMES:      "+statBase);
		System.out.println("WHITE WINS: "+(white*100)+"%");
		System.out.println("BLACK WINS: "+(black*100)+"%");
		System.out.println("DRAWS:      "+(draw*100)+"%");
		System.out.println("---------------");
		*/
		for (int j=0; j<outcomes.length; j++)
		{
			System.out.println(outcomes[j]+": "+games[j]+" ("+(ratios[j]*100)+"%)");
		}
	}
	
	public void printGameDurationStatitics()
	{
		int maxDuration = 800;
		int games[] = new int[maxDuration];
		int statBase = 0;
		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			//if (g.isCheckmate())
			{
				int dur = g.getMoveNumber();
				if (dur>=maxDuration) System.out.println("Wow! "+dur);
				else
				{
					statBase++;
					games[dur]++;
				}
			}
		}
		int flag, flag2;
		for (flag=0; flag<maxDuration && games[flag]==0; flag++);
		for (flag2=maxDuration-1; flag2>=0 && games[flag2]==0; flag2--); 
		// System.out.println("Games: "+statBase);
		int total = 0; 
		for (int k=flag; k<=flag2; k++)
		{
			total+=(k*games[k]);
			// System.out.println("Move "+k+": "+games[k]);
		}
		float avg = 1.0f*total/statBase;
		// System.out.println("Average: "+avg);
		
		printAvgAndStandardDeviationWithWeight(games);
	}
	
	public void calculateFinalMaterial()
	{
		int winnerPawn[] = new int[20];
		int winnerKnight[] = new int[10];
		int winnerBishop[] = new int[10];
		int winnerRook[] = new int[10];
		int winnerQueen[] = new int[10];
		int winnerPromotions[] = new int[10];
		int loserPawn[] = new int[20];
		int loserKnight[] = new int[10];
		int loserBishop[] = new int[10];
		int loserRook[] = new int[10];
		int loserQueen[] = new int[10];
		int loserPromotions[] = new int[10];
		
		LocalUmpire lu = new LocalUmpire(null,null);
		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			boolean whitewin;
			if (!g.isCheckmate()) continue;
			
			g.replayGame(lu);
			whitewin = g.getResult().equals("1-0");
			
			winnerPawn[lu.squaresOfType((whitewin? LocalUmpire.WP : LocalUmpire.BP))
				+lu.promotions[(whitewin? 0 : 1)]]++;
			winnerKnight[lu.squaresOfType((whitewin? LocalUmpire.WN : LocalUmpire.BN))]++;
			winnerBishop[lu.squaresOfType((whitewin? LocalUmpire.WB : LocalUmpire.BB))]++;
			winnerRook[lu.squaresOfType((whitewin? LocalUmpire.WR : LocalUmpire.BR))]++;
			winnerQueen[lu.squaresOfType((whitewin? LocalUmpire.WQ : LocalUmpire.BQ))]++;
			loserPawn[lu.squaresOfType((whitewin? LocalUmpire.BP : LocalUmpire.WP))
				+lu.promotions[(whitewin? 1 : 0)]]++;
			loserKnight[lu.squaresOfType((whitewin? LocalUmpire.BN : LocalUmpire.WN))]++;
			loserBishop[lu.squaresOfType((whitewin? LocalUmpire.BB : LocalUmpire.WB))]++;
			loserRook[lu.squaresOfType((whitewin? LocalUmpire.BR : LocalUmpire.WR))]++;
			loserQueen[lu.squaresOfType((whitewin? LocalUmpire.BQ : LocalUmpire.WQ))]++;
			
			winnerPromotions[lu.promotions[(whitewin? 0 : 1)]]++;
			loserPromotions[lu.promotions[(whitewin? 1 : 0)]]++;
			
		}
		
		String s[] = {"Winner's pawns","Winner's knights","Winner's bishops","Winner's rooks",
			"Winner's queens", "Winner's promotions","Loser's pawns","Loser's knights","Loser's bishops","Loser's rooks",
			"Loser's queens","Loser's promotions" };
		int stats[] = {};
		for (int k=0; k<12; k++)
		{
			System.out.println(s[k]);
			switch (k)
			{
				case 0: stats = winnerPawn; break;
				case 1: stats = winnerKnight; break;
				case 2: stats = winnerBishop; break;
				case 3: stats = winnerRook; break;
				case 4: stats = winnerQueen; break;
				case 5: stats = winnerPromotions; break;
				case 6: stats = loserPawn; break;
				case 7: stats = loserKnight; break;
				case 8: stats = loserBishop; break;
				case 9: stats = loserRook; break;
				case 10: stats = loserQueen; break;
				case 11: stats = loserPromotions; break;
			}
			for (int j=0; j<stats.length; j++) System.out.print(stats[j]+" ");
			System.out.println();
			printAvgAndStandardDeviationWithWeight(stats);
		}
	}
	
	public void calculateBranchFactor()
	{
		int bucketSize = 5;
		int bucketTotal[] = new int[320];
		int bucketSamples[] = new int[320];
		
		LocalUmpire lu = new LocalUmpire(null,null);
		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			
			lu.resetBoard();
			int pl = (g.getResult().equals("1-0")? 0 : g.getResult().equals("0-1")? 1 : -1);
			//if (!g.getResult().equals("1/2-1/2")) continue;
			
			for (int m=0; m<g.getMoveNumber(); m++)
			{
				for (int turn=0; turn<2; turn++)
				{
					
					ExtendedPGNGame.PGNMoveData move = g.getMove(turn==0,m);
					if (move==null) break;
					Move fin = move.finalMove;
					if (fin==null) break;
					lu.doMove(fin,turn);
					if (turn!=pl) continue;
					//export to a Metaposition
					Metaposition sc = lu.exportChessboard(1-turn);
					int sample = sc.generateMoves(true,null).size(); //move number
					int bucket = m/bucketSize;
					bucketTotal[bucket]+=sample;
					bucketSamples[bucket]++;
				}
			}
			
			if (k%100==99) System.out.print("*");
		}
		System.out.println();
		
		for (int buc=0; buc<100; buc++)
		{
			float res = (bucketSamples[buc]>0? 1.0f*bucketTotal[buc]/bucketSamples[buc] : 0.0f);
			System.out.println(""+(buc*bucketSize+1)+"-"+(buc*bucketSize+bucketSize)+" "+res);
		}
	}
	
	public class RiskDataContainer
	{
		
	}
	
	
	public void calculateAverageRisk(int piece, int pcnumber, int oppnumber)
	{
		
		int bucketSize = 5;
		int bucketTotal[] = new int[100];
		int bucketSamples[] = new int[100];
		
		Darkboard p1, p2;
		

		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			p1 = new Darkboard(true);
			p2 = new Darkboard(false);
			StepwiseLocalUmpire lu = new StepwiseLocalUmpire(p1,p2);
			lu.verbose = false;
			
			lu.stepwiseInit(null,null);
			lu.resetBoard();
			
			int pl = (g.getResult().equals("1-0")? 0 : g.getResult().equals("0-1")? 1 : -1);
			//if (!g.getResult().equals("1/2-1/2")) continue;
			
			for (int m=0; m<g.getMoveNumber(); m++)
			{
				for (int turn=0; turn<2; turn++)
				{
					Darkboard p = (turn==0? p1:p2);
					ExtendedPGNGame.PGNMoveData move = g.getMove(turn==0,m);
					if (move==null) break;
					Move fin = move.finalMove;
					if (fin==null) break;
					p.lastMove = fin;
					lu.stepwiseArbitrate(fin);
					if (turn!=pl) continue;
					//export to a Metaposition
					Metaposition sc = p.simplifiedBoard;
					if (oppnumber==(sc.pawnsLeft+sc.piecesLeft) && pcnumber==sc.getPieceNumber()-1)
					for (byte x=0; x<8; x++)
						for (byte y=0; y<8; y++)
					{
							//sample
							if (sc.getFriendlyPiece(x,y)==piece)
							{
								float risk = sc.dangerRating(x,y);
								int bucket = (int)(100.0f*risk);
								if (bucket>99) bucket = 99; if (bucket<0) bucket = 0;
								bucketSamples[bucket]++;
							}
							
					}
				}
			}
			
			if (k%100==99) System.out.print("*");
		}
		System.out.println();
		bucketSamples[99] = 0;
		//System.out.println(piece+" "+pcnumber+" "+oppnumber);
		for (int buc=0; buc<100; buc++)
		{
			System.out.println(bucketSamples[buc]);
		}
		printAvgAndStandardDeviationWithWeight(bucketSamples);
	}
	
	
	public void calculateAverageProtection(int piece, int pcnumber, int oppnumber)
	{
		
		int bucketSize = 5;
		int bucketTotal[] = new int[16];
		int bucketSamples[] = new int[16];
		
		Darkboard p1, p2;
		

		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			p1 = new Darkboard(true);
			p2 = new Darkboard(false);
			StepwiseLocalUmpire lu = new StepwiseLocalUmpire(p1,p2);
			lu.verbose = false;
			
			lu.stepwiseInit(null,null);
			lu.resetBoard();
			
			int pl = (g.getResult().equals("1-0")? 0 : g.getResult().equals("0-1")? 1 : -1);
			//if (!g.getResult().equals("1/2-1/2")) continue;
			
			for (int m=0; m<g.getMoveNumber(); m++)
			{
				for (int turn=0; turn<2; turn++)
				{
					Darkboard p = (turn==0? p1:p2);
					ExtendedPGNGame.PGNMoveData move = g.getMove(turn==0,m);
					if (move==null) break;
					Move fin = move.finalMove;
					if (fin==null) break;
					p.lastMove = fin;
					lu.stepwiseArbitrate(fin);
					if (turn!=pl) continue;
					//export to a Metaposition
					Metaposition sc = p.simplifiedBoard;
					if (oppnumber==(sc.pawnsLeft+sc.piecesLeft) && pcnumber==sc.getPieceNumber()-1)
					{
						sc.computeProtectionMatrix(true);
					for (byte x=0; x<8; x++)
						for (byte y=0; y<8; y++)
					{
							//sample
							if (sc.getFriendlyPiece(x,y)==piece)
							{
								
								int bucket = p.globals.protectionMatrix[x][y];
								if (bucket>15) bucket = 15; if (bucket<0) bucket = 0;
								bucketSamples[bucket]++;
							}
							
					}
					}
				}
			}
			
			if (k%100==99) System.out.print("*");
		}
		System.out.println();
		//bucketSamples[99] = 0;
		// System.out.println(piece+" "+pcnumber+" "+oppnumber);
		for (int buc=0; buc<bucketSamples.length; buc++)
		{
			System.out.println(bucketSamples[buc]);
		}
		printAvgAndStandardDeviationWithWeight(bucketSamples);
	}
	
	public void calculateAverageRiskForAll(Vector playerList)
	{
		
		int bucketSize = 5;
		int bucketTotal[] = new int[100];
		int bucketSamples[] = new int[100];
		int largeSampleSet[][][][] = new int[6][16][16][100];
		float outputSet[][][][] = new float[6][16][16][2];
		
		Darkboard p1, p2;
		

		
		for (int k=0; k<gameNumber(); k++)
		{
			boolean follow[] = new boolean[2];	
		
			ExtendedPGNGame g = getGame(k);
			p1 = new Darkboard(true);
			p2 = new Darkboard(false);
			StepwiseLocalUmpire lu = new StepwiseLocalUmpire(p1,p2);
			lu.verbose = false;
			
			lu.stepwiseInit(null,null);
			lu.resetBoard();
			
			//int pl = (g.getResult().equals("1-0")? 0 : g.getResult().equals("0-1")? 1 : -1);
			//if (!g.getResult().equals("1/2-1/2")) continue;
			
			//only follow the best players
			for (int k2=0; k2<playerList.size(); k2++)
			{
				DatabasePlayer dp = (DatabasePlayer)playerList.get(k2);
				if (dp.name.equals(g.getWhite())) follow[0] = true;
				if (dp.name.equals(g.getBlack())) follow[1] = true;
			}
			
			if (!follow[0] && !follow[1]) continue;
			
			for (int m=0; m<g.getMoveNumber(); m++)
			{
				for (int turn=0; turn<2; turn++)
				{
					Darkboard p = (turn==0? p1:p2);
					ExtendedPGNGame.PGNMoveData move = g.getMove(turn==0,m);
					if (move==null) break;
					Move fin = move.finalMove;
					if (fin==null) break;
					p.lastMove = fin;
					lu.stepwiseArbitrate(fin);
					//export to a Metaposition
					Metaposition sc = p.simplifiedBoard;
					//if (oppnumber==(sc.pawnsLeft+sc.piecesLeft) && pcnumber==sc.getPieceNumber()-1)
					if (follow[turn]) for (byte x=0; x<8; x++)
						for (byte y=0; y<8; y++)
					{
							//sample
							int pc = sc.getFriendlyPiece(x,y);
							if (pc!=Chessboard.EMPTY)
							{
								float risk = sc.dangerRating(x,y);
								int bucket = (int)(100.0f*risk);
								if (bucket>99) bucket = 99; if (bucket<0) bucket = 0;
								try
								{
								largeSampleSet[pc][sc.getPieceNumber()-1][sc.pawnsLeft+sc.piecesLeft][bucket]++;
								} catch (Exception e) { System.out.println(sc.getRepresentation(Chessboard.KING));
									System.out.println(g); return;}
							}
							
					}
				}
			}
			
			if (k%100==99) System.out.print("*");
		}
		System.out.println();
		bucketSamples[99] = 0;
		for (int abc=0; abc<6; abc++)
		for (int x1=0; x1<16; x1++)
		for (int y1=0; y1<16; y1++)
		{
			outputAvgAndStandardDeviationWithWeight(largeSampleSet[abc][x1][y1],outputSet[abc][x1][y1]);
			System.out.println("array["+abc+"]["+x1+"]["+y1+"][0]="+outputSet[abc][x1][y1][0]+"; "+
			"array["+abc+"]["+x1+"]["+y1+"][1]="+outputSet[abc][x1][y1][1]+";");
		}
	}
	
	public void calculateOngoingMaterial()
	{
		int bucketSize = 5;
		int bucketTotal[] = new int[320];
		int bucketSamples[] = new int[320];
		
		LocalUmpire lu = new LocalUmpire(null,null);
		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			
			lu.resetBoard();
			int pl = (g.getResult().equals("1-0")? 1 : g.getResult().equals("0-1")? 0 : -1);
			//if (!g.getResult().equals("1/2-1/2")) continue;
			
			for (int m=0; m<g.getMoveNumber(); m++)
			{
				for (int turn=0; turn<2; turn++)
				{
					
					ExtendedPGNGame.PGNMoveData move = g.getMove(turn==0,m);
					if (move==null) break;
					Move fin = move.finalMove;
					if (fin==null) break;
					lu.doMove(fin,turn);
					if (turn!=pl) continue;
					//export to a Metaposition
					int sample = lu.getMaterial(pl);
					int bucket = m/bucketSize;
					bucketTotal[bucket]+=sample;
					bucketSamples[bucket]++;
				}
			}
			
			if (k%100==99) System.out.print("*");
		}
		System.out.println();
		
		for (int buc=0; buc<100; buc++)
		{
			float res = (bucketSamples[buc]>0? 1.0f*bucketTotal[buc]/bucketSamples[buc] : 0.0f);
			// System.out.println(""+(buc*bucketSize+1)+"-"+(buc*bucketSize+bucketSize)+" "+res);
		}
		
		
	}
	
	class FinalPositionHelper
	{
		public String position;
		public int count;
	}
	
	public class PositionComparator implements Comparator
	{ 
		public int compare(Object o1, Object o2)
		{
			FinalPositionHelper dp1 = (FinalPositionHelper)o1;
			FinalPositionHelper dp2 = (FinalPositionHelper)o2;
			if (dp1.count!=dp2.count) return dp2.count-dp1.count;
			if (dp1.position.length()!=dp2.position.length()) return dp2.position.length()-
				dp1.position.length();
			return (dp1.position.compareTo(dp2.position));
		}
		public boolean equals(Object obj) { return false; }
	}
	
	public FinalPositionHelper findPosition(Vector v, String s)
	{
		for (int k=0; k<v.size(); k++)
		{
			FinalPositionHelper h = (FinalPositionHelper)v.get(k);
			if (h.position.equals(s)) return h;
		}
		return null;
	}
	
	public void calculateFinalPositions()
	{
		Vector positionVector = new Vector();
		int bucketSize = 5;
		int bucketTotal[] = new int[320];
		int bucketSamples[] = new int[320];
		
		LocalUmpire lu = new LocalUmpire(null,null);
		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			if (!g.getResult().equals("1/2-1/2")) continue;
			//if (g.getICCResult().indexOf("stalemated")==-1) continue;
			lu.resetBoard();
			g.replayGame(lu);
			int winner = (lu.exportPlayerMaterial(0).length()
				>= lu.exportPlayerMaterial(1).length()? LocalUmpire.WHITE : LocalUmpire.BLACK);
			
			String posit = lu.exportPlayerMaterials(winner);
			FinalPositionHelper fph = findPosition(positionVector,posit);
			if (fph!=null) fph.count++;
			else
			{
				fph = new FinalPositionHelper();
				fph.count=1; fph.position=posit;
				positionVector.add(fph);
			}

			if (k%100==99) System.out.print("*");
		}
		System.out.println();
		Collections.sort(positionVector,new PositionComparator());
		for (int k=0; k<positionVector.size(); k++)
		{
			FinalPositionHelper fph = (FinalPositionHelper)positionVector.get(k);
			System.out.println(fph.position+ " -> "+fph.count);
		}
	}
	
	public void findFinal(String s1, String s2)
	{
		int wins = 0;
		int draws = 0;
		LocalUmpire lu = new LocalUmpire(null,null);
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			lu.resetBoard();
			g.replayGame(lu);
			String f1 = lu.exportPlayerMaterial(0);
			String f2 = lu.exportPlayerMaterial(1);
			if ((s1.equals(f1) && s2.equals(f2) && g.getWhite().toLowerCase().equals("darkboard")) || (s1.equals(f2) && s2.equals(f1) && g.getBlack().toLowerCase().equals("darkboard")))
			{
				if (g.getResult().equals("1/2-1/2")) 
				{
					draws++;
					//System.out.println(g);
				}
				else wins++;
			}
		}
		// System.out.println(s1+s2+": wins "+wins+", draws "+draws);
	}
	
	public void printCheckmateLocations()
	{
		int sq[][] = new int[8][8];
		LocalUmpire lu = new LocalUmpire(null,null);
		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame g = getGame(k);
			boolean whitewin;
			if (!g.isCheckmate()) continue;
			
			g.replayGame(lu);
			whitewin = g.getResult().equals("1-0");
			int r[] = lu.getPieceLocation((whitewin? LocalUmpire.BK : LocalUmpire.WK));
			if (r!=null)
			{
				sq[r[0]][(whitewin? r[1] : 7-r[1])]++;
			}
		}		
		
		// System.out.println("Checkmated king positions");
		for (int j=7; j>=0; j--)
		{
			for (int k=0; k<8; k++) System.out.print(sq[k][j]+" ");
			System.out.println();
		}
		
	}
	
	public static OpeningBook depurate(OpeningBook start, GameDatabase db, String playerName, boolean white, double percentageToKeep, int maxMoves)
	{
		int values[] = new int[start.size()];
		
		for (int k=0; k<db.gameNumber(); k++)
		{
			if (k%50==0) System.out.print("*");
			ExtendedPGNGame game = db.getGame(k);
			if (!(white? game.getWhite() : game.getBlack()).equals(playerName)) continue;
			//make sure you lost this game
			if (game.getResult().equals("1/2-1/2")) continue;
			if (game.getResult().equals("1-0") && white) continue;
			if (game.getResult().equals("0-1") && !white) continue;
			
			//ok, check...
			for (int op=0; op<start.size(); op++)
			{
				Opening o = start.getOpening(op);
				int match = 0; //number of matching moves from the start
				for (int j=0; j<maxMoves; j++)
				{
					PGNMoveData data = game.getMove(white,j);
					if (data==null) break;
					Move mov = o.getMove(j); 
					if (mov==null) break;
					if (mov.equals(data.finalMove)) match++;
					else break;
				}
				if (match>3) values[op]+=match;
			}

		}
		
		int toKeep = (int)(start.size()*percentageToKeep);
		int toDelete = start.size()-toKeep;
		for (int del=0; del<toDelete; del++)
		{
			int worst = -1;
			int worstScore = -1;
			for (int flag=0; flag<start.size(); flag++)
			{
				if (values[flag]>worstScore)
				{
					worst = flag;
					worstScore = values[flag];
				}
			}
			if (worst!=-1)
			{
				// System.out.println("Deleting opening, score "+worstScore);
				System.out.println(start.getOpening(worst).toString());
				values[worst] = -1;
			}
		}
		
		OpeningBook output = new OpeningBook();
		for (int flag=0; flag<start.size(); flag++)
		{
			if (values[flag]!=-1) output.addOpening(start.getOpening(flag));
		}
		return output;
	}
	
	public static boolean isSafe(Opening o, int fromMove, boolean strictly)
	{
		Metaposition m = new Metaposition(new Player());
		m.setup(o.white);
		
		for (int k=0; k<o.getMoveNumber(); k++)
		{
			Move move = o.getMove(k);
			m = Metaposition.evolveAfterMove(m,move,Chessboard.NO_CAPTURE,-1,-1,Chessboard.NO_CHECK,
					Chessboard.NO_CHECK,0);
			if (k<fromMove) continue;
			
			int king = m.getSquareWithPiece(Chessboard.KING);
			int kx = king/8; int ky = king % 8;
			
			if (kx<0 || ky<0 || kx>7 || ky>7) return false;
			
			//all the squares the attack might come from...
			int dx[] = {1,1,1,0,0,-1,-1,-1,1,1,2,2,-1,-1,-2,-2};
			int dy[] = {0,1,-1,1,-1,0,1,-1,2,-2,1,-1,2,-2,1,-1};
			
			m.setEmpty((byte)kx,(byte)ky); //remove the king so it won't contribute to the matrix
			m.computeProtectionMatrix(true);
			
			
			for (int index=0; index<dx.length; index++)
			{
				int cx = kx+dx[index];
				int cy = ky+dy[index];
				if (cx>=0 && cy>=0 && cx<8 && cy<8)
				{
					if (m.owner.globals.protectionMatrix[cx][cy]<1)
					{
						if (strictly) return false;
						boolean s = false;
						for (int mx=kx-1; mx<=kx+1; mx++)
							for (int my=ky-1; my<=ky+1; my++)
							{
								if (mx<0 || my<0 || mx>7 || my>7) continue;
								if (mx==cx && my==cy) continue;
								if (m.getFriendlyPiece(mx,my)!=Chessboard.EMPTY) continue;
								if (index>7) { s = true; break; } //enough if it's a Knight...
								//but for a Queen...
								boolean directionFound = true;
								for (int direction=0; direction<8; direction++)
								{
									int deltax = dx[direction]; int deltay = dy[direction];
									int beamx = mx+deltax; int beamy = my+deltay;
									while (beamx>=0 && beamy>=0 && beamx<8 && beamy<8 && m.getFriendlyPiece(beamx,beamy)==Chessboard.EMPTY)
									{
										if (beamx==cx && beamy==cy) {beamx=-1; directionFound=false;}
										else
										{
											beamx+=deltax; beamy+=deltay;
										}
									}
								}
								if (directionFound) s = true;
								
							}
						if (!s) 
						{
							m.setFriendlyPiece(kx,ky,Chessboard.KING);
							System.out.println(m.getRepresentation(Chessboard.KING));
							return false;
						}
					}
				}
			}
			
			m.setFriendlyPiece(kx,ky,Chessboard.KING);
			Metaposition.evolveAfterOpponentMove(m,-1,-1,Chessboard.NO_CHECK,Chessboard.NO_CHECK,0);
			
		}
		
		return true;
	}
	
	public static OpeningBook depurateOpeningBook(OpeningBook ob)
	{
		OpeningBook result = new OpeningBook();
		System.out.println("Size: "+ob.openings.size());
		
		for (int k=0; k<ob.openings.size(); k++)
		{
			Opening o = ob.getOpening(k);
			if (isSafe(o,5,false)) result.addOpening(o);
			//else System.out.println("Rejecting "+o);
		}
		
		System.out.println("Final Size: "+result.openings.size());
		return result;
	}
	
	public double avgElo(String player)
	{
		String pls[] = {"MeisterZinger","Sting-R","rjay","Pasi","Budgie","diswin","CJunk","Kilgor","paoloc",
				"christofischer","kino79","boing","kriegspiel","damir1813","tomtomtomtom"};
		
		Vector<String> opponents = new Vector<String>();
		
		double elo = 0.0;
		double enemyElo = 0.0;
		double score = 0.0;
		int samples = 0;
		double scoreTop = 0.0;
		int samplesTop = 0;
		int highestElo = 0;
		
		for (int k=0; k<gameNumber(); k++)
		{
			ExtendedPGNGame pgn = getGame(k);
			if (pgn.getWhite().toUpperCase().startsWith("GUEST")) continue;
			if (pgn.getBlack().toUpperCase().startsWith("GUEST")) continue;
			if (pgn.getWhite().toUpperCase().equals(player.toUpperCase()))
			{
				String s = pgn.getBlack();
				boolean found = false;
				for (int l=0; l<opponents.size(); l++)
					if (opponents.get(l).toUpperCase().equals(s.toUpperCase()))
					{
						found = true; break;
					}
				if (!found) opponents.add(s);
				
				if (pgn.getWhiteElo()>0)
				{
					if (pgn.getBlackElo()>highestElo) highestElo = pgn.getBlackElo();

					
					if (pgn.getWhiteElo()<pgn.getBlackElo()) elo++;
					enemyElo += pgn.getBlackElo();
					
					for (int j=0; j<pls.length; j++)
						if (pgn.getBlack().toUpperCase().equals(pls[j].toUpperCase()))
						{
							samplesTop++;
							if (pgn.getResult().equals("1-0")) scoreTop += 1.0;
							else if (pgn.getResult().equals("1/2-1/2")) scoreTop += 0.5;
						}
					
					if (pgn.getResult().equals("1-0")) score += 1.0;
					else if (pgn.getResult().equals("1/2-1/2")) score += 0.5;
					//elo += pgn.getWhiteElo();
					samples++;
				}
			}
			if (pgn.getBlack().toUpperCase().equals(player.toUpperCase()))
			{
				if (pgn.getBlackElo()>0)
				{
					String s = pgn.getWhite();
					boolean found = false;
					for (int l=0; l<opponents.size(); l++)
						if (opponents.get(l).toUpperCase().equals(s.toUpperCase()))
						{
							found = true; break;
						}
					if (!found) opponents.add(s);
					if (pgn.getWhiteElo()>highestElo) highestElo = pgn.getWhiteElo();

					if (pgn.getWhiteElo()>pgn.getBlackElo()) elo++;
					enemyElo += pgn.getWhiteElo();
					
					for (int j=0; j<pls.length; j++)
						if (pgn.getWhite().toUpperCase().equals(pls[j].toUpperCase()))
						{
							samplesTop++;
							if (pgn.getResult().equals("0-1")) scoreTop += 1.0;
							else if (pgn.getResult().equals("1/2-1/2")) scoreTop += 0.5;
						}
					
					if (pgn.getResult().equals("0-1")) score += 1.0;
					else if (pgn.getResult().equals("1/2-1/2")) score += 0.5;
					//elo += pgn.getBlackElo();
					samples++;
				}
			}
		}
		
		enemyElo = enemyElo/samples;
		score = score/samples;
		/*
		System.out.println("Samples : "+samples);
		System.out.println("Opponents: "+opponents.size());
		System.out.println("Enemy Elo: "+enemyElo);
		System.out.println("Highest Enemy Elo: "+highestElo);
		System.out.println("Score: "+score);
		*/
		if (samplesTop>0)
		{
			scoreTop = scoreTop/samplesTop;
			/*
			System.out.println("SamplesTop : "+samplesTop);
			System.out.println("Score Top: "+scoreTop);
			*/
		}
		
		return (elo/samples);
	}
	
	public static void main(String args[])
	{
		//if (args.length==0) return;
		//Darkboard.initialize(args[0]);
		String path = System.getProperty("user.home") + "/darkboard_data/";
		// System.out.println(path);
		Darkboard.initialize(path);
		
		GameDatabase db = new GameDatabase();
		GameDatabase db2 = new GameDatabase();
		//OpeningBook ob = depurateOpeningBook(Globals.blackOpeningBook);
		//ob.save(new File(Globals.PGNPath+"/blacksafe.opn"));
		
		//db.addGames(new File("/Users/giampi/PhD/KriegspielDatabase/complete.pgn"));
		//System.out.println("Ratio is "+PGNParser.getTryPerMoveRatio(db, "darkboard"));
		//db.addGames(new File("/Users/giampi/Darkboard/databasecream.pgn"));
		db.addGames(new File(GameDatabase.class.getResource("g/old1.pgn").getPath()));
		db.addGames(new File(GameDatabase.class.getResource("g/old2.pgn").getPath()));
		//db.addGames(new File("/Users/giampi/Darkboard/db2.pgn"));
		db2.addGames(new File(GameDatabase.class.getResource("g/round1.pgn").getPath()));
		db2.addGames(new File(GameDatabase.class.getResource("g/round2.pgn").getPath()));
		db2.addGames(new File(GameDatabase.class.getResource("g/round3.pgn").getPath()));
		//db.addGames(new File("/Users/giampi/Darkboard/db2.pgn"));
		//db.addGames(new File("/Users/giampi/r22"));
		
		System.out.println("OLD: "+db.avgElo("Darkboard"));
		System.out.println("NEW: "+db2.avgElo("Darkboard"));
		
		//db.findFinal("KR", "K");
		//db.findFinal("KQ", "K");
		//db.findFinal("KBB", "K");
		//db.findFinal("KBN", "K");
		
		//db.profile();
		
		if (db!=null) return;
		
		Collections.sort(db.database, db.new EloComparator());
		
		GameDatabase sub1 = new GameDatabase();
		GameDatabase sub2 = new GameDatabase();
		
		for (int k=0; k<db.database.size(); k++)
		{
			if (k<db.database.size()/2) sub1.database.add(db.database.get(k));
			else sub2.database.add(db.database.get(k));
		}
		
		System.out.println(sub1.getGame(0).getBestElo());
		System.out.println(sub2.getGame(0).getBestElo());
		
		/*PlayerModel pm = new PlayerModel();
		pm.getFromDatabase(db, true, null, 80);
		pm.save(new File("/Users/giampi/Darkboard/genericwhitemodel.mdl"));
		PlayerModel pm2 = new PlayerModel();
		pm2.getFromDatabase(db, false, null, 80);
		pm2.save(new File("/Users/giampi/Darkboard/genericblackmodel.mdl"));*/
		PlayerModel pm = new PlayerModel();
		pm.getFromDatabase(sub1, true, null, 80);
		pm.save(new File("/Users/giampi/Darkboard/sub1whitemodel.mdl"));
		PlayerModel pm2 = new PlayerModel();
		pm2.getFromDatabase(sub1, false, null, 80);
		pm2.save(new File("/Users/giampi/Darkboard/sub1blackmodel.mdl"));
		
		pm = new PlayerModel();
		pm.getFromDatabase(sub2, true, null, 80);
		pm.save(new File("/Users/giampi/Darkboard/sub2whitemodel.mdl"));
		pm2 = new PlayerModel();
		pm2.getFromDatabase(sub2, false, null, 80);
		pm2.save(new File("/Users/giampi/Darkboard/sub2blackmodel.mdl"));
		/*db.addGames(new File(Globals.PGNPath+"/testbed.pgn"));
		OpeningBook white = depurate(Globals.whiteOpeningBook,db,"darkboard",true,0.66,10);
		white.save(new File(Globals.PGNPath + "/wop.opn"));
		OpeningBook black = depurate(Globals.blackOpeningBook,db,"darkboard",false,0.66,10);
		black.save(new File(Globals.PGNPath + "/bop.opn"));*/
		//db.addGames(new File("/Users/giampi/Darkboard/databasemini.pgn"));
		//db.creamOfTheCrop(new File("/Users/giampi/PhD/KriegspielDatabase/filtered.pgn"),
		//new File("/Users/giampi/PhD/KriegspielDatabase/filtered2.pgn"));
		//System.out.println(pm);
		//System.out.println(pm2);
		//db.calculateAverageProtection(Chessboard.QUEEN,10,10);
		//db.calculateBranchFactor();
		//db.calculateOngoingMaterial();
		//db.calculateFinalPositions();
		
		//db.printResultStatistics();
		//db.printGameDurationStatitics();
		//db.calculateFinalMaterial();
		//db.printCheckmateLocations();
		/*
		PlayerRoster roster = new PlayerRoster(db);
		roster.sortRoster();
		Vector v = new Vector();
		String pls[] = {"MeisterZinger","Sting-R","rjay","Pasi","Budgie","diswin","CJunk","Kilgor","paoloc",
			"christofischer","kino79","boing","kriegspiel","damir1813"};
		for (int k=0; k<pls.length; k++)
		{
			 v.add(roster.getPlayerWithName(pls[k]));  } 
		
		//System.out.println(roster);
		
		db.calculateAverageRiskForAll(v);*/
		
		//DatabaseDataExtractor.extractPieceDensity(db).save(new File("/Users/giampi/Darkboard/density.dns"));
		
		//System.out.println(roster.toString());
		
		/*OpeningBook ob = new OpeningBook();
		for (int k=0; k<12; k++)
		{
			ob = roster.getPlayer(k).generateOpeningBook(true,10,ob);
		}
		//System.out.println(ob);
		System.out.println("Opening book size (white): "+ob.size());
		ob.save(new File("/Users/giampi/Darkboard/whiteopen.opn"));
		
		OpeningBook ob2 = new OpeningBook();
		for (int k=0; k<12; k++)
		{
			ob2 = roster.getPlayer(k).generateOpeningBook(false,10,ob2);
		}
		//System.out.println(ob2);
		System.out.println("Opening book size: "+ob2.size());
		ob2.save(new File("/Users/giampi/Darkboard/blackopen.opn"));*/
		

			/*MoveDatabase mdb = roster.getPlayerWithName("paoloc").generateFirstMoveMap(10);
			System.out.println("Stats for paoloc");
			System.out.println(mdb);*/
		
		/*
		
		db.saveDatabase(new File("/Users/giampi/Darkboard/database.db"));*/
	}

}
