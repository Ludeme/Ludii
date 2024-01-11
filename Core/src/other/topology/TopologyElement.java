package other.topology;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import game.types.board.RelationType;
import game.types.board.SiteType;
import game.util.directions.DirectionFacing;
import game.util.graph.Properties;
import main.math.Point3D;
import main.math.RCL;

/**
 * Common graph element that is extended by vertex, edge and cell.
 * 
 * @author Matthew.stephenson and Eric.Piette
 */
public abstract class TopologyElement
{
	/** Index into list of the graph element. */
	protected int index;

	/** Centroid of the graph element. */
	protected Point3D centroid;
	
	/** [row,col,layer] coordinate on the board. */
	protected RCL coord = new RCL();

	/** Cell label. */
	protected String label = "?";

	/** The cost of the graph element. */
	private int cost;

	/** The phase of the element. */
	private int phase;

	/** Supported directions. */
	private final List<DirectionFacing> supportedDirections = new ArrayList<DirectionFacing>();

	/** Supported Orthogonal directions. */
	private final List<DirectionFacing> supportedOrthogonalDirections = new ArrayList<DirectionFacing>();

	/** Supported Diagonal directions. */
	private final List<DirectionFacing> supportedDiagonalDirections = new ArrayList<DirectionFacing>();

	/** Supported Adjacent directions. */
	private final List<DirectionFacing> supportedAdjacentDirections = new ArrayList<DirectionFacing>();

	/** Supported Off directions. */
	private final List<DirectionFacing> supportedOffDirections = new ArrayList<DirectionFacing>();

	// --------------------Pre-Generation----------------------------------------------

	/**
	 * Properties of the graph element (Inner, Centre, Corner, etc..).
	 */
	protected Properties properties = new Properties();

	/** A list of all the sites at a specific distance from that site. */
	private final List<List<TopologyElement>> sitesAtDistance = new ArrayList<List<TopologyElement>>();

	//-----------------Pregenerated for features-------------------------------
	
	/**
	 * Array of orthogonally connected vertices, sorted for indexing by features.
	 * Also contains null entries for off-board!
	 */
	protected TopologyElement[] sortedOrthos = null;

	//----------------------------Methods----------------------------------------
	
	/**
	 * @return The graph element type.
	 */
	public abstract SiteType elementType();

	/**
	 * @return The centroid of the graph element.
	 */
	public Point2D centroid()
	{
		return new Point2D.Double(centroid.x(), centroid.y());
	}
	
	/**
	 * @return The centroid in 3D of the graph element.
	 */
	public Point3D centroid3D()
	{
		return centroid;
	}

	/**
	 * To set the centroid of the vertex (useful for DrawnState methods)
	 * 
	 * @param centroidX The new centroid X.
	 * @param centroidY The new centroid Y.
	 * @param centroidZ The new centroid Z.
	 */
	public void setCentroid(final double centroidX, final double centroidY, final double centroidZ)
	{
		centroid = new Point3D(centroidX, centroidY, centroidZ);
	}

	/**
	 * @return Index into list in Graph.
	 */
	public int index()
	{
		return index;
	}

	/**
	 * @return Phase, i.e. colour, if relevant.
	 */
	public int phase()
	{
		return phase;
	}

	/**
	 * To set the phase of the vertex.
	 * 
	 * @param phase
	 */
	public void setPhase(final int phase)
	{
		this.phase = phase;
	}

	/**
	 * To set the index.
	 * 
	 * @param index The new index.
	 */
	public void setIndex(final int index)
	{
		this.index = index;
	}

	/**
	 * @return element label.
	 */
	public String label()
	{
		return label;
	}

	/**
	 * @return the row coordinate.
	 */
	public int row()
	{
		return coord.row();
	}
	
	/**
	 * Set the row.
	 * 
	 * @param r The new row.
	 */
	public void setRow(final int r)
	{
		coord.setRow(r);
	}
	
	/**
	 * Set the column.
	 * 
	 * @param c The new column.
	 */
	public void setColumn(final int c)
	{
		coord.setColumn(c);
	}
	
	/**
	 * Set the layer.
	 * 
	 * @param l The new layer.
	 */
	public void setLayer(final int l)
	{
		coord.setLayer(l);
	}

	/**
	 * @return the col coordinate.
	 */
	public int col()
	{
		return coord.column();
	}

	/**
	 * @return the layer coordinate.
	 */
	public int layer()
	{
		return coord.layer();
	}

	/**
	 * To set the label of the element.
	 * 
	 * @param label
	 */
	public void setLabel(final String label)
	{
		this.label = label;
	}

	/**
	 * @return Cost of the element.
	 */
	public int cost()
	{
		return cost;
	}

	/**
	 * To set the cost of an element.
	 * 
	 * @param cost The new cost.
	 */
	public void setCost(final int cost)
	{
		this.cost = cost;
	}

	/**
	 * To set the coordinates.
	 * 
	 * @param row
	 * @param col
	 * @param level
	 */
	public void setCoord(final int row, final int col, final int level)
	{
		coord.set(row, col, level);
	}

//	/**
//	 * To set the row coordinate.
//	 * 
//	 * @param row
//	 */
//	public void setRow(final int row)
//	{
//		coord.row = row;
//	}
//
//	/**
//	 * To set the col coordinate.
//	 * 
//	 * @param col
//	 */
//	public void setColumn(final int col)
//	{
//		coord.column = col;
//	}
//
//	/**
//	 * To set the layer coordinate.
//	 * 
//	 * @param layer
//	 */
//	public void setLayer(final int layer)
//	{
//		coord.layer = layer;
//	}
	
	/**
	 * @return The vertices of this element.
	 */
	public abstract List<Vertex> vertices();
	
	/**
	 * @return The edges of this element.
	 */
	public abstract List<Edge> edges();
	
	/**
	 * @return The cells of this element.
	 */
	public abstract List<Cell> cells();
	
	/**
	 * @return The vertices enclosed by this element.
	 */
	public abstract List<Vertex> regionVertices();
	
	/**
	 * @return The edges enclosed by this element.
	 */
	public abstract List<Edge> regionEdges();
	
	/**
	 * @return The cells enclosed by this element.
	 */
	public abstract List<Cell> regionCells();

	//--------------------Pre-Generation methods----------------------------------

	/**
	 * @return The properties of the graph element (INNER, CENTRE, CORNER, etc...).
	 */
	public Properties properties()
	{
		return properties;
	}

	/**
	 * Sets the array of sorted orthos
	 * 
	 * @param sortedOrthos
	 */
	public void setSortedOrthos(final TopologyElement[] sortedOrthos)
	{
		this.sortedOrthos = sortedOrthos;
	}
	
	/**
	 * @return Sorted array of sorted orthogonal
	 */
	public TopologyElement[] sortedOrthos()
	{
		return sortedOrthos;
	}

	/**
	 * Set the properties of the graph element (INNER, CENTRE, CORNER, etc...).
	 * 
	 * @param properties The properties.
	 */
	public void setProperties(final Properties properties)
	{
		this.properties = properties;
	}

	/**
	 * @return The orthogonal elements of that element.
	 */
	public abstract List<? extends TopologyElement> orthogonal();

	/**
	 * @return The diagonal elements of that element.
	 */
	public abstract List<? extends TopologyElement> diagonal();

	/**
	 * @return The off elements of that element.
	 */
	public abstract List<? extends TopologyElement> off();

	/**
	 * @return The neighbours elements of that element.
	 */
	public abstract List<? extends TopologyElement> neighbours();

	/**
	 * @return The adjacent elements of that element.
	 */
	public abstract List<? extends TopologyElement> adjacent();

	/**
	 * @param relationType The relation type.
	 * 
	 * @return The list of supported directions according to a type of relation.
	 */
	public List<DirectionFacing> supportedDirections(final RelationType relationType)
	{
		switch (relationType)
		{
		case Adjacent:
			return supportedAdjacentDirections;
		case Diagonal:
			return supportedDiagonalDirections;
		case All:
			return supportedDirections;
		case OffDiagonal:
			return supportedOffDirections;
		case Orthogonal:
			return supportedOrthogonalDirections;
		default:
			break;
		}

		return supportedDirections;
	}

	/**
	 * @return The list of sites at a specific distance.
	 */
	public List<List<TopologyElement>> sitesAtDistance()
	{
		return sitesAtDistance;
	}

	/**
	 * @return The list of supported directions.
	 */
	public List<DirectionFacing> supportedDirections()
	{
		return supportedDirections;
	}

	/**
	 * @return The list of orthogonal supported directions.
	 */
	public List<DirectionFacing> supportedOrthogonalDirections()
	{
		return supportedOrthogonalDirections;
	}

	/**
	 * @return The list of off supported directions.
	 */
	public List<DirectionFacing> supportedOffDirections()
	{
		return supportedOffDirections;
	}

	/**
	 * @return The list of diagonal supported directions.
	 */
	public List<DirectionFacing> supportedDiagonalDirections()
	{
		return supportedDiagonalDirections;
	}

	/**
	 * @return The list of adjacent supported directions.
	 */
	public List<DirectionFacing> supportedAdjacentDirections()
	{
		return supportedAdjacentDirections;
	}

}
