package game.functions.intArray.math;

import java.util.BitSet;

import annotations.Name;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import gnu.trove.list.array.TIntArrayList;
import other.IntArrayFromRegion;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns an array of all the results of the function for each site 'from' to each site 'to'.
 * 
 * @author Eric.Piette
 */
public final class Results extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region 1. */
	private final IntArrayFromRegion regionFromFn;

	/** Which region 2. */
	private final IntArrayFromRegion regionToFn;

	/** Which function. */
	private final IntFunction functionFn;

	//-------------------------------------------------------------------------

	/**
	 * @param from     The 'from' site.
	 * @param From     The 'from' region.
	 * @param to       The 'to' site.
	 * @param To       The 'to' region.
	 * @param function The function to compute for each site 'from' and each site
	 *                 'to'.
	 * @example (results from:(last To) to:(sites LineOfSight at:(from) All) (count
	 *          Steps All (from) (to)))
	 */
	public Results
	(       
		@Name @Or  final IntFunction    from, 
		@Name @Or  final RegionFunction From,
		@Name @Or2 final IntFunction    to,
		@Name @Or2 final RegionFunction To,
		           final IntFunction    function
	)
	{

		int numNonNull = 0;
		if (from != null)
			numNonNull++;
		if (From != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Only one Or parameter must be non-null.");
		
		int numNonNull2 = 0;
		if (to != null)
			numNonNull2++;
		if (To != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException("Only one Or2 parameter must be non-null.");

		functionFn = function;
		regionFromFn = new IntArrayFromRegion(from, From);
		regionToFn = new IntArrayFromRegion(to, To);
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		final TIntArrayList resultList = new TIntArrayList();
		final int[] sitesFrom = regionFromFn.eval(context);
		final int originFrom = context.from();
		final int originTo = context.to();

		for (final int from : sitesFrom)
		{
			context.setFrom(from);
			final int[] sitesTo = regionToFn.eval(context);
			for (final int to : sitesTo)
			{
				context.setTo(to);
				resultList.add(functionFn.eval(context));
			}
		}

		context.setFrom(originFrom);
		context.setTo(originTo);
		return resultList.toArray();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flag = 0;
		flag |= regionFromFn.gameFlags(game);
		flag |= regionToFn.gameFlags(game);
		flag |= functionFn.gameFlags(game);
		return flag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(regionFromFn.concepts(game));
		concepts.or(regionToFn.concepts(game));
		concepts.or(functionFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(regionFromFn.writesEvalContextRecursive());
		writeEvalContext.or(regionToFn.writesEvalContextRecursive());
		writeEvalContext.or(functionFn.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.From.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(regionFromFn.readsEvalContextRecursive());
		readEvalContext.or(regionToFn.readsEvalContextRecursive());
		readEvalContext.or(functionFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		regionFromFn.preprocess(game);
		regionToFn.preprocess(game);
		functionFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= regionFromFn.missingRequirement(game);
		missingRequirement |= regionToFn.missingRequirement(game);
		missingRequirement |= functionFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= regionFromFn.willCrash(game);
		willCrash |= regionToFn.willCrash(game);
		willCrash |= functionFn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the results of applying " + functionFn.toEnglish(game) + " from "  + regionFromFn.toEnglish(game) + " to " + regionToFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}
