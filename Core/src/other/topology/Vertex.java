package other.topology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import game.types.board.SiteType;
import main.math.Point3D;

/**
 * Vertex of the graph (equivalent to any intersection of a board).
 * 
 * @author Eric.Piette
 */
public final class Vertex extends TopologyElement implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-----------------------------------------------------------------------------

	/** List of cells. */
	private final List<Cell> cells = new ArrayList<Cell>();

	/** List of edge. */
	private final List<Edge> edges = new ArrayList<Edge>();

	private Vertex pivot = null; 

	//----------------------------------------------------------------------------

	/** Orthogonally connected vertices. */
	private final List<Vertex> orthogonal = new ArrayList<Vertex>();

	/** Diagonally connected vertices. */
	private final List<Vertex> diagonal = new ArrayList<Vertex>();

	/** Off connected vertices. */
	private final List<Vertex> off = new ArrayList<Vertex>();

	/** Adjacent neighbours. */
	private final List<Vertex> adjacent = new ArrayList<Vertex>();
	
	/** Neighbours. */
	private final List<Vertex> neighbours = new ArrayList<Vertex>();

	//-------------------------------------------------------------------------

	/**
	 * Definition of a vertex.
	 * 
	 * @param index
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vertex(final int index, final double x, final double y, final double z)
	{
		this.index = index;
		centroid = new Point3D(x, y, z);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return cells using that vertex.
	 */
	@Override
	public List<Cell> cells()
	{
		return cells;
	}

	/**
	 * @return Edges using that vertex.
	 */
	@Override
	public List<Edge> edges()
	{
		return edges;
	}
	
	@Override
	public List<Vertex> vertices() 
	{
		final ArrayList<Vertex> vertices = new ArrayList<>();
		vertices.add(this);
		return vertices;
	}
	
	/**
	 * @return The pivot vertex.
	 */
	public Vertex pivot()
	{
		return pivot;
	}

	/**
	 * Set the pivot vertex.
	 * 
	 * @param vertex The pivot vertex.
	 */
	public void setPivot(final Vertex vertex)
	{
		pivot = vertex;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Vertex: " + index;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Optimises memory usage by this vertex.
	 */
	public void optimiseMemory()
	{
		((ArrayList<Cell>) cells).trimToSize();
		((ArrayList<Edge>) edges).trimToSize();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public SiteType elementType() 
	{
		return SiteType.Vertex;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Orthogonally connected vertices.
	 */
	@Override
	public List<Vertex> orthogonal()
	{
		return orthogonal;
	}

	/**
	 * @return Diagonally connected vertices.
	 */
	@Override
	public List<Vertex> diagonal()
	{
		return diagonal;
	}

	/**
	 * @return Off connected vertices.
	 */
	@Override
	public List<Vertex> off()
	{
		return off;
	}

	/**
	 * @return All the adjacent neighbours.
	 */
	@Override
	public List<Vertex> adjacent()
	{
		return adjacent;
	}

	/**
	 * @return All the neighbours.
	 */
	@Override
	public List<Vertex> neighbours()
	{
		return neighbours;
	}

	/**
	 * 
	 * @param vid
	 * @return True if the vertex vid is a neighbor of the current vertex.
	 */
	public boolean neighbour(final int vid)
	{
		for (final Vertex v : diagonal)
		{
			if (v.index() == vid)
				return true;
		}
		for (final Vertex v : orthogonal)
		{
			if (v.index() == vid)
				return true;
		}
		return false;
	}

	/**
	 * @return The number of orthogonally connected vertices.
	 */
	public int orthogonalOutDegree()
	{
		return orthogonal.size();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public List<Vertex> regionVertices() 
	{
		return vertices();
	}

	@Override
	public List<Edge> regionEdges() 
	{
		return new ArrayList<>();
	}

	@Override
	public List<Cell> regionCells() 
	{
		return new ArrayList<>();
	}

}
