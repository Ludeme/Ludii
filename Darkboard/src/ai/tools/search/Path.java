package ai.tools.search;

import java.util.Vector;

import core.Move;

public class Path {

	public Vector<Move> moves = new Vector();
	
	public Path()
	{
		
	}
	
	public Path(Move m)
	{
		if (m!=null) moves.add(m);
	}
	
	public Path(Path p, Move m)
	{
		for (int k=0; k<p.moves.size(); k++) moves.add(p.moves.get(k));
		if (m!=null) moves.add(m);
	}
	
	public Path(Path p)
	{
		for (int k=0; k<p.moves.size(); k++) moves.add(p.moves.get(k));
	}
	
	public Path(Path p1, Path p2)
	{
		for (int k=0; k<p1.moves.size(); k++) moves.add(p1.moves.get(k));
		for (int k=0; k<p2.moves.size(); k++) moves.add(p2.moves.get(k));
	}
	
	public void copy(Path p)
	{
		moves.clear();
		for (int k=0; k<p.moves.size(); k++)
			moves.add(p.moves.get(k));
	}
	
	public String toString()
	{
		String s = "";
		for (int k=0; k<moves.size(); k++) s+=moves.get(k)+"; ";
		return s;
	}
	
}
