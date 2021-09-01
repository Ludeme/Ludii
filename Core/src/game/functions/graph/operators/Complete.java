package game.functions.graph.operators;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.graph.Face;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Creates an edge between each pair of vertices in the graph.
 * 
 * @author cambolbro
 */
public final class Complete extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

    private final GraphFunction graphFn;

	/** Precompute once and cache, if possible. */
	private Graph precomputedGraph = null;
	
	private final boolean eachCell;

	//-------------------------------------------------------------------------

	/**
	 * @param graph    The graph to complete.
	 * @param eachCell Whether to complete each cell individually.
	 * 
	 * @example (complete (hex 1))
	 * @example (complete (hex 3) eachCell:True)
	 */
	public Complete
	(
			 	   final GraphFunction graph,
		@Opt @Name final Boolean       eachCell
	) 
	{
		graphFn = graph;
		this.eachCell = (eachCell == null) ? false : eachCell.booleanValue(); 
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		if (precomputedGraph != null)
			return precomputedGraph;
		
		final Graph graph = graphFn.eval(context, siteType);

		if (eachCell)
		{
			// Create an edge between all vertex pairs within each cell
			for (final Face face : graph.faces())
				for (int va = 0; va < face.vertices().size(); va++)
				{
					final Vertex vertexA = face.vertices().get(va);
					for (int vb = va + 1; vb < face.vertices().size(); vb++)
					{
						final Vertex vertexB = face.vertices().get(vb);
						graph.findOrAddEdge(vertexA, vertexB);
					}
				}
		}
		else
		{
			// Create an edge between all vertex pairs within graph
			graph.clear(SiteType.Edge);
			
			for (int va = 0; va < graph.vertices().size(); va++)
				for (int vb = va + 1; vb < graph.vertices().size(); vb++)
					graph.findOrAddEdge(va, vb);
			
			graph.makeFaces(true);
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
		long flags = graphFn.gameFlags(game);

		if (game.board().defaultSite() != SiteType.Cell)
			flags |= GameType.Graph;
		
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
