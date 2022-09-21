/*
 * Created on 4-apr-05
 *
 */
package core;

import java.io.Serializable;

/**
 * @author Nikola Novarlic
 * Incapsulates the data required to describe a Kriegspiel move
 */
public class Move implements Cloneable, Serializable {
	
	public byte piece;
	public byte fromX;
	public byte fromY;
	public byte toX;
	public byte toY;
	public byte promotionPiece=Chessboard.QUEEN;
	
	public boolean capture = false; //convenient when exporting to PGN...
	public boolean check = false;
	public boolean doublecheck = false;
	public boolean checkmate = false;
	public boolean showFromX = true;
	public boolean showFromY = true;
	public boolean showSeparator = true;
	
	public static String ranks[] = {"1","2","3","4","5","6","7","8"};
	public static String files[] = {"a","b","c","d","e","f","g","h"};
	
	
	
	public Object clone()
	{
		try
		{
			return super.clone();
		} catch (Exception e) { e.printStackTrace(); return null; }
	}
	
	
	public String pieceLetter(int p)
	{
		switch (p)
		{
			case Chessboard.PAWN: return "";
			case Chessboard.BISHOP: return "B";
			case Chessboard.ROOK: return "R";
			case Chessboard.KNIGHT: return "N";
			case Chessboard.QUEEN: return "Q";
			case Chessboard.KING: return "K";
		}
		return "";
	}
	
	public String toString()
	{
		String result;
		
		if (piece==Chessboard.KING && toX==fromX+2) return "O-O";
		if (piece==Chessboard.KING && toX==fromX-2) return "O-O-O";
		
		result = pieceLetter(piece) + 
			(showFromX? files[fromX] : "") + 
			(showFromY? ranks[fromY] : "") + 
			(showSeparator || capture? (capture? "x" : "-") : "" )+ files[toX] + ranks[toY];
		if (piece==Chessboard.PAWN && (toY==0 || toY==7)) result+=("="+pieceLetter(promotionPiece));
		if (check && !checkmate) result+="+";
		//if (doublecheck && !checkmate) result+="+";
		if (checkmate) result+="#";
		

		
		return result;
	}

	public String sourceSquare()
	{
		return (files[fromX] + ranks[fromY]);
	}
	public String destinationSquare()
	{
		return (files[toX] + ranks[toY]);
	}
	
	public static String squareString(int x, int y)
	{
		return (files[x] + ranks[y]);
	}
	
	public static boolean pieceCompatible(int piece, int checkCode)
	{
		switch (checkCode)
		{
			case Chessboard.CHECK_KNIGHT:
				return (piece==Chessboard.KNIGHT);
				
			case Chessboard.CHECK_FILE:
			case Chessboard.CHECK_RANK:
				return (piece==Chessboard.QUEEN || piece==Chessboard.ROOK);
				
			case Chessboard.CHECK_LONG_DIAGONAL:
			case Chessboard.CHECK_SHORT_DIAGONAL:
				return (piece==Chessboard.QUEEN || piece==Chessboard.BISHOP
				|| piece==Chessboard.PAWN);
		}
		return false;
	}
	
	public boolean equals(Object o)
	{
		if (o.getClass()!=this.getClass()) return false;
		Move m = (Move)o;
		return (m.fromX==fromX && m.fromY==fromY && m.toX==toX && m.toY==toY &&
			m.piece==piece);
	}
	
	public int hashCode()
	{
		return ((toX<<3)+toY) | (((fromX<<3)+fromY)<<6) | (piece<<12);
	}

}
