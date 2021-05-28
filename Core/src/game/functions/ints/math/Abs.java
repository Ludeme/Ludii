package game.functions.ints.math;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import main.Constants;
import other.concept.Concept;
import other.context.Context;

/**
 * Return the absolute value of a value.
 * 
 * @author Eric Piette
 */
public final class Abs extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value. */
	protected final IntFunction value;

	/** Pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * Return the absolute value of a value.
	 * 
	 * @param value The value.
	 * @example (abs (value Piece at:(to)))
	 */
	public Abs
	(
		final IntFunction value
	)
	{
		this.value = value;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		return Math.abs(value.eval(context));
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The value.
	 */
	public IntFunction value()
	{
		return value;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return value.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return value.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(value.concepts(game));
		concepts.set(Concept.Absolute.id(), true);
		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(value.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(value.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		value.preprocess(game);

		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= value.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= value.willCrash(game);
		return willCrash;
	}
}
