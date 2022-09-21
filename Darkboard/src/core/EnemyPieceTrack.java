/*
 * Created on 7-apr-06
 *
 */
package core;

	/**
	 * This class contains information about "tracks" left by enemy pieces, which are created
	 * in the event of illegal moves. Tracks provide bonuses to the evaluation function for
	 * moves that step on "tracked" square, though the changes fade out as the chessboard's
	 * age increases.
	 * @author Nikola Novarlic
	 *
	 */
	public class EnemyPieceTrack
	{
		public byte x1, y1, x2, y2;
		public byte dx, dy; //convenience fields, -1,0,1
		
		public EnemyPieceTrack(Move illegal)
		{
			dx = (byte)(illegal.toX > illegal.fromX? 1 : illegal.toX==illegal.fromX? 0 : -1);
			dy = (byte)(illegal.toY > illegal.fromY? 1 : illegal.toY==illegal.fromY? 0 : -1);
			
			//exclude the first and last squares
			
			x1 = (byte)(illegal.fromX+dx); x2 = (byte)(illegal.toX-dx);
			y1 = (byte)(illegal.fromY+dy); y2 = (byte)(illegal.toY-dy);
		}
		
		public int getSquareNumber()
		{
			int x = x2-x1; if (x<0) x*=-1;
			int y = y2-y1; if (y<0) y*=-1;
			int result = (x>y? x : y)+1;
			return result;
		}
		public int getSquareX(int index)
		{
			return (x1+dx*index);
		}
		public int getSquareY(int index)
		{
			return (y1+dy*index);
		}
		public boolean containsSquare(int x, int y)
		{
			for (int index=0; index<getSquareNumber(); index++)
			{
				if (x==getSquareX(index) && y==getSquareY(index)) return true;
			}
			return false;
		}
		public boolean isSubsetOf(EnemyPieceTrack ept)
		{
			for (int k=0; k<getSquareNumber(); k++)
			{
				if (!ept.containsSquare(getSquareX(k),getSquareY(k))) return false;
			}
			return true;
		}
		public boolean isSupersetOf(EnemyPieceTrack ept)
		{
			return ept.isSubsetOf(this);
		}
	}
