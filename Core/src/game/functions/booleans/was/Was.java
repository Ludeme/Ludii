package game.functions.booleans.was;

import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import other.context.Context;

/**
 * Returns whether a specified event has occurred in the game.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public class Was extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param wasType The event to query.
	 * 
	 * @example (was Pass)
	 */
	public static BooleanFunction construct
	(
		final WasType wasType
	)
	{
		switch (wasType)
		{
		case Pass:
			return new WasPass();
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Was(): A wasType is not implemented.");
	}

	//-------------------------------------------------------------------------

	private Was()
	{
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public boolean isStatic()
	{
		// Should never be there
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		// Should never be there
		return 0L;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public boolean eval(Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Was.eval(): Should never be called directly.");

		// return false;
	}
}
