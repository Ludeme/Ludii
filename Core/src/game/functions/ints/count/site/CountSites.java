package game.functions.ints.count.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.region.RegionFunction;
import other.ContainerId;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Returns the number of sites of a container or of a region.
 * 
 * @author Eric.Piette
 *
 * @remarks Renamed from Sites to avoid compiler confusion with Sites ludeme.
 */
@Hide
public final class CountSites extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Integer preComputedInteger = null;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final IntArrayFromRegion region;

	/** The container id. */
	private final ContainerId containerId;
	
	/**
	 * @param in   The region to count the number of sites.
	 * @param at   The location to count the number of sites.
	 * @param name The name of the region.
	 */
	public CountSites
	(
		@Opt @Or @Name final RegionFunction in,
		@Opt @Or @Name final IntFunction    at, 
		@Opt @Or       final String         name
	)
	{
		region = new IntArrayFromRegion(
				(in == null && at != null ? at : in == null ? new LastTo(null) : null),
				(in != null) ? in : null);
							
		containerId = (name == null && at == null) ? null : new ContainerId(at, name, null, null, null);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (preComputedInteger != null)
			return preComputedInteger.intValue();
		
		if (containerId != null)
		{
			final int cid = containerId.eval(context);
			return context.containers()[cid].numSites();
		}
		else
		{
			return region.eval(context).length;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (containerId != null)
			return true;

		return region.isStatic();
	}

	@Override
	public String toString()
	{
		return "Sites()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return region.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		return region.concepts(game);
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
	
	@Override
	public String toEnglish(final Game game) 
	{
		if(region != null) {
			return " the number of sites in " + region.toEnglish(game);
		} else {
			return "";
		}
	}
}
