package game.functions.graph.operators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Graph;
import game.util.graph.Poly;
import game.util.graph.Vertex;
import main.math.Polygon;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Keeps a specified shape within a graph and discards the remainder.
 * 
 * @author cambolbro
 */
public final class Keep extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final GraphFunction graphFn;
	private final Polygon polygon;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param graphFn Graph to modify.
	 * @param poly    Float points defining keep region.
	 * 
	 * @example (keep (square 4) (poly { { 1 1 } { 1 3 } { 4 0 } }))
	 */
	public Keep
	(
		final GraphFunction graphFn,
		final Poly			poly
	) 
	{
		this.graphFn = graphFn;
		this.polygon = poly.polygon();
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph source = graphFn.eval(context, siteType);
			
		if (polygon.size() < 3)
		{
			System.out.println("** Keep region only has " + polygon.size() + " points.");
			return source;
		}
		
		polygon.inflate(0.1);

		// Create reference lists of vertices and edges based on graphs[0]
		final List<Vertex> vertices = new ArrayList<Vertex>();
		final List<Edge>   edges    = new ArrayList<Edge>();
		
		final BitSet remove = new BitSet();
		
		for (final Vertex vertex : source.vertices())
		{
			final Vertex newVertex = new Vertex(vertices.size(), vertex.pt());
			newVertex.setTilingAndShape(vertex.basis(), vertex.shape());
			vertices.add(newVertex);
			
			if (!polygon.contains(vertex.pt2D()))
				remove.set(vertex.id(), true);
		}
		
		for (final Edge edge : source.edges())
		{
			final Vertex va = vertices.get(edge.vertexA().id());
			final Vertex vb = vertices.get(edge.vertexB().id());
			
			if (!remove.get(va.id()) && !remove.get(vb.id()))
			{
				// Add this non-clipped edge
				final Edge newEdge = new Edge(edges.size(), va, vb);  //, edge.curved());
				newEdge.setTilingAndShape(edge.basis(), edge.shape());
				edges.add(newEdge);
			}
		}
			
		// Remove clipped vertices
		for (int v = vertices.size() - 1; v >= 0; v--)
			if (remove.get(v))
				vertices.remove(v);  // remove this clipped vertex
		
		// Recalibrate indices
		for (int v = 0; v < vertices.size(); v++)
			vertices.get(v).setId(v);
		
		final Graph graph = new Graph(vertices, edges);
		
		graph.setBasisAndShape(source.basis(), ShapeType.NoShape);
		
		//System.out.println(graph);

		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return graphFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
		
		flags |= graphFn.gameFlags(game);

		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
//		type = SiteType.use(type, game);

		graphFn.preprocess(game);

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
		// conclude about the tiling
		// concepts.or(graphFn.concepts(game));
		return concepts;
	}
}
