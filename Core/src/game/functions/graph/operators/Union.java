package game.functions.graph.operators;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Face;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.MathRoutines;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Returns the union of two or more graphs.
 * 
 * @author cambolbro
 * 
 * @remarks The graphs are simply combined with each other, with no connection between them.
 */
public final class Union extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * A pair of pivots.
	 */
	public class PivotPair
	{
		/**
		 * The id of the pair.
		 */
		public int id;

		/**
		 * The id of the pivot.
		 */
		public int pivotId;
			
		/**
		 * Constructor.
		 * 
		 * @param id      The id of the pair.
		 * @param pivotId The id of the pivot.
		 */
		public PivotPair(final int id, final int pivotId)
		{
			this.id = id;
			this.pivotId = pivotId;
		}
	}
	
	//-------------------------------------------------------------------------
	
	private final GraphFunction[] graphFns;

	private final boolean connect;

	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * For making the union of two graphs.
	 * 
	 * @param graphA  First graph to combine.
	 * @param graphB  Second graph to combine.
	 * @param connect Whether to connect newly added vertices to nearby neighbours
	 *                [False].
	 * 
	 * @example (union (square 5) (square 3))
	 */
	public Union
	(
			       final GraphFunction graphA,
			       final GraphFunction graphB,
		@Name @Opt final Boolean       connect
	) 
	{
		this.graphFns = new GraphFunction[2];
		this.graphFns[0] = graphA;
		this.graphFns[1] = graphB;
		this.connect = (connect == null) ? false : connect.booleanValue();
	}

	/**
	 * For making the union of many graphs.
	 * 
	 * @param graphs  Graphs to merge.
	 * @param connect Whether to connect newly added vertices to nearby neighbours
	 *                [False].
	 * 
	 * @example (union { (rectangle 6 2) (square 4) (rectangle 7 2) })
	 */
	public Union
	(
			      final GraphFunction[] graphs,
	   @Name @Opt final Boolean         connect
	) 
	{
		this.graphFns = graphs;
		this.connect = (connect == null) ? false : connect.booleanValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph[] graphs = new Graph[graphFns.length];
		for (int n = 0; n < graphFns.length; n++)
		{
			final GraphFunction fn = graphFns[n];
			graphs[n] = fn.eval(context, siteType);
			
			//System.out.println("Graph " + n + ":\n" + graphs[n]);
		}
		
		// Note: Retain the tiling and shape of the original elements.
		
//		// Collate vertices and edges
//		final List<Vertex> vertices = new ArrayList<Vertex>();
//		final List<Edge>   edges    = new ArrayList<Edge>();
//		
//		int offset = 0;
//		
//		// Store pivot pairs to restore afterwards
//		final List<PivotPair> pivots = new ArrayList<PivotPair>();
//		
//		for (final Graph graph : graphs)		
//		{
//			for (final Vertex vertex : graph.vertices())
//			{
//				final Vertex newVertex = new Vertex(vertices.size(), vertex.pt().x(), vertex.pt().y(), vertex.pt().z());
//				newVertex.setTilingAndShape(vertex.basis(), vertex.shape());
//				if (vertex.pivot() != null)
//					pivots.add(new PivotPair(vertices.size(), vertex.pivot().id() + offset));
//				vertices.add(newVertex);
//			}
//		
//			for (final Edge edge : graph.edges())
//			{
//				final Vertex va = vertices.get(edge.vertexA().id() + offset);
//				final Vertex vb = vertices.get(edge.vertexB().id() + offset);
//				final Edge newEdge = new Edge(edges.size(), va, vb);  //, edge.curved());
//				newEdge.setTilingAndShape(edge.basis(), edge.shape());
//				edges.add(newEdge);
//			}
//
//			offset += graph.vertices().size();
//		}
//
//		// Restore pivot vertices
//		for (final PivotPair pivot : pivots)
//			vertices.get(pivot.id).setPivot(vertices.get(pivot.pivotId));
//
//		final Graph graph = new Graph(vertices, edges);
//		
////		// Accumulate graphs together
////		final Graph graph = graphs[0];
////		for (int g = 1; g < graphs.length; g++)
////		{
////			graph.vertices().addAll(graphs[g].vertices());
////			graph.edges().addAll(graphs[g].edges());
////			graph.faces().addAll(graphs[g].faces());
////			
//////			for (final Vertex vertex : graphs[g].vertices())
//////				graph.vertices().add(vertex);
//////			
//////			for (final Edge edge : graphs[g].edges())
//////				graph.edges().add(edge);
//////			
//////			for (final Face face : graphs[g].faces())
//////				graph.faces().add(face);
////		}
////		graph.reorder();
////		
////		System.out.println("Graph has " + graph.vertices().size() + " vertices.");
//
//		// Now connect new vertices to nearby neighbours, if desired
//		if (connect)
//		{
//			final double threshold = 1.1 * graph.averageEdgeLength();
//			
//			for (final Vertex vertexA : graph.vertices())
//				for (final Vertex vertexB : graph.vertices())
//				{
//					if (vertexA.id() == vertexB.id())
//						continue;
//					
//					if (MathRoutines.distance(vertexA.pt2D(), vertexB.pt2D()) < threshold)
//						graph.findOrAddEdge(vertexA.id(), vertexB.id());
//				}
//			graph.makeFaces(true);
//			
//			// **
//			// ** No not reorder here! This breaks some graphs e.g. Mancala games.
//			// **
//			//graph.reorder();
//		}
//
//		graph.resetBasis();
//		graph.resetShape();
//		
//		//System.out.println(graph);
//		
//		return graph;
		
		for (int n = 1; n < graphFns.length; n++)
		{
			for (final Vertex vertex : graphs[n].vertices())
				graphs[0].addVertex(vertex);
			
			for (final Edge edge : graphs[n].edges())
				graphs[0].addEdge(edge);
			
			for (final Face face : graphs[n].faces())
				graphs[0].addFace(face);
			
			graphs[0].synchroniseIds();
		}
		
		// Now connect new vertices to nearby neighbours, if desired
		if (connect)
		{
			final double threshold = 1.1 * graphs[0].averageEdgeLength();
			
			for (final Vertex vertexA : graphs[0].vertices())
				for (final Vertex vertexB : graphs[0].vertices())
				{
					if (vertexA.id() == vertexB.id())
						continue;
					
					if (MathRoutines.distance(vertexA.pt2D(), vertexB.pt2D()) < threshold)
						graphs[0].findOrAddEdge(vertexA.id(), vertexB.id());
				}
			graphs[0].makeFaces(true);
			
			// **
			// ** No not reorder here! This breaks some graphs e.g. Mancala games.
			// **
			//graph.reorder();
		}
		
		graphs[0].resetBasis();
		graphs[0].resetShape();
		
		//System.out.println(graphs[0]);
		
		return graphs[0];
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		for (final GraphFunction fn : graphFns)
			if (!fn.isStatic())
				return false;
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
		
		for (final GraphFunction fn : graphFns)
			flags |= fn.gameFlags(game);

		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
//		type = SiteType.use(type, game);

		for (final GraphFunction fn : graphFns)
			fn.preprocess(game);

		if (isStatic())
			precomputedGraph = eval(new Context(game, null),
					(game.board().defaultSite() == SiteType.Vertex ? SiteType.Vertex : SiteType.Cell));
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		// Commented because if some modifications are done to the graph we can not
		// conclude about the tiling or the shape.
		// for (final GraphFunction fn : graphFns)
		// concepts.or(fn.concepts(game));
		return concepts;
	}
}
