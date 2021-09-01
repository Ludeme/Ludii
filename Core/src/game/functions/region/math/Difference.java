package game.functions.region.math;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.util.equipment.Region;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the set difference, i.e. elements of the source region are not in the
 * subtraction region.
 * 
 * @author mrraow and cambolbro and Eric.Piette
 */
public final class Difference extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region 1. */
	private final RegionFunction source;

	/** Which region 2. */
	private final RegionFunction subtraction;

	/** Which site. */
	private final IntFunction siteToRemove;

	/** If we can, we'll precompute once and cache */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * @param source       The original region.
	 * @param subtraction  The region to remove from the original.
	 * @param siteToRemove The site to remove from the original.
	 * @example (difference (sites Occupied by:Mover) (sites Mover))
	 */
	public Difference
	(
			final RegionFunction source, 
		@Or final RegionFunction subtraction,
		@Or final IntFunction    siteToRemove
	)
	{
		
		this.source = source;
		
		int numNonNull = 0;
		if (subtraction != null)
			numNonNull++;
		if (siteToRemove != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("one parameter must be non-null.");
		
		this.siteToRemove = siteToRemove;
		this.subtraction  = subtraction;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final Region sites1 = new Region(source.eval(context));
		if (subtraction != null)
		{
			final Region sites2 = subtraction.eval(context);
			sites1.remove(sites2);
			return sites1;
		}
		else
		{
			final int site2 = siteToRemove.eval(context);
			if (site2 >= 0)
				sites1.remove(site2);
			return sites1;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (subtraction != null && !subtraction.isStatic())
			return false;
		if (siteToRemove != null && !siteToRemove.isStatic())
			return false;
		return source.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flag = source.gameFlags(game);
		
		if (subtraction != null)
			flag |= subtraction.gameFlags(game);
		
		if (siteToRemove != null)
			flag |= siteToRemove.gameFlags(game);
		
		return flag;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(source.concepts(game));

		if (subtraction != null)
			concepts.or(subtraction.concepts(game));

		if (siteToRemove != null)
			concepts.or(siteToRemove.concepts(game));

		concepts.set(Concept.Complement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(source.writesEvalContextRecursive());

		if (subtraction != null)
			writeEvalContext.or(subtraction.writesEvalContextRecursive());

		if (siteToRemove != null)
			writeEvalContext.or(siteToRemove.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(source.readsEvalContextRecursive());

		if (subtraction != null)
			readEvalContext.or(subtraction.readsEvalContextRecursive());

		if (siteToRemove != null)
			readEvalContext.or(siteToRemove.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= source.missingRequirement(game);

		if (subtraction != null)
			missingRequirement |= subtraction.missingRequirement(game);

		if (siteToRemove != null)
			missingRequirement |= siteToRemove.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= source.willCrash(game);

		if (subtraction != null)
			willCrash |= subtraction.willCrash(game);

		if (siteToRemove != null)
			willCrash |= siteToRemove.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		source.preprocess(game);
		
		if (subtraction != null)
			subtraction.preprocess(game);
		
		if (siteToRemove != null)
			siteToRemove.preprocess(game);
		
		if (isStatic())
		{
			final Context context = new Context(game, null);
			final Region sites1 = new Region(source.eval(context));
			if (subtraction != null)
			{
				final Region sites2 = subtraction.eval(context);
				sites1.remove(sites2);
			}
			else
			{
				final int site2 = siteToRemove.eval(context);
				sites1.remove(site2);
			}

			precomputedRegion = sites1;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Source region
	 */
	public RegionFunction source()
	{
		return source;
	}
	
	/**
	 * @return Region to subtract
	 */
	public RegionFunction subtraction()
	{
		return subtraction;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "board " + precomputedRegion;
	}
}
