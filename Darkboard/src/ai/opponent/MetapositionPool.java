package ai.opponent;

import java.util.Random;
import java.util.Vector;

import ai.player.Player;
import core.Chessboard;
import core.Metaposition;
import core.Move;
import gui.test.MetapositionPoolComponent;
import umpire.local.LocalUmpire;

public class MetapositionPool {

	Vector<Metaposition> pool = new Vector<Metaposition>();
	Vector<Vector<Metaposition>> old = new Vector<Vector<Metaposition>>();
	Player p;
	Player opp;
	int size = 100;
	boolean complete = true; //if true, the pool contains every possible state
	
	Vector<Metaposition> lastReferencePool = new Vector<Metaposition>();
	
	Random r = new Random(); 
	
	LocalUmpire lu = new LocalUmpire(new Player(), new Player());
	
	public MetapositionPoolComponent mpc = null;
	
	public MetapositionPool(Player pl, Player opponent, int s, Metaposition start)
	{
		p = pl; opp = opponent; size = s;
		
		pool.add(start);
		lastReferencePool.add(pl.simplifiedBoard);
		
	}
	
	public MetapositionPool(Player pl, Player opponent, int s)
	{
		p = pl; opp = opponent; size = s;
		
		Metaposition st = Metaposition.getChessboard(opponent);
		st.setup(!pl.isWhite);
		pool.add(st);
		lastReferencePool.add(pl.simplifiedBoard);
		
	}
	
	public void addMetaposition(Metaposition m)
	{
		if (pool.size()<size) { pool.add(m); return; }
	}
	
	public int getPoolSize()
	{
		return pool.size();
	}
	
	public Metaposition getSample(int k)
	{
		return pool.get(k);
	}
	
	public static double accuracy(Metaposition m, LocalUmpire truth)
	{
		double score = 0.0;
		double maxScore = 0.0;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int piece = truth.getBoard(k, j);
				boolean white = (piece>=1 && piece<=6);
				
				if ((piece!=LocalUmpire.EMPTY && white==m.isWhite()) /* || m.getFriendlyPiece(k, j)==Chessboard.PAWN */ )
				{
					
					int pc2 = truth.umpire2ChessboardPieceCode(piece);
					/*if (pc2!=Chessboard.PAWN && m.getFriendlyPiece(k, j)!=Chessboard.PAWN) continue;
					if (pc2==Chessboard.PAWN) maxScore+=1.0;
					if (m.getFriendlyPiece(k, j)==Chessboard.PAWN) maxScore+=1.0;
					if (m.getFriendlyPiece(k, j)==pc2) score += 2.0;*/
					
					maxScore+=1.0;
					if (m.getFriendlyPiece(k, j)!=Chessboard.EMPTY)
					{
						score += 0.5;
						
						if (m.getFriendlyPiece(k, j)==pc2) score += 0.5;
					}
				}
			}
		
		return (maxScore>0? score/maxScore : 1.0);
	}
	
	public double accuracy(LocalUmpire truth)
	{
		double score = 0.0;
		if (pool.size()==0) return 0.0;
		for (int k=0; k<pool.size(); k++) score += accuracy(pool.get(k),truth);
		return (score/pool.size());
	}
	
	public double probability(int x, int j, int what)
	{
		int samples = 0;
		
		for (int k=0; k<pool.size(); k++)
		{
			if (pool.get(k).getFriendlyPiece(x, j) == what) samples++;
		}
		
		return (1.0*samples/pool.size());
	}
	
	public double protection(int x, int y)
	{
		int samples = 0;
		
		for (int k=0; k<pool.size(); k++)
		{
			pool.get(k).computeProtectionMatrix(true);
			samples += pool.get(k).owner.globals.protectionMatrix[x][y];
		}
		
		return (1.0*samples/pool.size());
	}
	
	public void computeModelingData(Player p)
	{
		for (int k=0; k<pool.size(); k++)
		{
			Metaposition mp = pool.get(k);
		
			for (int x=0; x<8; x++)
				for (int y=0; y<8; y++)
				{
					p.globals.opponentDensityData[x][y][mp.getFriendlyPiece(x, y)]++;
				}
			
			mp.computeProtectionMatrix(true);
			for (int x=0; x<8; x++)
				for (int y=0; y<8; y++)
				{
					p.globals.opponentProtectionData[x][y]+=mp.owner.globals.protectionMatrix[x][y];
				}
		}
		
		int siz = pool.size();
		
		for (int x=0; x<8; x++)
			for (int y=0; y<8; y++)
			{
				for (int w=0; w<7; w++)
				{
					p.globals.opponentDensityData[x][y][w]/=siz;
				}
				p.globals.opponentProtectionData[x][y]/=siz;
			}
			
	}
	
	public double protectionError(LocalUmpire truth)
	{
		Metaposition m = truth.exportPerfectInformationChessboard(opp.isWhite? 0: 1);
		m.computeProtectionMatrix(true);
		
		double val = 0.0;
		
		LocalUmpire l = new LocalUmpire((p.isWhite? p: opp),(p.isWhite? opp: p));
		
		int mat[][] = new int[8][8];
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				mat[k][j] = m.owner.globals.protectionMatrix[k][j];
		
		for (int a=0; a<pool.size(); a++)
		{
			int mt[][] = l.getProtection(lastReferencePool.get(0), pool.get(a), opp.isWhite);
			int delta = 0;
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					int d = (mt[k][j]-mat[k][j]);
					if (d<0) d = -d;
					delta+= d;
				}	
			double v2 = delta/64.0;
			val += v2;
		}
		
		val /= pool.size();
		return val;
	}
	
	public Vector<Metaposition> getPoolForLevel(int level)
	{
		return (level<=0? pool : (level<old.size()? old.get(level) : null));
	}
	
	public void refillPool()
	{
		refillPool(0);
	}
	
	public void refillPool(int level)
	{
		//System.out.println("REFILL!");
		
		Vector<Metaposition> vec = getPoolForLevel(level);
		if (vec==null) return;
		
		Metaposition reference = lastReferencePool.get(level);
		
		//System.out.println(reference);
		
		int add = size - vec.size();
		for (int k=0; k<add; k++)
		{
			Vector<Metaposition> mv = MetapositionGenerator.generateMetaposition(opp, reference, 1, null);
			/*int best = 0;
			
			if (mv.size()>0)
			{
				double b = ((Darkboard)p).evaluate(mv.get(0), null, mv.get(0), null);
				for (int v=1; v<mv.size(); v++)
				{
					double b2 = ((Darkboard)p).evaluate(mv.get(v), null, mv.get(v), null);
					if (b2>b)
					{
						best = v; b = b2;
					}
				}
			}*/
			
			//vec.add(mv.get(best));
			for (int j=0; j<mv.size(); j++) vec.add(mv.get(j));
		}
		
		//Vector<Metaposition> next = getPoolForLevel(level+1);
		//if (next==null) return;
		
		//if (next.size()==0) refillPool(level+1);
		
		
	}
	
	public boolean compatibleWithPlayerMove(Metaposition mp, Metaposition source, Move m, Metaposition dest, int cap, int cx, int cy, int ck1, int ck2, int tries)
	{
		try
		{
			if (lu.areMetapositionsCompatibleWithIllegalMove(source, mp, source.isWhite(), m))
			{
				return false;
			} else
			if (!lu.areMetapositionsCompatibleWithUmpireMessage(dest, mp, !source.isWhite(), cx, cy, ck1, ck2, tries))
			{
				return false;
			} else
			{
				if (cap==Chessboard.CAPTURE_PAWN && mp.getFriendlyPiece(cx, cy)!=Chessboard.PAWN)
				{
					return false;
				}
				if (cap==Chessboard.CAPTURE_PIECE && mp.getFriendlyPiece(cx, cy)==Chessboard.PAWN)
				{
					return false;
				}

			}
		} catch (Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	public void updateWithPlayerMove(Metaposition source, Move m, Metaposition dest, int cap, int cx, int cy, int ck1, int ck2, int tries)
	{
		@SuppressWarnings("unused")
		LocalUmpire lu = new LocalUmpire(new Player(), new Player());
		
		for (int k=0; k<pool.size(); k++)
		{
			Metaposition mp = pool.get(k);
			if (compatibleWithPlayerMove(mp, source, m, dest, cap, cx, cy, ck1, ck2, tries))
			{
				Metaposition evolve = Metaposition.evolveAfterOpponentMove(mp, cx, cy, ck1, ck1, tries);
				pool.setElementAt(evolve, k);
			} else
			{
				//try to change the metaposition a bit to see if it works
				boolean fixed = false;
				/*if (old.size()>2 && !complete)
				for (int j=0; j<100; j++)
				{
					Metaposition mp2 = MetapositionGenerator.mutateMetaposition(mp, dest);
					if (mp2==null) continue;
					if (compatibleWithPlayerMove(mp2, source, m, dest, cap, cx, cy, ck1, ck2, tries))
					{
						//this one works
						Metaposition evolve = Metaposition.evolveAfterOpponentMove(mp2, cx, cy, ck1, ck1, tries);
						pool.setElementAt(evolve, k);
						fixed = true;
						break;
					}
					
				}*/
				if (!fixed) { pool.remove(mp); k--; }
			}
			
		}
		
		lastReferencePool.setElementAt(dest, 0);
		
		if (pool.size()<size && /*this.old.size()>2 &&*/ !complete) 
		{
			/*getPoolForLevel(1).clear();*/
			/*Vector<Metaposition> tempPool = pool;
			boolean finished = false;
			pool = new Vector<Metaposition>();
			int originalSize = size;
			int attempts = 0;
			
			while (!finished)
			{
				attempts++;
				size = originalSize - tempPool.size();*/
				refillPool();
				/*for (int k=0; k<pool.size(); k++)
				{
					Metaposition mp = pool.get(k);
					if (!compatibleWithPlayerMove(mp, source, m, dest, cap, cx, cy, ck1, ck2, tries))
					{
						pool.remove(mp); k--;
					} else
					{
						Metaposition evolve = Metaposition.evolveAfterOpponentMove(mp, cx, cy, ck1, ck1, tries);
						pool.setElementAt(evolve, k);
					}
				}
				tempPool.addAll(pool);
				pool.clear();
				if (tempPool.size()==originalSize) finished = true;
				if (attempts>=10) finished = true;
			}
			size = originalSize;
			pool = tempPool;*/
		}
		//System.out.println(this);
		
		if (mpc!=null) mpc.redo();
	}
	
	
	public void updateWithOpponentMove(Metaposition source, Metaposition dest, int cap, int cx, int cy, int ck1, int ck2, int tries)
	{
		//LocalUmpire lu = new LocalUmpire(new Player(), new Player());
		Vector<Metaposition> evolution = new Vector<Metaposition>(); //new belief state for the next move.
		Vector<Double> evaluations = new Vector<Double>();
		Vector<Move> dummy = new Vector<Move>();
		
		for (int k=0; k<pool.size(); k++)
		{
			Metaposition mp = pool.get(k);
			int legalChildren = 0;
			//find all possible moves...
			Vector<Move> moves = mp.generateMoves(true, mp.owner);
			Vector<Move> legal = new Vector<Move>();
			Vector<Metaposition> evol = new Vector<Metaposition>();
			//now prune those that are illegal or bring the wrong umpire messages...
			for (int mv=0; mv<moves.size(); mv++)
			{
				try
				{
					if (lu.areMetapositionsCompatibleWithIllegalMove(source, mp, mp.isWhite(), moves.get(mv))) continue;
				} catch (Exception e)
				{
					continue;
				}
				Metaposition evo = Metaposition.evolveAfterMove(mp, moves.get(mv), cap, cx, cy, ck1, ck2, tries);
				try
				{
					if (!lu.areMetapositionsCompatibleWithUmpireMessage(source, evo, !source.isWhite(), cx, cy, ck1, ck2, tries)) continue;
				} catch (Exception e2)
				{
					continue;
				}
				//double eval = ((Darkboard)p).evaluate(mp,moves.get(mv),evo,dummy);
				//evolution.add(evo);
				
				legal.add(moves.get(mv));
				evol.add(evo);
				//evaluations.add(new Double(eval));
				legalChildren++;
			}
			
			if (legalChildren==0)
			{
				pool.remove(mp); k--;
			} else
			{
				Vector<Metaposition> mvv = MetapositionGenerator.evolveMetaposition(mp, source, legal, evol, (complete? size : 10));
				evolution.addAll(mvv);
			}
		}
		
		int limit = size; //(old.size()>=3? (int)(size*0.8): size); //always leave some room for new boards
		
		complete=false;
		
		if (evolution.size()>limit)
		{
			//trim vector
			//TODO do it with evaluation function rather than pure randomness
			/*Vector<Metaposition> temp = new Vector<Metaposition>();
			int samples[] = new int[3];
			
			for (int k=0; k<limit; k++) 
			{
				for (int j=0; j<samples.length; j++) samples[j] = r.nextInt(evolution.size());
				
				int max = 0;
				
				for (int j=1; j<samples.length; j++) 
					if (evaluations.get(samples[j]).doubleValue()>evaluations.get(samples[max]).doubleValue()) max = j;
				
				Metaposition rn = evolution.get(samples[max]);
				
				temp.add(rn);
				evolution.remove(rn);
				evaluations.remove(samples[max]);
			}
			evolution = temp;*/
			
			complete = false;
			
			while (evolution.size()>limit)
			{
				evolution.remove(r.nextInt(evolution.size()));
			}
		}
		
		//pool becomes the first of the old
		old.insertElementAt(pool, 0);
		lastReferencePool.insertElementAt(dest, 0);
		pool = evolution;
		
		//trim old pools when too large
		if (old.size()>10) old.remove(10);
		
		if (pool.size()<size && /*this.old.size()>2*/ !complete) 
		{ 
			/*getPoolForLevel(1).clear();*/
			/*Vector<Metaposition> tempPool = pool;
			boolean finished = false;
			boolean doChecks = true;
			pool = new Vector<Metaposition>();
			int originalSize = size;
			int attempts = 0;
			
			while (!finished)
			{
				attempts++;
				size = originalSize - tempPool.size();*/
				refillPool();
				/*for (int k=0; k<pool.size() && doChecks; k++)
				{
					Metaposition mp = pool.get(k);
					try
					{
						if (!lu.areMetapositionsCompatibleWithUmpireMessage(source, mp, source.isWhite(), cx, cy, ck1, ck2, tries))
						{
							pool.remove(mp); k--;
						} else
						{
							Metaposition evolve = Metaposition.evolveAfterOpponentMove(mp, cx, cy, ck1, ck1, tries);
							pool.setElementAt(evolve, k);
						}
					} catch (IncompatibleMetapositionException ime) { pool.remove(mp); k--; }
				}
				tempPool.addAll(pool);
				pool.clear();
				if (tempPool.size()==originalSize) finished = true;
				if (attempts>=2) doChecks = false;
			}
			size = originalSize;
			pool = tempPool;*/
		}
		//System.out.println(this);
		
		if (mpc!=null) mpc.redo();
	}
	
	public boolean compatibleWithIllegalMove(Metaposition source, Metaposition mp, boolean white, Move m)
	{
		try
		{
			if (!lu.areMetapositionsCompatibleWithIllegalMove(source, mp, source.isWhite(), m))
			{
				return false;
			}
		} catch (Exception e)
		{
			return false;
		}
		return true;
	}
	
	public void updateWithIllegalMove(Metaposition source, Move m)
	{
		//LocalUmpire lu = new LocalUmpire(new Player(), new Player());
		
		for (int k=0; k<pool.size(); k++)
		{
			Metaposition mp = pool.get(k);
			if (!compatibleWithIllegalMove(source,mp,source.isWhite(),m))
			{
				boolean fixed = false;
				/*if (!complete)
				for (int j=0; j<100; j++)
				{
					Metaposition mp2 = MetapositionGenerator.mutateMetaposition(mp, source);
					if (mp2==null) continue;
					if (compatibleWithIllegalMove(source,mp2,source.isWhite(),m))
					{
						fixed = true;
						pool.setElementAt(mp2, k);
						break;
					}
				}*/
				if (!fixed) { pool.remove(mp); k--; }
			}
		}
		
		if (pool.size()==0 && !complete)
		{
			/*Vector<Metaposition> tempPool = pool;
			boolean finished = false;
			pool = new Vector<Metaposition>();
			int originalSize = size;
			int attempts = 0;
			
			while (!finished)
			{
				attempts++;
				size = originalSize - tempPool.size();*/
				refillPool();
				/*for (int k=0; k<pool.size(); k++)
				{
					Metaposition mp = pool.get(k);
					if (!compatibleWithIllegalMove(source,mp,source.isWhite(),m))
					{
						pool.remove(mp); k--;
					}
				}
				tempPool.addAll(pool);
				pool.clear();
				if (tempPool.size()==originalSize) finished = true;
				if (attempts>=10) finished = true;
			}
			size = originalSize;
			pool = tempPool;*/
		}
		
		//System.out.println(this);
		if (mpc!=null) mpc.redo();
	}
	
	public String toString()
	{
		String result = "SIZE = "+pool.size()+"\n";
		for (int k=0; k<pool.size(); k++)
		{
			result+=""+k+":\n"+pool.get(k).toString()+"\n";
		}
		return result;
	}
}
