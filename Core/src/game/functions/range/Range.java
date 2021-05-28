package game.functions.range;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import other.context.Context;

/**
 * Returns a range of values (inclusive) according to some specified condition.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Range extends BaseRangeFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For a range between two int functions.
	 * 
	 * @param min Lower extent of range (inclusive).
	 * @param max Upper extent of range (inclusive) [same as min].
	 *
	 * @example (range (from) (to))
	 */
	public Range
	(
			 final IntFunction min,
		@Opt final IntFunction max
	)
	{
		super(min, max == null ? min : max);
	}

	//-------------------------------------------------------------------------

	@Override
	public Range eval(final Context context)
	{
		if (precomputedRange != null)
			return precomputedRange;

		return this;  //new game.util.math.Range(Integer.valueOf(minFn.eval(context)), Integer.valueOf(maxFn.eval(context)));
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context The context.
	 * 
	 * @return The minimum of the range.
	 */
	public int min(final Context context)
	{
		return minFn.eval(context);
	}

	/**
	 * @param context The context.
	 * 
	 * @return The maximum of the range.
	 */
	public int max(final Context context)
	{
		return maxFn.eval(context);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return minFn.isStatic() && maxFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return minFn.gameFlags(game) | maxFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(minFn.concepts(game));
		concepts.or(maxFn.concepts(game));
		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= minFn.missingRequirement(game);
		missingRequirement |= maxFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= minFn.willCrash(game);
		willCrash |= maxFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		minFn.preprocess(game);
		maxFn.preprocess(game);

		if (isStatic())
			precomputedRange = eval(new Context(game, null));
	}
	
	@Override
	public String toString()
	{
		return "[" + max(null) + ";" + min(null) + "]";
	}

	//-------------------------------------------------------------------------
	
}
