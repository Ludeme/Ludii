package game.functions.ints.math;

import java.util.BitSet;

import annotations.Alias;
import annotations.Or;
import game.Game;
import game.functions.intArray.IntArrayConstant;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import main.Constants;
import other.concept.Concept;
import other.context.Context;

/**
 * Adds many values.
 * 
 * @author Eric.Piette and cambolbro
 */
@Alias(alias = "+")
public final class Add extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The array to sum. */
	private final IntArrayFunction array;

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * To add two values.
	 * 
	 * @param a The first value.
	 * @param b The second value.
	 * @example (+ (value Piece at:(from)) (value Piece at:(to)))
	 */
	public Add
	(
		final IntFunction a,
		final IntFunction b
	)
	{
		array = new IntArrayConstant(new IntFunction[]
		{ a, b });
	}

	/**
	 * To add all the values of a list.
	 * 
	 * @param list  The list of the values.
	 * @param array The array of values to sum.
	 * @example (+ {(value Piece at:(from)) (value Piece at:(to)) (value Piece
	 *          at:(between))})
	 */
	public Add
	(
		@Or final IntFunction[] list, 
		@Or final IntArrayFunction array
	)
	{
		int numNonNull = 0;
		if (list != null)
			numNonNull++;
		if (array != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Add(): One 'list' or 'array' parameters must be non-null.");

		this.array = (array != null) ? array : new IntArrayConstant(list);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int[] values = array.eval(context);

		int sum = 0;
		for (final int val : values)
			sum += val;

		return sum;
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
		concepts.set(Concept.Addition.id(), true);
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
	public void preprocess(final Game game)
	{
		array.preprocess(game);
		
		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "add the following values: " + array.toEnglish(game);
	}
}
