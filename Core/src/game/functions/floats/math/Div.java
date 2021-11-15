package game.functions.floats.math;

import java.util.BitSet;

import annotations.Alias;
import game.Game;
import game.functions.floats.BaseFloatFunction;
import game.functions.floats.FloatFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * To divide a value by another.
 * 
 * @author Eric.Piette
 */
@Alias(alias = "/")
public final class Div extends BaseFloatFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The first value. */
	private final FloatFunction a;

	/** The second value. */
	private final FloatFunction b;

	//-------------------------------------------------------------------------

	/**
	 * To divide a value by another.
	 * 
	 * @param a The first value.
	 * @param b The second value.
	 * @example (/ 5.5 2.32)
	 */
	public Div
	(
		final FloatFunction a, 
		final FloatFunction b
	)
	{
		this.a = a;
		this.b = b;
	}

	//-------------------------------------------------------------------------

	@Override
	public float eval(Context context)
	{
		final float evalB = b.eval(context);
		if (evalB == 0)
			throw new IllegalArgumentException("Division by zero.");

		return a.eval(context) / evalB;
	}

	@Override
	public long gameFlags(Game game)
	{
		long flag = 0l;

		if (a != null)
			flag |= a.gameFlags(game);
		if (b != null)
			flag |= b.gameFlags(game);

		return flag;
	}

	@Override
	public BitSet concepts(Game game)
	{
		final BitSet concepts = new BitSet();

		if (a != null)
			concepts.or(a.concepts(game));
		if (b != null)
			concepts.or(b.concepts(game));

		concepts.set(Concept.Float.id(), true);
		concepts.set(Concept.Division.id(), true);

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
		if (a != null)
			a.preprocess(game);
		if (b != null)
			b.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (a != null)
			missingRequirement |= a.missingRequirement(game);
		if (b != null)
			missingRequirement |= b.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (a != null)
			willCrash |= a.willCrash(game);
		if (b != null)
			willCrash |= b.willCrash(game);
		return willCrash;
	}
}
