package other;

import java.util.BitSet;

import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import other.context.Context;

/**
 * Optimised class to get an integer array of non negative values from an
 * IntFunction or a RegionFunction.
 * 
 * @author Eric.Piette
 */
public class IntArrayFromRegion
{
	/** The IntegerFunction. */
	private final IntFunction intFunction;

	/** The RegionFunction. */
	private final RegionFunction regionFunction;
	
	/** Precomputed int[]. */
	private int[] precomputedArray;
	
	/**
	 * @param intFunction    The IntFunction.
	 * @param regionFunction The RegionFunction.
	 */
	public IntArrayFromRegion
	(
		final IntFunction intFunction,
		final RegionFunction regionFunction
	)
	{
		this.intFunction = intFunction;
		this.regionFunction = regionFunction;
	}
	
	/**
	 * @param context The context.
	 * @return The result of the evaluation of the functions.
	 */
	public int[] eval(final Context context)
	{
		if (precomputedArray != null)
			return precomputedArray;
		
		if (intFunction != null)
		{
			final int value = intFunction.eval(context);
			if (value >= 0)
				return new int[]
				{ value };
		}
		else if (regionFunction != null)
		{
			return regionFunction.eval(context).sites();
		}

		return new int[0];
	}
	
	/**
	 * @return True if static.
	 */
	public boolean isStatic()
	{
		if (intFunction != null && !intFunction.isStatic())
			return false;

		if (regionFunction != null && !regionFunction.isStatic())
			return false;

		return true;
	}

	/**
	 * @param game The game.
	 * @return The game flags.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (intFunction != null)
			gameFlags |= intFunction.gameFlags(game);

		if (regionFunction != null)
			gameFlags |= regionFunction.gameFlags(game);

		return gameFlags;
	}
	
	/**
	 * @param game The game.
	 * @return The concepts.
	 */
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (intFunction != null)
			concepts.or(intFunction.concepts(game));
		if (regionFunction != null)
			concepts.or(regionFunction.concepts(game));
		return concepts;
	}

	/**
	 * @return The bitset about writing EvalContext variables.
	 */
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (intFunction != null)
			writeEvalContext.or(intFunction.writesEvalContextRecursive());
		if (regionFunction != null)
			writeEvalContext.or(regionFunction.writesEvalContextRecursive());
		return writeEvalContext;
	}

	/**
	 * @return The bitset about reading EvalContext variables.
	 */
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (intFunction != null)
			readEvalContext.or(intFunction.readsEvalContextRecursive());
		if (regionFunction != null)
			readEvalContext.or(regionFunction.readsEvalContextRecursive());
		return readEvalContext;
	}

	/**
	 * @param game The game.
	 * @return The missing requirements.
	 */
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (intFunction != null)
			missingRequirement |= intFunction.missingRequirement(game);
		if (regionFunction != null)
			missingRequirement |= regionFunction.missingRequirement(game);
		return missingRequirement;
	}

	/**
	 * @param game The game.
	 * @return The will crash information.
	 */
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (intFunction != null)
			willCrash |= intFunction.willCrash(game);
		if (regionFunction != null)
			willCrash |= regionFunction.willCrash(game);
		return willCrash;
	}

	/**
	 * Preprocess method.
	 * 
	 * @param game The game.
	 */
	public void preprocess(final Game game)
	{
		if (intFunction != null)
			intFunction.preprocess(game);
		if (regionFunction != null)
			regionFunction.preprocess(game);

		if (isStatic())
			precomputedArray = eval(new Context(game, null));
	}
	
	@Override
	public String toString()
	{
		if (intFunction != null)
			return "[IntArrayFromRegion: " + intFunction + "]";
		else if (regionFunction != null)
			return "[IntArrayFromRegion: " + regionFunction + "]";
		else
			return "[Empty IntArrayFromRegion]";
	}

}
