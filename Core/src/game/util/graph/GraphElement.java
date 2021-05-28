package game.util.graph;

import java.awt.geom.Point2D;
import java.util.List;

import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import main.Constants;
import main.math.Point3D;

//-----------------------------------------------------------------------------

/**
 * Shared storage for graph elements.
 *
 * @author combolbro
 */
public abstract class GraphElement
{
	/** Id is not final; can change when merging or modifying graphs. */
	protected int id = Constants.UNDEFINED;
	
	protected Point3D pt;
	
	/** Tiling and shape used when creating this element. */
	protected BasisType basis = null;
	protected ShapeType shape = null;

	protected Properties properties = new Properties();
	
	protected final Situation situation = new Situation();
	
	protected boolean flag = false;
	
	//-------------------------------------------------------------------------

	/**
	 * @return The index of the graph element.
	 */
	public int id()
	{
		return id;
	}

	/**
	 * Set the index of the graph element.
	 * 
	 * @param newId The new index.
	 */
	public void setId(final int newId)
	{
		id = newId;
	}	

	/**
	 * Decrement the index.
	 */
	public void decrementId()
	{
		id--;
	}	
	
	/**
	 * @return 3D position of the centroid.
	 */
	public Point3D pt()
	{
		return pt;
	}

	/**
	 * @return 2D position of the centroid.
	 */
	public Point2D pt2D()
	{
		return new Point2D.Double(pt.x(), pt.y());
	}

	/**
	 * Overrides the elements reference point.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setPt(final double x, final double y, final double z)
	{
		pt = new Point3D(x, y, z);
	}
	
	/**
	 * @return The properties of the element.
	 */
	public Properties properties()
	{
		return properties;
	}

	/**
	 * @return Positional situation, for coordinate labelling.
	 */
	public Situation situation()
	{
		return situation;
	}
	
	/**
	 * @return True if the element is flagged.
	 */
	public boolean flag()
	{
		return flag;
	}
	
	/**
	 * Set the flag of the element.
	 * 
	 * @param value New flag value.
	 */
	public void setFlag(final boolean value)
	{
		flag = value;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return This element's pivot vertex, else null if none.
	 */
	public abstract Vertex pivot();

	//-------------------------------------------------------------------------

	/**
	 * @return This element's basis type.
	 */
	public BasisType basis()
	{
		return basis;
	}

	/**
	 * @param type Basis type to set this element.
	 */
	public void setBasis(final BasisType type)
	{
		basis = type;
	}

	/**
	 * @return This element's shape type.
	 */
	public ShapeType shape()
	{
		return shape;
	}

	/**
	 * @param type Shape type to set this element.
	 */
	public void setShape(final ShapeType type)
	{
		shape = type;
	}
		
	/**
	 * @return The type of the graph element.
	 */
	public abstract SiteType siteType();

	//-------------------------------------------------------------------------
	
	/**
	 * Sets this element's basis and shape type.
	 * 
	 * @param basisIn The new basis.
	 * @param shapeIn The new shape.
	 */
	public void setTilingAndShape(final BasisType basisIn, final ShapeType shapeIn)
	{
		basis = basisIn;
		shape = shapeIn;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other The graph element to check.
	 * @return True if this is this graph element.
	 */
	public boolean matches(final GraphElement other)
	{
		return siteType() == other.siteType() && id == other.id;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Element label for debugging purposes.
	 */
	public String label() 
	{
		return siteType().toString().substring(0, 1) + id;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return List of connected neighbour elements.
	 */
	public abstract List<GraphElement> nbors();

	/**
	 * @param steps List of steps from this element, by relation type.
	 */
	public abstract void stepsTo(final Steps steps);
	
	//-------------------------------------------------------------------------

}
