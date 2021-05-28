package game.functions.graph.operators;

import java.util.BitSet;

import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Poly;
import main.math.Polygon;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Cuts a hole in a graph according to a specified shape.
 * 
 * @author cambolbro
 * 
 * @remarks Any face of the graph whose midpoint falls within the hole is removed, 
 *          as are any edges or vertices isolated as a result.
 */
public final class Hole extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	private final GraphFunction graphFn;
	private final Polygon polygon;
	
	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param graphFn Graph to clip.
	 * @param poly    Float points defining hole region.
	 * 
	 * @example (hole (square 8) (poly { { 2.5 2.5 } { 2.5 5.5 } { 4.5 5.5 } { 4.5 4.5 } {5.5 4.5 } { 5.5 2.5 } }))
	 */
	public Hole
	(
		final GraphFunction graphFn,
		final Poly     		poly
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
		
		final Graph graph = graphFn.eval(context, siteType);
			
		if (polygon.size() < 3)
		{
			System.out.println("** Hole: Clip region only has " + polygon.size() + " points.");
			return graph;
		}

		polygon.inflate(0.1);
		
		for (int fid = graph.faces().size() - 1; fid >= 0; fid--)
			if (polygon.contains(graph.faces().get(fid).pt2D()))
				graph.removeFace(fid, true);

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
