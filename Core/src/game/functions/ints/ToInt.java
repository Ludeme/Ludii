package game.functions.ints;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.floats.FloatFunction;
import other.context.Context;

/**
 * Converts a BooleanFunction or a FloatFunction to an integer.
 * 
 * @author Eric.Piette
 */
public final class ToInt extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The boolean function. */
	private final BooleanFunction boolFn;

	/** The float function. */
	private final FloatFunction floatFn;

	// -------------------------------------------------------------------------

	/**
	 * @param boolFn  The boolean function.
	 * @param floatFn The float function.
	 *
	 * @example (toInt (is Full))
	 * @example (toInt (sqrt 2))
	 */
	public ToInt
	(
	    @Or final BooleanFunction boolFn, 
	    @Or final FloatFunction   floatFn 
	) 
	{
		int numNonNull = 0;
		if (boolFn != null)
			numNonNull++;
		if (floatFn != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"ToInt(): One boolFn or floatFn parameter must be non-null.");
		
		this.boolFn = boolFn;
		this.floatFn = floatFn;
	}

	// -------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (boolFn != null)
		{
			final boolean result = boolFn.eval(context);
			if (result)
				return 1;
			return 0;
		}

		return (int) floatFn.eval(context);
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (boolFn != null)
			return boolFn.isStatic();

		if (floatFn != null)
			return floatFn.isStatic();

		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (boolFn != null)
			gameFlags |= boolFn.gameFlags(game);
		if (floatFn != null)
			gameFlags |= floatFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (boolFn != null)
			concepts.or(boolFn.concepts(game));
		if (floatFn != null)
			concepts.or(floatFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (boolFn != null)
			writeEvalContext.or(boolFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (boolFn != null)
			readEvalContext.or(boolFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (boolFn != null)
			boolFn.preprocess(game);
		if (floatFn != null)
			floatFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (boolFn != null)
			missingRequirement |= boolFn.missingRequirement(game);
		if (floatFn != null)
			missingRequirement |= floatFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (boolFn != null)
			willCrash |= boolFn.willCrash(game);
		if (floatFn != null)
			willCrash |= floatFn.willCrash(game);
		return willCrash;
	}
}