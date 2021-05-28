package game.functions.booleans.all;

import annotations.Name;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.all.simple.AllDiceEqual;
import game.functions.booleans.all.simple.AllDiceUsed;
import game.functions.booleans.all.simple.AllPassed;
import game.functions.booleans.all.sites.AllDifferent;
import game.functions.booleans.all.sites.AllSites;
import game.functions.booleans.all.values.AllValues;
import game.functions.intArray.IntArrayFunction;
import game.functions.region.RegionFunction;
import other.context.Context;

/**
 * Returns whether all aspects of the specified query are true.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public class All extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * For checking a condition in each value of a integer array.
	 * 
	 * @param allType The query type to perform.
	 * @param array   The array to check.
	 * @param If      The condition to check.
	 * 
	 * @example (all Values (values Remembered) if:(> 2 (value)))
	 */
	public static BooleanFunction construct
	(
			  final AllValuesType    allType,
			  final IntArrayFunction array,
		@Name final BooleanFunction  If
	)
	{
		switch (allType)
		{
		case Values:
			return new AllValues(array, If);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("All(): A AllType is not implemented.");
	}
	
	/**
	 * For checking a condition in each site of a region.
	 * 
	 * @param allType The query type to perform.
	 * @param region  The region to check.
	 * @param If      The condition to check.
	 * 
	 * @example (all Sites (sites Occupied by:Mover) if:(= 2 (size Stack
	 *          at:(site))))
	 */
	public static BooleanFunction construct
	(
			  final AllSitesType    allType,
			  final RegionFunction  region,
		@Name final BooleanFunction If
	)
	{
		switch (allType)
		{
		case Sites:
			return new AllSites(region, If);
		case Different:
			return new AllDifferent(region, If);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("All(): A AllType is not implemented.");
	}

	/**
	 * For a test with no parameter.
	 * 
	 * @param allType The query type to perform.
	 * 
	 * @example (all DiceUsed)
	 * @example (all Passed)
	 * @example (all DiceEqual)
	 */
	public static BooleanFunction construct
	(
		final AllSimpleType allType
	)
	{
		switch (allType)
		{
		case DiceUsed:
			return new AllDiceUsed();
		case Passed:
			return new AllPassed();
		case DiceEqual:
			return new AllDiceEqual();
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("All(): A AllType is not implemented.");
	}

	// -------------------------------------------------------------------------

	private All()
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
		throw new UnsupportedOperationException("All.eval(): Should never be called directly.");

		// return false;
	}

}
