package game.functions.graph.operators;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.floats.FloatFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Scales a graph by the specified amount.
 * 
 * @author cambolbro
 * 
 * This operation modifies the locations of vertices within the graph.
 */
public final class Scale extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final FloatFunction scaleXFn;
	private final FloatFunction scaleYFn;
	private final FloatFunction scaleZFn;
	private final GraphFunction graphFn;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param scaleX Amount to scale in the x direction.
	 * @param scaleY Amount to scale in the y direction [scaleX].
	 * @param scaleZ Amount to scale in the z direction [1].
	 * @param graph  The graph to scale.
	 * 
	 * @example (scale 2 (square 5))
	 * @example (scale 2 3.5 (square 5))
	 */
	public Scale
	(
			 final FloatFunction scaleX,
		@Opt final FloatFunction scaleY,
		@Opt final FloatFunction scaleZ,
		     final GraphFunction graph
	) 
	{
		this.graphFn  = graph;
		this.scaleXFn = scaleX;
		this.scaleYFn = scaleY;
		this.scaleZFn = scaleZ;
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
		
		// Scale the graph
		final double sx = scaleXFn.eval(context);
		final double sy = (scaleYFn != null) ? scaleYFn.eval(context) : scaleXFn.eval(context);
		final double sz = (scaleZFn != null) ? scaleZFn.eval(context) : 1;

		graph.scale(sx, sy, sz);
		
		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return 	graphFn.isStatic() 
				&& 
				scaleXFn.isStatic() && scaleYFn.isStatic() && scaleZFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long flags = graphFn.gameFlags(game) 
					 | 
					 scaleXFn.gameFlags(game) | scaleYFn.gameFlags(game) | scaleZFn.gameFlags(game);

		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
//		type = SiteType.use(type, game);

		graphFn.preprocess(game);
		scaleXFn.preprocess(game);
		scaleYFn.preprocess(game);
		scaleZFn.preprocess(game);

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
