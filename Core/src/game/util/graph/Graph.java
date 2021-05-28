package game.util.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.math.MathRoutines;
import main.math.Point3D;
import main.math.Vector;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines the graph of a custom board described by a set of vertices and edges.
 * 
 * @author cambolbro
 */
public class Graph extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	/** Types of graph elements. */
	public final static SiteType[] siteTypes = { SiteType.Vertex, SiteType.Edge, SiteType.Cell }; 

	private final List<Vertex> vertices = new ArrayList<Vertex>();
	private final List<Edge>   edges    = new ArrayList<Edge>();
	private final List<Face>   faces    = new ArrayList<Face>();

	/** 
	 * List of perimeters (vertices) for each connected component in the graph. 
	 * This list is used for rough working during graph measurements 
	 * (for thread safety) and is not intended for reuse afterwards. 
	 */
	private final List<Perimeter> perimeters = new ArrayList<Perimeter>();

	private final Trajectories trajectories = new Trajectories();
	
	private final boolean[] duplicateCoordinates = new boolean[SiteType.values().length];
	
	//-------------------------------------------------------------------------

	/**
	 * @param vertices List of vertex positions in {x y} or {x y z} format.
	 * @param edges    List of vertex index pairs {vi vj} describing edge end points.
	 * 
	 * @example (graph vertices:{ {0 0} {1.5 0 0.5} {0.5 1} } 
	 *                    edges:{ {0 1} {0 2} {1 2} } )
	 */
	public Graph
	(		
			 @Name final Float[][]   vertices,
		@Opt @Name final Integer[][] edges
	)
	{
		setVertices(vertices);
		
		if (edges != null)
			setEdges(edges);
	
		assemble(false);
	}

	/**
	 * @param vertices The vertices.
	 * @param edges    The edges.
	 */
	@Hide
	public Graph
	(
		final List<Vertex> vertices, 
		final List<Edge>   edges
	)
	{
		for (final Vertex vertex : vertices)
			addVertex(vertex.pt());
		
		// Link up pivots
		for (final Vertex vertex : vertices)
			if (vertex.pivot() != null)
			{
				final Vertex newVertex = this.vertices.get(vertex.id());
				final Vertex pivot = findVertex(vertex.pivot().pt2D());
				if (pivot == null)
				{
//					System.out.println("** Failed to find pivot in Graph constructor.");
				}
				newVertex.setPivot(pivot);
			}
		
		for (final Edge edge : edges)
		{
			final Edge newEdge = findOrAddEdge(edge.vertexA().id(), edge.vertexB().id());  //, edge.curved());
			if (edge.tangentA() != null)
				newEdge.setTangentA(new Vector(edge.tangentA()));
			if (edge.tangentB() != null)
				newEdge.setTangentB(new Vector(edge.tangentB()));
		}
		
		assemble(false);
	}

	/**
	 * @param other Stop JavaDoc whining.
	 */
	@Hide
	public Graph(final Graph other)
	{	
		deepCopy(other);
	}

	/**
	 * Default constructor when edges and vertices are not known (or not needed
	 * to be known), e.g. for Mancala.
	 */
	@Hide
	public Graph()
	{
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The vertices of the graph.
	 */
	public List<Vertex> vertices()
	{
		return Collections.unmodifiableList(vertices);
	}

	/**
	 * @return The edges of the graph.
	 */
	public List<Edge> edges()
	{
		return Collections.unmodifiableList(edges);
	}

	/**
	 * @return The faces of the graph.
	 */
	public List<Face> faces()
	{
		return Collections.unmodifiableList(faces);
	}
	
	/**
	 * @return The perimeters of the graph.
	 */
	public List<Perimeter> perimeters()
	{
		return Collections.unmodifiableList(perimeters);
	}
	
	/**
	 * @return The trajectories of the graph.
	 */
	public Trajectories trajectories()
	{
		return trajectories;
	}

	/**
	 * @param siteType Site type to check.
	 * 
	 * @return Whether this graph contains duplicate coordinates.
	 */
	public boolean duplicateCoordinates(final SiteType siteType)
	{
		return duplicateCoordinates[siteType.ordinal()];
	}

	/**
	 * Sets the duplicate coordinate flag to true.
	 *
	 * @param siteType Site type to check.
	 */
	public void setDuplicateCoordinates(final SiteType siteType)
	{
		duplicateCoordinates[siteType.ordinal()] = true;
	}

	//-------------------------------------------------------------------------

	/**
	 * Set the dimension of the graph.
	 * 
	 * @param array
	 */
	public void setDim(final int[] array)
	{
		dim = array;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Clear the perimeters.
	 */
	public void clearPerimeters()
	{
		perimeters.clear();
	}

	/**
	 * Add a perimeter.
	 * 
	 * @param perimeter The perimeter to add.
	 */
	public void addPerimeter(final Perimeter perimeter)
	{
		perimeters.add(perimeter);
	}

	/**
	 * Remove a perimeter.
	 * 
	 * @param n The index of the perimeter.
	 */
	public void removePerimeter(final int n)
	{
		perimeters.remove(n);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type.
	 * 
	 * @return The graph element list of the specified type.
	 */
	public List<? extends GraphElement> elements(final SiteType type)
	{
		switch (type)
		{
		case Vertex: return vertices;
		case Edge:   return edges;
		case Cell: 	 return faces;
		}
		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type.
	 * @param id   The index of the element.
	 * 
	 * @return The specified graph element of the specified type.
	 */
	public GraphElement element(final SiteType type, final int id)
	{
		switch (type)
		{
		case Vertex: return vertices.get(id);
		case Edge:   return edges.get(id);
		case Cell: 	 return faces.get(id);
		}
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper function for convenience.
	 * 
	 * @param vertexList The list of the vertices.
	 * @param edgeList   The list of the edges.
	 * 
	 * @return Graph made from lists of vertex (x,y) pairs and edge (a, b) pairs.
	 */
	public static Graph createFromLists
	(
		final List<double[]> vertexList, 
		final List<int[]>    edgeList
	)
	{
		// Convert to arrays to construct graph
		final Float[][] vertexArray = new Float[vertexList.size()][3];
		for (int v = 0; v < vertexList.size(); v++)
		{
			final double[] values = vertexList.get(v);
			vertexArray[v][0] = Float.valueOf((float)values[0]);
			vertexArray[v][1] = Float.valueOf((float)values[1]);
			if (values.length > 2)
				vertexArray[v][2] = Float.valueOf((float)values[2]);
			else
				vertexArray[v][2] = Float.valueOf(0f);
		}
		
		final Integer[][] edgeArray = new Integer[edgeList.size()][2];
		for (int e = 0; e < edgeList.size(); e++)
		{
			edgeArray[e][0] = Integer.valueOf(edgeList.get(e)[0]);
			edgeArray[e][1] = Integer.valueOf(edgeList.get(e)[1]);
		}

		return new Graph(vertexArray, edgeArray);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Clear the graph.
	 */
	public void clear()
	{
		faces.clear();
		edges.clear();
		vertices.clear();
	}
	
	/**
	 * Clear a type of graph element.
	 * 
	 * @param siteType The type of the graph element.
	 */
	public void clear(final SiteType siteType)
	{
		switch (siteType)
		{
		case Vertex:
			vertices.clear();
			//$FALL-THROUGH$
		case Edge:
			edges.clear();
			for (final Vertex vertex : vertices())
				vertex.clearEdges();
			//$FALL-THROUGH$
		case Cell:
			faces.clear();
			for (final Edge edge : edges())
			{
				edge.setLeft(null);
				edge.setRight(null);
			}
			for (final Vertex vertex : vertices())
				vertex.clearFaces();
			//$FALL-THROUGH$
		default:  // do nothing
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Make a deep copy.
	 * 
	 * @param other
	 */
	public void deepCopy(final Graph other)
	{
		clear();
		
		for (final Vertex otherVertex : other.vertices)
			addVertex(otherVertex.pt.x(), otherVertex.pt.y(), otherVertex.pt.z());
		
		for (final Edge otherEdge : other.edges)
			findOrAddEdge(otherEdge.vertexA().id(), otherEdge.vertexB().id());  // links to vertices

		for (final Face otherFace : other.faces)
		{
			final int[] vids = new int[otherFace.vertices().size()];
			for (int v = 0; v < otherFace.vertices().size(); v++)
				vids[v] = otherFace.vertices().get(v).id();
			findOrAddFace(vids);  // links to vertices and edges, and sets edge faces
		}
		
		perimeters.clear();  // just delete old perimeters
		
		//trajectories.clear();

		// Do not assemble: instead, manually link edges and faces
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Shift all elements by the specified amount.
	 * 
	 * @param dx The x position.
	 * @param dy The y position.
	 * @param dz The z position.
	 */
	public void translate(final double dx, final double dy, final double dz)
	{
		for (final Vertex vertex : vertices)
		{
			final double xx = vertex.pt().x() + dx;
			final double yy = vertex.pt().y() + dy;
			final double zz = vertex.pt().z() + dz;
			
			vertex.pt().set(xx, yy, zz);
		}
		recalculateEdgeAndFacePositions();
	}
	
	/**
	 * Scale all elements by the specified amount.
	 * 
	 * @param sx The scale on x.
	 * @param sy The scale on y.
	 * @param sz The scale on z.
	 */
	public void scale(final double sx, final double sy, final double sz)
	{
		for (final Vertex vertex : vertices)
		{
			final double xx = vertex.pt().x() * sx;
			final double yy = vertex.pt().y() * sy;
			final double zz = vertex.pt().z() * sz;
			
			vertex.pt().set(xx, yy, zz);
		}
		recalculateEdgeAndFacePositions();
	}
	
	/**
	 * Rotate all elements by the specified amount (including edge end point
	 * tangents).
	 * 
	 * @param degrees The degrees of the rotation.
	 */
	public void rotate(final double degrees)
	{
		final double theta = Math.toRadians(degrees);

//		for (final Vertex vertex : vertices)
//			vertex.pt().set(vertex.pt());
		
		// Find pivot
		final Rectangle2D bounds = bounds();
		final double pivotX = bounds.getX() + bounds.getWidth() / 2.0;
		final double pivotY = bounds.getY() + bounds.getHeight() / 2.0;
			
//		// Pivot is centre of mass
//		double pivotX = 0;
//		double pivotY = 0;
//		for (final Vertex vertex : graph.vertices())
//		{
//			pivotX += vertex.x();
//			pivotY += vertex.y();		
//		}
//		pivotX /= graph.vertices().size();
//		pivotY /= graph.vertices().size();
		
		// Perform rotation
		for (final Vertex vertex : vertices)
		{
			// x′ = x.cosθ − y.sinθ
			// y′ = y.cosθ + x.sinθ
			final double dx = vertex.pt().x() - pivotX;
			final double dy = vertex.pt().y() - pivotY;
			
			final double xx = pivotX + dx * Math.cos(theta) - dy * Math.sin(theta);
			final double yy = pivotY + dy * Math.cos(theta) + dx * Math.sin(theta);
			
			vertex.pt().set(xx, yy);
		}
		
		// Also rotate edge end point tangents (if any)
		for (final Edge edge : edges)
		{
			if (edge.tangentA() != null)
				edge.tangentA().rotate(theta);
			if (edge.tangentB() != null)
				edge.tangentB().rotate(theta);
		}
		
		recalculateEdgeAndFacePositions();	
	}
	
	/**
	 * Skew all elements by the specified amount (including edge end point
	 * tangents).
	 * 
	 * @param amount The amount.
	 */
	public void skew(final double amount)
	{
		final Rectangle2D bounds = bounds();
		
		for (final Vertex vertex : vertices)
		{
			final double offset = (vertex.pt().y() - bounds.getMinY()) * amount;
			
			final double xx = vertex.pt().x() + offset;
			final double yy = vertex.pt().y();
			final double zz = vertex.pt().z();
			
			vertex.pt().set(xx, yy, zz);
		}
		
		// Also skew edge end point tangents (if any)
		for (final Edge edge : edges)
		{
			if (edge.tangentA() != null)
			{
				final double offset = (edge.tangentA().y() - bounds.getMinY()) * amount;
			
				final double xx = edge.tangentA().x() + offset;
				final double yy = edge.tangentA().y();
				final double zz = edge.tangentA().z();
			
				edge.tangentA().set(xx, yy, zz);
				//edge.tangentA().normalise();
			}
			
			if (edge.tangentB() != null)
			{
				final double offset = (edge.tangentB().y() - bounds.getMinY()) * amount;
			
				final double xx = edge.tangentB().x() + offset;
				final double yy = edge.tangentB().y();
				final double zz = edge.tangentB().z();
			
				edge.tangentB().set(xx, yy, zz);
				//edge.tangentB().normalise();
			}
		}

		recalculateEdgeAndFacePositions();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return True if the graph is regular.
	 */
	public boolean isRegular()
	{
		if (basis == BasisType.Square || basis == BasisType.Triangular || basis == BasisType.Hexagonal)
			return true;
		
		// Get reference basis to check
		BasisType refBasis = null;
		if (faces.size() > 0)
			refBasis = faces.get(0).basis();
		else if (edges.size() > 0)
			refBasis = edges.get(0).basis();
		else if (vertices.size() > 0)
			refBasis = vertices.get(0).basis();
		
		if (refBasis == null || refBasis == BasisType.NoBasis)
			return false;  // no basis found
			
		for (final GraphElement ge : vertices)
			if (ge.basis() != refBasis)
				return false;
		
		for (final GraphElement ge : edges)
			if (ge.basis() != refBasis)
				return false;
		
		for (final GraphElement ge : faces)
			if (ge.basis() != refBasis)
				return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param pt        The Point2D.
	 * 
	 * @return Vertex at same location within specified tolerance (e.g. 0.1).
	 */
	public Vertex findVertex(final Point2D pt)
	{
		return findVertex(pt.getX(), pt.getY(), 0);
	}

	/**
	 * @param pt        The Point3D.
	 * 
	 * @return Vertex at same location within specified tolerance (e.g. 0.1).
	 */
	public Vertex findVertex(final Point3D pt)
	{
		return findVertex(pt.x(), pt.y(), pt.z());
	}

	/**
	 * @param vertex    The vertex.
	 * 
	 * @return Vertex at same location within specified tolerance (e.g. 0.1).
	 */
	public Vertex findVertex(final Vertex vertex)
	{
		return findVertex(vertex.pt.x(), vertex.pt.y(), vertex.pt.z());
	}
		
	/**
	 * @param x         The x position.
	 * @param y         The y position.
	 * 
	 * @return Vertex at same location within specified tolerance (e.g. 0.1).
	 */
	public Vertex findVertex(final double x, final double y)
	{
		for (final Vertex vertex : vertices)
			if (vertex.coincident(x, y, 0, tolerance))
				return vertex;
		return null;
	}
		
	/**
	 * @param x         The x position.
	 * @param y         The y position.
	 * @param z         The z position.
	 * 
	 * @return Vertex at same location within specified tolerance (e.g. 0.1).
	 */
	public Vertex findVertex(final double x, final double y, final double z)
	{
		for (final Vertex vertex : vertices)
			if (vertex.coincident(x, y, z, tolerance))
				return vertex;
		return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param idA The index of the vertex A.
	 * @param idB The index of the vertex B.
	 * 
	 * @return The corresponding edge.
	 */
	public Edge findEdge(final int idA, final int idB)
	{
		for (final Edge edge : edges)
			if (edge.matches(idA, idB))
				return edge;
		return null;
	}

	/**
	 * @param idA    The index of the vertex A.
	 * @param idB    The index of the vertex B.
	 * @param curved True if the edge has to be curved.
	 * 
	 * @return The corresponding edge.
	 */
	public Edge findEdge(final int idA, final int idB, final boolean curved)
	{
		for (final Edge edge : edges)
			if (edge.matches(idA, idB, curved))
				return edge;
		return null;
	}

	/**
	 * @param vertexA The vertex A.
	 * @param vertexB The vertex b.
	 * @return The corresponding edge.
	 */
	public Edge findEdge(final Vertex vertexA, final Vertex vertexB)
	{
		return findEdge(vertexA.id(), vertexB.id());
	}

	/**
	 * @param vertexA The vertex A.
	 * @param vertexB The vertex B.
	 * @param curved  True if the edge has to be curved.
	 * 
	 * @return The corresponding edge.
	 */
	public Edge findEdge(final Vertex vertexA, final Vertex vertexB, final boolean curved)
	{
		return findEdge(vertexA.id(), vertexB.id(), curved);
	}

	/**
	 * @param ax        The x position of the vertex A.
	 * @param ay        The y position of the vertex A.
	 * @param bx        The x position of the vertex B.
	 * @param by        The y position of the vertex B.
	 * 
	 * @return The corresponding edge.
	 */
	public Edge findEdge
	(
		final double ax, final double ay,  
		final double bx, final double by
	)
	{
		final Vertex vertexA = findVertex(ax, ay);
		if (vertexA == null)
			return null;
		
		final Vertex vertexB = findVertex(bx, by);
		if (vertexB == null)
			return null;
		
		return findEdge(vertexA.id(), vertexB.id());
	}

	/**
	 * 
	 * @param ax        The x position of the vertex A.
	 * @param ay        The y position of the vertex A.
	 * @param az        The z position of the vertex A.
	 * @param bx        The x position of the vertex B.
	 * @param by        The y position of the vertex B.
	 * @param bz        The z position of the vertex B.
	 * 
	 * @return The corresponding edge.
	 */
	public Edge findEdge
	(
		final double ax, final double ay, final double az, 
		final double bx, final double by, final double bz
	)
	{
		final Vertex vertexA = findVertex(ax, ay, az);
		if (vertexA == null)
			return null;
		
		final Vertex vertexB = findVertex(bx, by, bz);
		if (vertexB == null)
			return null;
		
		return findEdge(vertexA.id(), vertexB.id());
	}

	//-------------------------------------------------------------------------

	/**
	 * @param vertIds The indices of the vertices.
	 * @return The corresponding face.
	 */
	public Face findFace(final int ... vertIds)
	{
		for (final Face face : faces)
			if (face.matches(vertIds))
				return face;
		return null;
	}

	/**
	 * @param pts       The list of all the Point3D.
	 * @return The corresponding face.
	 */
	public Face findFace(final List<Point3D> pts)
	{
		final int[] vertIds = new int[pts.size()];
		for (int v = 0; v < pts.size(); v++)
		{
			final Point3D pt = pts.get(v);
			final Vertex vertex = findVertex(pt.x(), pt.y(), pt.z());
			if (vertex == null)
				return null;
			vertIds[v] = vertex.id();
		}
		return findFace(vertIds);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param vertexA The vertex A.
	 * @param vertexB The vertex B.
	 * @return True if the edge containing these vertices exist.
	 */
	public boolean containsEdge(final Vertex vertexA, final Vertex vertexB)
	{
		return findEdge(vertexA.id(), vertexB.id()) != null;
	}

	/**
	 * @param idA The index of the vertex A.
	 * @param idB The index of the vertex B.
	 * @return True if the edge containing these vertices exist.
	 */
	public boolean containsEdge(final int idA, final int idB)
	{
		return findEdge(idA, idB) != null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param vids The indices of the vertices.
	 * @return True if the face containing these vertices exist.
	 */
	public boolean containsFace(final int ... vids)
	{
		return findFace(vids) != null;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Add a vertex.
	 * 
	 * @param vertex The vertex.
	 */
	public void addVertex(final Vertex vertex)
	{
		vertices.add(vertex);
	}
	
	/**
	 * Add an edge.
	 * 
	 * @param edge The edge.
	 */
	public void addEdge(final Edge edge)
	{
		edges.add(edge);
	}
	
	/**
	 * Add a face.
	 * 
	 * @param face The face.
	 */
	public void addFace(final Face face)
	{
		faces.add(face);
	}
	
	/**
	 * Add a face to the front of the face list.
	 * 
	 * @param face The face.
	 */
	public void addFaceToFront(final Face face)
	{
		faces.add(0, face);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param pt The point2D of the vertex centroid.
	 * @return The vertex added.
	 */
	public Vertex addVertex(final Point2D pt)
	{
		return addVertex(pt.getX(), pt.getY(), 0);
	}
	
	/**
	 * @param pt The point3D of the vertex centroid.
	 * @return The vertex added.
	 */
	public Vertex addVertex(final Point3D pt)
	{
		return addVertex(pt.x(), pt.y(), pt.z());
	}

	/**
	 * @param x The x position of the vertex.
	 * @param y The y position of the vertex.
	 * @return The vertex added.
	 */
	public Vertex addVertex(final double x, final double y)
	{
		return addVertex(x, y, 0);
	}

	/**
	 * @param x The x position of the vertex.
	 * @param y The y position of the vertex.
	 * @param z The z position of the vertex.
	 * @return The vertex added.
	 */
	public Vertex addVertex(final double x, final double y, final double z)
	{
		final Vertex newVertex = new Vertex(vertices.size(), x, y, z); 
		vertices.add(newVertex);
		return newVertex;
	}

	/**
	 * @param x         The x position of the vertex.
	 * @param y         The y position of the vertex.
	 * @return The vertex if found.
	 */
	public Vertex findOrAddVertex(final double x, final double y)
	{
		return findOrAddVertex(x, y, 0);
	}

	/**
	 * @param pt        The point2D of the vertex centroid.
	 * @return The vertex if found.
	 */
	public Vertex findOrAddVertex(final Point2D pt)
	{
		return findOrAddVertex(pt.getX(), pt.getY(), 0);
	}
	
	/**
	 * 
	 * @param x         The x position of the vertex.
	 * @param y         The y position of the vertex.
	 * @param z         The z position of the vertex.
	 * @return The vertex if found.
	 */
	public Vertex findOrAddVertex(final double x, final double y, final double z)
	{
		final Vertex existing = findVertex(x, y, z);
		if (existing != null)
			return existing;
		return addVertex(x, y, z);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Existing edge if found, otherwise new edge.
	 */
//	public Edge findOrAddEdge(final Vertex vertexA, final Vertex vertexB, final boolean curved)
//	{
//		return findOrAddEdge(vertexA.id(), vertexB.id(), curved);
//	}
	
	/**
	 * @param vertexA  The vertex A.
	 * @param vertexB  The vertex B.
	 * @param tangentA The tangent A.
	 * @param tangentB The tangent B.
	 * 
	 * @return The edge found or added.
	 */
	public Edge findOrAddEdge(final Vertex vertexA, final Vertex vertexB, final Vector tangentA, final Vector tangentB)
	{
		return findOrAddEdge(vertexA.id(), vertexB.id(), tangentA, tangentB);
	}
	
	/**
	 * @param vertexA The vertex A.
	 * @param vertexB The vertex B.
	 * 
	 * @return The edge found or added.
	 */
	public Edge findOrAddEdge(final Vertex vertexA, final Vertex vertexB)
	{
		return findOrAddEdge(vertexA.id(), vertexB.id());  //, false);
	}
	
//	public Edge findOrAddEdge(final int vertIdA, final int vertIdB)
//	{
//		return findOrAddEdge(vertIdA, vertIdB, false);
//	}
	
	/**
	 * 
	 * @param vertIdA The index of the vertex A.
	 * @param vertIdB The index of the vertex B.
	 * 
	 * @return The edge found or added.
	 */
	public Edge findOrAddEdge(final int vertIdA, final int vertIdB)  //, final boolean curved)
	{
		if (vertIdA >= vertices.size() || vertIdB >= vertices.size())
		{
			System.out.println("** Graph.addEdge(): Trying to add edge " + vertIdA + "-" + 
									vertIdB + " but only " + vertices.size() + " vertices.");
			return null;
		}
		
		for (final Edge edge : edges)
			if (edge.matches(vertIdA, vertIdB))  //, curved))
			{
				//System.out.println("Duplicate edge found.");
				return edge;  // don't add duplicate edge
			}

		return addEdge(vertIdA, vertIdB);  //, curved);
	}
	
	/**
	 * 
	 * @param vertIdA  The index of the vertex A.
	 * @param vertIdB  The index of the vertex B.
	 * @param tangentA The tangent A.
	 * @param tangentB The tangent B.
	 * 
	 * @return The edge found or added.
	 */
	public Edge findOrAddEdge
	(
		final int vertIdA, 		final int vertIdB, 
		final Vector tangentA, 	final Vector tangentB
	)
	{
		if (vertIdA >= vertices.size() || vertIdB >= vertices.size())
		{
			System.out.println("** Graph.addEdge(): Trying to add edge " + vertIdA + "-" + 
									vertIdB + " but only " + vertices.size() + " vertices.");
			return null;
		}
		
		// **
		// ** TODO: Match should include tangents? Otherwise can't distinguish 
		// **       curved edges from non-curved edges or each other.
		// **
		for (final Edge edge : edges)
			if (edge.matches(vertIdA, vertIdB))  //, tangentA, tangentB))
			{
				//System.out.println("Duplicate edge found.");
				return edge;  // don't add duplicate edge
			}

		return addEdge(vertIdA, vertIdB, tangentA, tangentB);
	}
	
	/**
	 * @param vertexA The vertex A.
	 * @param vertexB The vertex B.
	 * 
	 * @return The edge added.
	 */
	public Edge addEdge(final Vertex vertexA, final Vertex vertexB)
	{
		return addEdge(vertexA.id(), vertexB.id());  //, false);
	}
	
//	public Edge addEdge(final Vertex vertexA, final Vertex vertexB, final boolean curved)
//	{
//		return addEdge(vertexA.id(), vertexB.id(), curved);
//	}
	
//	public Edge addEdge(final int vertIdA, final int vertIdB)
//	{
//		return addEdge(vertIdA, vertIdB);  //, false);
//	}
	
	/**
	 * @param vertIdA The index of the vertex A.
	 * @param vertIdB The index of the vertex B.
	 * 
	 * @return The edge added.
	 */
	public Edge addEdge(final int vertIdA, final int vertIdB)  //, final boolean curved)
	{
		if (vertIdA >= vertices.size() || vertIdB >= vertices.size())
		{
			System.out.println("** Graph.addEdge(): Trying to add edge " + vertIdA + "-" + 
									vertIdB + " but only " + vertices.size() + " vertices.");
			return null;
		}
		
		final Vertex vertexA = vertices.get(vertIdA);
		final Vertex vertexB = vertices.get(vertIdB);
		
		final Edge newEdge = new Edge(edges.size(), vertexA, vertexB);  //, curved);
				
		edges.add(newEdge);
		
		vertexA.addEdge(newEdge);
		vertexA.sortEdges();
		
		vertexB.addEdge(newEdge);
		vertexB.sortEdges();
		
		// Set basis and shape to common basis and shape, if any
		if (vertexA.basis() == vertexB.basis())
			newEdge.setBasis(vertexA.basis());
		
		if (vertexA.shape() == vertexB.shape())
			newEdge.setShape(vertexA.shape());
		
		return newEdge;
	}

	/**
	 * @param vertexA  The vertex A.
	 * @param vertexB  The vertex B.
	 * @param tangentA The tangent A.
	 * @param tangentB The tangent B.
	 * 
	 * @return The edge added.
	 */
	public Edge addEdge(final Vertex vertexA, final Vertex vertexB, final Vector tangentA, final Vector tangentB)
	{
		return addEdge(vertexA.id(), vertexB.id(), tangentA, tangentB);
	}
	
	/**
	 * 
	 * @param vertIdA  The index of the vertex A.
	 * @param vertIdB  The index of the vertex B.
	 * @param tangentA The tangent A.
	 * @param tangentB The tangent B.
	 * 
	 * @return The edge added.
	 */
	public Edge addEdge(final int vertIdA, final int vertIdB, final Vector tangentA, final Vector tangentB)
	{
		if (vertIdA >= vertices.size() || vertIdB >= vertices.size())
		{
			System.out.println("** Graph.addEdge(): Trying to add edge " + vertIdA + "-" + 
									vertIdB + " but only " + vertices.size() + " vertices.");
			return null;
		}
		
		final Vertex vertexA = vertices.get(vertIdA);
		final Vertex vertexB = vertices.get(vertIdB);
		
		final Edge newEdge = new Edge(edges.size(), vertexA, vertexB);  //, (tangentA != null && tangentB != null));
				
		edges.add(newEdge);
		
		vertexA.addEdge(newEdge);
		vertexA.sortEdges();
		
		vertexB.addEdge(newEdge);
		vertexB.sortEdges();
		
		// Set basis and shape to common basis and shape, if any
		if (vertexA.basis() == vertexB.basis())
			newEdge.setBasis(vertexA.basis());
		
		if (vertexA.shape() == vertexB.shape())
			newEdge.setShape(vertexA.shape());
		
		newEdge.setTangentA(tangentA);
		newEdge.setTangentB(tangentB);
		
		return newEdge;
	}

	//-------------------------------------------------------------------------

	/**
	 * Creates edges between vertices that are approximately a unit distance apart.
	 */
	public void makeEdges()  //final double u)
	{
		for (final Vertex vertexA : vertices)		
			for (final Vertex vertexB : vertices)
			{
				if (vertexA.id() == vertexB.id())
					continue;
				
				final double dist = vertexA.pt().distance(vertexB.pt());
				if (Math.abs(dist - unit) < 0.05)
					findOrAddEdge(vertexA, vertexB);
			}
	}	

	//-------------------------------------------------------------------------

	/**
	 * @param vertIds The list of the indices of the vertices.
	 * @return The face added or found.
	 */
	public Face findOrAddFace(final int ... vertIds)
	{
		if (vertIds.length > vertices.size())
		{
//			System.out.println("** vertIds.length=" + vertIds.length + ", vertices.size()=" + vertices.size());
			return null;  // not a simple face
		}
		
		for (final int vid : vertIds)
			if (vid >= vertices.size())
			{
				System.out.println("** Graph.addFace(): Vertex " + vid + " specified but only " + vertices.size() + " vertices.");
				return null;
			}

		for (final Face face : faces)
			if (face.matches(vertIds))
			{
				System.out.println("** Matching face found.");
				return face;  // just return existing face
			}

		// Create the new face
		final Face newFace = new Face(faces.size());
		
		final BasisType refBasis = vertices.isEmpty() ? null : vertices.get(0).basis();
		final ShapeType refShape = vertices.isEmpty() ? null : vertices.get(0).shape();
		
		boolean allSameBasis = true;
		final boolean allSameShape = true;
		
		for (int v = 0; v < vertIds.length; v++)
		{
			final int m = vertIds[v];
			final int n = vertIds[(v + 1) % vertIds.length];
			
			final Vertex vert = vertices.get(m);
			final Vertex next = vertices.get(n);
			
			final Edge edge = findEdge(vert.id(), next.id());
			if (edge == null)
			{
				// Edge already exists. Don't add a new edge here or create 
				// faces can crash; it uses the vertices edge list but adding
				// and edge will update them => ConcurrentModificationException.
				System.out.println("** Graph.addFace(): Couldn't find edge between V" + m + " and V" + n + ".");
				return null;  // can't make this face
				// Add an edge
				//addEdge(vert.id(), next.id());
				//edge = edges.get(edges.size() - 1);
			}

			if (v > 0 && vert.basis() != refBasis)
				allSameBasis = false;

			if (v > 0 && vert.shape() != refShape)
				allSameBasis = false;

//			if (edge.right() != null)
//				System.out.println("** Graph.addFace(): Edge between V" + vm.id() + " and V" + vn.id() + " already has a right face.");

			if (edge.vertexA().id() == vert.id())
				edge.setRight(newFace);  // edge facing direction of travel
			else
				edge.setLeft(newFace);   // edge is backwards
			
			newFace.addVertexAndEdge(vert, edge);  // also updates face midpoint
		}

//		// Set tiling and shape of cell based on majority of edges
//		final int numBasisTypes = BasisType.values().length;
//		final int numShapeTypes = ShapeType.values().length;
//		
//		final int[] bases  = new int[numBasisTypes];
//		final int[] shapes = new int[numShapeTypes];
//		
//		for (final Edge edge : newFace.edges())
//		{
//			if (edge.basis() != null)
//				bases[edge.basis().ordinal()]++;
//			
//			if (edge.shape() != null)
//				shapes[edge.shape().ordinal()]++;
//		}
//		
//		// Find maximum tiling and shape type among edges 
//		int maxTilingIndex = -1;
//		int maxTilingCount =  0;
//		
//		for (int t = 0; t < numBasisTypes; t++)
//			if (bases[t] > maxTilingCount)
//			{
//				maxTilingIndex = t;
//				maxTilingCount = bases[t];
//			}
//		
//		int maxShapeIndex = -1;
//		int maxShapeCount =  0;
//		
//		for (int s = 0; s < numShapeTypes; s++)
//			if (shapes[s] > maxShapeCount)
//			{
//				maxShapeIndex = s;
//				maxShapeCount = shapes[s];
//			}
		
		// Add new face to incident vertex lists
		for (final Vertex vertex : newFace.vertices())
//			vertex.faces().add(newFace);
			vertex.addFace(newFace);
		
//		// Set maximum type, if found
//		BasisType basisM = BasisType.NoBasis;
//		ShapeType shapeM = ShapeType.NoShape;
//		
//		if (maxTilingIndex != -1)
//			basisM = BasisType.values()[maxTilingIndex];
//		
//		if (maxShapeIndex != -1)
//			shapeM = ShapeType.values()[maxShapeIndex];
		
		final BasisType basisF = (allSameBasis) ? refBasis : BasisType.NoBasis;
		final ShapeType shapeF = (allSameShape) ? refShape : ShapeType.NoShape;
		
		newFace.setTilingAndShape(basisF, shapeF);
				
		faces.add(newFace);
		
//		System.out.println("Created new face " + newFace.id() + "...");
		
		return newFace;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param pt Point to test.
	 * @return Face containing this point, else null if none.
	 */
	public Face faceContaining(final Point2D pt)
	{
		for (final Face face : faces)
			if (face.contains(pt))
				return face;
		return null;
	}

	//-------------------------------------------------------------------------
		
	/**
	 * Make the faces.
	 * 
	 * @param checkCrossings
	 */
	public void makeFaces(final boolean checkCrossings)
	{
		final int MAX_FACE_SIDES = 32;
		
		if (!faces.isEmpty())
			clearFaces();  // remove existing faces
			
		for (final Vertex vertexStart : vertices)
		{
//			System.out.println("\nStarting at vertex " + vertexStart.id() + "...");
						
			for (final Edge edgeStart : vertexStart.edges())
			{
//				System.out.println("+ Has edge " + edgeStart.id());
				
				// Look for cell containing this edge
				final TIntArrayList vertIds = new TIntArrayList();
				vertIds.add(vertexStart.id());
				
				Vertex vert = vertexStart;
				Edge   edge = edgeStart;
							
				boolean closed = false;
				while (vertIds.size() <= MAX_FACE_SIDES)
				{
//					System.out.print("Vertex " + vert.id() + " has edges:");
//					for (final Edge e : vert.edges())
//						System.out.print(" " + e.id());
//					System.out.println();
						
					final Vertex prev = vert;
					
					// Get next edge in CW order and move to next vertex
					int n = vert.edgePosition(edge);
					Vertex next = null;
					final int numEdges = vert.edges().size();
					int m;
					for (m = 1; m < numEdges; m++)
					{
						// Get next edge in CW order on the same level
						//
						// TODO: Handle 3D faces!
						//
						edge = vert.edges().get((n + m) % vert.edges().size());
						next = edge.otherVertex(vert);
					
//						System.out.println("next vert " + next.id() + ": v.z=" + next.pt().z() + ", vs.z=" + vertexStart.pt().z());
						
						if (Math.abs(next.pt().z() - vertexStart.pt().z()) < 0.0001)
						{
							// Next planar vert found
							vert = next;
							break;
						}
					}
					if (m >= numEdges)
						break;  // no next CW edge found on same level
					
//					System.out.println("+ + Vertex " + vert.id());
					
					if (vert.id() == vertexStart.id())
					{
						// Face has been closed
						closed = true;
						break;
					}
					
					// Check for self-intersection without closure
					if (vertIds.contains(vert.id()))
						break;
					
					vertIds.add(vert.id());
					
					if (checkCrossings && isEdgeCrossing(prev, vert))
						break;
				}
				
				if (closed && vertIds.size() >= 3)
				{
					// Polygon is closed and has at least three sides -- is a face
					final List<Point2D> poly = new ArrayList<Point2D>();
					for (int v = 0; v < vertIds.size(); v++)
					{
						final Vertex vertex = vertices.get(vertIds.getQuick(v));
						poly.add(new Point2D.Double(vertex.pt.x(), vertex.pt.y()));
					}
					
//					System.out.println("poly has " + poly.size() + " sides...");
					
					if (MathRoutines.clockwise(poly))
					{
//						System.out.println("Is clockwise...");
						final int[] vids = vertIds.toArray();
						if (!containsFace(vids))
						{
//							System.out.println("Not already present...");
							findOrAddFace(vids);
						}
					}
				}
			}
		}
	}

	/**
	 * Clear all existing faces.
	 */
	public void clearFaces()
	{
		for (final Edge edge : edges)
		{
			edge.setLeft(null);
			edge.setRight(null);
		}

		for (final Vertex vertex : vertices)
			vertex.clearFaces();
		
		faces.clear();
	}

	//-------------------------------------------------------------------------

	/**
	 * Remove an element from the graph.
	 * 
	 * @param element       The element.
	 * @param removeOrphans True if the orphans elements have also to be removed.
	 */
	public void remove(final GraphElement element, final boolean removeOrphans)
	{
		switch (element.siteType())
		{
		case Vertex:
			removeVertex(element.id());
			break;
		case Edge:
			removeEdge(element.id());  //, removeOrphans);
			break;
		case Cell:
			removeFace(element.id(), removeOrphans);
			break;
		default:  // do nothing
		}
	}
		
	//-------------------------------------------------------------------------

	/**
	 * Remove a vertex.
	 * 
	 * @param vertex The vertex.
	 */
	public void removeVertex(final Vertex vertex)
	{
		removeVertex(vertex.id());
	}
	
	/**
	 * Remove a vertex.
	 * 
	 * @param vid The index of the vertex.
	 */
	public void removeVertex(final int vid)
	{
//		System.out.println("Removing vertex " + vid + "...");
		
		if (vid >= vertices.size())
		{
			System.out.println("Graph.removeVertex(): Index " + vid + " out of range.");
			return;
		}
		
		final Vertex vertex = vertices.get(vid);
		
		// Mark incident faces for removal
		final BitSet facesToRemove = new BitSet();
		for (final Face face : faces)
			if (face.contains(vertex))
				facesToRemove.set(face.id(), true);
		
		// Unlink faces marked for removal from edge references
		for (final Edge edge : edges)
		{
			if (edge.left() != null && facesToRemove.get(edge.left().id()))
				edge.setLeft(null);
			
			if (edge.right() != null && facesToRemove.get(edge.right().id()))
				edge.setRight(null);
		}

		// Unlink faces marked for removal from vertex face lists
		for (final Vertex vertexF : vertices())
			for (int f = vertexF.faces().size() - 1; f >= 0; f--)
			{
				final Face face = vertexF.faces().get(f);
				if (facesToRemove.get(face.id()))
					vertexF.faces().remove(f);
			}
		
		// Remove faces marked for removal
		for (int fid = faces.size() - 1; fid >= 0; fid--)
			if (facesToRemove.get(fid))
				faces.remove(fid);
					
		// Mark incident edges for removal
		final BitSet edgesToRemove = new BitSet();
		for (final Edge edge : vertex.edges())
			edgesToRemove.set(edge.id(), true);

		// Unlink edges marked for removal from all vertex lists
		for (final Vertex vertexE : vertices())
			for (int e = vertexE.edges().size() - 1; e >= 0; e--)
			{
				final Edge edge = vertexE.edges().get(e);
				if (edgesToRemove.get(edge.id()))
					vertexE.removeEdge(e);
			}
		
		// Delete edges marked for removal
		for (int eid = edges().size() - 1; eid >= 0; eid--)
			if (edgesToRemove.get(eid))
				edges.remove(eid);

		vertices.remove(vid);  // remove vertex

		// Recalibrate indices
		for (int f = 0; f < faces.size(); f++)
			faces.get(f).setId(f);
		
		for (int e = 0; e < edges.size(); e++)
			edges.get(e).setId(e);
		
		for (int v = 0; v < vertices.size(); v++)
			vertices.get(v).setId(v);		
	}

	//-------------------------------------------------------------------------

	/**
	 * Removes edge defined by end point indices.
	 * 
	 * @param vidA The index of the vertex A.
	 * @param vidB The index of the vertex B.
	 */
	public void removeEdge(final int vidA, final int vidB)
	{
		final Edge edge = findEdge(vidA, vidB);
		if (edge != null)
			removeEdge(edge.id());
	}

	/**
	 * @param eid           Index of edge to remove.
	 */
	public void removeEdge(final int eid)  //, final boolean removeOrphans)
	{
		if (eid >= edges.size())
		{
			System.out.println("Graph.removeEdge(): Index " + eid + " out of range.");
			return;
		}

		// Remove evidence of edge from graph
		final Edge edge = edges.get(eid);
			
		// Remove any faces that use this edge
		for (int fid = faces.size() - 1; fid >= 0; fid--)
			if (faces.get(fid).contains(edge))
				removeFace(fid, false);
		
		// Remember the end point indices (ordered [min,max])
		final Vertex[] endpoints = new Vertex[2];
		endpoints[0] = (edge.vertexA().id() <= edge.vertexB().id()) ? edge.vertexA() : edge.vertexB();  
		endpoints[1] = (edge.vertexA().id() <= edge.vertexB().id()) ? edge.vertexB() : edge.vertexA();  
		
		// Remove the edge from each endpoint's list of outgoing edges
//		boolean deadVertex = false;
		for (int v = 0; v < 2; v++)
		{
			final Vertex vertex = endpoints[v];
			for (int e = vertex.edges().size() - 1; e >= 0; e--)
				if (vertex.edges().get(e).id() == edge.id())
				{
					vertex.removeEdge(e);  // remove edge from vertex's outgoing list
					
//					if (removeOrphans && vertex.edges().isEmpty())
//					{
//						vertex.setId(-1);  // potential orphan
//						deadVertex = true;
//					}
				}
		}
		
//		if (deadVertex)
//		{
//			// Remove dead vertices
//			for (int v = vertices.size() - 1; v >= 0; v--)
//			{
//				final Vertex vertex = vertices.get(v);
//				if (vertex.id() == -1)
//					vertices.remove(v);
//			}
//		
//			// Recalibrate remaining vertex indices
//			for (int v = 0; v < vertices.size(); v++)
//				vertices.get(v).setId(v);
//		}
		
		// Remove the edge and recalibrate indices. 
		// Do this *after* removing edge from vertex edge lists!
		edges.remove(eid);  
		for (int e = eid; e < edges.size(); e++)
			edges.get(e).decrementId();		
	}

	//-------------------------------------------------------------------------

	/**
	 * @param fid           The index of the face.
	 * @param removeOrphans Remove edges that are faceless after the removal. This
	 *                      is primarily for the Hole operator.
	 */
	public void removeFace(final int fid, final boolean removeOrphans)
	{
		if (fid >= faces.size())
		{
			System.out.println("Graph.removeFace(): Index " + fid + " out of range.");
			return;
		}

		final Face face = faces.get(fid);
		
		// Remove face from edges' left and right references
		final BitSet edgesToRemove = new BitSet();
		for (final Edge edge : face.edges())
		{
			// Don't call removeEdge() yet, as that will delete this face 
			if (edge.left() != null && edge.left().id() == fid)
			{
				// Unlink left reference
				edge.setLeft(null);
				if (removeOrphans && edge.right() == null)
					edgesToRemove.set(edge.id());  // edge no longer needed
			}
			
			if (edge.right() != null && edge.right().id() == fid)
			{
				// Unlink right reference
				edge.setRight(null);
				if (removeOrphans && edge.left() == null)
					edgesToRemove.set(edge.id());  // edge no longer needed
			}
		}

		// Remove face from vertex lists
		for (final Vertex vertex : face.vertices())
//		for (final Vertex vertex : vertices())
			for (int n = vertex.faces().size() - 1; n >= 0; n--)
				if (vertex.faces().get(n).id() == fid)
//					vertex.faces().remove(n);
					vertex.removeFace(n);

		// Remove this face and recalibrate indices
		faces.remove(fid);
		for (int f = fid; f < faces.size(); f++)
			faces.get(f).decrementId();
	
		// Now it's safe to remove edges
		for (int e = edges.size() - 1; e >= 0; e--)
			if (edgesToRemove.get(e))
				removeEdge(e);
		
//		System.out.println("Now " + faces.size() + " faces.");
		
//		for (final Face fc : faces)
//			System.out.println(fc.id());
	}

	//-------------------------------------------------------------------------

	/**
	 * Sets the tiling and shape type of all elements for this graph, unless they
	 * have already been set.
	 * 
	 * @param bt The basis.
	 * @param st the shape.
	 */
	public void setBasisAndShape(final BasisType bt, final ShapeType st)
	{
		for (final Vertex vertex : vertices)
		{
			if (vertex.basis() == null)
				vertex.setBasis(bt);
			if (vertex.shape() == null)
				vertex.setShape(st);
		}
		
		for (final Edge edge : edges)
		{
			if (edge.basis() == null)
				edge.setBasis(bt);
			if (edge.shape() == null)
				edge.setShape(st);
		}
		
		for (final Face face : faces)
		{
			if (face.basis() == null)
				face.setBasis(bt);
			if (face.shape() == null)
				face.setShape(st);
		}

		basis = bt;
		shape = st;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Synchronise the indices.
	 */
	public void synchroniseIds()
	{
		for (int n = 0; n < vertices.size(); n++)
			vertices.get(n).setId(n);

		for (int n = 0; n < edges.size(); n++)
			edges.get(n).setId(n);

		for (int n = 0; n < faces.size(); n++)
			faces.get(n).setId(n);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reorder the indices.
	 */
	public void reorder()
	{
		reorder(SiteType.Vertex);
		reorder(SiteType.Edge);
		reorder(SiteType.Cell);
	}
		
	/**
	 * Reorder the indices of a graph element type.
	 * 
	 * @param type The graph element type.
	 */
	public void reorder(final SiteType type)
	{
		// Give each element a score that prefers higher and righter positions
		final List<? extends GraphElement> elements = elements(type);
		
		final List<ItemScore> rank = new ArrayList<ItemScore>();
		for (int n = 0; n < elements.size(); n++)
		{
			final GraphElement ge = elements.get(n);
			final double score = ge.pt().y() * 100 + ge.pt().x();
			rank.add(new ItemScore(n, score));
		}
		Collections.sort(rank);
		
		// Append elements in new order
		for (int es = 0; es < rank.size(); es++)
		{
			final GraphElement ge = elements.get(rank.get(es).id());
			ge.setId(es);
			
			switch (type)
			{
			case Vertex: vertices.add((Vertex)ge); break;
			case Edge:	 edges.add((Edge)ge);	   break;
			case Cell:	 faces.add((Face)ge);	   break;
			default:  // do nothing   
			}
		}

		// Remove unneeded duplicate entries
		for (int es = 0; es < rank.size(); es++)
			elements.remove(0);
		
		for (int n = 0; n < elements.size(); n++)
			elements.get(n).setId(n);
	}

	//-------------------------------------------------------------------------
	
	private void setVertices(final Number[][] positions)
	{
		vertices.clear();
		
		if (positions != null)
			for (int v = 0; v < positions.length; v++)
			{
				final Number[] position = positions[v];
				if (position.length == 2)
					addVertex(position[0].floatValue(), position[1].floatValue(), 0);
				else
					addVertex(position[0].floatValue(), position[1].floatValue(), position[2].floatValue());
			}
	}

	/**
	 * Note: This method is inefficient as it checks all edges for uniqueness.
	 *       This is good to do for manually specified edge lists which can contain errors.
	 *       Automatically generated edge lists may not need this test.  
	 */
	private void setEdges(final Integer[][] pairs)
	{
		edges.clear();
		
		if (pairs != null)
			for (int e = 0; e < pairs.length; e++)
				findOrAddEdge(pairs[e][0].intValue(), pairs[e][1].intValue());
	}

	//-------------------------------------------------------------------------

	private void assemble(final boolean checkCrossings)
	{
		linkEdgesToVertices();		
		makeFaces(checkCrossings);
		linkFacesToVertices();
		
		// This will not override tiling and shape already set in elements
		setBasisAndShape(BasisType.NoBasis, ShapeType.NoShape);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Ensure that each vertex has a complete list of its edges,
	 * sorted in clockwise order.
	 */
	public void linkEdgesToVertices()
	{
		for (final Vertex vertex : vertices)
//			vertex.edges().clear();
			vertex.clearEdges();
		
		for (final Edge edge : edges)
		{
//			vertices.get(edge.vertexA().id()).edges().add(edge);
//			vertices.get(edge.vertexB().id()).edges().add(edge);
			vertices.get(edge.vertexA().id()).addEdge(edge);
			vertices.get(edge.vertexB().id()).addEdge(edge);
		}
		
		for (final Vertex vertex : vertices)
			vertex.sortEdges();
	}
	
	/**
	 * Ensure that each vertex has a complete list of its faces,
	 * sorted in clockwise order.
	 */
	public void linkFacesToVertices()
	{
		for (final Vertex vertex : vertices)
//			vertex.faces().clear();
			vertex.clearFaces();
		
		for (final Face face : faces)
			for (final Vertex vertex : face.vertices())
//				vertex.faces().add(face);
				vertex.addFace(face);
		
		for (final Vertex vertex : vertices)
			vertex.sortFaces();
	}
	

	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param vertexA The vertex A.
	 * @param vertexB The vertex B.
	 * @return True if the corresponding edge is crossing another.
	 */
	public boolean isEdgeCrossing(final Vertex vertexA, final Vertex vertexB)
	{
		final double localTolerance = 0.001;
		
		final Point2D ptA = vertexA.pt2D();
		final Point2D ptB = vertexB.pt2D();
	
		for (final Edge edge : edges)
		{
			if (edge.matches(vertexA, vertexB))
				return false;  // don't treat as crossing
		
			final Point2D ptEA = edge.vertexA().pt2D();
			final Point2D ptEB = edge.vertexB().pt2D();
		
			if 
			(
				ptA.distance(ptEA) < localTolerance || ptA.distance(ptEB) < localTolerance 
				|| 
				ptB.distance(ptEA) < localTolerance || ptB.distance(ptEB) < localTolerance
			)
				continue;
			
			if 
			(
				MathRoutines.lineSegmentsIntersect
				(
					 ptA.getX(),  ptA.getY(),  ptB.getX(),  ptB.getY(), 
					ptEA.getX(), ptEA.getY(), ptEB.getX(), ptEB.getY() 
				)
			)
				return true;
		}
		
		return false;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Trim.
	 */
	public void trim()
	{
		// Trim orphaned edges
		for (int eid = edges.size() - 1; eid >= 0; eid--)
		{
			final Edge edge = edges.get(eid);
			if (edge.vertexA().edges().size() == 1 || edge.vertexB().edges().size() == 1)
				removeEdge(eid);  // orphaned edge
		}
		
		// Detect pivots
		final BitSet pivotIds = new BitSet();
		for (final Vertex vertex : vertices)
			if (vertex.pivot() != null)
				pivotIds.set(vertex.pivot().id());

		// Trim orphaned vertices that are not pivots
		for (int vid = vertices.size() - 1; vid >= 0; vid--)
		{
			final Vertex vertex = vertices.get(vid);
			if (vertex.edges().isEmpty() && !pivotIds.get(vid))
				removeVertex(vid);  // orphaned non-pivot vertex
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Clear the properties.
	 */
	public void clearProperties()
	{
		for (final Vertex vertex : vertices)
			vertex.properties().clear();
		
		for (final Edge edge : edges)
			edge.properties().clear();
		
		for (final Face face : faces)
			face.properties().clear();
	}

	//-------------------------------------------------------------------------

	/**
	 * Compute the measures.
	 * 
	 * @param boardless True if the board is boardless.
	 */
	public void measure(final boolean boardless)
	{
		MeasureGraph.measure(this, false);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Measure of variance in edge length.
	 */
	public double distance()
	{
		if (vertices.size() < 2)
			return 0;
		
		double avg = 0;
		int found = 0;
		for (final Vertex va : vertices)
			for (final Vertex vb : vertices)
			{
				if (va.id() == vb.id())
					continue;		
				final double dist = va.pt.distance(vb.pt);
				avg += dist;
				found++;
			}
		avg /= found;
		return avg;
	}
	
	/**
	 * @return Measure of variance in edge length.
	 */
	public double variance()
	{
		if (vertices.size() < 2)
			return 0;

		final double avg = distance();
		double varn = 0;
		int found = 0;
		for (final Vertex va : vertices)
			for (final Vertex vb : vertices)
			{
				if (va.id() == vb.id())
					continue;
				found++;
				final double dist = va.pt.distance(vb.pt);
				varn += Math.abs(dist - avg);  // * (dist - avg);
			}
		varn /= found;
		return varn;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Bounding box of original vertex pairs.
	 */
	public Rectangle2D bounds()
	{
		return bounds(vertices);
	}

	/**
	 * @param elements The elements of the graph.
	 * @return Bounding box of original vertex pairs.
	 */
	public static Rectangle2D bounds(final List<? extends GraphElement> elements)
	{
		final double limit = 1000000;
		double x0 =  limit;
		double y0 =  limit;
		double x1 = -limit;
		double y1 = -limit;
		
		for (final GraphElement ge : elements)
		{
			final double x = ge.pt.x();
			final double y = ge.pt.y();
			if (x < x0)	x0 = x;
			if (x > x1)	x1 = x;
			if (y < y0)	y0 = y;
			if (y > y1)	y1 = y;
		}
		
		if (x0 == limit || y0 == limit)
			return new Rectangle2D.Double(0, 0, 0, 0);
		
		return new Rectangle2D.Double(x0, y0, x1-x0, y1-y0);
	}

	//-------------------------------------------------------------------------

	/**
	 * Normalise all vertices.
	 */
	public void normalise()
	{
		final Rectangle2D bounds = bounds(elements(SiteType.Vertex));
		
		final double maxExtent = Math.max(bounds.getWidth(), bounds.getHeight());
		if (maxExtent == 0)
		{
			System.out.println("** Normalising graph with zero bounding box.");
			return;
		}
		
		final double scale = 1.0 / maxExtent;
		
		final double offX = -bounds.getX() + (maxExtent - bounds.getWidth())  / 2.0;  // * scale;
		final double offY = -bounds.getY() + (maxExtent - bounds.getHeight()) / 2.0;  // * scale;
		
		for (final Vertex vertex : vertices)
		{
			final double xx = (vertex.pt.x() + offX) * scale;
			final double yy = (vertex.pt.y() + offY) * scale;
			final double zz = (vertex.pt.z() + offY) * scale;
			
			vertex.pt.set(xx, yy, zz);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Compute again the edges and faces positions.
	 */
	public void recalculateEdgeAndFacePositions()
	{
		for (final Edge edge : edges)
			edge.setMidpoint();
			
		for (final Face face : faces)
			face.setMidpoint();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Remove duplicate edges from the specified edge list.
	 * 
	 * @param edges the list of the edges.
	 */
	public static void removeDuplicateEdges(final List<Edge> edges)
	{
		for (int ea = 0; ea < edges.size(); ea++) 
		{
			final Edge edgeA = edges.get(ea);
			for (int eb = edges.size() - 1; eb > ea; eb--)
				if (edgeA.matches(edges.get(eb)))
				{
					// Remove this duplicate edge
					for (int ec = eb + 1; ec < edges.size(); ec++)
						edges.get(ec).decrementId();
					edges.remove(eb);
				}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Average edge length.
	 */
	public double averageEdgeLength()
	{
		if (edges.isEmpty())
			return 0;
		
		double avg = 0;
		for (final Edge edge : edges)
			avg += edge.length();
		return avg / edges.size();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Centroid of graph.
	 */
	public Point2D centroid()
	{
		if (vertices.isEmpty())
			return new Point2D.Double(0, 0);
		
		double midX = 0;
		double midY = 0;
		
		for (final Vertex vertex : vertices)
		{
			midX += vertex.pt().x();
			midY += vertex.pt().y();
		}
		
		return new Point2D.Double(midX / vertices().size(), midY / vertices().size());
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		return this;
	}

	@Override
	public long gameFlags(Game game)
	{
		return 0;
	}

	@Override
	public void preprocess(Game game)
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		if (vertices.isEmpty())
			return "Graph has no vertices.";
		
		sb.append("Graph basis: " + basis + "\n");
		sb.append("Graph shape: " + shape + "\n");
		
		sb.append("Graph is " + (isRegular() ? "" : "not ") + "regular.\n");
		
		sb.append("Vertices:\n");	
		for (final Vertex vertex : vertices)
			sb.append("- V: " + vertex.toString() + "\n");
		
		if (edges.isEmpty())
		{
			sb.append("No edges.");
		}
		else
		{
			sb.append("Edges:\n");	
			for (final Edge edge : edges)
				sb.append("- E: " + edge.toString() + "\n");
		}
		
		if (faces.isEmpty())
		{
			sb.append("No faces.");
		}
		else
		{
			sb.append("Faces:\n");	
			for (final Face face : faces)
				sb.append("- F: " + face.toString() + "\n");
		}
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------

}
