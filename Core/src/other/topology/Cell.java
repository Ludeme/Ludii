package other.topology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import game.types.board.SiteType;
import main.math.Point3D;

/**
 * Cell of the board (equivalent to a face of the graph).
 * 
 * @author Eric.Piette and Matthew.Stephenson
 */
public final class Cell extends TopologyElement implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-----------------------------------------------------------------------------

	/** List of vertices of this cell. */
	private List<Vertex> vertices = new ArrayList<Vertex>();

	/** List of Edges using this edge. */
	private final List<Edge> edges = new ArrayList<Edge>();

	//----------------------------------------------------------------------------

	/** Orthogonally connected vertices. */
	private List<Cell> orthogonal = new ArrayList<Cell>();

	/** Diagonally connected vertices. */
	private  List<Cell> diagonal = new ArrayList<Cell>();

	/** Secondary diagonally connected vertices. */
	private  List<Cell> off = new ArrayList<Cell>();

	/** Adjacent cells. */
	private final List<Cell> adjacent = new ArrayList<Cell>();

	/** Neighbours. */
	private final List<Cell> neighbours = new ArrayList<Cell>();

	//-------------------------------------------------------------------------

	/**
	 * Definition of a cell.
	 * 
	 * @param index The index of the cell.
	 * @param x     The x coordinate.
	 * @param y     The y coordinate.
	 * @param z     The z coordinate.
	 */
	public Cell(final int index, final double x, final double y, final double z)
	{
		this.index = index;
		label = index+"";
		centroid = new Point3D(x, y, z);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Orthogonally connected cells.
	 */

	@Override
	public List<Cell> orthogonal()
	{
		return orthogonal;
	}

	/**
	 * To set the orthogonally connected cells.
	 * 
	 * @param orthogonal The orthogonal cells.
	 */
	public void setOrthogonal(final List<Cell> orthogonal)
	{
		this.orthogonal = orthogonal;
	}
	
	/**
	 * @return Diagonally connected cells.
	 */
	@Override
	public List<Cell> diagonal()
	{
		return diagonal;
	}

	/**
	 * To set the diagonally connected cells.
	 * 
	 * @param diagonal The diagonal cells.
	 */
	public void setDiagonal(final List<Cell> diagonal)
	{
		this.diagonal = diagonal;
	}
	
	/**
	 * @return neighbours cells.
	 */
	@Override
	public List<Cell> neighbours()
	{
		return neighbours;
	}

	/**
	 * @return Off connected cells.
	 */
	@Override
	public List<Cell> off()
	{
		return off;
	}
	
	/**
	 * To set the off connected cells.
	 * 
	 * @param off The off cells.
	 */
	public void setOff(final List<Cell> off)
	{
		this.off = off;
	}
	
	/**
	 * @return Adjacent cells.
	 */
	@Override
	public List<Cell> adjacent()
	{
		return adjacent;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param x
	 * @param y
	 * @return True if the coord are the same.
	 */
	public boolean matchCoord(final int x, final int y)
	{
		if (coord.row() == x && coord.column() == y)
			return true;
		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param x
	 * @param y
	 * @return Whether the specified position matches this cell.
	 */
	public boolean matches(final double x, final double y)
	{
		final double dx = x - centroid().getX();
		final double dy = y - centroid().getY();

		return Math.abs(dx) < 0.0001 && Math.abs(dy) < 0.0001;
	}

	/**
	 * @param other
	 * @return Whether the specified cell's position matches this vertex's.
	 */
	public boolean matches(final Cell other)
	{
		final double dx = other.centroid().getX() - centroid().getX();
		final double dy = other.centroid().getY() - centroid().getY();

		return Math.abs(dx) < 0.0001 && Math.abs(dy) < 0.0001;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "Cell: " + index;
		return str;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean equals(final Object o)
	{
		final Cell cell = (Cell) o;
		return cell != null && index == cell.index;
	}

	@Override
	public int hashCode()
	{
		return index;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Vertices of the cell.
	 */
	@Override
	public List<Vertex> vertices()
	{
		return vertices;
	}
	
	/**
	 * To set the vertices of the cell.
	 * Note: Use only in BoardlessPlacement.
	 * 
	 * @param v The new list of vertices.
	 */
	public void setVertices(final List<Vertex> v)
	{
		vertices = v;
	}

	/**
	 * @return Edges of the cell.
	 */
	@Override
	public List<Edge> edges()
	{
		return edges;
	}
	

	@Override
	public List<Cell> cells() 
	{
		final ArrayList<Cell> cells = new ArrayList<>();
		cells.add(this);
		return cells;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Optimises memory usage by this cell.
	 */
	public void optimiseMemory()
	{
		((ArrayList<Vertex>) vertices).trimToSize();
		((ArrayList<Edge>) edges).trimToSize();
		((ArrayList<Cell>) orthogonal).trimToSize();
		((ArrayList<Cell>) diagonal).trimToSize();
		((ArrayList<Cell>) off).trimToSize();
		((ArrayList<Cell>) adjacent).trimToSize();
		((ArrayList<Cell>) neighbours).trimToSize();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public SiteType elementType() 
	{
		return SiteType.Cell;
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
		return cells();
	}
	
}
