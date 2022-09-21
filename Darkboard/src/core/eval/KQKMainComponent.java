/*
 * Created on 8-apr-06
 *
 */
package core.eval;

import java.util.Vector;

import core.Chessboard;
import core.EvaluationFunctionComponent;
import core.Metaposition;
import core.Move;

/**
 * @author Nikola Novarlic
 * Evaluates a position in the KQK endgame. Adapted from Bolognesi.
 */
public class KQKMainComponent extends EvaluationFunctionComponent {
	
	private static int matrix[][] =
	{
		{1,1,0,0,0,0,1,1},
		{1,0,0,0,0,0,0,1},
		{0,0,-2,-4,-4,-2,0,0},
		{0,0,-4,-4,-4,-4,0,0},
		{0,0,-4,-4,-4,-4,0,0},
		{0,0,-2,-4,-4,-2,0,0},
		{1,0,0,0,0,0,0,1},
		{1,1,0,0,0,0,1,1}
	};
	
	public static boolean KING_IN_CHECK = false;

	public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
	{
		float result = 0.0f;
		int rx, ry, kx, ky;
		
		
		int rook = dest.getSquareWithPiece(Chessboard.QUEEN);
		rx = rook/8; ry = rook%8;
		
		int king = dest.getSquareWithPiece(Chessboard.KING);
		kx = king/8; ky = king%8;
		
		int mateDetector = stalemateAlert(start,m,dest);
		if (mateDetector<0) return mateDetector*10000;
		
		Metaposition meta = Metaposition.evolveAfterOpponentMove(dest,-1,
				-1,Chessboard.NO_CHECK,Chessboard.NO_CHECK,0);
		
		int f1 = rookUnderAttack(dest,rx,ry);
		int f2 = evalKingDistance(meta,kx,ky);
		int f3 = evalAreas(meta,rx,ry);
		int f4 = adjacentRook(rx,ry,kx,ky);
		int f5 = kingMatrix(meta);
		
		result = -420 + f1*840 - f2 - f3 + f4 + f5;
		if (mateDetector>0 && f1>0) result+=10000;
		
		return result;
	}
	
	
	private int rookUnderAttack(Metaposition m, int rookx, int rooky)
	{
		if (m.owner.globals.protectionMatrix[rookx][rooky]>0) return 1;
		
		for (int k=rookx-1; k<=rookx+1; k++)
			for(int j=rooky-1; j<=rooky+1; j++)
			{
				if (k<0 || k>7 || j<0 || j>7) continue;
				if (k==rookx && j==rooky) continue;
				if (m.canContain((byte)k,(byte)j,(byte)Chessboard.KING)) return 0;
			}
			
		return 1;
	}
	
	private int evalKingDistance(Metaposition m, int kingx, int kingy)
	{
		int maxDistance = 0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (m.canContainEnemyKing((byte)k,(byte)j))
				{
					int distx = k-kingx; int disty = j-kingy;
					if (distx<0) distx = -distx; if (disty<0) disty = -disty;
					int dist = distx + disty;
					if (dist>maxDistance) maxDistance = dist;
				}
			}
			
		return maxDistance;
	}
	
	protected int evalAreas(Metaposition m, int rx, int ry)
	{
		int areas = 0;
		int squares = 0;
		boolean quad[] = new boolean[4];
		
		for (int k=0; k<4; k++)
		{
			//check the four quadrants in turn
			boolean found = false;
			int x1,x2,y1,y2;
			if (k<2) { x1 = 0; x2 = rx;} else { x1 = rx; x2 = 7; }
			if (k%2==0) { y1 = 0; y2 = ry; } else { y1 = ry; y2 = 7; }
			
			if (rx==0 || ry==0 || rx==7 || ry==7) 
				{found=true;}
			
			for (int x=x1; x<=x2; x++)
				for (int y=y1; y<=y2; y++)
				{
					if (m.canContainEnemyKing((byte)x,(byte)y))
					{
						found = true;
						quad[k]=true;
						squares++;
					}
				}
				
			if (found) 
				{
					areas++;
					//squares += (x2-x1+y2-y1);
				}
		}
		
		int bonus=0;
		
		if (areas==2)
		{
			boolean hor = (quad[0]&&quad[1])||(quad[2]&&quad[3]);
			boolean ver = (quad[0]&&quad[2])||(quad[1]&&quad[3]);
			
			if (core.eval.KRKEvaluationFunction.verbose)
				System.out.println(quad[0]+" "+quad[1]+" "+quad[2]+" "+quad[3]);
			
			if (hor) 
			{
				if (core.eval.KRKEvaluationFunction.verbose) System.out.println("H!");
				int x = (ry>7-ry?ry:7-ry);
				bonus+=(x*2);
			}
			if (ver) 
			{
				if (core.eval.KRKEvaluationFunction.verbose) System.out.println("V!");
				int x = (rx>7-rx?rx:7-rx);
				bonus+=(x*2);
			}
		}
		
		return (areas*squares)/*+bonus*/;
		
	}
	
	private int adjacentRook(int rx, int ry, int kx, int ky)
	{
		int dx = kx-rx;
		int dy = ky-ry;
		if (dx<0) dx = -dx; if (dy<0) dy= -dy;
		int dist = (dx>dy? dx : dy);
		if (dist==1) return 1; else return 0;
	}
	
	private int kingMatrix(Metaposition m)
	{
		int total = 0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (m.canContainEnemyKing((byte)k,(byte)j)) total += matrix[k][j];
			}
			
		return total;
	}
	
	/**
	 * Checks for the possibility of stalemate/checkmate by projecting the opponent's next move.
	 * @param start
	 * @param m
	 * @param dest
	 * @return 1 if checkmate is certain, -1 if potential stalemate, 0 otherwise.
	 */
	public static int stalemateAlert(Metaposition start, Move m, Metaposition dest)
	{
		boolean targets[][] = new boolean[8][8];
		
		boolean definiteCheckmate = true;
		
		for (byte x=0; x<8; x++)
			for (byte y=0; y<8; y++)
			{
				if (dest.canContainEnemyKing(x,y))
				{
					boolean escapeRoute = false;
					for (byte x2=(byte)(x-1); x2<=x+1; x2++)
						for (byte y2=(byte)(y-1); y2<=y+1; y2++)
						{
							if (x2<0 || x2>7 || y2<0 || y2>7) continue;
							if (x2==x && y2==y) continue;
							if (start.owner.globals.protectionMatrix[x2][y2]<1)
							{
								escapeRoute = true;
								//if (KING_IN_CHECK) return 0;
								//return 0;
							}
						}
					//if the king has no escape routes, it all depends on whether its current location
					//is under attack. If it is, no problem; may even be a checkmate. But if it isn't, we have
					//a potential stalemate.
					if (!escapeRoute)
					{
						if (start.owner.globals.protectionMatrix[x][y]<1) return -1;
					} else definiteCheckmate = false;
				}
			}
		
		if (definiteCheckmate) return 1; else return 0;
	}


}
