package game.functions.ints.math;

import java.util.BitSet;

import game.Game;
import game.functions.intArray.IntArrayConstant;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import main.Constants;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the minimum of specified values.
 * 
 * @author Eric.Piette
 */
public final class Min extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The array to minimise. */
	private final IntArrayFunction array;

	//-------------------------------------------------------------------------

	/**
	 * For returning the minimum value between two values.
	 * 
	 * @param valueA The first value.
	 * @param valueB The second value.
	 * 
	 * @example (min (mover) (next))
	 */
	public Min
	(
		final IntFunction valueA, 
		final IntFunction valueB
	)
	{
		array = new IntArrayConstant(new IntFunction[]
		{ valueA, valueB });
	}

	/**
	 * For returning the minimum value between an array of values.
	 * 
	 * @param array The array of values to minimise.
	 * 
	 * @example (min (results from:(last To) to:(sites LineOfSight at:(from) All)
	 *          (count Steps All (from) (to))))
	 */
	public Min(final IntArrayFunction array)
	{
		this.array = array;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int[] values = array.eval(context);

		if (values.length == 0)
			return Constants.UNDEFINED;

		int min = values[0];
		for (int i = 1; i < values.length; i++)
			min = Math.min(min, values[i]);
		return min;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return array.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return array.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(array.concepts(game));
		concepts.set(Concept.Minimum.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(array.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(array.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		array.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= array.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= array.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the minimum of the following values: " + array.toEnglish(game);
	}
}
