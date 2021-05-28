package game.functions.floats.math;

import java.util.BitSet;

import annotations.Alias;
import game.Game;
import game.functions.floats.BaseFloatFunction;
import game.functions.floats.FloatFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the subtraction A minus B.
 * 
 * @author Eric Piette
 */
@Alias(alias = "-")
public final class Sub extends BaseFloatFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value A. */
	private final FloatFunction valueA;

	/** Which value B. */
	private final FloatFunction valueB;

	//-------------------------------------------------------------------------

	/**
	 * @param valueA The value A.
	 * @param valueB The value B.
	 * @example (- 5.6 1.1)
	 */
	public Sub
	(
		final FloatFunction valueA, 
		final FloatFunction valueB
	)
	{
		this.valueA = valueA;
		this.valueB = valueB;
	}

	//-------------------------------------------------------------------------

	@Override
	public float eval(Context context)
	{
		return valueA.eval(context) - valueB.eval(context);
	}

	@Override
	public long gameFlags(Game game)
	{
		return valueA.gameFlags(game) | valueB.gameFlags(game);
	}

	@Override
	public BitSet concepts(Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(valueA.concepts(game));
		concepts.or(valueB.concepts(game));
		concepts.set(Concept.Float.id(), true);
		concepts.set(Concept.Subtraction.id(), true);
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
		valueA.preprocess(game);
		valueB.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= valueA.missingRequirement(game);
		missingRequirement |= valueB.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= valueA.willCrash(game);
		willCrash |= valueB.willCrash(game);
		return willCrash;
	}
}
