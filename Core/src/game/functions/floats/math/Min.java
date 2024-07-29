package game.functions.floats.math;

import java.util.BitSet;

import game.Game;
import game.functions.floats.BaseFloatFunction;
import game.functions.floats.FloatFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the minimum of specified values.
 * 
 * @author Eric.Piette
 */
public final class Min extends BaseFloatFunction
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
	 * To get the minimum value between two.
	 * 
	 * @param a The first value.
	 * @param b The second value.
	 * @example (min 5.5 2.32)
	 */
	public Min
	(
		final FloatFunction a, 
		final FloatFunction b
	)
	{
		this.a = a;
		this.b = b;
		this.list = null;
	}

	/**
	 * To get the minimum value in a list.
	 * 
	 * @param list The list of the values.
	 * @example (min {10.1 2.8 5.1})
	 */
	public Min(final FloatFunction[] list)
	{
		this.a = null;
		this.b = null;
		this.list = list;
	}

	//-------------------------------------------------------------------------

	@Override
	public float eval(Context context)
	{
		if (list == null)
		{
			return Math.min(a.eval(context),b.eval(context));
		}

		float min = list[0].eval(context);
		for (int i = 1 ; i < list.length; i++)
			min = Math.min(min, list[i].eval(context));

		return min;
	}

	@Override
	public long gameFlags(Game game)
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
	public BitSet concepts(Game game)
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
		concepts.set(Concept.Minimum.id(), true);

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
		if (list != null)
			for (final FloatFunction elem : list)
				elem.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (a != null)
			missingRequirement |= a.missingRequirement(game);
		if (b != null)
			missingRequirement |= b.missingRequirement(game);
		b.preprocess(game);
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
		b.preprocess(game);
		if (list != null)
			for (final FloatFunction elem : list)
				willCrash |= elem.willCrash(game);
		return willCrash;
	}
}
