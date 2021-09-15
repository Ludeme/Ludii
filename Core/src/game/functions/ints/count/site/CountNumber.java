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
import game.types.state.GameType;
import other.IntArrayFromRegion;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the count over all sites.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountNumber extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final IntArrayFromRegion region;
	
	/** Cell/Edge/Vertex. */
	private SiteType type;
	
	/**
	 * @param type The graph element type.
	 * @param in   The region to count.
	 * @param at   The location to count.
	 */
	public CountNumber
	(
		@Opt 	        final SiteType       type,
		@Opt @Or2 @Name final RegionFunction in,
		@Opt @Or2 @Name final IntFunction    at 
	)
	{
		region = new IntArrayFromRegion(
				(in == null && at != null ? at : in == null ? new LastTo(null) : null),
				(in != null) ? in : null);

		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int[] sites;
		int count = 0;

		sites = region.eval(context);

		if (context.game().isStacking())
		{
			// Accumulate size Stack over region sites
			for (final int siteI : sites)
			{
				if (siteI < 0)
					continue;
				
				int cid = 0;
				if (type.equals(SiteType.Cell))
				{
					if (siteI >= context.containerId().length)
						continue;
					else
						cid = context.containerId()[siteI];
				}
				count += context.state().containerStates()[cid].sizeStack(siteI, type);
			}
		}
		else
		{
			// Accumulate count over region sites
			for (final int siteI : sites)
			{
				if (siteI < 0)
					continue;
				
				int cid = 0;
				if (type.equals(SiteType.Cell))
				{
					if (siteI >= context.containerId().length)
						continue;
					else
						cid = context.containerId()[siteI];
				}
				count += context.state().containerStates()[cid].count(siteI, type);
			}
		}

		return count;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Dim()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Count;

		gameFlags |= SiteType.gameFlags(type);
		
		gameFlags |= region.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.PieceCount.id(), true);
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
		region.preprocess(game);
		type = (type != null) ? type : game.board().defaultSite();
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String regionString = " on the board";
		if (region != null)
			regionString = " in " + region.toEnglish(game);
		
		return "the total number of " + type.name().toLowerCase() + regionString;
	}
	
	//-------------------------------------------------------------------------
	
}
