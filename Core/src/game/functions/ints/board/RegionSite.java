package game.functions.ints.board;

import java.util.BitSet;

import annotations.Name;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import main.Constants;
import other.context.Context;

/**
 * Returns one site of a region.
 * 
 * @author Eric Piette
 */
public final class RegionSite extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Which region. */
	private final RegionFunction region;

	/** Which site. */
	private final IntFunction indexFn;

	/** Precomputed value if possible. */
	private int precomputedValue = Constants.OFF;

	// -------------------------------------------------------------------------

	/**
	 * @param region The region.
	 * @param index  The index of the site in the region.
	 * @example (regionSite (sites Empty) index:(value))
	 */
	public RegionSite
	(
		     final RegionFunction region,
	   @Name final IntFunction    index
	)
	{
		this.region = region;	
		this.indexFn = index;
	}

	// -------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int[] sites = region.eval(context).sites();
		final int index = indexFn.eval(context);

		if (index < 0)
		{
			System.out.println("** Negative index in (regionSite ...).");
			return Constants.OFF;
		}
		else if (index < sites.length)
		{
			return sites[index];
		}


		return Constants.OFF;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return region.isStatic() && indexFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return region.gameFlags(game) | indexFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(region.concepts(game));
		concepts.or(indexFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(indexFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(indexFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		region.preprocess(game);
		indexFn.preprocess(game);

		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= indexFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		willCrash |= indexFn.willCrash(game);
		return willCrash;
	}
}
