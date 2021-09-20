package game.functions.ints;

import other.BaseLudeme;
import other.context.Context;

/**
 * Common functionality for IntFunction - override where necessary.
 * 
 * @author mrraow
 */
public abstract class BaseIntFunction extends BaseLudeme implements IntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	@Override
	public boolean isHint()
	{
		return false;
	}
	
	/**
	 * @return if is in hand.
	 */
	@Override
	public boolean isHand()
	{
		return false;
	}
	
	@Override
	public boolean exceeds(final Context context, final IntFunction other)
	{
		return eval(context) > other.eval(context);
	}
}
