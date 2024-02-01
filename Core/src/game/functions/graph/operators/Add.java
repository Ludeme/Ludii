package game.functions.graph.operators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.floats.FloatFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.graph.Edge;
import game.util.graph.Graph;
import game.util.graph.GraphElement;
import game.util.graph.Vertex;
import main.math.MathRoutines;
import main.math.Point3D;
import main.math.Vector;
import other.context.Context;

/**
 * Adds elements to a graph.
 * 
 * @author cambolbro
 * 
 * @remarks The elements to be added can be vertices, edges or faces.
 *          Edges and faces will create the specified vertices if they don't already exist.
 *          When defining curved edges, the first and second set of numbers are the 
 *          end points locations and the third and fourth set of numbers are the
 *          tangent directions for the edge end points. 
 */
public final class Add extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final GraphFunction graphFn;

	private final FloatFunction[][]   vertexFns;
	private final FloatFunction[][][] edgeFns;
	private final FloatFunction[][][] edgeCurvedFns;
	private final FloatFunction[][][] faceFns;

	private final DimFunction[][] edgeIndexFns;
	private final DimFunction[][] faceIndexFns;
	
	private final boolean connect;

	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param graph    The graph to remove elements from.
	 * @param vertices Locations of vertices to add.
	 * @param edges    Locations of end points of edges to add.
	 * @param Edges    Indices of end point vertices to add.
	 * @param edgesCurved Locations of end points and tangents of edges to add.
	 * @param cells    Locations of vertices of faces to add.
	 * @param Cells    Indices of vertices of faces to add.
	 * @param connect  Whether to connect newly added vertices to nearby neighbours [False].
	 * 
	 * @example (add (square 4) vertices:{ {1 2} })
	 * @example (add edges:{ {{0 0} {1 1}} })
	 * @example (add (square 4) cells:{ {{1 1} {1 2} {3 2} {3 1}} })
	 * @example (add (square 2) edgesCurved:{ {{0 0} {1 0} {1  2} {-1  2}} }) 
 	 */
	public Add
	(
		@Opt		   final GraphFunction       graph,
		@Opt 	 @Name final FloatFunction[][]   vertices,
		@Opt @Or @Name final FloatFunction[][][] edges,
			@Opt @Or @Name final DimFunction[][] Edges,
		@Opt   	 @Name final FloatFunction[][][] edgesCurved,
		@Opt @Or @Name final FloatFunction[][][] cells,
			@Opt @Or @Name final DimFunction[][] Cells,
		@Opt     @Name final Boolean             connect
	)
	{
		int numNonNullE = 0;
		if (edges != null)
			numNonNullE++;
		if (Edges != null)
			numNonNullE++;
		
		if (numNonNullE > 1)
			throw new IllegalArgumentException("Only one 'edge' parameter can be non-null.");

		int numNonNullF = 0;
		if (cells != null)
			numNonNullF++;
		if (Cells != null)
			numNonNullF++;
		
		if (numNonNullF > 1)
			throw new IllegalArgumentException("Only one 'face' parameter can be non-null.");

		graphFn   = graph;
		
		vertexFns = vertices;
		edgeFns   = edges;
		faceFns   = cells;

		edgeCurvedFns = edgesCurved;
		
		edgeIndexFns = Edges;
		faceIndexFns = Cells;
		
		this.connect = (connect == null) ? false : connect.booleanValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph graph = (graphFn == null) ? new Graph() : graphFn.eval(context, siteType);
				
		// Note: Retain the tiling and shape of the original elements.
		
		// Load specified vertices
		final List<Point3D> vertices = new ArrayList<Point3D>();
		if (vertexFns != null)
		{
			for (int v = 0; v < vertexFns.length; v++)
			{
				final FloatFunction[] fns = vertexFns[v];		
				
				if (fns.length < 2)
				{
					System.out.println("** Add.eval(): Two or three values expected for vertex " + v + ".");
					continue;
				}
				
				final double x = fns[0].eval(context);
				final double y = fns[1].eval(context);
				final double z = (fns.length > 2) ? fns[2].eval(context) : 0;
				
				vertices.add(new Point3D(x, y, z));	
			}
		}
		
		// Load specified edges
		final List<List<Point3D>> edges = new ArrayList<List<Point3D>>();
		if (edgeFns != null)
		{
			for (int e = 0; e < edgeFns.length; e++)
			{
				final FloatFunction[][] fns = edgeFns[e];
						
				if (fns.length != 2)
				{
					System.out.println("** Add.eval(): Two vertex definitions expected for edge " + e + ".");
					continue;
				}

				if (fns[0].length < 2)
				{
					System.out.println("** Add.eval(): Two values expected for vertex A for edge " + e + ".");
					continue;
				}
				
				if (fns[1].length < 2)
				{
					System.out.println("** Add.eval(): Two values expected for vertex B for edge " + e + ".");
					continue;
				}
				
				final double ax = fns[0][0].eval(context);
				final double ay = fns[0][1].eval(context);
				final double az = (fns[0].length > 2) ? fns[0][2].eval(context) : 0;
				
				final double bx = fns[1][0].eval(context);
				final double by = fns[1][1].eval(context);
				final double bz = (fns[1].length > 2) ? fns[1][2].eval(context) : 0;
				
				final List<Point3D> vertexPair = new ArrayList<Point3D>();
				vertexPair.add(new Point3D(ax, ay, az));
				vertexPair.add(new Point3D(bx, by, bz));
				
				edges.add(vertexPair);	
			}
		}
		
		// Load specified curved edges
		if (edgeCurvedFns != null)
		{
			for (int e = 0; e < edgeCurvedFns.length; e++)
			{
				final FloatFunction[][] fns = edgeCurvedFns[e];
						
				if (fns.length != 4)
				{
					System.out.println("** Add.eval(): Four points expected for curved edge " + e + ".");
					continue;
				}

				if (fns[0].length < 2)
				{
					System.out.println("** Add.eval(): Two values expected for vertex A for edge " + e + ".");
					continue;
				}
				
				if (fns[1].length < 2)
				{
					System.out.println("** Add.eval(): Two or three values expected for vertex B for edge " + e + ".");
					continue;
				}
			
				if (fns[2].length != 2)
				{
					System.out.println("** Add.eval(): Two or three values expected for tangent A for edge " + e + ".");
					continue;
				}
				
				if (fns[3].length != 2)
				{
					System.out.println("** Add.eval(): Two values expected for tangent B for edge " + e + ".");
					continue;
				}
				
				final double ax = fns[0][0].eval(context);
				final double ay = fns[0][1].eval(context);
				final double az = (fns[0].length > 2) ? fns[0][2].eval(context) : 0;
				
				final double bx = fns[1][0].eval(context);
				final double by = fns[1][1].eval(context);
				final double bz = (fns[1].length > 2) ? fns[1][2].eval(context) : 0;

				final double tax = fns[2][0].eval(context);
				final double tay = fns[2][1].eval(context);

				final double tbx = fns[3][0].eval(context);
				final double tby = fns[3][1].eval(context);

				final Vector tangentA = new Vector(tax, tay);
				final Vector tangentB = new Vector(tbx, tby);
				
				// Don't normalise vectors! Allow arbitrary lengths for better control
				//tangentA.normalise();
				//tangentB.normalise();

				final Vertex vertexA = graph.findOrAddVertex(ax, ay, az);
				final Vertex vertexB = graph.findOrAddVertex(bx, by, bz);
				
				graph.addEdge(vertexA, vertexB, tangentA, tangentB);
			}
		}
		
		// Load specified faces
		final List<List<Point3D>> faces = new ArrayList<List<Point3D>>();
		if (faceFns != null)
		{
			for (int f = 0; f < faceFns.length; f++)
			{
				final FloatFunction[][] fns = faceFns[f];
			
//				if (fns.length < 6)
//				{
//					System.out.println("** Add.eval(): At least six values expected for face " + f + ".");
//				}
				
				final List<Point3D> face = new ArrayList<Point3D>();
				for (int v = 0; v < fns.length; v++)
				{
					final double x = fns[v][0].eval(context);
					final double y = fns[v][1].eval(context);
					final double z = (fns[v].length > 2) ? fns[v][2].eval(context) : 0;
				
					face.add(new Point3D(x, y, z));
				}
				faces.add(face);
			}
		}
		
		// Add vertices
		final List<Vertex> newVertices = new ArrayList<Vertex>();
		for (final Point3D pt : vertices)
		{
			final Vertex vertex = graph.findVertex(pt);
			if (vertex != null)
			{
				System.out.println("** Duplicate vertex found - not adding.");
			}
			else
			{
				newVertices.add(graph.addVertex(pt));
			}
		}
		
		// Add edges
		for (final List<Point3D> edgePts : edges)
		{
			final Point3D ptA = edgePts.get(0);			
			final Point3D ptB = edgePts.get(1);			
			
			// Find or create first vertex
			Vertex vertexA = graph.findVertex(ptA);
			if (vertexA == null)
			{
				graph.addVertex(ptA);
				vertexA = graph.vertices().get(graph.vertices().size() - 1);
			}
			
			// Find or create second vertex
			Vertex vertexB = graph.findVertex(ptB);
			if (vertexB == null)
			{
				graph.addVertex(ptB);
				vertexB = graph.vertices().get(graph.vertices().size() - 1);
			}
			
			graph.findOrAddEdge(vertexA.id(), vertexB.id());
		}
		
		// Add faces
		for (final List<Point3D> facePts : faces)
		{
			// Determine vertex ids
			final int[] vertIds = new int[facePts.size()];
			
			// Pass 1: Create vertices if needed
			for (int n = 0; n < facePts.size(); n++)
			{
				// Find or create vertex
				final Point3D pt = facePts.get(n);
				Vertex vertex = graph.findVertex(pt);
				if (vertex == null)
				{
					graph.addVertex(pt);
					vertex = graph.vertices().get(graph.vertices().size() - 1);
				}
				vertIds[n] = vertex.id();
			}

			// Pass 2: Create edges if needed
			for (int n = 0; n < vertIds.length; n++)
			{
				final int vidA = vertIds[n];
				final int vidB = vertIds[(n + 1) % vertIds.length];
				
				final Edge edge = graph.findEdge(vidA, vidB);
				if (edge == null)
					graph.findOrAddEdge(vidA, vidB);
			}
			
			graph.findOrAddFace(vertIds);
		}
		
		// Now check for edge and face additions by index, after graph has been created
		
		if (edgeIndexFns != null)
		{
			// Add edges by vertex index pairs 
			for (int e = 0; e < edgeIndexFns.length; e++)
			{
				if (edgeIndexFns[e].length < 2)
				{
					System.out.println("** Add.eval(): Edge index pair does not have two entries.");
					continue;
				}
				
				final int aid = edgeIndexFns[e][0].eval();
				final int bid = edgeIndexFns[e][1].eval();
				
				if (aid >= graph.vertices().size() || bid >= graph.vertices().size())
				{
					System.out.println("** Add.eval(): Invalid edge vertex index in " + aid + " or " + bid + ".");
					continue;
				}
								
				graph.findOrAddEdge(aid, bid);
			}
		}
		
		if (faceIndexFns != null)
		{
			// Add faces by vertex index 
			for (int f = 0; f < faceIndexFns.length; f++)
			{
				if (faceIndexFns[f].length < 3)
				{
					System.out.println("** Add.eval(): Face index list must have at least three entries.");
					continue;
				}
				
				final int[] vertIds = new int[faceIndexFns[f].length];
				
				int n;
				for (n = 0; n < faceIndexFns[f].length; n++)
				{
					vertIds[n] = faceIndexFns[f][n].eval();
					if (vertIds[n] >= graph.vertices().size())
					{
						System.out.println("** Add.eval(): Invalid face vertex index " + vertIds[n] + ".");
						break;
					}
				}
				
				if (n < faceIndexFns[f].length)
					continue;  // couldn't load this face
				
				graph.findOrAddFace(vertIds);
			}
		}

		// Now connect new vertices to nearby neighbours, if desired
		if (connect)
		{
			final double threshold = 1.1 * graph.averageEdgeLength();
			
			for (final GraphElement vertexA : newVertices)
				for (final GraphElement vertexB : graph.elements(SiteType.Vertex))
				{
					if (vertexA.id() == vertexB.id())
						continue;
					
					if (MathRoutines.distance(vertexA.pt2D(), vertexB.pt2D()) < threshold)
						graph.findOrAddEdge(vertexA.id(), vertexB.id());
				}
			//graph.createFaces();
		}

		graph.resetBasis();
		graph.resetShape();
		
		//System.out.println(graph);
		
		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		boolean isStatic = true;
		
		for (int v = 0; v < vertexFns.length; v++)
			for (int n = 0; n < vertexFns[v].length; n++)
				isStatic = isStatic && vertexFns[v][n].isStatic();
		
		for (int e = 0; e < edgeFns.length; e++)
			for (int v = 0; v < edgeFns[e].length; v++)
				for (int n = 0; n < edgeFns[e][v].length; n++)
					isStatic = isStatic && edgeFns[e][v][n].isStatic();
			
		for (int f = 0; f < faceFns.length; f++)
			for (int v = 0; v < faceFns[f].length; v++)
				for (int n = 0; n < faceFns[f][v].length; n++)
					isStatic = isStatic && faceFns[f][v][n].isStatic();

		return isStatic;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
		
		for (int v = 0; v < vertexFns.length; v++)
			for (int n = 0; n < vertexFns[v].length; n++)
				flags |= vertexFns[v][n].gameFlags(game);
		
		for (int e = 0; e < edgeFns.length; e++)
			for (int v = 0; v < edgeFns[e].length; v++)
				for (int n = 0; n < edgeFns[e][v].length; n++)
					flags |= edgeFns[e][v][n].gameFlags(game);
			
		for (int f = 0; f < faceFns.length; f++)
			for (int v = 0; v < faceFns[f].length; v++)
				for (int n = 0; n < faceFns[f][v].length; n++)
					flags |= faceFns[f][v][n].gameFlags(game);

		if (game.board().defaultSite() != SiteType.Cell)
			flags |= GameType.Graph;
		
		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		for (int v = 0; v < vertexFns.length; v++)
			for (int n = 0; n < vertexFns[v].length; n++)
				vertexFns[v][n].preprocess(game);
		
		for (int e = 0; e < edgeFns.length; e++)
			for (int v = 0; v < edgeFns[e].length; v++)
				for (int n = 0; n < edgeFns[e][v].length; n++)
					edgeFns[e][v][n].preprocess(game);
			
		for (int f = 0; f < faceFns.length; f++)
			for (int v = 0; v < faceFns[f].length; v++)
				for (int n = 0; n < faceFns[f][v].length; n++)
					faceFns[f][v][n].preprocess(game);

		if (isStatic())
			precomputedGraph = eval(new Context(game, null),
					(game.board().defaultSite() == SiteType.Vertex ? SiteType.Vertex : SiteType.Cell));
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(graphFn.concepts(game));
		return concepts;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "with additional elements added";
	}
}
