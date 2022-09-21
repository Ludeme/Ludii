/*
 * Created on 5-ott-05
 *
 */
package core;

import java.util.Vector;

/**
 * @author Nikola Novarlic
 * Represents an enemy piece and the subset of squares where it may be.
 */
public class PiecePosition {
	
	
	public class Square
	{
		public byte x;
		public byte y;
	}
	
	boolean piece[] = new boolean[7]; //uses Chessboard piece codes
	int age = 0;
	Vector possibleSquares;
	
	private static Square squares[][] = null; //we keep using the same structures.
	
	public PiecePosition()
	{
		possibleSquares = new Vector();
		clearPieces();
		
		if (squares==null)
		{
			squares = new Square[8][8];
			for (byte k=0; k<8; k++)
				for (byte j=0; j<8; j++)
				{
					squares[k][j] = new Square();
					squares[k][j].x = k;
					squares[k][j].y = j;
				}
		}
	}
	
	public void setPiece(int p,boolean value)
	{
		if (p<0 || p>=piece.length) return;
		piece[p] = value;
	}
	
	public int getSquareNumber()
	{
		return possibleSquares.size();
	}
	
	public Square getSquare(int k)
	{
		if (k<0 || k>=possibleSquares.size()) return null;
		return (Square)possibleSquares.get(k);
	}
	
	public boolean isThereSquare(byte x, byte y)
	{
		for (int k=0; k<possibleSquares.size(); k++)
		{
			Square s = getSquare(k);
			if (s.x==x && s.y==y) return true;
		}
		return false;
	}
	
	public void addSquare(byte x, byte y)
	{
		if (x<0 || y<0 || x>=8 || y>=8) return;
		if (isThereSquare(x,y)) return;
		possibleSquares.add(squares[x][y]);
	}
	
	public void clear()
	{
		possibleSquares.clear();
	}
	
	public void clearPieces()
	{
		for (int k=0; k<7; k++) piece[k] = false;
	}
	
	public void doAge()
	{
		age++;
	}
	
	public void setAge(int k)
	{
		age = k;
	}
	
	public int getAge() { return age; }
	
	

}
