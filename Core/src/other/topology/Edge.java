package other.topology;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import game.types.board.RelationType;
import game.types.board.SiteType;
import main.math.Point3D;
import main.math.Vector;

/**
 * Edge of the graph (equivalent to the edge of the board).
 * 
 * @author Eric Piette and cambolbro
 */
public final class Edge extends TopologyElement implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Vertex end points. */
	private final Vertex[] vertices = new Vertex[2];

	/** The cells of the edge. */
	private final List<Cell> cells = new ArrayList<Cell>();

	/** Adjacent edges. */
	private final List<Edge> adjacent = new ArrayList<Edge>();

	/** Whether to use end point pivots to define curved edge. */
//	private boolean curved = false;
	
	private Vector tangentA = null;
	private Vector tangentB = null;

	//-------------------------------------------------------------------------

	/** Edges crossed. */
	private BitSet doesCross = new BitSet();

	//---------------------------------------------------------------------------

	/**
	 * Definition of an edge.
	 * 
	 * @param v0 The first vertex of the edge.
	 * @param v1 The second vertex of the edge.
	 */
	public Edge(final Vertex v0, final Vertex v1)
	{
		index = -1;
		vertices[0] = v0;
		vertices[1] = v1;

		// We compute the centroid.
		final double x = (vertices[0].centroid3D().x() + vertices[1].centroid3D().x()) / 2.0;
		final double y = (vertices[0].centroid3D().y() + vertices[1].centroid3D().y()) / 2.0;
		final double z = (vertices[0].centroid3D().z() + vertices[1].centroid3D().z()) / 2.0;
		centroid = new Point3D(x, y, z);
	}

	/**
	 * Edge with a specific index.
	 * 
	 * @param index The index of the edge.
	 * @param v0    The first vertex of the edge.
	 * @param v1    The second vertex of the edge.
	 */
	public Edge(final int index, final Vertex v0, final Vertex v1)
	{
		this.index = index;
		vertices[0] = v0;
		vertices[1] = v1;

		// We compute the centroid.
		final double x = (vertices[0].centroid3D().x() + vertices[1].centroid3D().x()) / 2.0;
		final double y = (vertices[0].centroid3D().y() + vertices[1].centroid3D().y()) / 2.0;
		final double z = (vertices[0].centroid3D().z() + vertices[1].centroid3D().z()) / 2.0;
		centroid = new Point3D(x, y, z);
	}

	//-------------------------------------------------------------------------

	/**
	 * To set the bitset doesCross.
	 * 
	 * @param doesCross The new bitset.
	 */
	public void setDoesCross(final BitSet doesCross)
	{
		this.doesCross = doesCross;
	}

	/**
	 * To set an edge crossing that one.
	 * 
	 * @param indexEdge Index of the edge crossing.
	 */
	public void setDoesCross(final int indexEdge)
	{
		doesCross.set(indexEdge);
	}

	/**
	 * @param edge The index of the edge.
	 * @return True if the edge crosses this edge.
	 */
	public boolean doesCross(final int edge)
	{
		if (edge < 0 || edge >= doesCross.size())
			return false;

		return doesCross.get(edge);
	}

	/**
	 * @param which
	 * @return A vertex of the edge.
	 */
	public Vertex vertex(final int which)
	{
		return vertices[which];
	}

	/**
	 * @return The vertex A.
	 */
	public Vertex vA()
	{
		return vertices[0];
	}

	/**
	 * @return The vertex B.
	 */
	public Vertex vB()
	{
		return vertices[1];
	}

	/**
	 * @param v
	 * 
	 * @return The vertex not given.
	 */
	public Vertex otherVertex(final Vertex v)
	{
		if (vertices[0] == v)
			return vertices[1];
		else if (vertices[1] == v)
			return vertices[0];
		else
			return null;
	}
	
//	public boolean curved()
//	{
//		return curved;
//	}
//	
//	public void setCurved(final boolean value)
//	{
//		curved = value;
//	}

	/**
	 * @return The vector of the tangent from the vertex A.
	 */
	public Vector tangentA()
	{
		return tangentA;
	}
	
	/**
	 * Set the vector of the tangent A.
	 * 
	 * @param vec
	 */
	public void setTangentA(final Vector vec)
	{
		tangentA = vec;
	}
	
	/**
	 * @return The vector of the tangent from the vertex B.
	 */
	public Vector tangentB()
	{
		return tangentB;
	}
		
	/**
	 * Set the vector of the tangent B.
	 * 
	 * @param vec
	 */
	public void setTangentB(final Vector vec)
	{
		tangentB = vec;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether this edge is curved.
	 */
	public boolean isCurved()
	{
		return tangentA != null && tangentB != null;
	}
	
	
	//-------------------------------------------------------------------------

	/**
	 * @return Whether there is an arrow from B => A (directed).
	 */
	public static boolean toA()
	{
		// **
		// ** TODO: Assume A => B always for the moment, but eventually this
		// **       can be overrideable in the description. 
		// **
		return false;
	}
	
	/**
	 * @return Whether there is an arrow from A => B (directed).
	 */
	public static boolean toB()
	{
		// **
		// ** TODO: Assume A => B always for the moment, but eventually this
		// **       can be overrideable in the description. 
		// **
		return true;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The type of the edge.
	 */
	public RelationType type()
	{
		if (vA().orthogonal().contains(vB()))
			return RelationType.Orthogonal;
		else if (vA().diagonal().contains(vB()))
			return RelationType.Diagonal;
		else
			return RelationType.OffDiagonal;
	}

	/**
	 * @return cells of the edge.
	 */
	@Override
	public List<Cell> cells()
	{
		return cells;
	}
	
	/**
	 * @return vertices of the edge.
	 */
	@Override
	public List<Vertex> vertices()
	{
		return Arrays.asList(vertices);
	}
	
	@Override
	public List<Edge> edges() 
	{
		final ArrayList<Edge> edges = new ArrayList<>();
		edges.add(this);
		return edges;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param indexV
	 * @return True if v is used by the edge.
	 */
	public boolean containsVertex(final int indexV)
	{
		return indexV == vA().index() || vB().index() == indexV;
	}
	
	/**
	 * @param va The vertex A.
	 * @param vb The vertex B.
	 * @return Whether this edge matches the specified vertices in the graph.
	 */
	public boolean matches(final Vertex va, final Vertex vb)
	{
		return va.index() == vertices[0].index() && vb.index() == vertices[1].index()
				|| va.index() == vertices[1].index() && vb.index() == vertices[0].index();
	}

	/**
	 * @param pa The point2D A.
	 * @param pb The point2D B.
	 * @return Whether this edge matches with the specified point.
	 */
	public boolean matches(final Point2D pa, final Point2D pb)
	{
		return pa == vertices[0].centroid() && pb == vertices[1].centroid()
				|| pa == vertices[1].centroid() && pb == vertices[0].centroid();
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "Edge(" + vertices[0].index() + "-" + vertices[1].index() + ")";
		return str;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Optimises memory usage by this edge.
	 */
	public void optimiseMemory()
	{
		((ArrayList<Cell>) cells).trimToSize();
		((ArrayList<Edge>) adjacent).trimToSize();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public SiteType elementType() 
	{
		return SiteType.Edge;
	}

	@Override
	public String label() 
	{
		return String.valueOf(index);
	}

	/**
	 * @return Orthogonally connected vertices.
	 */
	@Override
	public List<Edge> orthogonal()
	{
		return adjacent;
	}

	/**
	 * @return Diagonally connected vertices.
	 */
	@Override
	public List<Edge> diagonal()
	{
		return new ArrayList<Edge>();
	}

	/**
	 * @return Off connected vertices.
	 */
	@Override
	public List<Edge> off()
	{
		return new ArrayList<Edge>();
	}

	/**
	 * @return All the adjacent neighbours.
	 */
	@Override
	public List<Edge> adjacent()
	{
		return adjacent;
	}

	/**
	 * @return All the neighbours.
	 */
	@Override
	public List<Edge> neighbours()
	{
		return adjacent;
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
		return edges();
	}

	@Override
	public List<Cell> regionCells() 
	{
		return new ArrayList<>();
	}

}
