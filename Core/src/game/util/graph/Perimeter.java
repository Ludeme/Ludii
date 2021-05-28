package game.util.graph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Vertex perimeter of a connected component in a graph.
 * 
 * @author cambolbro
 */
public class Perimeter
{	
	/** Vertices on this perimeter. */
	private final List<GraphElement> elements = new ArrayList<GraphElement>();
	
	/** Positions of vertices on this perimeter (for using with MathRoutines). */
	private final List<Point2D> positions = new ArrayList<Point2D>();
	
	/** List of vertices inside this perimeter, not including the perimeter. */
	private final List<GraphElement> inside = new ArrayList<GraphElement>();

	/** Indices of vertices on this perimeter. */ 
	final BitSet on = new BitSet();

	/** Indices of vertices inside this perimeter. */ 
	final BitSet in = new BitSet();

	//-------------------------------------------------------------------------

	/**
	 * @return The list of graph elements.
	 */
	public List<GraphElement> elements()
	{
		return Collections.unmodifiableList(elements);
	}
		
	/**
	 * @return The positions of the elements.
	 */
	public List<Point2D> positions()
	{
		return Collections.unmodifiableList(positions);
	}
		
	/**
	 * @return All the graph elements inside the perimeter.
	 */
	public List<GraphElement> inside()
	{
		return Collections.unmodifiableList(inside);
	}

	/**
	 * @return The bitset of the indices of vertices on this perimeter.
	 */
	public BitSet on()
	{
		return on;
	}
		
	/**
	 * @return The bitset of the indices of vertices in this perimeter.
	 */
	public BitSet in()
	{
		return in;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The starting point of the perimeter.
	 */
	public Point2D startPoint()
	{
		if (positions.isEmpty())
			return null;
		
		return positions.get(0);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Clear the perimeter.
	 */
	public void clear()
	{
		elements.clear();
		positions.clear();
		inside.clear();
		on.clear();
		in.clear();
	}
		
	//-------------------------------------------------------------------------
	
	/**
	 * Add a vertex to the perimeter.
	 * 
	 * @param vertex The vertex.
	 */
	public void add(final Vertex vertex)
	{
		elements.add(vertex);
		positions.add(vertex.pt2D());
		on.set(vertex.id(), true);
	}

	/**
	 * Add a vertex inside the perimeter.
	 * 
	 * @param vertex The vertex.
	 */
	public void addInside(final Vertex vertex)
	{
		inside.add(vertex);
		in.set(vertex.id(), true);
	}
	
	//-------------------------------------------------------------------------
			
//	public void generatePoint2D()
//	{
//		perimeter2D.clear();
//		
//		for (final GraphElement ge : perimeter)
//			perimeter2D.add(ge.pt2D());
//	}

	//-------------------------------------------------------------------------
				
}
