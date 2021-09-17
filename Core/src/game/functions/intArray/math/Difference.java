package game.functions.intArray.math;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;

/**
 * Returns the difference between two arrays of integers, i.e. the elements in A
 * that are not in B.
 * 
 * @author Eric.Piette
 */
public final class Difference extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region 1. */
	private final IntArrayFunction source;

	/** Which region 2. */
	private final IntArrayFunction subtraction;

	/** Which site. */
	private final IntFunction intToRemove;

	/** If we can, we'll precompute once and cache */
	private int[] precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * @param source      The original array.
	 * @param subtraction The array to remove from the original array.
	 * @param intToRemove The integer to remove from the original array.
	 * 
	 * @example (difference (values Remembered) 2)
	 */
	public Difference
	(       
			final IntArrayFunction source, 
		@Or final IntArrayFunction subtraction,
		@Or final IntFunction      intToRemove
	)
	{

		this.source = source;

		int numNonNull = 0;
		if (subtraction != null)
			numNonNull++;
		if (intToRemove != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Only one Or parameter must be non-null.");

		this.intToRemove = intToRemove;
		this.subtraction = subtraction;
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final int[] arraySource = source.eval(context);
		final TIntArrayList sourcelist = new TIntArrayList(arraySource);
		if (subtraction != null)
		{
			final int[] subSource = subtraction.eval(context);
			final TIntArrayList subList = new TIntArrayList(subSource);
			sourcelist.removeAll(subList);
		}
		else
		{
			final int integerToRemove = intToRemove.eval(context);
			sourcelist.remove(integerToRemove);
		}

		return sourcelist.toArray();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (!source.isStatic())
			return false;

		if (subtraction != null)
			if (!subtraction.isStatic())
				return false;

		if (intToRemove != null && !intToRemove.isStatic())
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Difference(" + source + "," + subtraction + ")";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flag = 0;
		flag |= source.gameFlags(game);

		if (subtraction != null)
			flag |= subtraction.gameFlags(game);

		if (intToRemove != null)
			flag |= intToRemove.gameFlags(game);

		return flag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(source.concepts(game));
		if(subtraction != null)
			concepts.or(subtraction.concepts(game));
		
		if (intToRemove != null)
			concepts.or(intToRemove.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(source.writesEvalContextRecursive());

		if (subtraction != null)
			writeEvalContext.or(subtraction.writesEvalContextRecursive());

		if (intToRemove != null)
			writeEvalContext.or(intToRemove.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(source.readsEvalContextRecursive());

		if (subtraction != null)
			readEvalContext.or(subtraction.readsEvalContextRecursive());

		if (intToRemove != null)
			readEvalContext.or(intToRemove.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= source.missingRequirement(game);

		if (subtraction != null)
			missingRequirement |= subtraction.missingRequirement(game);

		if (intToRemove != null)
			missingRequirement |= intToRemove.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= source.willCrash(game);

		if (subtraction != null)
			willCrash |= subtraction.willCrash(game);

		if (intToRemove != null)
			willCrash |= intToRemove.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		source.preprocess(game);

		if (subtraction != null)
			subtraction.preprocess(game);

		if (intToRemove != null)
			intToRemove.preprocess(game);

		if (isStatic())
		{
			final Context context = new Context(game, null);
			final int[] arraySource = source.eval(context);

			final TIntArrayList sourcelist = new TIntArrayList(arraySource);
			if (subtraction != null)
			{
				final int[] subSource = subtraction.eval(context);
				final TIntArrayList subList = new TIntArrayList(subSource);
				sourcelist.removeAll(subList);
			}
			else
			{
				final int integerToRemove = intToRemove.eval(context);
				sourcelist.remove(integerToRemove);
			}

			precomputedRegion = sourcelist.toArray();
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the difference between " + source.toEnglish(game) + " and " + subtraction.toEnglish(game);
	}
		
	//-------------------------------------------------------------------------
}
