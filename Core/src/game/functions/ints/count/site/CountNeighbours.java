package game.functions.ints.count.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or2;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import main.Constants;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Returns the number of neighbours of a graph element.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountNeighbours extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Integer preComputedInteger = null;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final IntArrayFromRegion region;

	/** Cell/Edge/Vertex. */
	private SiteType type;
	
	/**
	 * @param type The graph element type.
	 * @param in   The region to count the neighbours elements.
	 * @param at   The location to count the neighbours elements.
	 */
	public CountNeighbours
	(
		@Opt 	        final SiteType       type,
		@Opt @Or2 @Name final RegionFunction in,
		@Opt @Or2 @Name final IntFunction    at
	)
	{
		this.region = new IntArrayFromRegion(
				(in == null && at != null ? at : in == null ? new LastTo(null) : null),
				(in != null) ? in : null);
		this.type   = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (preComputedInteger != null)
			return preComputedInteger.intValue();

		final SiteType realSiteType = (type != null) ? type
				: context.board().defaultSite();
		final int[] sites;
		
		sites = region.eval(context);
		switch (realSiteType)
		{
		case Cell:
			if (sites[0] < context.topology().cells().size())
				return context.topology().cells().get(sites[0]).neighbours().size();
			break;
		case Edge:
			if (sites[0] < context.topology().edges().size())
				return context.topology().edges().get(sites[0]).vA().edges().size()
						+ context.topology().edges().get(sites[0]).vB().edges().size();
			break;
		case Vertex:
			if (sites[0] < context.topology().vertices().size())
				return context.topology().vertices().get(sites[0]).neighbours().size();
			break;
		}
		
		return Constants.UNDEFINED;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return region.isStatic();
	}

	@Override
	public String toString()
	{
		return "Neighbours()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = region.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(region.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		region.preprocess(game);
		if (isStatic())
			preComputedInteger = Integer.valueOf(eval(new Context(game, null)));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		return willCrash;
	}
}
