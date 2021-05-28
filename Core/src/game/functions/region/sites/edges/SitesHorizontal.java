package game.functions.region.sites.edges;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns all the horizontal edges of the board.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesHorizontal extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	// -------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public SitesHorizontal()
	{
		// Nothing to do.
	}

	// -------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final other.topology.Topology graph = context.topology();
		return new Region(graph.horizontal(SiteType.Edge));
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "Horizontal()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Graph | GameType.Edge;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		precomputedRegion = eval(new Context(game, null));
	}
}
