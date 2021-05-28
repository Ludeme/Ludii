package game.functions.booleans;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.floats.FloatFunction;
import game.functions.ints.IntFunction;
import other.context.Context;

/**
 * Converts a IntFunction or a FloatFunction to a boolean (false if 0, else
 * true).
 * 
 * @author Eric.Piette
 */
public final class ToBool extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The int function. */
	private final IntFunction intFn;

	/** The float function. */
	private final FloatFunction floatFn;

	// -------------------------------------------------------------------------

	/**
	 * @param intFn   The int function.
	 * @param floatFn The float function.
	 *
	 * @example (toBool (count Moves))
	 * @example (toBool (sqrt 2))
	 */
	public ToBool
	(
		@Or final IntFunction   intFn, 
		@Or final FloatFunction floatFn
	)
	{
		int numNonNull = 0;
		if (intFn != null)
			numNonNull++;
		if (floatFn != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("ToBool(): One intFn or floatFn parameter must be non-null.");

		this.intFn = intFn;
		this.floatFn = floatFn;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (intFn != null)
			return intFn.eval(context) != 0;

		return floatFn.eval(context) != 0.0;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (intFn != null)
			return intFn.isStatic();

		if (floatFn != null)
			return floatFn.isStatic();

		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (intFn != null)
			gameFlags |= intFn.gameFlags(game);
		if (floatFn != null)
			gameFlags |= floatFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (intFn != null)
			concepts.or(intFn.concepts(game));
		if (floatFn != null)
			concepts.or(floatFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (intFn != null)
			writeEvalContext.or(intFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (intFn != null)
			readEvalContext.or(intFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (intFn != null)
			intFn.preprocess(game);
		if (floatFn != null)
			floatFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (intFn != null)
			missingRequirement |= intFn.missingRequirement(game);
		if (floatFn != null)
			missingRequirement |= floatFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (intFn != null)
			willCrash |= intFn.willCrash(game);
		if (floatFn != null)
			willCrash |= floatFn.willCrash(game);
		return willCrash;
	}
}
