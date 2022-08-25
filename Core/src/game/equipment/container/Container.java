package game.equipment.container;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import game.Game;
import game.equipment.Item;
import game.equipment.container.board.Track;
import game.types.board.SiteType;
import game.types.play.RoleType;
import metadata.graphics.util.ContainerStyleType;
import metadata.graphics.util.ControllerType;
import other.ItemType;
import other.concept.Concept;
import other.state.symmetry.SymmetryUtils;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.Vertex;

/**
 * Defines a container.
 *
 * @author cambolbro and Eric.Piette
 */
@SuppressWarnings("static-method")
public abstract class Container extends Item implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private static final double SYMMETRY_ACCURACY = 1e-6; // Allows for errors caused by double precision/trig functions

	/** The graph associated with the container. */
	protected Topology topology = new Topology();

	/** Number of sites. */
	protected int numSites = 0;

	/** List of tracks. */
	protected List<Track> tracks = new ArrayList<Track>();

	/** List of tracks refering by owner. */
	protected Track[][] ownedTracks;

	//-------------------------GUI---------------------------------
	
	/** The style of the container. */
	protected ContainerStyleType style;

	/** MVC controller for this container. */
	protected ControllerType controller; 

	//-------------------------------------------------------------------------

	/** The default type of graph element used by the game/board. */
	protected SiteType defaultSite = SiteType.Cell;

	//-------------------------------------------------------------------------

	/**
	 * @param label The name of the container.
	 * @param index the unique index of the container.
	 * @param role  The owner of the container.
	 */
	public Container
	(
		final String   label,
		final int      index, 
		final RoleType role
	)
	{
		super(label, index, role);
		setType(ItemType.Container);
	}

	/**
	 * Copy constructor.
	 *
	 * Protected because we do not want the compiler to detect it, this is called
	 * only in Clone method.
	 * 
	 * @param other
	 */
	protected Container(final Container other)
	{
		super(other);
		topology = other.topology;
		numSites = other.numSites;
		tracks = other.tracks;
		style = other.style;
		controller = other.controller;
		defaultSite = other.defaultSite;
		style = other.style;
		controller = other.controller;
	}

	//-------------------------------------------------------------------------

	/**
	 * To create the graph of the container.
	 * 
	 * @param beginIndex The first index to count the site of the cells.
	 * @param numEdges   The number of edges by cell.
	 */
	public abstract void createTopology(final int beginIndex, final int numEdges);

	//-------------------------------------------------------------------------
	/**
	 * @return The default type of graph element to play.
	 */
	public SiteType defaultSite()
	{
		return defaultSite;
	}

	/**
	 * @return Number of sites this container contains. If this is the board, it
	 *         returns the number of default sites (Cells, Vertices, Edges).
	 */
	public int numSites()
	{
		if (!defaultSite.equals(SiteType.Cell))
			return topology.getGraphElements(defaultSite).size();

		return numSites;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return True if the container is a hand.
	 */
	public boolean isHand()
	{
		return false;
	}

	/**
	 * @return True if the container is a dice.
	 */
	public boolean isDice()
	{
		return false;
	}

	/**
	 * @return True if the container is a deck.
	 */
	public boolean isDeck()
	{
		return false;
	}

	/**
	 * @return True if the container is boardless.
	 */
	public boolean isBoardless()
	{
		return false;
	}

	//-------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		if (!this.tracks().isEmpty())
		{
			concepts.set(Concept.Track.id(), true);
			for (final Track track : this.tracks())
				concepts.or(track.concepts(game));
		}
		return concepts;
	}

	@Override
	public Container clone()
	{
		Container c;
		try
		{
			c = (Container)super.clone();
			c.setName(name());
			c.setIndex(index());
			c.setNumSites(numSites);
		}
		catch (final CloneNotSupportedException e)
		{
			throw new Error();
		}
		return c;
	}

	/**
	 * Set the number of sites of the container.
	 * 
	 * @param numSites The number of sites.
	 */
	public void setNumSites(final int numSites)
	{
		this.numSites = numSites;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return List of track.
	 */
	public List<Track> tracks()
	{
		return Collections.unmodifiableList(tracks);
	}

	/**
	 * @return The graph of the container.
	 */
	public Topology topology()
	{
		return topology;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return The style of the board.
	 */
	public ContainerStyleType style()
	{
		return style;
	}

	/**
	 * To set the style of the board.
	 * 
	 * @param st
	 */
	public void setStyle(final ContainerStyleType st)
	{
		style = st;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The controler of the board.
	 */
	public ControllerType controller()
	{
		return controller;
	}
	
	/**
	 * To set the controler of the board.
	 * 
	 * @param controller
	 */
	public void setController(final ControllerType controller)
	{
		this.controller = controller;
	}

	/**
	 * @param owner The owner of the track.
	 * @return The tracks owned by a specific player.
	 */
	public Track[] ownedTracks(final int owner)
	{
		if (owner < ownedTracks.length)
			return ownedTracks[owner];
		else
			return new Track[0];
	}

	/**
	 * Set the owned Tracks.
	 * 
	 * @param ownedTracks The owned tracks.
	 */
	public void setOwnedTrack(final Track[][] ownedTracks)
	{
		this.ownedTracks = ownedTracks;
	}

	// ---------------------------------------Symetries-----------------------------------------------------------------

	/**
	 * Tries to find symmetries by rotating topologys, edges, and vertices at all
	 * reasonable angles The following will be caught by this code:
	 * 
	 * no symmetry half-turn symmetry; e.g. hex board 3-fold symmetry (triangle)
	 * quarter turn symmetry (square) 5-fold symmetry (Kaooa) 6-fold symmetry
	 * (hexagon)
	 * 
	 * Other symmetries can be added if needed
	 * 
	 * @param topo
	 */
	public static void createSymmetries(final Topology topo)
	{
		createSymmetries(topo, 24); // Will create 2-, 3-, 4-, and 6- fold symmetries;

		if (topo.cellReflectionSymmetries().length == 0 && topo.cellRotationSymmetries().length == 0)
			createSymmetries(topo, 5); // There are one or two traditional games that use 5-fold symmetry

		if (topo.cellReflectionSymmetries().length == 0 && topo.cellRotationSymmetries().length == 0)
			createSymmetries(topo, 7); // There are one or two traditional games that use 5-fold symmetry

	}

	/**
	 * Tries to find symmetries by rotating topologys, edges, and vertices by the
	 * angles suggested by symmetries
	 * 
	 * @param topology
	 * @param symmetries
	 */
	private static void createSymmetries(final Topology topology, final int symmetries)
	{
		final List<Cell> cells = topology.cells();
		final List<Edge> edges = topology.edges();
		final List<Vertex> vertices = topology.vertices();

		final Point2D origin1 = topology.centrePoint();
		Point2D origin2 = new Point2D.Double(0.5, 0.5);
		if (origin1.equals(origin2))
			origin2 = null; // avoids unnecessary processing

		// All rotation processing
		// Rotation angles are 0 <= angle < 2*PI
		{
			final int[][] cellRotations = new int[symmetries][];
			final int[][] edgeRotations = new int[symmetries][];
			final int[][] vertexRotations = new int[symmetries][];
			int rotCount = 0;
			for (int turns = 0; turns < symmetries; turns++)
			{
				int[] cRots = calcCellRotation(cells, origin1, turns, symmetries);
				int[] eRots = calcEdgeRotation(edges, origin1, turns, symmetries);
				int[] vRots = calcVertexRotation(vertices, origin1, turns, symmetries);

				// Try again with origin2, if origin1 failed
				if (origin2 != null && (!SymmetryUtils.isBijective(cRots) || !SymmetryUtils.isBijective(eRots)
						|| !SymmetryUtils.isBijective(vRots)))
				{
					cRots = calcCellRotation(cells, origin1, turns, symmetries);
					eRots = calcEdgeRotation(edges, origin1, turns, symmetries);
					vRots = calcVertexRotation(vertices, origin1, turns, symmetries);
				}

				// If we successfully rotated everything, store the symmetry
				if (SymmetryUtils.isBijective(cRots) && SymmetryUtils.isBijective(eRots)
						&& SymmetryUtils.isBijective(vRots))
				{
					cellRotations[rotCount] = cRots;
					edgeRotations[rotCount] = eRots;
					vertexRotations[rotCount] = vRots;
					rotCount++;
				}
			}
			topology.setCellRotationSymmetries(Arrays.copyOf(cellRotations, rotCount));
			topology.setEdgeRotationSymmetries(Arrays.copyOf(edgeRotations, rotCount));
			topology.setVertexRotationSymmetries(Arrays.copyOf(vertexRotations, rotCount));
		}

		// All reflection processing
		// Note that because of symmetries, reflection angles are 0 <= angle < PI, not
		// 2PI - this is taken care of in the reflection routine.
		{
			final int[][] cellReflections = new int[symmetries][];
			final int[][] edgeReflections = new int[symmetries][];
			final int[][] vertexReflections = new int[symmetries][];

			int refCount = 0;
			for (int turns = 0; turns < symmetries; turns++)
			{
				int[] cRefs = calcCellReflection(cells, origin1, turns, symmetries);
				int[] eRefs = calcEdgeReflection(edges, origin1, turns, symmetries);
				int[] vRefs = calcVertexReflection(vertices, origin1, turns, symmetries);

				// Try again with origin2, if origin1 failed
				if (origin2 != null && (!SymmetryUtils.isBijective(cRefs) || !SymmetryUtils.isBijective(eRefs)
						|| !SymmetryUtils.isBijective(vRefs)))
				{
					cRefs = calcCellReflection(cells, origin1, turns, symmetries);
					eRefs = calcEdgeReflection(edges, origin1, turns, symmetries);
					vRefs = calcVertexReflection(vertices, origin1, turns, symmetries);
				}

				// If we successfully rotated everything, store the symmetry
				if (SymmetryUtils.isBijective(cRefs) && SymmetryUtils.isBijective(eRefs)
						&& SymmetryUtils.isBijective(vRefs))
				{
					cellReflections[refCount] = cRefs;
					edgeReflections[refCount] = eRefs;
					vertexReflections[refCount] = vRefs;
					refCount++;
				}
			}
			topology.setCellReflectionSymmetries(Arrays.copyOf(cellReflections, refCount));
			topology.setEdgeReflectionSymmetries(Arrays.copyOf(edgeReflections, refCount));
			topology.setVertexReflectionSymmetries(Arrays.copyOf(vertexReflections, refCount));
		}
	}

	private static int[] calcCellRotation(final List<Cell> cells, final Point2D origin, final int turns,
			final int symmetries)
	{
		final int[] rots = new int[cells.size()];
		for (int cell = 0; cell < cells.size(); cell++)
		{
			final Point2D start = cells.get(cell).centroid();
			final Point2D end = SymmetryUtils.rotateAroundPoint(origin, start, turns, symmetries);
			rots[cell] = findMatchingCell(cells, end);
			if (rots[cell] == -1)
				break; // Symmetry broken, early exit
		}
		return rots;
	}

	private static int[] calcEdgeRotation(final List<Edge> edges, final Point2D origin, final int turns,
			final int symmetries)
	{
		final int[] rots = new int[edges.size()];
		for (int edge = 0; edge < edges.size(); edge++)
		{
			final Point2D pt1 = edges.get(edge).vA().centroid();
			final Point2D pt2 = edges.get(edge).vB().centroid();
			final Point2D end1 = SymmetryUtils.rotateAroundPoint(origin, pt1, turns, symmetries);
			final Point2D end2 = SymmetryUtils.rotateAroundPoint(origin, pt2, turns, symmetries);
			rots[edge] = findMatchingEdge(edges, end1, end2);
			if (rots[edge] == -1)
				break; // Symmetry broken, early exit
		}
		return rots;
	}

	private static int[] calcVertexRotation(final List<Vertex> vertices, final Point2D origin, final int turns,
			final int symmetries)
	{
		final int[] rots = new int[vertices.size()];
		for (int vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex++)
		{
			final Point2D start = vertices.get(vertexIndex).centroid();
			final Point2D end = SymmetryUtils.rotateAroundPoint(origin, start, turns, symmetries);
			rots[vertexIndex] = findMatchingVertex(vertices, end);
			if (rots[vertexIndex] == -1)
				break; // Symmetry broken, early exit
		}
		return rots;
	}

	private static int findMatchingVertex(List<Vertex> vertices, Point2D end)
	{
		for (int vertex = 0; vertex < vertices.size(); vertex++)
			if (SymmetryUtils.closeEnough(end, vertices.get(vertex).centroid(), SYMMETRY_ACCURACY))
				return vertex;

		return -1;
	}

	private static int findMatchingEdge(final List<Edge> edges, final Point2D pos1, final Point2D pos2)
	{
		for (int edgeIndex = 0; edgeIndex < edges.size(); edgeIndex++)
		{
			final Edge edge = edges.get(edgeIndex);
			final Point2D ptA = edge.vA().centroid();
			final Point2D ptB = edge.vB().centroid();
			if (SymmetryUtils.closeEnough(pos1, ptA, SYMMETRY_ACCURACY)
					&& SymmetryUtils.closeEnough(pos2, ptB, SYMMETRY_ACCURACY)
					|| SymmetryUtils.closeEnough(pos1, ptB, SYMMETRY_ACCURACY)
							&& SymmetryUtils.closeEnough(pos2, ptA, SYMMETRY_ACCURACY))
				return edgeIndex;
		}
		return -1;
	}

	private static int findMatchingCell(final List<Cell> cells, final Point2D pos)
	{
		for (int cell = 0; cell < cells.size(); cell++)
			if (SymmetryUtils.closeEnough(pos, cells.get(cell).centroid(), SYMMETRY_ACCURACY))
				return cell;

		return -1;
	}

	private static int[] calcCellReflection(final List<Cell> cells, final Point2D origin, final int turns,
			final int symmetries)
	{
		final int[] refs = new int[cells.size()];
		for (int cell = 0; cell < cells.size(); cell++)
		{
			final Point2D start = cells.get(cell).centroid();
			final Point2D end = SymmetryUtils.reflectAroundLine(origin, start, turns, symmetries);
			refs[cell] = findMatchingCell(cells, end);
			if (refs[cell] == -1)
				break; // Symmetry broken - early exit
		}
		return refs;
	}

	private static int[] calcEdgeReflection(final List<Edge> edges, final Point2D origin, final int turns,
			final int symmetries)
	{
		final int[] refs = new int[edges.size()];
		for (int edgeIndex = 0; edgeIndex < edges.size(); edgeIndex++)
		{
			final Point2D p1 = edges.get(edgeIndex).vA().centroid();
			final Point2D p2 = edges.get(edgeIndex).vB().centroid();
			final Point2D end1 = SymmetryUtils.reflectAroundLine(origin, p1, turns, symmetries);
			final Point2D end2 = SymmetryUtils.reflectAroundLine(origin, p2, turns, symmetries);
			refs[edgeIndex] = findMatchingEdge(edges, end1, end2);
			if (refs[edgeIndex] == -1)
				break; // Symmetry broken - early exit
		}
		return refs;
	}

	private static int[] calcVertexReflection(final List<Vertex> vertices, final Point2D origin, final int turns,
			final int symmetries)
	{
		final int[] refs = new int[vertices.size()];
		for (int vertex = 0; vertex < vertices.size(); vertex++)
		{
			final Point2D start = vertices.get(vertex).centroid();
			final Point2D end = SymmetryUtils.reflectAroundLine(origin, start, turns, symmetries);
			refs[vertex] = findMatchingVertex(vertices, end);
			if (refs[vertex] == -1)
				break; // Symmetry broken - early exit
		}
		return refs;
	}
}