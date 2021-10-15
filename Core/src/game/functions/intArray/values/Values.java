package game.functions.intArray.values;

import annotations.Opt;
import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.IntArrayFunction;
import other.context.Context;

/**
 * Returns an array of values.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Values extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For returning the sizes of all the groups.
	 * 
	 * @param sizesType The property to return the size.
	 * @param name      The name of the remembering values.
	 * 
	 * @example (values Remembered)
	 */
	public static IntArrayFunction construct
	(
			 final ValuesStringType valuesType, 
		@Opt final String           name
	)
	{
		switch (valuesType)
		{
		case Remembered:
			return new ValuesRemembered(name);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Values(): A ValuesStringType is not implemented.");
	}

	private Values()
	{
		// Make grammar pick up construct() and not default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Values.eval(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------

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
}
