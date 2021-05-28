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
 * Returns the maximum of specified values.
 * 
 * @author Eric Piette
 */
public final class Max extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The array to minimise. */
	private final IntArrayFunction array;

	//-------------------------------------------------------------------------

	/**
	 * For returning the maximum value between two values.
	 * 
	 * @param valueA The first value.
	 * @param valueB The second value.
	 * 
	 * @example (max (mover) (next))
	 */
	public Max
	(
		final IntFunction valueA, 
		final IntFunction valueB
	)
	{
		this.array = new IntArrayConstant(new IntFunction[]
		{ valueA, valueB });
	}

	/**
	 * For returning the maximum value between an array of values.
	 * 
	 * @param array The array of values to maximise.
	 * 
	 * @example (max (results from:(last To) to:(sites LineOfSight at:(from) All)
	 *          (count Steps All (from) (to))))
	 */
	public Max(final IntArrayFunction array)
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

		int max = values[0];
		for (int i = 1; i < values.length; i++)
			max = Math.max(max, values[i]);
		return max;
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
		concepts.set(Concept.Maximum.id(), true);
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
}
