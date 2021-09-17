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
 * Returns the product of values.
 * 
 * @author Eric.Piette
 */
@Alias(alias = "*")
public final class Mul extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The array to multiply. */
	private final IntArrayFunction array;

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * For the product of two values.
	 * 
	 * @param a The first value.
	 * @param b The second value.
	 * 
	 * @example (* (mover) (next))
	 */
	public Mul
	(
		 final IntFunction a,
		 final IntFunction b
	)
	{
		array = new IntArrayConstant(new IntFunction[]
		{ a, b });
	}
	
	/**
	 * For the product of many values.
	 * 
	 * @param list  The list of the values.
	 * @param array The array of values to multiply.
	 * @example (* { (mover) (next) (prev) })
	 */
	public Mul
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
			throw new IllegalArgumentException("Mul(): One 'list' or 'array' parameters must be non-null.");

		this.array = (array != null) ? array : new IntArrayConstant(list);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int[] values = array.eval(context);

		if (values.length == 0)
			return 0;

		int product = 1;
		for (final int val : values)
			product *= val;

		return product;
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
		concepts.set(Concept.Multiplication.id(), true);
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
		
		if (isStatic())
			precomputedValue = eval(new Context(game, null));
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "multiply all the values in " + array.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
