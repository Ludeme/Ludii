package ai.opponent;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import core.Chessboard;
import core.Metaposition;
import core.Move;

public class Metapool {
	
	public class MetapoolState
	{
		int startX, startY; //the piece being followed
		int type; //its type (might be different from original type if promoted)
		int nowX, nowY; //its current location
		double probability;
		Metaposition board; //the rest of the board
		
		public MetapoolState()
		{
			
		}
		
		public MetapoolState(int x, int y, Metaposition b)
		{
			startX = x; startY = y;
			//type = b.getFriendlyPiece(x,y);
			if (y==1 || y==6) type = Chessboard.PAWN;
			else if (x==0 || x==7) type = Chessboard.ROOK;
			else if (x==1 || x==6) type = Chessboard.KNIGHT;
			else if (x==2 || x==5) type = Chessboard.BISHOP;
			else if (x==3) type = Chessboard.QUEEN; else type = Chessboard.KING;
			nowX = x; nowY = y;
			probability = 1.0;
			board = b;
		}

		public Vector<MetapoolState> evolveWithPlayerMove(
				Metapool owner,
				Metaposition start, 
				Move m, 
				Metaposition end,
				int cx,
				int cy,
				int cwhat,
				int ck1,
				int ck2,
				int tries)
		{
			Vector<MetapoolState> result = new Vector<MetapoolState>();
			
			Vector<Move> mv = board.generateMoves(true, board.owner);
			
			//if the move should not have been possible, stop here
			boolean found = false;
			for (int k=0; k<mv.size(); k++)
				if (mv.get(k).equals(m)) { found = true; break; }
			if (!found) 
			{
				return result;
			}
			
			//if the thing cannot be evolved consistently, stop here
			Metaposition ev = Metaposition.evolveAfterMoveWithChecks(board, m, cwhat, cx, cy, ck1, ck2, tries);
			if (ev==null) 
			{
				return result;
			}
			
			//if the resulting MP is not a subset of the general MP, stop here
			if (!ev.isSubsetOf(end) || ev.isEmpty()) 
				return result;
			
			if (ev.getFriendlyPiece(nowX, nowY)!=Chessboard.EMPTY || ev.mayBeEmpty(nowX, nowY)) 
			{
				return result;
			}
			
			//now output your evolution
			MetapoolState next = new MetapoolState();
			next.startX = startX;
			next.startY = startY;
			next.type = type; //its type
			next.nowX = nowX; next.nowY = nowY; //its current location
			next.probability = probability;
			next.board = ev;
			
			if (cx!=-1 && captureMask[startX][startY])
			{
				if (isOnlyOneOnSquare(cx, cy, startX, startY))
				{
					aliveProbability[startX][startY] = 0.0;
					return result;
				}
			}
			
			if (cx==nowX && cy==nowY)
			{
				//piece dead
				double ratio = captureWhat[startX][startY]/captureOccupiedProb;
				probability /= ratio;
				
				aliveProbability[startX][startY] -= probability;
				return result;
			}
			
			result.add(next);
			
			return result;
		}
		
		public Vector<MetapoolState> evolveWithIllegalMove(
				Metapool owner,
				Metaposition start, 
				Move m, 
				int cx,
				int cy,
				int ck1,
				int ck2,
				int tries)
		{
			Vector<MetapoolState> result = new Vector<MetapoolState>();
			
			Vector<Move> mv = board.generateMoves(true, board.owner);
			
			//if the move should have been possible, stop here
			for (int k=0; k<mv.size(); k++)
				if (mv.get(k).equals(m)) return result;
			
			Metaposition ev = Metaposition.evolveAfterIllegalMove(start, m, cx, cy, ck1, ck2, tries);
			//now output your evolution
			MetapoolState next = new MetapoolState();
			next.startX = startX;
			next.startY = startY;
			next.type = type; //its type 
			next.nowX = nowX; next.nowY = nowY; //its current location
			next.probability = probability;
			next.board = ev;
			
			result.add(next);
			
			return result;
		}
		
		/**
		 * This is the trickiest of the three update methods. The highlighted piece may or may
		 * not move, and the distribution among the possible moves is delegated to another component
		 * for later opponent modeling.
		 * @param owner
		 * @param start
		 * @param end
		 * @param cx
		 * @param cy
		 * @param ck1
		 * @param ck2
		 * @param tries
		 * @return
		 */
		public Vector<MetapoolState> evolveWithOpponentMove(
				Metapool owner,
				Metaposition start, 
				Metaposition end,
				int cx,
				int cy,
				int ck1,
				int ck2,
				int tries)
		{
			Vector<MetapoolState> result = new Vector<MetapoolState>();
			
			//first, assume some other piece was moved
			board.owner.globals.stopSquareFromOpponentMove = true;
			board.owner.globals.stopSquareX = nowX;
			board.owner.globals.stopSquareY = nowY;
			Metaposition ev = Metaposition.evolveAfterOpponentMove(board, cx, cy, ck1, ck2, tries);
			board.owner.globals.stopSquareFromOpponentMove = false;
			
			MetapoolState next = new MetapoolState();
			next.probability = 0.0;
			if (ev.isSubsetOf(end) && !ev.isEmpty())
			{
				next.startX = startX;
				next.startY = startY;
				next.type = type; //its type 
				next.nowX = nowX; next.nowY = nowY; //its current location
				next.probability = probability*probabilityOfAnotherPieceBeingMoved();
				next.board = ev;
				result.add(next); //we will have to adjust the probability of next if no moves are allowed
			}
			
			//now test other possible moves
			Vector<Move> mov = board.possibleOpponentMoves(cx, cy, ck1, ck2, tries, nowX, nowY, type);
			Vector<Metaposition> met = new Vector<Metaposition>();
			
			for (int k=0; k<mov.size(); k++)
			{
				//Metaposition mp = Metaposition.evolveAfterOpponentMove(board, cx, cy, ck1, ck2, tries);
				Move theMove = mov.get(k);
				Metaposition mp = Metaposition.getChessboard(board);
				int ind1 = theMove.fromX * 8 + theMove.fromY;
				int ind2 = theMove.toX * 8 + theMove.toY;
				mp.squares[ind2] = mp.squares[ind1];
				mp.setEmpty(theMove.fromX, theMove.fromY);
				if (mp.isSubsetOf(end) && !mp.isEmpty())
				 met.add(mp);
				else
				{
					mov.remove(k); k--;
				}
			}
			
			if (mov.size()==0) next.probability = probability; //no other choice, so it gets all the probability
			
			//now create the corresponding new states.
			double probs[] = probabilitiesOfMoveBeingChosen(mov,met);
			for (int k=0; k<mov.size(); k++)
			{
				MetapoolState state = new MetapoolState();
				state.startX = startX;
				state.startY = startY;
				state.type = type; //its type 
				state.nowX = mov.get(k).toX; state.nowY = mov.get(k).toY; //its new location
				state.probability = (probability-next.probability)*probs[k];
				state.board = met.get(k);
				
				//pretend promotion is death
				if (type==Chessboard.PAWN && (state.nowY==0 || state.nowY==7)) 
				{
					aliveProbability[state.startX][state.startY] -= state.probability;
					continue;
				}
				
				result.add(state);
			}
			
			return result;
		}
		
		public MetapoolState compress(MetapoolState other)
		{
			MetapoolState state = new MetapoolState();
			state.startX = startX;
			state.startY = startY;
			state.type = type;
			state.nowX = nowX; state.nowY = nowY;
			state.probability = probability+other.probability;
			state.board = Metaposition.union(board, other.board);
			return state;
		}
		
		/**
		 * To be extended with opponent modeling.
		 * @return
		 */
		public double probabilityOfAnotherPieceBeingMoved()
		{
			if (board.getOpponentPawnTries()>0) return 0.5;
			int tot = board.pawnsLeft + board.piecesLeft;
			return (1.0*tot/(tot+1));
		}
		
		/**
		 * To be extended with opponent modeling.
		 * @param mov
		 * @param met
		 * @return
		 */
		public double[] probabilitiesOfMoveBeingChosen(Vector<Move> mov, Vector<Metaposition> met)
		{
			double out[] = new double[mov.size()];
			
			for (int k=0; k<out.length; k++) out[k] = 1.0 / out.length;
			
			return out;
		}
		
	}
	
	public class PoolstateComparator implements Comparator<MetapoolState>
	{
		public int compare (MetapoolState o1, MetapoolState o2)
		{
			double diff = o1.probability - o2.probability;
			if (diff>0.0f) return 1;
			if (diff<0.0f) return -1;
			return 0;
		}
	}	
	
	Vector<MetapoolState> pool[][][][] = new Vector[8][8][8][8];
	public PoolstateComparator comparator = new PoolstateComparator();
	int poolSize = 3;
	boolean white;
	double aliveProbability[][] = new double[8][8];
	double compoundProbabilities[][][][] = new double[8][8][8][8];
	
	//Utilities
	boolean captureMask[][];
	double captureWhat[][];
	double captureOccupiedProb;
	
	public void compressPool(int sx, int sy, int ex, int ey)
	{
		Vector<MetapoolState> p = pool[sx][sy][ex][ey];
		
		if (p==null) return;
		
		while (p.size()>poolSize)
		{
			Collections.sort(p,comparator);
			
			//take the two smallest states and merge them
			MetapoolState ms1 = p.remove(p.size()-1);
			MetapoolState ms2 = p.remove(p.size()-1);
			MetapoolState ms3 = ms1.compress(ms2);
			p.add(ms3);
		}
	}
	
	public void compressPool()
	{
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
						compressPool(a,b,c,d);
	}
	
	public void updateProbabilities()
	{
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
			{
				double prob = 0.0;
				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
					{
						Vector<MetapoolState> p = pool[a][b][c][d];
						if (p!=null) for (int k=0; k<p.size(); k++) prob += p.get(k).probability;
					}
				double factor = aliveProbability[a][b] / prob;

				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
					{
						Vector<MetapoolState> p = pool[a][b][c][d];
						if (p!=null) for (int k=0; k<p.size(); k++)
						{
							MetapoolState ms = p.get(k);
							ms.probability*=factor;
						}
					}
			}
	}
	
	public void updatePool(Vector<MetapoolState> v)
	{
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
					{
						Vector<MetapoolState> p = pool[a][b][c][d];
						if (p!=null) p.clear();
					}
		
		for (int k=0; k<v.size(); k++)
		{
			MetapoolState ms = v.get(k);
			pool[ms.startX][ms.startY][ms.nowX][ms.nowY].add(ms);
		}
		
		compressPool();
		updateProbabilities();
	}
	
	public void evolveWithPlayerMove(Metaposition start, Move m, Metaposition end, int cap, int cx, int cy, int ck1, int ck2, int tries)
	{
		Vector<MetapoolState> msv = new Vector<MetapoolState>();
		
		if (cx!=-1)
		{
			captureMask = whatOnSquare(cx,cy);
			captureWhat = howOnSquare(cx, cy);
			captureOccupiedProb = totalProbabilityOnSquare(cx, cy);
		}
		
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
					{
						Vector<MetapoolState> p = pool[a][b][c][d];
						if (p!=null)
							for (int k=0; k<p.size(); k++)
							{
								Vector<MetapoolState> mv = p.get(k).evolveWithPlayerMove(this, start, m, end, cx, cy, cap, ck1, ck2, tries);
								msv.addAll(mv);
							}
					}
		updatePool(msv);
	}
	
	public void evolveWithIllegalMove(Metaposition start, Move m, int cx, int cy, int ck1, int ck2, int tries)
	{
		Vector<MetapoolState> msv = new Vector<MetapoolState>();
		
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
					{
						Vector<MetapoolState> p = pool[a][b][c][d];
						if (p!=null)
							for (int k=0; k<p.size(); k++)
							{
								Vector<MetapoolState> mv = p.get(k).evolveWithIllegalMove(this, start, m, cx, cy, ck1, ck2, tries);
								msv.addAll(mv);
							}
					}
		updatePool(msv);
	}
	
	public void evolveWithOpponentMove(Metaposition start, Metaposition end, int cx, int cy, int ck1, int ck2, int tries)
	{
		Vector<MetapoolState> msv = new Vector<MetapoolState>();
		
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
					{
						Vector<MetapoolState> p = pool[a][b][c][d];
						if (p!=null)
							for (int k=0; k<p.size(); k++)
							{
								Vector<MetapoolState> mv = p.get(k).evolveWithOpponentMove(this,start,end,cx,cy,ck1,ck2,tries);
								msv.addAll(mv);
							}
					}
		updatePool(msv);
	}
	
	public double[][] howOnSquare(int x, int y)
	{
		double out[][] = new double[8][8];
		
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
			{
				Vector<MetapoolState> v = pool[a][b][x][y];
				if (v==null) continue;
				for (int k=0; k<v.size(); k++)
				{
					MetapoolState ms = v.get(k);
					out[ms.startX][ms.startY] += ms.probability;
				}
			}
		
		return out;
	}
	
	public boolean[][] whatOnSquare(int x, int y)
	{
		boolean out[][] = new boolean[8][8];
	
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
			{
				Vector<MetapoolState> v = pool[a][b][x][y];
				if (v==null) continue;
				for (int k=0; k<v.size(); k++)
				{
					MetapoolState ms = v.get(k);
					out[ms.startX][ms.startY] = true;
				}
			}
		
		return out;
	}
	
	public boolean isOnlyOneOnSquare(int x, int y, int sx, int sy)
	{
		boolean a[][] = whatOnSquare(x,y);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				if (a[k][j] && (k!=sx || j!=sy)) return false;
		
		return true;
	}
	
	public double totalProbabilityOnSquare(int x, int y)
	{
		double out = 0.0;
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
			{
				Vector<MetapoolState> v = pool[a][b][x][y];
				if (v==null) continue;
				for (int k=0; k<v.size(); k++)
				{
					MetapoolState ms = v.get(k);
					out += ms.probability;
				}
			}
		
		return out;
	}
	
	public Metapool(Metaposition start)
	{
		white = start.isWhite();
		
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
				aliveProbability[a][b] = 1.0;
		
		for (int a=0; a<8; a++)
			for (int b=0; b<8; b++)
				for (int c=0; c<8; c++)
					for (int d=0; d<8; d++)
					{
						compoundProbabilities[a][b][c][d] = 0.0;
						if (b==1 || b==6) pool[a][b][c][d] = new Vector<MetapoolState>();
					}
		int where = (start.isWhite()? 6 : 1);
		for (int k=0; k<8; k++) pool[k][where][k][where].add(new MetapoolState(k,where,start));
		
	}
	


}
