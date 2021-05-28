package game.functions.graph.operators;

import java.util.BitSet;

import game.Game;
import game.functions.floats.FloatFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Rotates a graph by the specified number of degrees anticlockwise.
 * 
 * @author cambolbro
 * 
 * @remarks The vertices within the graph are rotated about the graph's midpoint.
 */
public final class Rotate extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final FloatFunction degreesFn;
	private final GraphFunction graphFn;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param degreesFn Number of degrees to rotate anticlockwise.
	 * @param graph     The graph to rotate.
	 * 
	 * @example (rotate 45 (square 5))
	 */
	public Rotate
	(
		final FloatFunction degreesFn,
		final GraphFunction graph
	) 
	{
		this.degreesFn = degreesFn;
		this.graphFn   = graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph graph = graphFn.eval(context, siteType);
		if (graph.vertices().isEmpty())
		{
			System.out.println("** Rotate.eval(): Rotating empty graph.");
			return graph;
		}
		
		// Rotate the graph
		final double degrees = degreesFn.eval(context);
		graph.rotate(degrees);
		
		//graph.reorder();
		
		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return graphFn.isStatic() && degreesFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long flags = graphFn.gameFlags(game) | degreesFn.gameFlags(game);

		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
//		type = SiteType.use(type, game);

		graphFn.preprocess(game);
		degreesFn.preprocess(game);

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
}
