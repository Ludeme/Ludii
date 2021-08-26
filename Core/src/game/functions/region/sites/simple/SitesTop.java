package game.functions.region.sites.simple;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns all the top sites of the board.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesTop extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * @param elementType The graph element type [default SiteType of the board].
	 */
	public SitesTop
	(
		@Opt final SiteType elementType
	)
	{
		type = elementType;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final SiteType realType = (type != null) ? type
				: context.board().defaultSite();

		final other.topology.Topology graph = context.topology();
		return new Region(graph.top(realType));
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "Top()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;

		flags |= SiteType.gameFlags(type);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
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
		type = SiteType.use(type, game);
		precomputedRegion = eval(new Context(game, null));
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "top site";
	}
}
