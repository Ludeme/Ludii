package game.functions.range;

import annotations.Hide;
import game.Game;
import game.functions.ints.IntFunction;
import other.BaseLudeme;
import other.context.Context;

/**
 * Common functionality for RangeFunction - override where necessary.
 * 
 * @author cambolbro and Eric.Piette
 */
@Hide
public abstract class BaseRangeFunction extends BaseLudeme implements RangeFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Lower extent of range (inclusive). */
	protected final IntFunction minFn;

	/** Upper extent of range (inclusive). */
	protected final IntFunction maxFn;

	/** Precompute once and cache if possible. */
	protected Range precomputedRange = null;

	//-------------------------------------------------------------------------

	/**
	 * The base range function.
	 * 
	 * @param min The minimum of the range.
	 * @param max The maximum of the range.
	 */
	public BaseRangeFunction
	(
		final IntFunction min, 
		final IntFunction max
	)
	{
		minFn = min;
		maxFn = max;
	}

	//-------------------------------------------------------------------------

	@Override
	public Range eval(final Context context)
	{
		System.out.println("BaseRangeFunction.eval(): Should not be called directly; call subclass.");		
		return null;
	}

	//-------------------------------------------------------------------------

	@Override
	public IntFunction minFn()
	{
		return minFn;
	}

	@Override
	public IntFunction maxFn()
	{
		return maxFn;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game) 
	{
		return "within the range [" + minFn.toEnglish(game) + "," + maxFn.toEnglish(game) + "]";
	}
	
	//-------------------------------------------------------------------------

}
