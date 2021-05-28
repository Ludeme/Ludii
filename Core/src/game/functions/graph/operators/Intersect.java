package game.functions.graph.operators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Returns the intersection of two or more graphs.
 * 
 * @author cambolbro
 * 
 * @remarks The intersection of two or more graphs is composed of the vertices 
 *          and edges that occur in all of those graphs.
 */
public final class Intersect extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final GraphFunction[] graphFns;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * For making the intersection of two graphs.
	 * 
	 * @param graphA First graph to intersect.
	 * @param graphB Second graph to intersect.
	 * 
	 * @example (intersect (square 4) (rectangle 2 5))
	 */
	public Intersect
	(
		final GraphFunction graphA,
		final GraphFunction graphB
	) 
	{
		this.graphFns = new GraphFunction[2];
		this.graphFns[0] = graphA;
		this.graphFns[1] = graphB;
	}

	/**
	 * For making the intersection of many graphs.
	 * 
	 * @param graphs Graphs to intersect.
	 * 
	 * @example (intersect { (rectangle 6 2) (square 4) (rectangle 7 2) })
	 */
	public Intersect
	(
		final GraphFunction[] graphs
	) 
	{
		this.graphFns = graphs;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final int numGraphs = graphFns.length;
		
		final Graph[] graphs = new Graph[numGraphs];
		for (int n = 0; n < numGraphs; n++)
		{
			final GraphFunction fn = graphFns[n];
			graphs[n] = fn.eval(context, siteType);
			
//			System.out.println(graphs[n]);
		}
		
		// Note: Retain the tiling and shape of the original elements.
		
		if (numGraphs == 1)
			return graphs[0];  // nothing to intersect with
		
		// Create reference lists of vertices and edges based on graphs[0]
		final List<Vertex> vertices = new ArrayList<Vertex>();
		final List<Edge>   edges    = new ArrayList<Edge>();
		
		for (final Vertex vertex : graphs[0].vertices())
		{
			final Vertex newVertex = new Vertex(vertices.size(), vertex.pt());
			newVertex.setTilingAndShape(vertex.basis(), vertex.shape());
			vertices.add(newVertex);
		}
		
		for (final Edge edge : graphs[0].edges())
		{
			final Vertex va = vertices.get(edge.vertexA().id());
			final Vertex vb = vertices.get(edge.vertexB().id());
			final Edge newEdge = new Edge(edges.size(), va, vb);  //, edge.curved());
			newEdge.setTilingAndShape(edge.basis(), edge.shape());
			edges.add(newEdge);
		}
		
		// Remove edges that do not occur in all subsequent graphs
		for (int e = edges.size() - 1; e >= 0; e--)
		{
			final Edge edge = edges.get(e);
			
			boolean foundInAll = true;
			for (int g = 1; g < numGraphs; g++)
			{
				boolean found = false;
				for (final Edge edgeG : graphs[g].edges())
					if (edge.coincidentVertices(edgeG, 0.01))  //tolerance))
					{
						found = true;
						break;
					}
				
				if (!found)
				{
					foundInAll = false;
					break;
				}
			}
			
			if (!foundInAll)
				edges.remove(e);  // no match in this graph, remove it
		}
		
		// Remove vertices that do not occur in all subsequent graphs
		for (int v = vertices.size() - 1; v >= 0; v--)
		{
			final Vertex vertex = vertices.get(v);
			
			boolean foundInAll = true;
			for (int g = 1; g < numGraphs; g++)
			{
				boolean found = false;
				for (final Vertex vertexG : graphs[g].vertices())
					if (vertex.coincident(vertexG, 0.01))  //tolerance))
					{
						found = true;
						break;
					}
				
				if (!found)
				{
					foundInAll = false;
					break;
				}
			}
			
			if (!foundInAll)
				vertices.remove(v);  // no match in this graph, remove it
		}
		
		// Recalibrate indices
		for (int v = 0; v < vertices.size(); v++)
			vertices.get(v).setId(v);

		for (int e = 0; e < edges.size(); e++)
			edges.get(e).setId(e);
		
		final Graph graph = new Graph(vertices, edges);
		
		graph.resetBasis();		
		graph.resetShape();
		
		//System.out.println(graph);

		return graph;
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
			precomputedGraph = eval
			(
				new Context(game, null),
				(game.board().defaultSite() == SiteType.Vertex ? SiteType.Vertex : SiteType.Cell)
			);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		// Commented because if some modifications are done to the graph we can not
		// conclude about the tiling
		// for (final GraphFunction fn : graphFns)
		// concepts.or(fn.concepts(game));
		return concepts;
	}
}
