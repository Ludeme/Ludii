package ai.chunk;

import java.util.Vector;

import core.Chessboard;
import core.Metaposition;
import core.Move;

/**
 * A chunk describes a set of geometrically related pieces
 * on the chessboard. Human players tend to mentally divide
 * the chessboard into chunks.
 * @author Nikola Novarlic
 *
 */
public class Chunk {
	
	public class ChunkExit
	{
		int x1,y1,x2,y2; //x2 and y2 are offsets
		int occurrences;
	}
	
	private int chunk[][];
	private int firstMove;
	private int lastMove;
	private int occurrences;
	private int occurrenceBreakdown[] = new int[16];
	private int totalGames;
	private int victories;
	private boolean white;
	private Vector<ChunkExit> exits = new Vector();
	
	public Chunk(int x, int y)
	{
		chunk = new int[x][y];
		clear();
		firstMove = lastMove = 1;
		occurrences = totalGames = 0;
		victories = 0;
		white = true;
	}
	
	public Chunk(int x[][], boolean w, int move, int materialLeft)
	{
		chunk = new int[x.length][x[0].length];

			for (int k=0; k<x.length; k++)
				for (int h=0; h<x[0].length; h++)
				{
					int k1 = k;
					int h1 = (!w? x[0].length-h-1: h);
					chunk[k1][h1] = x[k][h];
				}
		//chunk = x.clone();
		//clear();
		white = true;
		firstMove = lastMove = move;
		occurrenceBreakdown[materialLeft]++;
		occurrences = totalGames = 0;
		victories = 0;
	}
	
	/**
	 * Makes a chunk from a list of squares and pieces.
	 * @param v
	 * @param w
	 */
	public Chunk(Vector<int[]> v, boolean w)
	{
		//first of all, find the boundaries of the chunk...
		int left,top,right,bottom;
		white = w;
		if (v.size()<1) return;
		left=right=v.get(0)[0];
		top=bottom=v.get(0)[1];
		for (int k=1; k<v.size(); k++)
		{
			int a[] = v.get(k);
			if (a[0]<left) left=a[0]; if (a[0]>right) right = a[0];
			if (a[1]<top) top=a[1]; if (a[1]>bottom) bottom = a[1];
		}
		//now create the actual array
		chunk = new int[right-left+1][bottom-top+1];
		clear();
		for (int k=0; k<v.size(); k++)
		{
			int a[] = v.get(k);
			int x = a[0]-left; int y = a[1]-top;
			int piece = Chessboard.EMPTY;
			switch (a[2])
			{
			case 1: case -1: piece = Chessboard.PAWN; break;
			case 2: case -2: piece = Chessboard.KNIGHT; break;
			case 3: case -3: piece = Chessboard.BISHOP; break;
			case 4: case -4: piece = Chessboard.ROOK; break;
			case 5: case -5: piece = Chessboard.QUEEN; break;
			case 6: case -6: piece = Chessboard.KING; break;
			}
			chunk[x][y]=piece;
		}
		
		if (!w)
		{
			int r[][] = reflect(chunk,false,true);
			for (int k=0; k<chunk.length; k++)
				for (int h=0; h<chunk[0].length; h++)
				{
					chunk[k][h] = r[k][h];
				}
			white=true;
		}
	}
	
	public Chunk(Metaposition m, Vector<int[]> v, boolean w)
	{
		//first of all, find the boundaries of the chunk...
		int left,top,right,bottom;
		white = w;
		if (v.size()<1) return;
		left=right=v.get(0)[0];
		top=bottom=v.get(0)[1];
		for (int k=1; k<v.size(); k++)
		{
			int a[] = v.get(k);
			if (a[0]<left) left=a[0]; if (a[0]>right) right = a[0];
			if (a[1]<top) top=a[1]; if (a[1]>bottom) bottom = a[1];
		}
		//now create the actual array
		chunk = new int[right-left+1][bottom-top+1];
		clear();
		for (int k=0; k<v.size(); k++)
		{
			int a[] = v.get(k);
			int x = a[0]-left; int y = a[1]-top;
			chunk[x][y]=a[2];
		}
		
		if (!w)
		{
			int r[][] = reflect(chunk,false,true);
			for (int k=0; k<chunk.length; k++)
				for (int h=0; h<chunk[0].length; h++)
				{
					chunk[k][h] = r[k][h];
				}
			white=true;
		}
	}
	
	/**
	 * Clears the contents of a chunk
	 *
	 */
	public void clear()
	{
		for (int k=0; k<chunk.length; k++)
			for (int h=0; h<chunk[0].length; h++)
			{
				chunk[k][h]=Chessboard.EMPTY;
			}
	}
	
	public int pieceSize()
	{
		int total = 0;
		for (int k=0; k<chunk.length; k++)
			for (int h=0; h<chunk[0].length; h++)
			{
				if (chunk[k][h]!=Chessboard.EMPTY) total++;
			}
		return total;
	}
	
	/**
	 * Merges two istances of the same chunk, summing their stats.
	 * @param c
	 */
	public void integrateChunk(Chunk c)
	{
		if (c.firstMove<firstMove) firstMove = c.firstMove;
		if (c.lastMove>lastMove) lastMove = c.lastMove;
		
		occurrences += c.occurrences;
		totalGames += c.totalGames;
		victories += c.victories;
		for (int k=0; k<occurrenceBreakdown.length; k++) occurrenceBreakdown[k] += c.occurrenceBreakdown[k];
		
		for (int k=0; k<c.getExitNumber(); k++)
		{
			ChunkExit ce = c.exits.get(k);
			ChunkExit ours = exitExists(ce.x1,ce.y1,ce.x2,ce.y2);
			if (ours!=null)
			{
				ours.occurrences+=ce.occurrences;
			}
			else addExitOccurrence(ce.x1, ce.y1, ce.x2, ce.y2, c.white);
		}
	}
	
	/**
	 * Comparison includes horizontal and vertical mirror images of a chunk.
	 */
	public boolean equals(Object o)
	{
		if (!o.getClass().equals(this.getClass())) return false;
		
		Chunk c = (Chunk)o;
		
		if (chunk.length!=c.chunk.length) return false;
		if (chunk.length>0 && chunk[0].length!=c.chunk[0].length) return false;
		
		int comp[][] = (white==c.white? reflect(c.chunk,false, false) : reflect(c.chunk,false, true));
		
		if (compare(comp)) return true;
		//if (compare(reflect(comp,true,false))) return true;
		
		return false;
	}
	
	/**
	 * Reflects a chunk along zero or more axes.
	 * @param x
	 * @param horiz
	 * @param vertic
	 * @return
	 */
	private static int[][] reflect(int x[][], boolean horiz, boolean vertic)
	{
		int copy[][];
		copy = new int[x.length][x[0].length];
		
		//if (!horiz && !vertic) return copy;
		if (x.length<1 || x[0].length<1) return copy;
		
		for (int k=0; k<x.length; k++)
			for (int h=0; h<x[0].length; h++)
			{
				int k1 = (horiz? x.length-k-1: k);
				int h1 = (vertic? x[0].length-h-1: h);
				copy[k1][h1] = x[k][h];
			}
		
		return copy;
	}
	
	private boolean compare(int comp[][])
	{
		if (chunk.length!=comp.length) return false;
		if (chunk.length>0 && chunk[0].length!=comp[0].length) return false;
		
		for (int k=0; k<comp.length; k++)
			for (int h=0; h<comp[0].length; h++)
			{
				if (chunk[k][h]!=comp[k][h]) return false;
			}
		
		return true;
	}
	
	public String toString()
	{
		String s = "";
		for (int h=chunk[0].length-1; h>=0; h--)
		
		{
			for (int k=0; k<chunk.length; k++)
			{
				switch (chunk[k][h])
				{
				case Chessboard.EMPTY: s+="*"; break;
				case Chessboard.PAWN: s+="P"; break;
				case Chessboard.KNIGHT: s+="N"; break;
				case Chessboard.BISHOP: s+="B"; break;
				case Chessboard.ROOK: s+="R"; break;
				case Chessboard.QUEEN: s+="Q"; break;
				case Chessboard.KING: s+="K"; break;
				default: s+="?"; break;
				}
			}
			s+="\n";
		}
		return s;
	}
	
	public static void main(String args[])
	{
		Vector<Chunk> v = new Vector();
		
		int board[][] = { 
				{0,0,0,0,0,1,0,0},
				{0,0,1,0,0,0,4,0},
				{0,0,0,0,0,0,1,0},
				{0,0,1,0,1,0,0,0},
				{0,0,0,1,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{6,0,2,1,0,0,0,0},
				{5,0,0,3,0,0,0,0},
		};
		
		v = ai.chunk.ChunkMaker.chunkify(board, true);
		
		for (int k=0; k<v.size(); k++) System.out.println(v.get(k));
		
		//System.out.println(new Chunk(5,4));
		//for (int k=0; k<100; k++) v.add(new Chunk(1+(k%5),1+((k+3)%5)));
		//for (int k=1; k<100; k++) System.out.println(v.get(0).equals(v.get(k)));
	}

	public int getFirstMove() {
		return firstMove;
	}

	public void setFirstMove(int firstMove) {
		this.firstMove = firstMove;
	}

	public int getLastMove() {
		return lastMove;
	}

	public void setLastMove(int lastMove) {
		this.lastMove = lastMove;
	}

	public int getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
	}

	public int getTotalGames() {
		return totalGames;
	}

	public void setTotalGames(int totalGames) {
		this.totalGames = totalGames;
	}

	public int getVictories() {
		return victories;
	}

	public void setVictories(int victories) {
		this.victories = victories;
	}
	
	public void setOccurrenceForMaterial(int material,int value)
	{
		this.occurrenceBreakdown[material] = value;
	}
	
	public int getOccurrenceForMaterial(int material)
	{
		return this.occurrenceBreakdown[material];
	}
	
	public void addExitOccurrence(int x1, int y1, int x2, int y2, boolean w)
	{
		if (!w)
		{
			y1 = chunk[0].length - y1 - 1;
			y2 = -y2;
		}
		
		for (int k=0; k<exits.size(); k++)
		{
			ChunkExit ce = exits.get(k);
			if (ce.x1==x1 && ce.x2==x2 && ce.y1==y1 && ce.y2==y2)
			{
				ce.occurrences++;
				sortExits();
				return;
			}
		}
		
		ChunkExit exit = new ChunkExit();
		exit.x1=x1; exit.x2=x2; exit.y1=y1; exit.y2=y2; exit.occurrences=1;
		
		exits.add(exit);
		sortExits();
	}
	
	private void sortExits()
	{
		while (true)
		{	
			boolean swapped = false;
			for (int k=0; k<exits.size()-1; k++)
			{
				ChunkExit ce1 = exits.get(k);
				ChunkExit ce2 = exits.get(k+1);
				if (ce1.occurrences<ce2.occurrences)
				{
					swapped = true;
					exits.setElementAt(ce1, k+1);
					exits.setElementAt(ce2, k);
				}
			}
			if (!swapped) return;
		}
	}
	
	public ChunkExit exitExists(int x1, int y1, int x2, int y2)
	{
		for (int k=0; k<exits.size(); k++)
		{
			ChunkExit ce = exits.get(k);
			if (ce.x1==x1 && ce.y1==y1 && ce.x2==x2 && ce.y2==y2) return ce;
		}
		return null;
	}
	
	public int getExitNumber()
	{
		return exits.size();
	}
	
	public int[] getExit(int k)
	{
		ChunkExit ce = exits.get(k);
		int result[] = new int[5];
		result[0] = ce.x1;
		result[1] = ce.y1;
		result[2] = ce.x2;
		result[3] = ce.y2;
		result[4] = ce.occurrences;
		
		return result;
	}
	
	public Move getMoveForExit(int exit, int chunkx, int chunky, boolean w)
	{
		Move out = new Move();
		
		ChunkExit ce = exits.get(exit);
		
		out.fromX = (byte)(chunkx + ce.x1);
		out.toX = (byte)(out.fromX + ce.x2);
		
		out.fromY = (byte)(chunky + (w==white? ce.y1 : chunk[0].length-ce.y1-1));
		out.toY = (byte)(out.fromY + (w==white? ce.y2 : -ce.y2));
		
		out.piece = (byte)chunk[ce.x1][ce.y1];
		out.promotionPiece = Chessboard.QUEEN;
		
		
		return out;
	}

}
