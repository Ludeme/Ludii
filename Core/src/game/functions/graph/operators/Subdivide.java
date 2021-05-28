package game.functions.graph.operators;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.util.graph.Face;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Subdivides graph cells about their midpoint.
 * 
 * @author cambolbro
 * 
 * @remarks Each cell with N sides, where N > min, will be split into N cells.
 */
public final class Subdivide extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

    private final GraphFunction graphFn;
	private final int min;

	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;

	//-------------------------------------------------------------------------

	/**
	 * @param graph The graph to subdivide.
	 * @param min   Minimum cell size to subdivide [1].
	 * 
	 * @example (subdivide (tiling T3464 2) min:6)
	 */
	public Subdivide
	(
					final GraphFunction graph,
			@Opt @Name final DimFunction min
	) 
	{
		this.graphFn = graph;
		this.min = (min == null) ? 1 : min.eval();
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph graph = graphFn.eval(context, siteType);
		
		if (siteType == SiteType.Vertex)
			graph.makeFaces(true);  // can't subdivide without any faces!

		for (final Face face : graph.faces())
			face.setFlag(false);	
		
		for (int fid = graph.faces().size() - 1; fid >= 0; fid--)
		{
			// Split this face around its midpoint
			final Face face = graph.faces().get(fid);
			if (face.vertices().size() < min)
				continue;
			
			final Vertex pivot = graph.addVertex(face.pt());
			
			for (final Vertex vertex : face.vertices())
				graph.findOrAddEdge(pivot.id(), vertex.id());
			
			face.setFlag(true);  // flag this existing face for removal
		}

		if (siteType == SiteType.Cell)
		{
			// Delete faces *after* subdivision faces have been added,
			// otherwise shared edges might also be removed.
			for (int fid = graph.faces().size() - 1; fid >= 0; fid--)
				if (graph.faces().get(fid).flag())
					graph.removeFace(fid, false);
			
			// Create new faces
			graph.makeFaces(true);
		}
		else
		{
			graph.clear(SiteType.Cell);
		}
		
		graph.resetBasis();
		
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
}
