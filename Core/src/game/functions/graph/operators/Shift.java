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
 * Translate a graph by the specified x, y and z amounts.
 * 
 * @author cambolbro
 * 
 * @remarks This operation modifies the locations of vertices within the graph.
 */
public final class Shift extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final FloatFunction dxFn;
	private final FloatFunction dyFn;
	private final FloatFunction dzFn;
	private final GraphFunction graphFn;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param dx      Amount to translate in the x direction.
	 * @param dy      Amount to translate in the y direction.
	 * @param dz      Amount to translate in the z direction [0].
	 * @param graph   The graph to rotate.
	 * 
	 * @example (shift 0 10 (square 5))
	 */
	public Shift
	(
		     final FloatFunction dx,
		     final FloatFunction dy,
		@Opt final FloatFunction dz,
		     final GraphFunction graph
	) 
	{
		this.graphFn = graph;
		this.dxFn = dx;
		this.dyFn = dy;
		this.dzFn = dz;
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
		
		// Translate the graph
		final double dx = dxFn.eval(context);
		final double dy = dyFn.eval(context);
		final double dz = (dzFn != null) ? dzFn.eval(context) : 0;
		
		graph.translate(dx, dy, dz);
		
		return graph;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return  graphFn.isStatic() 
				&& 
				dxFn.isStatic() && dyFn.isStatic() && dzFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long flags = graphFn.gameFlags(game) 
					 | 
					 dxFn.gameFlags(game) | dyFn.gameFlags(game) | dzFn.gameFlags(game);

		return flags;
	}
	
	@Override
	public void preprocess(final Game game)
	{
//		type = SiteType.use(type, game);

		graphFn.preprocess(game);
		dxFn.preprocess(game);
		dyFn.preprocess(game);
		
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
