package game.functions.floats.math;

import java.util.BitSet;

import game.Game;
import game.functions.floats.BaseFloatFunction;
import game.functions.floats.FloatFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Return the absolute value of a float.
 * 
 * @author Eric Piette
 */
public final class Abs extends BaseFloatFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value. */
	protected final FloatFunction value;

	//-------------------------------------------------------------------------

	/**
	 * Return the absolute value of a value.
	 * 
	 * @param value The value.
	 * @example (abs (- 8.2 5.1))
	 */
	public Abs
	(
		final FloatFunction value
	)
	{
		this.value = value;
	}

	//-------------------------------------------------------------------------

	@Override
	public float eval(Context context)
	{
		return Math.abs(value.eval(context));
	}

	@Override
	public long gameFlags(Game game)
	{
		return value.gameFlags(game);
	}

	@Override
	public BitSet concepts(Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(value.concepts(game));
		concepts.set(Concept.Float.id(), true);
		concepts.set(Concept.Absolute.id(), true);
		return concepts;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(Game game)
	{
		value.preprocess(game);
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
