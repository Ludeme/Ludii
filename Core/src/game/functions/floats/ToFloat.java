package game.functions.floats;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import other.context.Context;

/**
 * Converts a BooleanFunction or an IntFunction to a float.
 * 
 * @author Eric.Piette
 */
public final class ToFloat extends BaseFloatFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The boolean function. */
	private final BooleanFunction boolFn;

	/** The int function. */
	private final IntFunction intFn;

	//-------------------------------------------------------------------------

	/**
	 * @param boolFn The boolean function.
	 * @param intFn  The int function.
	 *
	 * @example (toFloat (is Full))
	 * @example (toFloat (count Moves))
	 */
	public ToFloat
	(
	    @Or final BooleanFunction boolFn, 
	    @Or final IntFunction     intFn 
	) 
	{
		int numNonNull = 0;
		if (boolFn != null)
			numNonNull++;
		if (intFn != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"ToFloat(): One boolFn or intFn parameter must be non-null.");
		
		this.boolFn = boolFn;
		this.intFn = intFn;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float eval(final Context context)
	{
		if (boolFn != null)
		{
			final boolean result = boolFn.eval(context);
			if (result)
				return 1;
			return 0;
		}

		return intFn.eval(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (boolFn != null)
			return boolFn.isStatic();

		if (intFn != null)
			return intFn.isStatic();

		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (boolFn != null)
			gameFlags |= boolFn.gameFlags(game);
		if (intFn != null)
			gameFlags |= intFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (boolFn != null)
			concepts.or(boolFn.concepts(game));
		if (intFn != null)
			concepts.or(intFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (boolFn != null)
			writeEvalContext.or(boolFn.writesEvalContextRecursive());
		if (intFn != null)
			writeEvalContext.or(intFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (boolFn != null)
			readEvalContext.or(boolFn.readsEvalContextRecursive());
		if (intFn != null)
			readEvalContext.or(intFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (boolFn != null)
			boolFn.preprocess(game);
		if (intFn != null)
			intFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (boolFn != null)
			missingRequirement |= boolFn.missingRequirement(game);
		if (intFn != null)
			missingRequirement |= intFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (boolFn != null)
			willCrash |= boolFn.willCrash(game);
		if (intFn != null)
			willCrash |= intFn.willCrash(game);
		return willCrash;
	}
}
