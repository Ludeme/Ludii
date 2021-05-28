package game.functions.graph.operators;

import java.util.BitSet;

import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Skews a graph by the specified amount.
 * 
 * @author cambolbro
 * 
 * Skewing a graph can be useful for achieving a 3D isometric look.
 */
public final class Skew extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final double 		amount;
	private final GraphFunction graphFn;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param amount Amount to skew (1 gives a 45 degree skew).
	 * @param graph  The graph to scale.
	 * 
	 * @example (skew .5 (square 5))
	 */
	public Skew
	(
		final Float         amount,
		final GraphFunction graph
	) 
	{
		this.amount  = amount.floatValue();
		this.graphFn = graph;
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

		graph.skew(amount);
		
		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return 	graphFn.isStatic();
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
		concepts.or(graphFn.concepts(game));
		return concepts;
	}
}
