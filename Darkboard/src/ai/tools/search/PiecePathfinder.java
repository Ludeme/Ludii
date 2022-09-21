package ai.tools.search;

import java.util.Vector;

import core.Chessboard;
import core.Metaposition;

/**
 * Pathfinding component.
 * @author Nikola Novarlic
 *
 */
public class PiecePathfinder {
	
	public class PathfindingOptions
	{
		int targetX = -1;
		int targetY = -1;
	}
	
	public static int matrix[][] = new int[8][8];
	
	public static PathfindingOptions defaultOptions;
	
	public static void init()
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) matrix[k][j]=1000;
	}
	
	/**
	 * Executes a pathfinding iteration.
	 * @param m
	 * @param x
	 * @param y
	 * @param movementX
	 * @param movementY
	 * @param level
	 * @return
	 */
	protected static boolean iterate(Metaposition m, int x, int y, int movementX[], int movementY[], boolean once, int level)
	{
		boolean progress = false;
		int dirs = movementX.length;
		
		int x1 = (level==0? x : 0);
		int y1 = (level==0? y : 0);
		int x2 = (level==0? x+1 : 8);
		int y2 = (level==0? y+1 : 8);
		
		for (int k=x1; k<x2; k++)
			for (int j=y1; j<y2; j++)
			{
				if (matrix[k][j]==level)
				{
					for (int dir=0; dir<dirs; dir++)
					{
						int dx = movementX[dir];
						int dy = movementY[dir];
						int tx = k+dx; int ty = j+dy;
						while (true)
						{
							if (tx<0 || ty<0 || tx>=8 || ty>=8) break;
							if (matrix[tx][ty]!=1000) break;
							if (!m.mayBeEmpty(tx,ty)) break;
							matrix[tx][ty]=level+1;
							progress = true;
							tx+=dx; ty+=dy;
							if (once) break;
						}
					}
				}
			}
		return progress;
	}
	
	/*public static int pathfind(Metaposition m, int x, int y, int x2, int y2, int maxLevel)
	{
		init();
		
		int piece = m.getFriendlyPiece(x,y);
		boolean once = (piece==Chessboard.KING || piece==Chessboard.PAWN || piece==Chessboard.KNIGHT);
		
		if (piece==Chessboard.EMPTY) return -1;
		
		int mx[] = Chessboard.getPieceMovementVectorX(piece,m.isWhite()); //white/black is irrelevant... won't be used with pawns.
		int my[] = Chessboard.getPieceMovementVectorY(piece,m.isWhite()); 
		
		matrix[x][y] = 0;
		
		for (int iteration = 0; iteration != maxLevel; iteration++)
		{
			boolean progress = iterate(m,x,y,mx,my,once,iteration);
			if (matrix[x2][y2]!=1000) return matrix[x2][y2];
			if (!progress) return -1;
		}
		return -1;
	}*/
	
	/**
	 * New version of pathfinding algorithm, a bit slower but as accurate as metapositions get
	 */
	public static int pathfind(Metaposition m, int x, int y, int x2, int y2, int maxLevel)
	{
		int board[][] = new int[8][8];
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) matrix[k][j]=1000;
		return 0;
	}
	
	public static int distance(Metaposition m, int x, int y, Vector<int[]> destinations, int maxLevel, PathfindingOptions opt)
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) matrix[k][j]=1000;
		
		boolean target = (opt!=null && opt.targetX>=0);
		
		int piece = m.getFriendlyPiece(x, y);
		if (piece == Chessboard.EMPTY) return 0;
		
		int mx[] = Chessboard.getPieceMovementVectorX(piece,m.isWhite());
		int my[] = Chessboard.getPieceMovementVectorY(piece,m.isWhite()); 
		boolean once = (piece==Chessboard.KING || piece==Chessboard.KNIGHT || piece==Chessboard.PAWN);
		
		for (int k=0; k<destinations.size(); k++) matrix[destinations.get(k)[0]][destinations.get(k)[1]] = -1000;
		
		if (matrix[x][y]==-1000) return 0;
		
		matrix[x][y] = 0;
		
		for (int level=0; level<maxLevel; level++)
		{
			//boolean somethingChanged = false;
			//iterate...
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					if (matrix[k][j]==level)
					{
						//expand from here
						for (int dir=0; dir<mx.length; dir++)
						{
							int heuristicLevel = level+1;
							int dx = mx[dir]; int dy = my[dir];
							boolean pawnBonus = (piece==Chessboard.PAWN && j==m.getSecondRank());
							//if (pawnBonus) dy*=2;
							int x1 = k+dx; int y1 = j+dy;
							
							//block a piece from going vertically through a pawn
							if (x1<0 || x1>=8 || y1<0 || y1>=8) continue;
							//if (dx==0 && m.getFriendlyPiece(x1, y1)==Chessboard.PAWN) continue;
							
							do
							{
								int pc = m.getFriendlyPiece(x1, y1); 
								if (pc!=Chessboard.EMPTY)
								{
									//you found a friendly obstacle... depending on your search mode, do something
									if (target && !once)
									{
										//if we are trying to threaten a square for capturing purposes, the presence
										//of an allied piece with the same attacking capabilities already
										//threatening the square on the same line is not a problem. That piece will
										//attack first and if captured this one will avenge it.
										if (dx==0 && dy!=0 && opt.targetX==k && (pc==Chessboard.ROOK || pc==Chessboard.QUEEN)) {}
										else if (dx!=0 && dy==0 && opt.targetY==j && (pc==Chessboard.ROOK || pc==Chessboard.QUEEN)) {}
										else if (dx*dy==1 && (opt.targetX-opt.targetY==k-j)&& (pc==Chessboard.BISHOP || pc==Chessboard.QUEEN)) {}
										else if (dx*dy==-1 && (opt.targetX+opt.targetY==k+j)&& (pc==Chessboard.BISHOP || pc==Chessboard.QUEEN)) {}
										else heuristicLevel+=1;
									} else 
									{
										if (dx==0 && pc==Chessboard.PAWN) heuristicLevel+=8;
										else heuristicLevel+=1; //assume it will take some time to move the obstacle, more if it's a pawn
									}
								}
								if (matrix[x1][y1]==-1000) 
									{ 
										if (target && heuristicLevel==1) return 0; //special 'aiming' case
										return heuristicLevel; //done!
									}
								if (matrix[x1][y1]<=heuristicLevel) break; //already got here with a shorter path
								matrix[x1][y1] = heuristicLevel;
								//somethingChanged = true;
								if (once)
								{
									if (!pawnBonus) break;
									else pawnBonus = false;
								}
								x1 += dx; y1 += dy;
								if (heuristicLevel!=(level+1)) break;
							} while (x1>=0 && y1>=0 && x1<8 && y1<8);
						}
					}
				}
		}
		
		return -1;
	}
	
	public static int distance(Metaposition m, int x, int y, Vector<int[]> destinations, int maxLevel)
	{
		return distance(m,x,y,destinations,maxLevel,null);
	}
	
	public static int distance(Metaposition m, int x, int y, Vector<int[]> destinations, int maxLevel, int targx, int targy)
	{
		if (defaultOptions==null) defaultOptions = new PiecePathfinder().new PathfindingOptions();
		defaultOptions.targetX = targx; defaultOptions.targetY = targy;
		return distance(m,x,y,destinations,maxLevel,defaultOptions);
	}

}
