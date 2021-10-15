package game.functions.floats.math;

import java.util.BitSet;

import annotations.Alias;
import game.Game;
import game.functions.floats.BaseFloatFunction;
import game.functions.floats.FloatFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Adds many values.
 * 
 * @author Eric.Piette
 */
@Alias(alias = "+")
public final class Add extends BaseFloatFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The first value. */
	private final FloatFunction a;

	/** The second value. */
	private final FloatFunction b;

	/** The list of values. */
	protected final FloatFunction[] list;

	//-------------------------------------------------------------------------

	/**
	 * To add two values.
	 * 
	 * @param a The first value.
	 * @param b The second value.
	 * @example (+ 5.5 2.32)
	 */
	public Add
	(
		final FloatFunction a, 
		final FloatFunction b
	)
	{
		this.a = a;
		this.b = b;
		list = null;
	}

	/**
	 * To add all the values of a list.
	 * 
	 * @param list The list of the values.
	 * @example (+ {10.1 2.8 5.1})
	 */
	public Add(final FloatFunction[] list)
	{
		a = null;
		b = null;
		this.list = list;
	}

	//-------------------------------------------------------------------------

	@Override
	public float eval(final Context context)
	{
		if (list == null)
		{
			return a.eval(context) + b.eval(context);
		}

		float sum = 0f;
		for (final FloatFunction elem : list)
			sum += elem.eval(context);

		return sum;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flag = 0l;

		if (a != null)
			flag |= a.gameFlags(game);
		if (b != null)
			flag |= b.gameFlags(game);
		if (list != null)
			for (final FloatFunction elem : list)
				flag |= elem.gameFlags(game);

		return flag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (a != null)
			concepts.or(a.concepts(game));
		if (b != null)
			concepts.or(b.concepts(game));
		if (list != null)
			for (final FloatFunction elem : list)
				concepts.or(elem.concepts(game));

		concepts.set(Concept.Float.id(), true);
		concepts.set(Concept.Addition.id(), true);

		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (a != null)
			missingRequirement |= a.missingRequirement(game);
		if (b != null)
			missingRequirement |= b.missingRequirement(game);
		if (list != null)
			for (final FloatFunction elem : list)
				missingRequirement |= elem.missingRequirement(game);
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
		if (willCrash)
			for (final FloatFunction elem : list)
				willCrash |= elem.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (a != null)
			a.preprocess(game);
		if (b != null)
			b.preprocess(game);
		if (list != null)
			for (final FloatFunction elem : list)
				elem.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return a.toEnglish(game) + " + " + b.toEnglish(game);
	}
}
