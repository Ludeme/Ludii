package game.functions.graph.operators;

import java.util.BitSet;

import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Face;
import game.util.graph.Graph;
import other.BaseLudeme;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Returns the weak dual of the specified graph.
 * 
 * @author cambolbro
 * 
 * @remarks The weak dual of a graph is obtained by creating a vertex at the 
 *          midpoint of each face, then connecting with an edge vertices 
 *          corresponding to adjacent faces. This is equivalent to the dual of 
 *          the graph without the single ``outside'' vertex. The weak dual is 
 *          non-transitive and always produces a smaller graph; applying 
 *          (dual (dual <graph>)) does not restore the original graph.
 */
public final class Dual extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final GraphFunction graphFn;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param graph The graph to take the weak dual of.
	 * 
	 * @example (dual (square 5))
	 */
	public Dual
	(
		final GraphFunction graph
	) 
	{
		graphFn = graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph source = graphFn.eval(context, siteType);
		if (source.vertices().isEmpty() || source.edges().isEmpty() || source.faces().isEmpty())
		{
			System.out.println("** Dual.eval(): Taking dual of graph with no vertices, edges or faces.");
			return source;
		}

		final Graph graph = new Graph();

		// Create vertices
		for (final Face face : source.faces())
			graph.addVertex(face.pt());

		if (graph.vertices().isEmpty())
			return graph;
		
		// Create edges
		for (final Edge edge : source.edges())
			if (edge.left() != null && edge.right() != null)
				graph.addEdge(edge.left().id(), edge.right().id());

		//if (siteType == SiteType.Cell)
			graph.makeFaces(false);  //true);
		
		graph.setBasisAndShape(BasisType.Dual, ShapeType.NoShape);
		graph.reorder();
			
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
		final long flags = graphFn.gameFlags(game);
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
	
	@Override
	public String toEnglish(final Game game) 
	{
		return ((BaseLudeme) graphFn).toEnglish(game);
	}
}
