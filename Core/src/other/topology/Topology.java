package other.topology;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import game.Game;
import game.equipment.container.Container;
import game.equipment.other.Regions;
import game.functions.region.RegionFunction;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.CompassDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Graph;
import game.util.graph.GraphElement;
import game.util.graph.Perimeter;
import game.util.graph.Properties;
import game.util.graph.Radial;
import game.util.graph.Radials;
import game.util.graph.Step;
import game.util.graph.Trajectories;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.ArrayUtils;
import main.math.MathRoutines;
import other.GraphUtilities;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Topology of the graph of the game.
 * 
 * @author Eric.Piette and cambolbro and Dennis Soemers
 */
public class Topology implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** List of cells. */
	private final List<Cell> cells = new ArrayList<Cell>();

	/** List of edges. */
	private final List<Edge> edges = new ArrayList<Edge>();

	/** List of vertices. */
	private final List<Vertex> vertices = new ArrayList<Vertex>();

	/** Reference to generating graph, so board styles and designs can determine underlying basis. */
	private Graph graph = null;
	
	/** The number of edges of each tiling only if the graph uses a regular tiling. */
	private int numEdges = Constants.UNDEFINED;

	//----------------------Pre-generated parameters-----------------
	
	/**
	 * Record of relations between elements within a graph.
	 */
	private Trajectories trajectories;

	/** Supported directions for each element type. */
	private final Map<SiteType, List<DirectionFacing>> supportedDirections = new EnumMap<SiteType, List<DirectionFacing>>(SiteType.class);

	/** Supported Orthogonal directions for each element type. */
	private final Map<SiteType, List<DirectionFacing>> supportedOrthogonalDirections = new EnumMap<SiteType, List<DirectionFacing>>(SiteType.class);
	
	/** Supported Diagonal directions for each element type. */
	private final Map<SiteType, List<DirectionFacing>> supportedDiagonalDirections = new EnumMap<SiteType, List<DirectionFacing>>(SiteType.class);

	/** Supported Adjacent directions for each element type. */
	private final Map<SiteType, List<DirectionFacing>> supportedAdjacentDirections = new EnumMap<SiteType, List<DirectionFacing>>(SiteType.class);

	/** Supported Off directions for each element type. */
	private final Map<SiteType, List<DirectionFacing>> supportedOffDirections = new EnumMap<SiteType, List<DirectionFacing>>(SiteType.class);

	/** List of corners sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> corners = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of convex corners sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> cornersConvex = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of concave corners sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> cornersConcave = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of major generator sites in tiling for each graph element. */
	private final Map<SiteType, List<TopologyElement>> major = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of minor generator sites in tiling for each graph element. */
	private final Map<SiteType, List<TopologyElement>> minor = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of outer sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> outer = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of perimeter sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> perimeter = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of inner sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> inner = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of interlayer sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> interlayer = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of top sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> top = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of left sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> left = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of right sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> right = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of bottom sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> bottom = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of centre sites for each graph element. */
	private final Map<SiteType, List<TopologyElement>> centre = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of column sites for each graph element. */
	private final Map<SiteType, List<List<TopologyElement>>> columns = new EnumMap<SiteType, List<List<TopologyElement>>>(SiteType.class);

	/** List of row sites for each graph element. */
	private final Map<SiteType, List<List<TopologyElement>>> rows = new EnumMap<SiteType, List<List<TopologyElement>>>(SiteType.class);

	/** List of sites in a specific phase for each graph element. */
	private final Map<SiteType, List<List<TopologyElement>>> phases = new EnumMap<SiteType, List<List<TopologyElement>>>(SiteType.class);

	/** List of sides according to a direction for each graph element. */
	private final Map<SiteType, Map<DirectionFacing, List<TopologyElement>>> sides = new EnumMap<SiteType, Map<DirectionFacing, List<TopologyElement>>>(SiteType.class);

	/** Distances from every graph element to each other graph element of the same type. */
	private final Map<SiteType, int[][]> distanceToOtherSite = new EnumMap<SiteType, int[][]>(SiteType.class);

	/** Distances from every graph element to its closest corner. */
	private final Map<SiteType, int[]> distanceToCorners = new EnumMap<SiteType, int[]>(SiteType.class);
	
	/** Distances from every graph element to its closest side. */
	private final Map<SiteType, int[]> distanceToSides = new EnumMap<SiteType, int[]>(SiteType.class);

	/** Distances from every graph element to its closest center. */
	private final Map<SiteType, int[]> distanceToCentre = new EnumMap<SiteType, int[]>(SiteType.class);
	
	/** List of layers sites for each graph element. */
	private final Map<SiteType, List<List<TopologyElement>>> layers = new EnumMap<SiteType, List<List<TopologyElement>>>(SiteType.class);

	/** List of diagonal sites for each graph element. */
	private final Map<SiteType, List<List<TopologyElement>>> diagonals = new EnumMap<SiteType, List<List<TopologyElement>>>(SiteType.class);
	
	/** List of axials sites for each graph element. Computed currently only for edges. */
	private final Map<SiteType, List<TopologyElement>> axials = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);
	
	/** List of horizontal sites for each graph element. Computed currently only for edges. */
	private final Map<SiteType, List<TopologyElement>> horizontal = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of vertical sites for each graph element. Computed currently only for edges. */
	private final Map<SiteType, List<TopologyElement>> vertical = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);
	
	/** List of angled sites for each graph element. Computed currently only for edges. */
	private final Map<SiteType, List<TopologyElement>> angled = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);

	/** List of slash sites for each graph element. Computed currently only for edges. */
	private final Map<SiteType, List<TopologyElement>> slash = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);
	
	/** List of slosh sites for each graph element. Computed currently only for edges. */
	private final Map<SiteType, List<TopologyElement>> slosh = new EnumMap<SiteType, List<TopologyElement>>(SiteType.class);
	
	/**
	 * For every region in equipment, a table of distances for graph element to that
	 * region. Contains null arrays for regions that are not static.
	 */
	private final Map<SiteType, int[][]> distanceToRegions = new EnumMap<SiteType, int[][]>(SiteType.class);

	/** Cross reference of phase by element index. */
	private final Map<SiteType, int[]> phaseByElementIndex = new EnumMap<SiteType, int[]>(SiteType.class);
	
	// ----- Pre-generated stuff for Features-----------------------------
	
	/** Different numbers of true-ortho-connectivity that different elements in our graph have */
	private final Map<SiteType, TIntArrayList> connectivities = new EnumMap<SiteType, TIntArrayList>(SiteType.class);
	
//	/** 
//	 * We'll store all the relations between Vertex pairs that were removed during graph 
//	 * construction here, so we can patch up the connectivity data for features. Will 
//	 * clean this memory up when we're done with preprocessing for features
//	 */
//	private Map<Vertex, List<RemovedRelationData>> removedRelations = new HashMap<Vertex, List<RemovedRelationData>>();

	//------------------------------------------------------------------------------

	/**
	 * List of perimeters (vertices) for each connected component in the graph. This
	 * list is used for rough working during graph measurements (for thread safety)
	 * and is not intended for reuse afterwards.
	 */
	private List<Perimeter> perimeters = new ArrayList<Perimeter>();

	/**
	 * Constructor to init each list of sites pre-generated.
	 */
	public Topology()
	{
		for (final SiteType type : SiteType.values())
		{
			corners.put(type, new ArrayList<TopologyElement>());
			cornersConcave.put(type, new ArrayList<TopologyElement>());
			cornersConvex.put(type, new ArrayList<TopologyElement>());
			major.put(type, new ArrayList<TopologyElement>());
			minor.put(type, new ArrayList<TopologyElement>());
			outer.put(type, new ArrayList<TopologyElement>());
			perimeter.put(type, new ArrayList<TopologyElement>());
			inner.put(type, new ArrayList<TopologyElement>());
			interlayer.put(type, new ArrayList<TopologyElement>());
			top.put(type, new ArrayList<TopologyElement>());
			bottom.put(type, new ArrayList<TopologyElement>());
			left.put(type, new ArrayList<TopologyElement>());
			right.put(type, new ArrayList<TopologyElement>());
			centre.put(type, new ArrayList<TopologyElement>());
			phases.put(type, new ArrayList<List<TopologyElement>>());
			for (int i = 0; i < Constants.MAX_CELL_COLOURS; i++)
				phases.get(type).add(new ArrayList<TopologyElement>());
			rows.put(type, new ArrayList<List<TopologyElement>>());
			columns.put(type, new ArrayList<List<TopologyElement>>());
			layers.put(type, new ArrayList<List<TopologyElement>>());
			diagonals.put(type, new ArrayList<List<TopologyElement>>());
			axials.put(type, new ArrayList<TopologyElement>());
			horizontal.put(type, new ArrayList<TopologyElement>());
			vertical.put(type, new ArrayList<TopologyElement>());
			angled.put(type, new ArrayList<TopologyElement>());
			slash.put(type, new ArrayList<TopologyElement>());
			slosh.put(type, new ArrayList<TopologyElement>());
			sides.put(type, new HashMap<DirectionFacing, List<TopologyElement>>());
			for (final CompassDirection direction : CompassDirection.values())
				sides.get(type).put(direction, new ArrayList<TopologyElement>());
			supportedDirections.put(type, new ArrayList<DirectionFacing>());
			supportedOrthogonalDirections.put(type, new ArrayList<DirectionFacing>());
			supportedDiagonalDirections.put(type, new ArrayList<DirectionFacing>());
			supportedAdjacentDirections.put(type, new ArrayList<DirectionFacing>());
			supportedOffDirections.put(type, new ArrayList<DirectionFacing>());
		}
	}

	//-----Methods--------------------------------------------------------------------------

	/**
	 * @return The corresponding graph.
	 */
	public Graph graph()
	{
		return graph;
	}
	
	/**
	 * To set the graph.
	 * 
	 * @param gr The new graph.
	 */
	public void setGraph(final Graph gr)
	{
		graph = gr;
	}
	
	/* Rotations and reflections of cells, edges, vertices */
	private int[][] cellRotationSymmetries;
	private int[][] cellReflectionSymmetries;
	private int[][] edgeRotationSymmetries;
	private int[][] edgeReflectionSymmetries;
	private int[][] vertexRotationSymmetries;
	private int[][] vertexReflectionSymmetries;
	
	/**
	 * @param cellRotationSymmetries the cellRotationSymmetries to set
	 */
	public void setCellRotationSymmetries(final int[][] cellRotationSymmetries) {
		this.cellRotationSymmetries = cellRotationSymmetries;
	}

	/**
	 * @param cellReflectionSymmetries the cellReflectionSymmetries to set
	 */
	public void setCellReflectionSymmetries(final int[][] cellReflectionSymmetries) {
		this.cellReflectionSymmetries = cellReflectionSymmetries;
	}

	/**
	 * @param edgeRotationSymmetries the edgeRotationSymmetries to set
	 */
	public void setEdgeRotationSymmetries(final int[][] edgeRotationSymmetries) {
		this.edgeRotationSymmetries = edgeRotationSymmetries;
	}

	/**
	 * @param edgeReflectionSymmetries the edgeReflectionSymmetries to set
	 */
	public void setEdgeReflectionSymmetries(final int[][] edgeReflectionSymmetries) {
		this.edgeReflectionSymmetries = edgeReflectionSymmetries;
	}

	/**
	 * @param vertexRotationSymmetries the vertexRotationSymmetries to set
	 */
	public void setVertexRotationSymmetries(final int[][] vertexRotationSymmetries) {
		this.vertexRotationSymmetries = vertexRotationSymmetries;
	}

	/**
	 * @param vertexReflectionSymmetries the vertexReflectionSymmetries to set
	 */
	public void setVertexReflectionSymmetries(final int[][] vertexReflectionSymmetries) {
		this.vertexReflectionSymmetries = vertexReflectionSymmetries;
	}

	/**
	 * @return the cellRotationSymmetries
	 */
	public int[][] cellRotationSymmetries() {
		return cellRotationSymmetries;
	}

	/**
	 * @return the cellReflectionSymmetries
	 */
	public int[][] cellReflectionSymmetries() {
		return cellReflectionSymmetries;
	}

	/**
	 * @return the edgeRotationSymmetries
	 */
	public int[][] edgeRotationSymmetries() {
		return edgeRotationSymmetries;
	}

	/**
	 * @return the edgeReflectionSymmetries
	 */
	public int[][] edgeReflectionSymmetries() {
		return edgeReflectionSymmetries;
	}

	/**
	 * @return the vertexRotationSymmetries
	 */
	public int[][] vertexRotationSymmetries() {
		return vertexRotationSymmetries;
	}

	/**
	 * @return the vertexReflectionSymmetries
	 */
	public int[][] vertexReflectionSymmetries() {
		return vertexReflectionSymmetries;
	}

	// ---------------------------------Methods------------------------------------------

	/**
	 * @param type The graph element type.
	 * @return List of corner sites.
	 */
	public List<TopologyElement> corners(final SiteType type)
	{
		return corners.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of concave corner sites.
	 */
	public List<TopologyElement> cornersConcave(final SiteType type)
	{
		return cornersConcave.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of convex corner sites.
	 */
	public List<TopologyElement> cornersConvex(final SiteType type)
	{
		return cornersConvex.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of major sites.
	 */
	public List<TopologyElement> major(final SiteType type)
	{
		return major.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of minor sites.
	 */
	public List<TopologyElement> minor(final SiteType type)
	{
		return minor.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of outer sites.
	 */
	public List<TopologyElement> outer(final SiteType type)
	{
		return outer.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of perimeter sites.
	 */
	public List<TopologyElement> perimeter(final SiteType type)
	{
		return perimeter.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of top sites.
	 */
	public List<TopologyElement> top(final SiteType type)
	{
		return top.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of bottom sites.
	 */
	public List<TopologyElement> bottom(final SiteType type)
	{
		return bottom.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of left sites.
	 */
	public List<TopologyElement> left(final SiteType type)
	{
		return left.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of right sites.
	 */
	public List<TopologyElement> right(final SiteType type)
	{
		return right.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of centre sites.
	 */
	public List<TopologyElement> centre(final SiteType type)
	{
		return centre.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of axial sites.
	 */
	public List<TopologyElement> axial(final SiteType type)
	{
		return axials.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of horizontal sites.
	 */
	public List<TopologyElement> horizontal(final SiteType type)
	{
		return horizontal.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of vertical sites.
	 */
	public List<TopologyElement> vertical(final SiteType type)
	{
		return vertical.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of angled sites.
	 */
	public List<TopologyElement> angled(final SiteType type)
	{
		return angled.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of slash sites.
	 */
	public List<TopologyElement> slash(final SiteType type)
	{
		return slash.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of slosh sites.
	 */
	public List<TopologyElement> slosh(final SiteType type)
	{
		return slosh.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of inner sites.
	 */
	public List<TopologyElement> inner(final SiteType type)
	{
		return inner.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of interlayer sites.
	 */
	public List<TopologyElement> interlayer(final SiteType type)
	{
		return interlayer.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of rows sites.
	 */
	public List<List<TopologyElement>> rows(final SiteType type)
	{
		return rows.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of columns sites.
	 */
	public List<List<TopologyElement>> columns(final SiteType type)
	{
		return columns.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of layers sites.
	 */
	public List<List<TopologyElement>> layers(final SiteType type)
	{
		return layers.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of diagonal sites.
	 */
	public List<List<TopologyElement>> diagonals(final SiteType type)
	{
		return diagonals.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of sites in a specific phase.
	 */
	public List<List<TopologyElement>> phases(final SiteType type)
	{
		return phases.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return List of sites on a side of the board.
	 */
	public Map<DirectionFacing, List<TopologyElement>> sides(final SiteType type)
	{
		return sides.get(type);
	}

	/**
	 * @return List of cells.
	 */
	public List<Cell> cells()
	{
		return cells;
	}

	/**
	 * @return List of edges.
	 */
	public List<Edge> edges()
	{
		return edges;
	}

	/**
	 * @return List of vertices.
	 */
	public List<Vertex> vertices()
	{
		return vertices;
	}

	/**
	 * @param relationType The relation type.
	 * @param type         The graph element type.
	 * 
	 * @return The list of supported directions according to a type of relation for
	 *         a type of graph element.
	 */
	public List<DirectionFacing> supportedDirections(final RelationType relationType, final SiteType type)
	{
		switch (relationType)
		{
		case Adjacent:
			return this.supportedAdjacentDirections.get(type);
		case Diagonal:
			return this.supportedDiagonalDirections.get(type);
		case All:
			return this.supportedDirections.get(type);
		case OffDiagonal:
			return this.supportedOffDirections.get(type);
		case Orthogonal:
			return this.supportedOrthogonalDirections.get(type);
		default:
			break;

		}

		return this.supportedDirections.get(type);
	}

	/**
	 * @param type The graph element type.
	 * 
	 * @return The list of supported directions for a type of graph element.
	 */
	public List<DirectionFacing> supportedDirections(final SiteType type)
	{
		return this.supportedDirections.get(type);
	}

	/**
	 * @param type The graph element type.
	 * 
	 * @return The list of orthogonal supported directions for a type of graph
	 *         element.
	 */
	public List<DirectionFacing> supportedOrthogonalDirections(final SiteType type)
	{
		return this.supportedOrthogonalDirections.get(type);
	}

	/**
	 * @param type The graph element type.
	 * 
	 * @return The list of off supported directions for a type of graph element.
	 */
	public List<DirectionFacing> supportedOffDirections(final SiteType type)
	{
		return this.supportedOffDirections.get(type);
	}

	/**
	 * @param type The graph element type.
	 * 
	 * @return The list of diagonal supported directions for a type of graph
	 *         element.
	 */
	public List<DirectionFacing> supportedDiagonalDirections(final SiteType type)
	{
		return this.supportedDiagonalDirections.get(type);
	}

	/**
	 * @param type The graph element type.
	 * 
	 * @return Tist of adjacent supported directions for a type of graph element.
	 */
	public List<DirectionFacing> supportedAdjacentDirections(final SiteType type)
	{
		return this.supportedAdjacentDirections.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return Array of distances to corners for all graph elements.
	 */
	public int[] distancesToCorners(final SiteType type)
	{
		return distanceToCorners.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return Array of distances to sides for all graph elements.
	 */
	public int[] distancesToSides(final SiteType type)
	{
		return distanceToSides.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return Array of distances to centre for all graph elements.
	 */
	public int[] distancesToCentre(final SiteType type)
	{
		return distanceToCentre.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return Array of distances to other graph element of the same type.
	 */
	public int[][] distancesToOtherSite(final SiteType type)
	{
		return distanceToOtherSite.get(type);
	}

	/**
	 * @param type The graph element type.
	 * @return Array with, for every region, an array of distances to regions.
	 *         Non-static regions will have null arrays.
	 */
	public int[][] distancesToRegions(final SiteType type)
	{
		return distanceToRegions.get(type);
	}

	/**
	 * Sets distances to corners
	 * 
	 * @param type              The graph element type.
	 * @param distanceToCorners
	 */
	public void setDistanceToCorners(final SiteType type, final int[] distanceToCorners)
	{
		this.distanceToCorners.put(type, distanceToCorners);
	}

	/**
	 * Sets distances to sides
	 * 
	 * @param type            The graph element type.
	 * @param distanceToSides
	 */
	public void setDistanceToSides(final SiteType type, final int[] distanceToSides)
	{
		this.distanceToSides.put(type, distanceToSides);
	}

	/**
	 * Sets distances to centres .
	 * 
	 * @param type             The graph element type.
	 * @param distanceToCentre
	 */
	public void setDistanceToCentre(final SiteType type, final int[] distanceToCentre)
	{
		this.distanceToCentre.put(type, distanceToCentre);
	}

	/**
	 * @return The number of edges of each tiling, only if the graph uses a regular
	 *         tiling.
	 */
	public int numEdges()
	{
		return this.numEdges;
	}

	/**
	 * @param type
	 * @param index
	 * @return Phase of specified element.
	 */
	public int phaseByElementIndex(final SiteType type, final int index)
	{
		return phaseByElementIndex.get(type)[index];
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param x
	 * @param y
	 * @return Matching cell, else null.
	 */
	public Cell findCell(final double x, final double y)
	{
		// Check for existing cell.
		for (final Cell cell : cells)
			if (cell.matches(x, y))
				return cell;

		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param va
	 * @param vb
	 * @return Matching edge, else null.
	 */
	public Edge findEdge(final Vertex va, final Vertex vb)
	{
		// Check for existing edge
		for (final Edge edge : edges)
			if (edge.matches(va, vb))
				return edge;

		return null;
	}

	/**
	 * @param pa
	 * @param pb
	 * @return Matching edge, else null.
	 */
	public Edge findEdge(final Point2D pa, final Point2D pb)
	{
		// Check for existing edge
		for (final Edge edge : edges)
			if (edge.matches(pa, pb))
				return edge;

		return null;
	}

	/**
	 * @param midpoint
	 * @return true if the midpoint is used by an edge.
	 */
	public Edge midpointEdgeUsed(final Point2D midpoint)
	{
		for (final Edge edge : edges)
			if (Math.abs(edge.centroid().getX() - midpoint.getX()) < 0.0001
					&& Math.abs(edge.centroid().getY() - midpoint.getY()) < 0.0001)
				return edge;

		return null;
	}

	/**
	 * @param row
	 * @param col
	 * @param level
	 * @return Cell with the correct coordinates. If not exists null.
	 */
	public Cell getCellWithCoords(final int row, final int col, final int level)
	{
		for (final Cell v : cells)
		{
			if (v.row() == row && v.col() == col && v.layer() == level)
				return v;
		}

		return null;
	}

	/**
	 * @param row
	 * @param col
	 * @param level
	 * @return Vertex with the correct coordinates. If not exists null.
	 */
	public Vertex getVertexWithCoords(final int row, final int col, final int level)
	{
		for (final Vertex v : vertices)
		{
			if (v.row() == row && v.col() == col && v.layer() == level)
				return v;
		}

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param graphElementType
	 * @param index
	 * @return Graph element of given type at given index
	 */
	public TopologyElement getGraphElement(final SiteType graphElementType, final int index)
	{
		switch(graphElementType)
		{
			case Vertex: return vertices().get(index); 
			case Edge: return edges().get(index); 
			case Cell: return cells().get(index);
			default: return null;
		}
	}
	
	/**
	 * @param graphElementType
	 * @return List of all graph elements of given type
	 */
	public List<? extends TopologyElement> getGraphElements(final SiteType graphElementType)
	{
		switch(graphElementType)
		{
			case Vertex: return vertices(); 
			case Edge: return edges(); 
			case Cell: return cells();
			default: return null;
		}
	}
	
	/**
	 * @param type
	 * @return Number of sites we have for given site type
	 */
	public int numSites(final SiteType type)
	{
		switch(type)
		{
			case Vertex: return vertices().size(); 
			case Edge: return edges().size(); 
			case Cell: return cells().size();
			default: return Constants.UNDEFINED;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return all elements of the graph.
	 */
	public ArrayList<TopologyElement> getAllGraphElements()
	{
		final ArrayList<TopologyElement> allGraphElements = new ArrayList<>();
		allGraphElements.addAll(vertices());
		allGraphElements.addAll(edges());
		allGraphElements.addAll(cells());
		return allGraphElements;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game 
	 * @return All elements of this graph that are used for some game rule, based on the concepts.
	 * 		   Used for evaluation Metrics.
	 */
	public ArrayList<TopologyElement> getAllUsedGraphElements(Game game)
	{
		final ArrayList<TopologyElement> allUsedGraphElements = new ArrayList<>();
		if (game.booleanConcepts().get(Concept.Vertex.id()))
			allUsedGraphElements.addAll(vertices());
		if (game.booleanConcepts().get(Concept.Edge.id()))
			allUsedGraphElements.addAll(edges());
		if (game.booleanConcepts().get(Concept.Cell.id()))
			allUsedGraphElements.addAll(cells());

		return allUsedGraphElements;
	}

	//-------------------------------------------------------------------------

	/**
	 * Pre-generates tables of distances to each element to each other.
	 * 
	 * @param type     The graph element type.
	 * @param relation The relation type of the distance to compute.
	 */
	public synchronized void preGenerateDistanceToEachElementToEachOther(final SiteType type, final RelationType relation)
	{
		if (this.distanceToOtherSite.get(type) != null)
			return;
		
		final List<? extends TopologyElement> elements = getGraphElements(type);
		final int[][] distances = new int[elements.size()][elements.size()];

		for (int idElem = 0; idElem < elements.size(); idElem++)
		{
			final TopologyElement element = elements.get(idElem);

			int currDist = 0;
			final TIntArrayList currList = new TIntArrayList();
			switch (relation)
			{
			case Adjacent:
				for (final TopologyElement elem : element.adjacent())
					currList.add(elem.index());
				break;
			case All:
				for (final TopologyElement elem : element.neighbours())
					currList.add(elem.index());
				break;
			case Diagonal:
				for (final TopologyElement elem : element.diagonal())
					currList.add(elem.index());
				break;
			case OffDiagonal:
				for (final TopologyElement elem : element.off())
					currList.add(elem.index());
				break;
			case Orthogonal:
				for (final TopologyElement elem : element.orthogonal())
					currList.add(elem.index());
				break;
			default:
				break;
			}
			final TIntArrayList nextList = new TIntArrayList();

			while (!currList.isEmpty())
			{
				++currDist;

				for (int i = 0; i < currList.size(); i++)
				{
					final int idNeighbour = currList.getQuick(i);

					if (idNeighbour == idElem || distances[idElem][idNeighbour] > 0)
						continue;

					distances[idElem][idNeighbour] = currDist;

					switch (relation)
					{
					case Adjacent:
						for (final TopologyElement elem : elements.get(idNeighbour).adjacent())
							if (!nextList.contains(elem.index()) && !currList.contains(elem.index()))
								nextList.add(elem.index());
						break;
					case All:
						for (final TopologyElement elem : elements.get(idNeighbour).neighbours())
							if (!nextList.contains(elem.index()) && !currList.contains(elem.index()))
								nextList.add(elem.index());
						break;
					case Diagonal:
						for (final TopologyElement elem : elements.get(idNeighbour).diagonal())
							if (!nextList.contains(elem.index()) && !currList.contains(elem.index()))
								nextList.add(elem.index());
						break;
					case OffDiagonal:
						for (final TopologyElement elem : elements.get(idNeighbour).off())
							if (!nextList.contains(elem.index()) && !currList.contains(elem.index()))
								nextList.add(elem.index());
						break;
					case Orthogonal:
						for (final TopologyElement elem : elements.get(idNeighbour).orthogonal())
							if (!nextList.contains(elem.index()) && !currList.contains(elem.index()))
								nextList.add(elem.index());
						break;
					default:
						break;
					}
				}

				currList.clear();
				currList.addAll(nextList);
				nextList.clear();
			}
		}

		this.distanceToOtherSite.put(type, distances);

		for (int idElem = 0; idElem < elements.size(); idElem++)
		{
			final TopologyElement element = elements.get(idElem);

			element.sitesAtDistance().clear();
			
			int maxDistance = 0;
			for (int idOtherElem = 0; idOtherElem < elements.size(); idOtherElem++)
			{
				if (maxDistance < distancesToOtherSite(type)[idElem][idOtherElem])
					maxDistance++;
			}

			// We add itself at distance zero.
			final List<TopologyElement> distanceZero = new ArrayList<TopologyElement>();
			distanceZero.add(element);
			element.sitesAtDistance().add(distanceZero);

			for (int distance = 1; distance <= maxDistance; distance++)
			{
				final List<TopologyElement> sitesAtDistance = new ArrayList<TopologyElement>();
				for (int idOtherElem = 0; idOtherElem < elements.size(); idOtherElem++)
					if (distancesToOtherSite(type)[idElem][idOtherElem] == distance)
						sitesAtDistance.add(elements.get(idOtherElem));
				element.sitesAtDistance().add(sitesAtDistance);
			}
		}
	}
	
	/**
	 * Pre-generates tables of distances to regions (for cells).
	 * 
	 * @param game    The game.
	 * @param regions The regions.
	 */
	public synchronized void preGenerateDistanceToRegionsCells(final Game game, final Regions[] regions)
	{
		final int numCells = cells.size();

		if (numCells == 0)
			return;

		final int[][] distances = new int[regions.length][];

		final Context dummyContext = new Context(game, new Trial(game));
		
		for (int i = 0; i < regions.length; ++i)
		{
			distances[i] = null;
			final Regions region = regions[i];
			int[] regionSites = null;

			if (region.region() != null)
			{
				boolean allStatic = true;

				for (final RegionFunction regionFunc : region.region())
				{
					if (regionFunc.type(game) != SiteType.Cell)
					{
						// We'll skip this one for now			TODO should think of a nicer solution for this case
						allStatic = false;
						continue;
					}
					else if (regionFunc.isStatic())
					{
						if (regionSites == null)
						{
//							regionSites = regionFunc.eval(null).sites();
							regionSites = regionFunc.eval(dummyContext).sites();
						}
						else
						{
//							final int[] toAppend = regionFunc.eval(null).sites();
							final int[] toAppend = regionFunc.eval(dummyContext).sites();
							regionSites = Arrays.copyOf(regionSites, regionSites.length + toAppend.length);
							System.arraycopy(toAppend, 0, regionSites, regionSites.length - toAppend.length,
									toAppend.length);
						}
					}
					else
					{
						allStatic = false;
						break;
					}
				}

				if (!allStatic)
					continue;
			}
			else if (region.sites() == null)
			{
				continue;
			}
			else
			{
				regionSites = region.sites();
			}

			distances[i] = new int[numCells];
			final boolean[] startingPoint = new boolean[numCells];

			for (int j = 0; j < regionSites.length; ++j)
			{
				final int regionSite = regionSites[j];
				if (regionSite >= cells.size())
					continue;
				final Cell regionCell = cells.get(regionSite);

				// Start flood-fill from this region Cell
				final boolean[] visited = new boolean[numCells];
				int currDist = 0;

				distances[i][regionSite] = currDist;
				visited[regionSite] = true;
				startingPoint[regionSite] = true;

				final List<Cell> currNeighbourList = new ArrayList<Cell>();
				currNeighbourList.addAll(regionCell.adjacent());
				final List<Cell> nextNeighbourList = new ArrayList<Cell>();

				while (!currNeighbourList.isEmpty())
				{
					++currDist;

					for (final Cell neighbour : currNeighbourList)
					{
						final int idx = neighbour.index();

						if (visited[idx] || startingPoint[idx])
							continue;

						if (distances[i][idx] > 0 && distances[i][idx] <= currDist)
							continue;

						distances[i][idx] = currDist;
						nextNeighbourList.addAll(neighbour.adjacent());
					}

					currNeighbourList.clear();
					currNeighbourList.addAll(nextNeighbourList);
					nextNeighbourList.clear();
				}
			}
		}

		this.distanceToRegions.put(SiteType.Cell, distances);
	}
	
	/**
	 * Pre-generates tables of distances to regions (for vertices).
	 * 
	 * @param game    The game.
	 * @param regions The regions.
	 */
	public synchronized void preGenerateDistanceToRegionsVertices(final Game game, final Regions[] regions)
	{
		final int numVertices = vertices.size();
		final int[][] distances = new int[regions.length][];

		final Context dummyContext = new Context(game, new Trial(game));
		
		for (int i = 0; i < regions.length; ++i)
		{
			distances[i] = null;
			final Regions region = regions[i];
			int[] regionSites = null;

			if (region.region() != null)
			{
				boolean allStatic = true;

				for (final RegionFunction regionFunc : region.region())
				{
					if (regionFunc.type(game) != SiteType.Vertex)
					{
						// We'll skip this one for now			TODO should think of a nicer solution for this case
						allStatic = false;
						continue;
					}
					else if (regionFunc.isStatic())
					{
						if (regionSites == null)
						{
							regionSites = regionFunc.eval(null).sites();
						}
						else
						{
//							final int[] toAppend = regionFunc.eval(null).sites();
							final int[] toAppend = regionFunc.eval(dummyContext).sites();
							regionSites = Arrays.copyOf(regionSites, regionSites.length + toAppend.length);
							System.arraycopy(toAppend, 0, regionSites, regionSites.length - toAppend.length,
									toAppend.length);
						}
					}
					else
					{
						allStatic = false;
						break;
					}
				}

				if (!allStatic)
					continue;
			}
			else if (region.sites() == null)
			{
				continue;
			}
			else
			{
				regionSites = region.sites();
			}

			distances[i] = new int[numVertices];
			final boolean[] startingPoint = new boolean[numVertices];

			for (int j = 0; j < regionSites.length; ++j)
			{
				final int regionSite = regionSites[j];
				
//				System.out.println("regionSite=" + regionSite + ", vertices.size()=" + vertices.size() + ".");
				
				final Vertex regionVertex = vertices.get(regionSite);

				// Start flood-fill from this region Vertex
				final boolean[] visited = new boolean[numVertices];
				int currDist = 0;

				distances[i][regionSite] = currDist;
				visited[regionSite] = true;
				startingPoint[regionSite] = true;

				final List<Vertex> currNeighbourList = new ArrayList<Vertex>();
				currNeighbourList.addAll(regionVertex.adjacent());
				final List<Vertex> nextNeighbourList = new ArrayList<Vertex>();

				while (!currNeighbourList.isEmpty())
				{
					++currDist;

					for (final Vertex neighbour : currNeighbourList)
					{
						final int idx = neighbour.index();

						if (visited[idx] || startingPoint[idx])
							continue;

						if (distances[i][idx] > 0 && distances[i][idx] <= currDist)
							continue;

						distances[i][idx] = currDist;
						nextNeighbourList.addAll(neighbour.adjacent());
					}

					currNeighbourList.clear();
					currNeighbourList.addAll(nextNeighbourList);
					nextNeighbourList.clear();
				}
			}
		}

		this.distanceToRegions.put(SiteType.Vertex, distances);
	}
	
	/** 
	 * Pre-generates tables of distances to precomputed regions such as centre, corners, sides, ...
	 * @param type Site type for which we want to generate distances
	 */
	public synchronized void preGenerateDistanceTables(final SiteType type)
	{
		preGenerateDistanceToPrecomputed(type, centre, distanceToCentre);
		preGenerateDistanceToPrecomputed(type, corners, distanceToCorners);
		preGenerateDistanceToPrecomputed(type, perimeter, distanceToSides);
	}
	
	/**
	 * Pre-generates tables of distances to corners.
	 * @param type Site type for which we want to generate distances
	 * @param precomputed Map of precomputed regions to which we want to generate distances
	 * @param distancesMap Map in which we want to store computed distances
	 */
	private synchronized void preGenerateDistanceToPrecomputed
	(
		final SiteType type, 
		final Map<SiteType, List<TopologyElement>> precomputed, 
		final Map<SiteType, int[]> distancesMap
	)
	{
		final List<? extends TopologyElement> elements = getGraphElements(type);
		final int numElements = elements.size();

		if (numElements == 0)
			return;

		final int[] distances = new int[numElements];
		Arrays.fill(distances, -1);
		int maxDistance = -1;
		final boolean[] startingPoint = new boolean[numElements];

		for (final TopologyElement corner : precomputed.get(type))
		{
			// Start flood-fill from this corner
			final boolean[] visited = new boolean[numElements];
			final int cornerIdx = corner.index();
			int currDist = 0;

			distances[cornerIdx] = currDist;
			visited[cornerIdx] = true;
			startingPoint[cornerIdx] = true;

			final List<TopologyElement> currNeighbourList = new ArrayList<TopologyElement>();
			currNeighbourList.addAll(corner.adjacent());
			final List<TopologyElement> nextNeighbourList = new ArrayList<TopologyElement>();

			while (!currNeighbourList.isEmpty())
			{
				++currDist;

				for (final TopologyElement neighbour : currNeighbourList)
				{
					final int idx = neighbour.index();

					if (visited[idx] || startingPoint[idx])
						continue;

					if (distances[idx] > 0 && distances[idx] <= currDist)
						continue;

					maxDistance = Math.max(maxDistance, currDist);
					distances[idx] = currDist;
					nextNeighbourList.addAll(neighbour.adjacent());
				}

				currNeighbourList.clear();
				currNeighbourList.addAll(nextNeighbourList);
				nextNeighbourList.clear();
			}
		}
		
		// Any remaining values of -1 are disconnected; give them max distance + 1
		ArrayUtils.replaceAll(distances, -1, maxDistance + 1);

		distancesMap.put(type, distances);
	}

	//-------------------------------------------------------------------------

	/**
	 * Optimises memory usage of the graph
	 */
	public void optimiseMemory()
	{
		((ArrayList<Cell>) cells).trimToSize();
		((ArrayList<Edge>) edges).trimToSize();
		((ArrayList<Vertex>) vertices).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : corners.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : cornersConcave.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : cornersConvex.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : major.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : minor.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : outer.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : perimeter.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : inner.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : interlayer.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : top.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : bottom.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : left.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : right.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : centre.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<List<TopologyElement>>> list : phases.entrySet())
			((ArrayList<List<TopologyElement>>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<List<TopologyElement>>> list : rows.entrySet())
			((ArrayList<List<TopologyElement>>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<List<TopologyElement>>> list : columns.entrySet())
			((ArrayList<List<TopologyElement>>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<List<TopologyElement>>> list : layers.entrySet())
			((ArrayList<List<TopologyElement>>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<List<TopologyElement>>> list : diagonals.entrySet())
			((ArrayList<List<TopologyElement>>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : axials.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : horizontal.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : vertical.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : angled.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : slash.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		for (final Entry<SiteType, List<TopologyElement>> list : slosh.entrySet())
			((ArrayList<TopologyElement>) list.getValue()).trimToSize();

		// Also tell every vertex, edge, and face, to optimise their memory use
		for (final Cell vertex : cells)
		{
			vertex.optimiseMemory();
		}

		for (final Edge edge : edges)
		{
			edge.optimiseMemory();
		}

		for (final Vertex face : vertices)
		{
			face.optimiseMemory();
		}
	}
	
	/**
	 * @param type
	 * @return Different numbers of true-ortho-connectivity that different elements of given type in our graph have
	 */
	public TIntArrayList connectivities(final SiteType type)
	{
		return connectivities.get(type);
	}
	
	/**
	 * @param game
	 * @return Different numbers of true-ortho-connectivity among elements
	 * we play on in given game
	 */
	public TIntArrayList trueOrthoConnectivities(final Game game)
	{
		if (game.board().defaultSite() == SiteType.Vertex)
			return connectivities.get(SiteType.Vertex);
		else
			return connectivities.get(SiteType.Cell);
	}
	
	/**
	 * Pregenerates connectivity data for features (for vertices as well
	 * as cells).
	 * 
	 * @param game
	 * @param container 
	 */
	public void pregenerateFeaturesData(final Game game, final Container container)
	{
		pregenerateFeaturesData(container, SiteType.Cell);
		pregenerateFeaturesData(container, SiteType.Vertex);
		
		if (game.board().defaultSite() == SiteType.Edge)
			pregenerateFeaturesData(container, SiteType.Edge);
	}

	/**
	 * List of perimeters (vertices) for each connected component in the graph. This
	 * list is used for rough working during graph measurements (for thread safety)
	 * and is not intended for reuse afterwards.
	 * 
	 * @return The list of perimeters.
	 */
	public List<Perimeter> perimeters()
	{
		return this.perimeters;
	}

	/**
	 * Set the perimeters.
	 * 
	 * @param perimeters The list of perimeters.
	 */
	public void setPerimeter(List<Perimeter> perimeters)
	{
		this.perimeters = perimeters;
	}
	
	/**
	 * For every site of given type in this topology, computes a sorted array
	 * of orthogonal neighbours, with null entries for off-board connections.
	 * 
	 * While we're going through this, we also find what the maximum
	 * number of orthogonal connections is across all cells.
	 * 
	 * This is used for features.
	 * 
	 * @param container
	 * @param type
	 */
	public void pregenerateFeaturesData(final Container container, final SiteType type)
	{
		final double ANGLES_TOLERANCE = 0.001;
		final List<? extends TopologyElement> elements = getGraphElements(type);
		
		connectivities.put(type, new TIntArrayList());
		
		for (final TopologyElement element : elements)
		{
			final int elementIdx = element.index;
			final List<TopologyElement> sortedOrthos = new ArrayList<TopologyElement>();
			
			// Create copy of radials list since we may have to insert extra placeholders
			final List<Radial> radials = new ArrayList<Radial>(trajectories().radials(type, elementIdx).inDirection(AbsoluteDirection.Orthogonal));
			final int numRealRadials = radials.size();

//			if (radials == null)
//				continue;

			// Our radials may be missing off-board continuations; we'll have to patch these up first
			// See if adding one more equal-angle radial completes 360 degrees (primarily for triangular tilings)
			if (numRealRadials == 2)
			{
				final Radial r1 = radials.get(0);
				final Radial r2 = radials.get(1);
				final double angle = Radials.angle(r1, r2);
				
				if (MathRoutines.approxEquals(angle, (2.0 * Math.PI) / 3.0, ANGLES_TOLERANCE))
				{
					// The two existing radials have a 120 degrees angle, so if we put one more fake radial
					// at another 120 degrees angle we complete the circle
					final Point2D newRadEndpoint = MathRoutines.rotate(angle, r2.steps()[1].pt2D(), r2.steps()[0].pt2D());
								
					// Add a tiny error towards right such that, if we get a new radial pointing northwards,
					// it gets correctly sorted as first instead of last (due to numeric errors)
					final double x = newRadEndpoint.getX() + 0.00001;	
					final double y = newRadEndpoint.getY();
					final double z = r2.steps()[1].pt().z();
					final Radial placeholder = 
							new Radial
							(
								new GraphElement[]
								{
									r2.steps()[0],
									new game.util.graph.Vertex(Constants.UNDEFINED, x, y, z)
								}, 
								AbsoluteDirection.Orthogonal
							);

					radials.add(placeholder);
					//System.out.println("added placeholder: " + placeholder);
					
					// Inserted placeholder, so re-sort
					Radials.sort(radials);
				}
			}
			
			// Only try other changes if we didn't already insert placeholder above
			if (numRealRadials == radials.size())
			{
				// See if any of our real radials need complementary straight-angle continuations
				for (int i = 0; i < numRealRadials; ++i)
				{
					final Radial realRadial = radials.get(i);
					final TopologyElement neighbour = elements.get(realRadial.steps()[1].id());
					
					// Search ALL radials of our neighbour for any that lead back to our current element
					// (not just orthogonals, because those sometimes get truncated -- see Kensington)
					final List<Radial> neighbourRadials = trajectories().radials(type, neighbour.index).inDirection(AbsoluteDirection.All);
					
					for (final Radial neighbourRadial : neighbourRadials)
					{
						final GraphElement[] path = neighbourRadial.steps();
						
						if (path.length == 2 && path[1].id() == elementIdx)
						{
							// This neighbour radial leads back to us, AND it does not continue afterwards
							
							// TODO may want to keep a set of directions we already handled here, and skip if no new directions
							
							// Create a placeholder radial to insert, starting at our element
							//
							// We need to add a step to a non-existing "off-board" element
							// We'll always make the placeholder element a vertex, since that's the least
							// complex constructor
							final double x = 2.0 * path[1].pt().x() - path[0].pt().x();
							final double y = 2.0 * path[1].pt().y() - path[0].pt().y();
							final double z = 2.0 * path[1].pt().z() - path[0].pt().z();
							
							final Radial placeholder = 
									new Radial
									(
										new GraphElement[]
										{
											path[1],
											new game.util.graph.Vertex(Constants.UNDEFINED, x, y, z)
										}, 
										AbsoluteDirection.Orthogonal
									);
							radials.add(placeholder);
							//System.out.println("added placeholder: " + placeholder);
						}
					}
				}
				
				// If we added any new radials, we'll have to re-sort our list of radials
				Radials.sort(radials);
				
				if (radials.size() > numRealRadials)
				{
					// We've added fake radials; let's see if we need to add even more to get nice uniform angles
					// between consecutive radials
					double realRadsAngleReference = Double.NaN;
					double fakeRadsAngleReference = Double.NaN;
					boolean realAnglesAllSame = true;
					boolean fakeAnglesAllSame = true;
					
					for (int i = 0; i < radials.size(); ++i)
					{
						final Radial r1 = radials.get(i);
						final Radial r2 = radials.get((i + 1) % radials.size());
						final double angle = Radials.angle(r1, r2);
						
						if (r1.steps()[1].id() == Constants.UNDEFINED || r2.steps()[1].id() == Constants.UNDEFINED)
						{
							// At least one radial is fake, so this is a fake angle
							if (Double.isNaN(fakeRadsAngleReference))
								fakeRadsAngleReference = angle;
							else if (Math.abs(angle - fakeRadsAngleReference) > ANGLES_TOLERANCE)
								fakeAnglesAllSame = false;		// Too big a difference
						}
						else
						{
							// This is an angle between two real radials
							if (Double.isNaN(realRadsAngleReference))
								realRadsAngleReference = angle;
							else if (Math.abs(angle - realRadsAngleReference) > ANGLES_TOLERANCE)
								realAnglesAllSame = false;		// Too big a difference
						}
					}
					
					if (realAnglesAllSame && !Double.isNaN(realRadsAngleReference))
					{
						// All angles between real radials are nicely the same; see if we can make
						// sure that this also happens for angles involving at least one fake radial
						if (!fakeAnglesAllSame || Math.abs(realRadsAngleReference - fakeRadsAngleReference) > ANGLES_TOLERANCE)
						{
							// We actually have work to do
							boolean failedFix = false;
							final List<Radial> newPlaceholders = new ArrayList<Radial>();
							for (int i = 0; i < radials.size(); ++i)
							{
								final Radial r1 = radials.get(i);
								final Radial r2 = radials.get((i + 1) % radials.size());
								final double angle = Radials.angle(r1, r2);
								
								// For now we just cover one case: where we simply divide the angle by 2 to make
								// it right. This covers the problem of the corners in the Hex rhombus
								if (Math.abs((angle / 2.0) - realRadsAngleReference) <= ANGLES_TOLERANCE)
								{
									// Dividing by 2 solves our problem!
									final Point2D newRadEndpoint = MathRoutines.rotate(angle / 2.0, r2.steps()[1].pt2D(), r2.steps()[0].pt2D());
									
									// Add a tiny error towards right such that, if we get a new radial pointing northwards,
									// it gets correctly sorted as first instead of last (due to numeric errors)
									final double x = newRadEndpoint.getX() + 0.00001;	
									final double y = newRadEndpoint.getY();
									final double z = r2.steps()[1].pt().z();
									final Radial placeholder = 
											new Radial
											(
												new GraphElement[]
												{
													r2.steps()[0],
													new game.util.graph.Vertex(Constants.UNDEFINED, x, y, z)
												}, 
												AbsoluteDirection.Orthogonal
											);
	
									newPlaceholders.add(placeholder);
								}
								else if (!MathRoutines.approxEquals(angle, realRadsAngleReference, ANGLES_TOLERANCE))
								{
									failedFix = true;
								}
							}
							
							if (failedFix)
							{
								// All real radials nicely had uniform angles between them, but fake
								// radials messed this up in a way we couldn't fix; better to revert 
								// to the original radials
								radials.clear();
								radials.addAll(trajectories().radials(type, elementIdx).inDirection(AbsoluteDirection.Orthogonal));
							}
							else
							{
								radials.addAll(newPlaceholders);
							}
							
							// Re-sort again, with again more fake radials inserted (maybe)
							Radials.sort(radials);
						}
					}
				}
			}
			
			// Compute our sorted array of orthogonal connectivities (with null placeholders)
			for (final Radial radial : radials)
			{
				final GraphElement[] path = radial.steps();
				
				if (path[1].id() == Constants.UNDEFINED)		// This is a placeholder
				{
					sortedOrthos.add(null);
				}
				else
				{
					sortedOrthos.add(elements.get(path[1].id()));
				}
			}

			if (!connectivities.get(type).contains(sortedOrthos.size()))
				connectivities.get(type).add(sortedOrthos.size());
			
			element.setSortedOrthos(sortedOrthos.toArray(new TopologyElement[sortedOrthos.size()]));
			//System.out.println(element + " has " + element.sortedOrthos().length + " orthos: " + Arrays.toString(element.sortedOrthos()));
		}
		
		connectivities.get(type).sort();
		connectivities.get(type).trimToSize();
	}
	
	/**
	 * Checks if we should ignore an array of radials when pre-generating data for features. We ignore
	 * an array of radials if we've already previously processed an array of radials that is 
	 * (maybe after "rotation") equal.
	 * 
	 * @param radials
	 * @param otherRadials
	 * @return
	 */
//	private static boolean shouldIgnoreRadials(final int[][] radials, final List<int[][]> otherRadials)
//	{
//		final int numRadials = radials.length;
//		for (final int[][] other : otherRadials)
//		{
//			final int numRotations = other.length;
//			
//			for (int rot = 0; rot < numRotations; ++rot)
//			{
//				boolean allEqual = true;
//				for (int i = 0; i < radials.length; ++i)
//				{
//					if (!Arrays.equals(radials[i], other[((i + rot) % numRadials + numRadials) % numRadials]))
//					{
//						allEqual = false;
//						break;
//					}
//				}
//				
//				if (allEqual)
//					return true;
//			}
//		}
//		
//		return false;
//	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The centre point of the board.
	 */
	public Point2D.Double centrePoint()
	{
		if (centre.get(SiteType.Cell).size() == 0)
			return new Point2D.Double(0.5, 0.5); // assume world coords in range [0..1]

		double avgX = 0;
		double avgY = 0;

		for (final TopologyElement element : centre.get(SiteType.Cell))
		{
			avgX += element.centroid().getX();
			avgY += element.centroid().getY();
		}
		avgX /= centre.get(SiteType.Cell).size();
		avgY /= centre.get(SiteType.Cell).size();

		return new Point2D.Double(avgX, avgY);
	}

	//-------------------------------------------------------------------------

	/**
	 * To store in the correct list the different properties of a graph element.
	 * 
	 * @param type
	 * @param element
	 */
	public void convertPropertiesToList(final SiteType type, final TopologyElement element)
	{
		final Properties properties = element.properties();
		
		if (properties.get(Properties.INNER))
			inner(type).add(element);

		if (properties.get(Properties.OUTER))
			outer(type).add(element);

		if (properties.get(Properties.INTERLAYER))
			interlayer(type).add(element);

		if (properties.get(Properties.PERIMETER))
			perimeter(type).add(element);

		if (properties.get(Properties.CORNER))
			corners(type).add(element);

		if (properties.get(Properties.CORNER_CONCAVE))
			cornersConcave(type).add(element);

		if (properties.get(Properties.CORNER_CONVEX))
			cornersConvex(type).add(element);

		if (properties.get(Properties.MAJOR))
			major(type).add(element);

		if (properties.get(Properties.MINOR))
			minor(type).add(element);

		if (properties.get(Properties.CENTRE))
			centre(type).add(element);

		if (properties.get(Properties.LEFT))
			left(type).add(element);

		if (properties.get(Properties.TOP))
			top(type).add(element);

		if (properties.get(Properties.RIGHT))
			right(type).add(element);

		if (properties.get(Properties.BOTTOM))
			bottom(type).add(element);

		if (properties.get(Properties.AXIAL))
			axial(type).add(element);

		if (properties.get(Properties.SLASH))
			slash(type).add(element);

		if (properties.get(Properties.SLOSH))
			slosh(type).add(element);

		if (properties.get(Properties.VERTICAL))
			vertical(type).add(element);

		if (properties.get(Properties.HORIZONTAL))
			horizontal(type).add(element);

		if (properties.get(Properties.ANGLED))
			angled(type).add(element);

		if (properties.get(Properties.PHASE_0))
		{
			phases(type).get(0).add(element);
			element.setPhase(0);
		}

		if (properties.get(Properties.PHASE_1))
		{
			phases(type).get(1).add(element);
			element.setPhase(1);
		}

		if (properties.get(Properties.PHASE_2))
		{
			phases(type).get(2).add(element);
			element.setPhase(2);
		}

		if (properties.get(Properties.PHASE_3))
		{
			phases(type).get(3).add(element);
			element.setPhase(3);
		}
		
		if (properties.get(Properties.PHASE_4))
		{
			phases(type).get(4).add(element);
			element.setPhase(4);
		}

		if (properties.get(Properties.PHASE_5))
		{
			phases(type).get(5).add(element);
			element.setPhase(5);
		}

		if (properties.get(Properties.SIDE_E))
			sides.get(type).get(CompassDirection.E).add(element);

		if (properties.get(Properties.SIDE_W))
			sides.get(type).get(CompassDirection.W).add(element);

		if (properties.get(Properties.SIDE_N))
			sides.get(type).get(CompassDirection.N).add(element);

		if (properties.get(Properties.SIDE_S))
			sides.get(type).get(CompassDirection.S).add(element);

		if (properties.get(Properties.SIDE_NE))
			sides.get(type).get(CompassDirection.NE).add(element);

		if (properties.get(Properties.SIDE_NW))
			sides.get(type).get(CompassDirection.NW).add(element);

		if (properties.get(Properties.SIDE_SW))
			sides.get(type).get(CompassDirection.SW).add(element);

		if (properties.get(Properties.SIDE_SE))
			sides.get(type).get(CompassDirection.SE).add(element);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param type  Graph element type.
	 * @param index Element index. 
	 * @return Phase (colouring) of the specified element, in range 0..3.
	 */
	public int elementPhase(final SiteType type, final int index) 
	{
		final List<? extends TopologyElement> elements = getGraphElements(type);
		
		for (int c = 0; c < Constants.MAX_CELL_COLOURS; c++)
			if (phases(type).get(c).contains(elements.get(index)))
				return c;
		return -1;
	}

	/**
	 * @param type Graph element type.
	 */
	public void crossReferencePhases(final SiteType type)
	{
		final List<? extends TopologyElement> elements = getGraphElements(type);
		
		final int[] values = new int[elements.size()];
		
		for (int e = 0; e < elements.size(); e++)
			values[e] = elementPhase(type, e);
		
		this.phaseByElementIndex.put(type, values);		
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Compute the relations and steps between each graph element of the same type.
	 * 
	 * @param type The graph element type.
	 */
	public void computeRelation(final SiteType type)
	{
		if (trajectories == null)
			return;

		final List<? extends TopologyElement> elements = getGraphElements(type);

		for (final TopologyElement element : elements)
		{			
			final List<Step> stepsNeighbours = trajectories.steps(type, element.index(), type, AbsoluteDirection.All);
			for (final Step step : stepsNeighbours)
			{
				GraphUtilities.addNeighbour(type, element, elements.get(step.to().id()));
			}

			final List<Step> stepsAdjacent = trajectories.steps(type, element.index(), type, AbsoluteDirection.Adjacent);
			for (final Step step : stepsAdjacent)
			{
				GraphUtilities.addAdjacent(type, element, elements.get(step.to().id()));
			}

			final List<Step> stepsOrthogonal = trajectories.steps(type, element.index(), type, AbsoluteDirection.Orthogonal);
			for (final Step step : stepsOrthogonal)
			{
				GraphUtilities.addOrthogonal(type, element, elements.get(step.to().id()));
			}

			final List<Step> stepsDiagonal = trajectories.steps(type, element.index(), type, AbsoluteDirection.Diagonal);
			for (final Step step : stepsDiagonal)
			{
				GraphUtilities.addDiagonal(type, element, elements.get(step.to().id()));
			}

			final List<Step> stepsOff = trajectories.steps(type, element.index(), type, AbsoluteDirection.OffDiagonal);
			for (final Step step : stepsOff)
			{
				GraphUtilities.addOff(type, element, elements.get(step.to().id()));
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Pre-generate the rows for a graph element.
	 * 
	 * @param type            The graph element type.
	 * @param threeDimensions True if this is a 3D game.
	 */
	public void computeRows(final SiteType type, final boolean threeDimensions)
	{
		rows(type).clear();

		if (graph() == null || graph().duplicateCoordinates(type) || threeDimensions) // If duplicate
		{
			final TDoubleArrayList rowCentroids = new TDoubleArrayList();
			for (final TopologyElement element : getGraphElements(type))
			{
				// We count the rows only on the ground.
				if (element.centroid3D().z() != 0)
					continue;

				final double yElement = element.centroid3D().y();
				boolean found = false;
				for (int i = 0; i < rowCentroids.size(); i++)
					if (Math.abs(yElement - rowCentroids.get(i)) < 0.001)
					{
						found = true;
						break;
					}

				if (!found)
					rowCentroids.add(yElement);
			}

			rowCentroids.sort();

			for (int i = 0; i < rowCentroids.size(); i++)
			{
				rows(type).add(new ArrayList<TopologyElement>());
				for (final TopologyElement element : getGraphElements(type))
					if (Math.abs(element.centroid3D().y() - rowCentroids.get(i)) < 0.001)
					{
						rows(type).get(i).add(element);
						element.setRow(i);
					}
			}
		}
		else // If not we take that from the graph.
		{
			for (final TopologyElement element : getGraphElements(type))
			{
				final int rowId = element.row();
				if (rows(type).size() > rowId)
					rows(type).get(rowId).add(element);
				else
				{
					while (rows(type).size() <= rowId)
						rows(type).add(new ArrayList<TopologyElement>());
					rows(type).get(rowId).add(element);
				}
			}
		}
	}

	/**
	 * Pre-generate the columns for a graph element.
	 * 
	 * @param type            The graph element type.
	 * @param threeDimensions True if this is a 3D game.
	 */
	public void computeColumns(final SiteType type, final boolean threeDimensions)
	{
		columns(type).clear();

		if (graph() == null || graph().duplicateCoordinates(type) || threeDimensions) // If duplicate
		{
			final TDoubleArrayList colCentroids = new TDoubleArrayList();
			for (final TopologyElement element : getGraphElements(type))
			{
				// We count the columns only on the ground.
				if (element.centroid3D().z() != 0)
					continue;

				final double xElement = element.centroid3D().x();
				boolean found = false;
				for (int i = 0; i < colCentroids.size(); i++)
					if (Math.abs(xElement - colCentroids.get(i)) < 0.001)
					{
						found = true;
						break;
					}

				if (!found)
					colCentroids.add(xElement);
			}

			colCentroids.sort();

			for (int i = 0; i < colCentroids.size(); i++)
			{
				columns(type).add(new ArrayList<TopologyElement>());
				for (final TopologyElement element : getGraphElements(type))
					if (Math.abs(element.centroid3D().x() - colCentroids.get(i)) < 0.001)
					{
						columns(type).get(i).add(element);
						element.setColumn(i);
					}
			}
		}
		else // If not we take that from the graph.
		{
			for (final TopologyElement element : getGraphElements(type))
			{
				final int columnId = element.col();
				if (columns(type).size() > columnId)
					columns(type).get(columnId).add(element);
				else
				{
					while (columns(type).size() <= columnId)
						columns(type).add(new ArrayList<TopologyElement>());
					columns(type).get(columnId).add(element);
				}
			}
		}
	}

	/**
	 * Pre-generate the columns for a graph element.
	 * 
	 * @param type The graph element type.
	 */
	public void computeLayers(final SiteType type)
	{
		layers(type).clear();

		if (graph() == null || graph().duplicateCoordinates(type)) // If duplicate
		{
			final TDoubleArrayList layerCentroids = new TDoubleArrayList();
			for (final TopologyElement element : getGraphElements(type))
			{
				final double z = element.centroid3D().z();
				
				if (layerCentroids.isEmpty())
				{
					layerCentroids.add(z);
				}
				else
				{
					final int insertIdx = layerCentroids.binarySearch(z);
					
					if (insertIdx < 0)
					{
						// Only insert if it's not already there
						layerCentroids.insert(-insertIdx, z);
					}
				}
			}

			//layerCentroids.sort();		Already sorted

			for (int i = 0; i < layerCentroids.size(); i++)
			{
				layers(type).add(new ArrayList<TopologyElement>());
				for (final TopologyElement element : getGraphElements(type))
					if (element.centroid3D().z() == layerCentroids.getQuick(i))
					{
						layers(type).get(i).add(element);

						element.setLayer(i);
					}
			}
		}
		else // If not we take that from the graph.
		{
			for (final TopologyElement element : getGraphElements(type))
			{
				final int layerId = element.layer();
				// System.out.println(type + " " + element.index() + " layer is " + layerId);
				if (layers(type).size() > layerId)
					layers(type).get(layerId).add(element);
				else
				{
					while (layers(type).size() <= layerId)
						layers(type).add(new ArrayList<TopologyElement>());
					layers(type).get(layerId).add(element);
				}
			}
		}
	}

	/**
	 * @param type The graph element type.
	 */
	public void computeCoordinates(final SiteType type)
	{
		if (graph() == null || graph().duplicateCoordinates(type)) // If duplicate
		{
			for (final TopologyElement element : getGraphElements(type))
			{
				String columnString = "";

				// To handle case with column coordinate at more than Z.
				if (element.col() >= 26)
					columnString = String.valueOf((char) ('A' + (element.col() / 26) - 1));

				columnString += String.valueOf((char) ('A' + element.col() % 26));
				final String rowString = String.valueOf(element.row() + 1);
				final String label = columnString + rowString;

				element.setLabel(label);
			}
		}
	}

	/**
	 * @return the trajectories.
	 */
	public Trajectories trajectories()
	{
		return trajectories;
	}

	/**
	 * Set the trajectories.
	 * 
	 * @param trajectories
	 */
	public void setTrajectories(final Trajectories trajectories)
	{
		this.trajectories = trajectories;
	}
	
	/**
	 * Compute the supported directions.
	 * 
	 * @param type The graph element type.
	 */
	public void computeSupportedDirection(final SiteType type)
	{
		if (trajectories == null)
			return;

		final List<? extends TopologyElement> elements = getGraphElements(type);
		final List<DirectionFacing> supportedDirection = supportedDirections.get(type);
		final List<DirectionFacing> supportedOrthogonalDirection = supportedOrthogonalDirections.get(type);
		final List<DirectionFacing> supportedDiagonalDirection = supportedDiagonalDirections.get(type);
		final List<DirectionFacing> supportedAdjacentDirection = supportedAdjacentDirections.get(type);
		final List<DirectionFacing> supportedOffDirection = supportedOffDirections.get(type);

		supportedDirection.clear();
		supportedOrthogonalDirection.clear();
		supportedDiagonalDirection.clear();
		supportedAdjacentDirection.clear();
		supportedOffDirection.clear();

		for (final TopologyElement element : elements)
		{
			final List<Step> steps = trajectories.steps(type, element.index(), type, AbsoluteDirection.All);
			final List<Step> stepsOrtho = trajectories.steps(type, element.index(), type, AbsoluteDirection.Orthogonal);
			final List<Step> stepsDiago = trajectories.steps(type, element.index(), type, AbsoluteDirection.Diagonal);
			final List<Step> stepsAdjacent = trajectories.steps(type, element.index(), type,
					AbsoluteDirection.Adjacent);
			final List<Step> stepsOff = trajectories.steps(type, element.index(), type, AbsoluteDirection.OffDiagonal);

			for (final Step step : steps)
			{
				for (int a = step.directions().nextSetBit(0); a >= 0; a = step.directions().nextSetBit(a + 1))
				{
					final AbsoluteDirection abs = AbsoluteDirection.values()[a];
					final DirectionFacing direction = AbsoluteDirection.convert(abs);
					if (direction != null)
					{
						if (!supportedDirection.contains(direction))
							supportedDirection.add(direction);

						if (!element.supportedDirections().contains(direction))
							element.supportedDirections().add(direction);
					}
				}
			}
			for (final Step step : stepsOrtho)
			{
				for (int a = step.directions().nextSetBit(0); a >= 0; a = step.directions().nextSetBit(a + 1))
				{
					final AbsoluteDirection abs = AbsoluteDirection.values()[a];
					final DirectionFacing direction = AbsoluteDirection.convert(abs);
					if (direction != null)
					{
						if (!supportedOrthogonalDirection.contains(direction))
							supportedOrthogonalDirection.add(direction);

						if (!element.supportedOrthogonalDirections().contains(direction))
							element.supportedOrthogonalDirections().add(direction);
					}
				}
			}
			for (final Step step : stepsDiago)
			{
				for (int a = step.directions().nextSetBit(0); a >= 0; a = step.directions().nextSetBit(a + 1))
				{
					final AbsoluteDirection abs = AbsoluteDirection.values()[a];
					final DirectionFacing direction = AbsoluteDirection.convert(abs);
					if (direction != null)
					{
						if (!supportedDiagonalDirection.contains(direction))
							supportedDiagonalDirection.add(direction);

						if (!element.supportedDiagonalDirections().contains(direction))
							element.supportedDiagonalDirections().add(direction);
					}
				}
			}
			for (final Step step : stepsAdjacent)
			{
				for (int a = step.directions().nextSetBit(0); a >= 0; a = step.directions().nextSetBit(a + 1))
				{
					final AbsoluteDirection abs = AbsoluteDirection.values()[a];
					final DirectionFacing direction = AbsoluteDirection.convert(abs);
					if (direction != null)
					{
						if (!supportedAdjacentDirection.contains(direction))
							supportedAdjacentDirection.add(direction);

						if (!element.supportedAdjacentDirections().contains(direction))
							element.supportedAdjacentDirections().add(direction);
					}
				}
			}
			for (final Step step : stepsOff)
			{
				for (int a = step.directions().nextSetBit(0); a >= 0; a = step.directions().nextSetBit(a + 1))
				{
					final AbsoluteDirection abs = AbsoluteDirection.values()[a];
					final DirectionFacing direction = AbsoluteDirection.convert(abs);
					if (direction != null)
					{
						if (!supportedOffDirection.contains(direction))
							supportedOffDirection.add(direction);

						if (!element.supportedOffDirections().contains(direction))
							element.supportedOffDirections().add(direction);
					}
				}
			}
		}

		// To sort the directions in compass direction.
		final Comparator<DirectionFacing> dirComparator = new Comparator<DirectionFacing>()
		{
			@Override
			public int compare(final DirectionFacing d1, final DirectionFacing d2)
			{
				return (d1.index() - d2.index());
			}
		};
		
		Collections.sort(supportedDirection, dirComparator);
		Collections.sort(supportedOrthogonalDirection, dirComparator);
		Collections.sort(supportedDiagonalDirection, dirComparator);
		Collections.sort(supportedAdjacentDirection, dirComparator);
		Collections.sort(supportedOffDirection, dirComparator);

//		System.out.println("SupportedDirections are " + supportedDirection);
//		System.out.println("SupportedOrthoDirections are " + supportedOrthogonalDirection);
//		System.out.println("SupportedDiagoDirections are " + supportedDiagonalDirection);
//		System.out.println("SupportedAdjacentDirections are " + supportedAdjacentDirection);
//		System.out.println("SupportedOffDirections are " + supportedOffDirection);
	}

	//-------------------------------------------------------------------------

	/**
	 * Compute all the edges crossing another.
	 */
	public void computeDoesCross()
	{
		for (final Edge edge: edges())
			edge.setDoesCross(new BitSet(edges.size()));
		
		for (int i = 0; i < edges.size(); i++)
		{
			final Edge edge = edges.get(i);
			final double tolerance = 0.001;

			final Point2D ptA = edge.vA().centroid();
			final Point2D ptB = edge.vB().centroid();

			for (int j = i + 1; j < edges.size(); j++)
			{
				final Edge otherEdge = edges.get(j);
				final Point2D ptEA = otherEdge.vA().centroid();
				final Point2D ptEB = otherEdge.vB().centroid();

				if (ptA.distance(ptEA) < tolerance || ptA.distance(ptEB) < tolerance || ptB.distance(ptEA) < tolerance
						|| ptB.distance(ptEB) < tolerance)
					continue;

				if (MathRoutines.lineSegmentsIntersect(ptA.getX(), ptA.getY(), ptB.getX(), ptB.getY(), ptEA.getX(),
						ptEA.getY(), ptEB.getX(), ptEB.getY()))
				{
					otherEdge.setDoesCross(edge.index());
					edge.setDoesCross(otherEdge.index());
				}
			}
		}
	}

	/**
	 * @return To know if the graph uses a regular tiling.
	 */
	public boolean isRegular()
	{
		return graph.isRegular();
	}

	/**
	 * Compute the number of edges of each tiling if the graph uses a regular
	 * tiling.
	 */
	public void computeNumEdgeIfRegular()
	{
		if (isRegular() && cells.size() > 0)
			this.numEdges = cells.get(0).edges().size();
	}

	//-------------------------------------------------------------------------

	/**
	 * @param type   The SiteType of the sites.
	 * @param origin The index of the origin site.
	 * @param target The index of the target site.
	 * @return One of the minimum walk between two sites in the graph (with the
	 *         minimum of Forward step).
	 */
	public StepType[] shortestWalk(final SiteType type, final int origin, final int target)
	{
		final List<? extends TopologyElement> elements = getGraphElements(type);
		final List<DirectionFacing> orthogonalSupported = supportedOrthogonalDirections.get(type);

		// No walk if the sites are the same or if the sites are incorrect.
		if (origin == target || origin < 0 || target < 0 || origin >= elements.size() || target >= elements.size())
		{
			return new StepType[0];
		}
		else
		{
			// The min walk to return.
			final List<StepType> returnedWalk = new ArrayList<StepType>();

			// The first direction of the origin site (North for square tiling).
			final DirectionFacing initialDirection = supportedOrthogonalDirections.get(type).get(0);

			// All the sites reached in stepping.
			final TIntArrayList reachedSites = new TIntArrayList();
			reachedSites.add(origin);

			// All the sites to explore or currently in exploration in stepping..
			final TIntArrayList toExplore = new TIntArrayList();

			// The current facing direction of the walk in each site currently in
			// exploration.
			final List<DirectionFacing> facingDirectionSitesToExplore = new ArrayList<DirectionFacing>();

			// The current steps used to reach the sites currently in exploration.
			final List<List<StepType>> stepUsed = new ArrayList<List<StepType>>();

			// We init the structures used to get the minwalk.
			toExplore.add(origin);
			facingDirectionSitesToExplore.add(initialDirection);
			stepUsed.add(new ArrayList<StepType>());

			// We check all the sites to explore in the order we found them.
			while (!toExplore.isEmpty())
			{
				// We get the site and direction.
				final int fromSite = toExplore.get(0);
				final DirectionFacing fromSiteDirection = facingDirectionSitesToExplore.get(0);

				// Get the site in the forward direction.
				final List<Step> stepsDirection = graph.trajectories().steps(type, fromSite,
						fromSiteDirection.toAbsolute());
				int toForward = Constants.UNDEFINED;
				for (final Step stepDirection : stepsDirection)
				{
					if (stepDirection.from().siteType() != stepDirection.to().siteType())
						continue;
					toForward = stepDirection.to().id();
				}
				// If that site exists and not reached until now.
				if (toForward != Constants.UNDEFINED && !reachedSites.contains(toForward))
				{
					toExplore.add(toForward);
					facingDirectionSitesToExplore.add(fromSiteDirection);
					stepUsed.add(new ArrayList<StepType>());

					final List<StepType> previousWalk = stepUsed.get(0);
					stepUsed.get(stepUsed.size() - 1).addAll(previousWalk);
					stepUsed.get(stepUsed.size() - 1).add(StepType.F);
					reachedSites.add(toForward);

					// If that's the target site we stop.
					if (toForward == target)
					{
						returnedWalk.addAll(stepUsed.get(stepUsed.size() - 1));
						break;
					}
				}

				// We try all the right and left directions.
				for (int i = 1; i < orthogonalSupported.size() / 2 + 1; i++)
				{
					// We get the right direction on that site.
					DirectionFacing currentRightDirection = fromSiteDirection;
					for (int j = 0; j < i; j++)
					{
						currentRightDirection = currentRightDirection.right();
						while (!orthogonalSupported.contains(currentRightDirection))
							currentRightDirection = currentRightDirection.right();
					}

					// We get the site in that direction.
					final List<Step> stepsRightDirection = graph.trajectories().steps(type, fromSite,
							currentRightDirection.toAbsolute());
					int toRight = Constants.UNDEFINED;
					for (final Step stepDirection : stepsRightDirection)
					{
						if (stepDirection.from().siteType() != stepDirection.to().siteType())
								continue;
						toRight = stepDirection.to().id();
					}

					// If that site exists in that direction and did not reached yet.
					if (toRight != Constants.UNDEFINED && !reachedSites.contains(toRight))
					{
						toExplore.add(toRight);
						facingDirectionSitesToExplore.add(currentRightDirection);

							final List<StepType> previousWalk = stepUsed.get(0);
						stepUsed.add(new ArrayList<StepType>());
							stepUsed.get(stepUsed.size() - 1).addAll(previousWalk);
						for (int j = 0; j < i; j++)
							stepUsed.get(stepUsed.size() - 1).add(StepType.R);
							stepUsed.get(stepUsed.size() - 1).add(StepType.F);
						reachedSites.add(toRight);

						// If that's the target site we stop.
						if (toRight == target)
						{
							returnedWalk.addAll(stepUsed.get(stepUsed.size() - 1));
							break;
						}
					}

					// We get the left direction on that site.
					DirectionFacing currentLeftDirection = fromSiteDirection;
					for (int j = 0; j < i; j++)
					{
						currentLeftDirection = currentLeftDirection.left();
						while (!orthogonalSupported.contains(currentLeftDirection))
							currentLeftDirection = currentLeftDirection.left();
					}

					// We get the site in that direction.
					final List<Step> stepsLeftDirection = graph.trajectories().steps(type, fromSite,
							currentLeftDirection.toAbsolute());
					int toLeft = Constants.UNDEFINED;
					for (final Step stepDirection : stepsLeftDirection)
						{
						if (stepDirection.from().siteType() != stepDirection.to().siteType())
							continue;
						toLeft = stepDirection.to().id();
						}

					// If that site exists in that direction and did not reached yet.
					if (toLeft != Constants.UNDEFINED && !reachedSites.contains(toLeft))
					{
						toExplore.add(toLeft);
						facingDirectionSitesToExplore.add(currentLeftDirection);
						final List<StepType> previousWalk = stepUsed.get(0);
						stepUsed.add(new ArrayList<StepType>());
						stepUsed.get(stepUsed.size() - 1).addAll(previousWalk);
						for (int j = 0; j < i; j++)
							stepUsed.get(stepUsed.size() - 1).add(StepType.L);
						stepUsed.get(stepUsed.size() - 1).add(StepType.F);
						reachedSites.add(toLeft);

						// If that's the target site we stop.
						if (toLeft == target)
						{
							returnedWalk.addAll(stepUsed.get(stepUsed.size() - 1));
							break;
						}
					}
				}

				// We remove the explored one.
				toExplore.removeAt(0);
				stepUsed.remove(0);
				facingDirectionSitesToExplore.remove(0);

				// To print the current exploration
				// System.out.println("\nNew Explo:");
				// for (int i = 0; i < exploration.size(); i++)
				// {
				// System.out.println("Explore " + exploration.get(i) + " current Direction Is "
				// + explorationDirection.get(i) + " current Steps are " + stepTypeUsed.get(i));
				// }
				// System.out.println();
			}

			// We convert to an array and we return.
			final StepType[] returnedWalkArray = new StepType[returnedWalk.size()];
			for (int i = 0; i < returnedWalk.size(); i++)
				returnedWalkArray[i] = returnedWalk.get(i);

			return returnedWalkArray;
		}
	}

//	/**
//	 * Compute one minimum walk between each pair of sites.
//	 * 
//	 * @param type The graph element type.
//	 * @return An array with one minimum walk between each pair of sites.
//	 *         [site1][site2][minWalk].
//	 */
//	public StepType[][][] oneMinWalkBetweanEachPairSites(final SiteType type)
//	{
//		final List<? extends TopologyElement> elements = getGraphElements(type);
//		final StepType[][][] minWalks = new StepType[elements.size()][elements.size()][];
//
//		for (int iElem = 0; iElem < elements.size(); iElem++)
//			for (int jElem = 0; jElem < elements.size(); jElem++)
//				minWalks[iElem][jElem] = oneMinWalkBetweenTwoSites(type, iElem, jElem);
//
//		return minWalks;
//	}

}
