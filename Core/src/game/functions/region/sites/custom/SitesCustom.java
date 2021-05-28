package game.functions.region.sites.custom;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.intArray.IntArrayConstant;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;

/**
 * Returns all the sites corresponding to the indices in entry.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesCustom extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * The array of sites.
	 */
	private final IntArrayFunction arrayFn;

	//-------------------------------------------------------------------------

	/**
	 * @param sites The sites of the region.
	 */
	public SitesCustom(final IntFunction[] sites)
	{
		this.arrayFn = new IntArrayConstant(sites);
	}

	/**
	 * @param array The intArray function.
	 */
	public SitesCustom(final IntArrayFunction array)
	{
		this.arrayFn = array;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final TIntArrayList sites = new TIntArrayList(arrayFn.eval(context));
		for (int i = sites.size() - 1; i >= 0; i--)
		{
			final int site = sites.get(i);
			if (site < 0)
				sites.removeAt(i);
		}
		return new Region(sites.toArray());
	}
	
	@Override
	public boolean contains(final Context context, final int location)
	{
		if (precomputedRegion != null)
			return precomputedRegion.contains(location);
		
		final int sites[] = arrayFn.eval(context);

		for (int i = 0; i < sites.length; i++)
		{
			if (location == sites[i])
				return true;
		}
		
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (!arrayFn.isStatic())
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "CustomSites()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		flags |= arrayFn.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(arrayFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(arrayFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(arrayFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= arrayFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= arrayFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		arrayFn.preprocess(game);

		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}
}
