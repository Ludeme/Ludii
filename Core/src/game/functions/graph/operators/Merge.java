package game.functions.graph.operators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.MathRoutines;
import main.math.Vector;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Returns the result of merging two or more graphs.
 * 
 * @author cambolbro
 * 
 * @remarks The graphs are overlaid with each other, such that incident vertices 
 *          (i.e. those with the same location) are merged into a single vertex.
 */
public final class Merge extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * A pivot pair.
	 */
	public class PivotPair
	{
		/**
		 * The index.
		 */
		public int id;

		/**
		 * The index of the pivot.
		 */
		public int pivotId;
			
		/**
		 * @param id      the index.
		 * @param pivotId The index of the pivot.
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
	 * For making the merge of two graphs.
	 * 
	 * @param graphA  First graph to merge.
	 * @param graphB  Second graph to merge.
	 * @param connect Whether to connect newly added vertices to nearby neighbours
	 *                [False].
	 * 
	 * @example (merge (rectangle 6 2) (rectangle 3 5))
	 */
	public Merge
	(
			 	   final GraphFunction graphA,
			 	   final GraphFunction graphB,
		@Opt @Name final Boolean       connect
	) 
	{
		this.graphFns = new GraphFunction[2];
		this.graphFns[0] = graphA;
		this.graphFns[1] = graphB;
		this.connect = (connect == null) ? false : connect.booleanValue();
	}

	/**
	 * For making the merge of many graphs.
	 * 
	 * @param graphs  Graphs to merge.
	 * @param connect Whether to connect newly added vertices to nearby neighbours
	 *                [False].
	 * 
	 * @example (merge { (rectangle 6 2) (square 4) (rectangle 7 2) })
	 */
	public Merge
	(
				   final GraphFunction[] graphs,
		@Opt @Name final Boolean         connect
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
		}
		
		// Note: Retain the tiling and shape of the original elements.
		
		// Collate vertices and edges 
		final List<Vertex> vertices = new ArrayList<Vertex>();
		final List<Edge>   edges    = new ArrayList<Edge>();
		
		int offset = 0;

		for (final Graph subGraph : graphs)		
		{
			for (final Vertex vertex : subGraph.vertices())
			{
				final Vertex newVertex = new Vertex(vertices.size(), vertex.pt().x(), vertex.pt().y(), vertex.pt().z());
				newVertex.setTilingAndShape(vertex.basis(), vertex.shape());
				if (vertex.pivot() != null)
				{
					// **
					// ** FIXME: Will be reference to old pivot, not new one.
					// **
					newVertex.setPivot(vertex.pivot());
				}
				vertices.add(newVertex);
			}
		
			for (final Edge edge : subGraph.edges())
			{
				final Vertex va = vertices.get(edge.vertexA().id() + offset);
				final Vertex vb = vertices.get(edge.vertexB().id() + offset);
				final Edge newEdge = new Edge(edges.size(), va, vb);  //, edge.curved());
				newEdge.setTilingAndShape(edge.basis(), edge.shape());
				if (edge.tangentA() != null)
					newEdge.setTangentA(new Vector(edge.tangentA()));
				if (edge.tangentB() != null)
					newEdge.setTangentB(new Vector(edge.tangentB()));
				edges.add(newEdge);
			}

			offset += subGraph.vertices().size();
		}

		// Merge incident vertices
		mergeVertices(vertices, edges);
		game.util.graph.Graph.removeDuplicateEdges(edges);
		
		final Graph graph = new Graph(vertices, edges);
		
		// Now connect new vertices to nearby neighbours, if desired
		if (connect)
		{
			final double threshold = 1.1 * graph.averageEdgeLength();
			
			for (final Vertex vertexA : graph.vertices())
				for (final Vertex vertexB : graph.vertices())
				{
					if (vertexA.id() == vertexB.id())
						continue;
					
					if (MathRoutines.distance(vertexA.pt2D(), vertexB.pt2D()) < threshold)
						graph.findOrAddEdge(vertexA.id(), vertexB.id());
				}
			graph.makeFaces(true);
		}

		graph.resetBasis();
		graph.resetShape();
		
		//System.out.println(graph);
		
//		System.out.println("Merge has " + graph.vertices().size() + " vertices and " + 
//							graph.edges().size() + " edges.");
		
		return graph;
	}

	//-------------------------------------------------------------------------

	/**
	 * Merge vertex vv into vertex v.
	 */
	private static void mergeVertices(final List<Vertex> vertices, final List<Edge> edges)
	{
		for (int v = 0; v < vertices.size(); v++)
		{
			final Vertex base = vertices.get(v);
			
			for (int vv = vertices.size()-1; vv > v; vv--)
			{
				final Vertex other = vertices.get(vv);
				if (base.coincident(other, 0.01))  // use more tolerance than usual
					mergeVertices(vertices, edges, v, vv);
			}
		}
	}
	
	/**
	 * Merge vertex vv into vertex v.
	 */
	private static void mergeVertices
	(
		final List<Vertex> vertices, final List<Edge> edges, 
		final int vid, final int coincidentId
	)
	{
		final Vertex survivor = vertices.get(vid);
		
		// Redirect all references to coincident vertex as pivot
		for (final Vertex vertex : vertices)
			if (vertex.pivot() != null && vertex.pivot().id() == coincidentId)
				vertex.setPivot(survivor);  // redirect to surviving vertex
	
		// Redirect edge references to coincident to vertex
		for (final Edge edge : edges)
		{
			if (edge.vertexA().id() == coincidentId)
				edge.setVertexA(survivor);
			if (edge.vertexB().id() == coincidentId)
				edge.setVertexB(survivor);
		}
		
		// Renumber vertices above coincidentId and remove coincident vertex
		for (int n = coincidentId + 1; n < vertices.size(); n++)
		{
			final Vertex vertexN = vertices.get(n);
			vertexN.setId(vertexN.id() - 1);
		}	
		
		vertices.remove(coincidentId);
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
		// conclude about the tiling or shape.
		// for (final GraphFunction fn : graphFns)
		// concepts.or(fn.concepts(game));
		return concepts;
	}
}
