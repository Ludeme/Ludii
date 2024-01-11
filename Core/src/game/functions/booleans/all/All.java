package game.functions.booleans.all;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.all.groups.AllGroups;
import game.functions.booleans.all.simple.AllDiceEqual;
import game.functions.booleans.all.simple.AllDiceUsed;
import game.functions.booleans.all.simple.AllPassed;
import game.functions.booleans.all.sites.AllDifferent;
import game.functions.booleans.all.sites.AllSites;
import game.functions.booleans.all.values.AllValues;
import game.functions.intArray.IntArrayFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.util.directions.Direction;
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

	//-------------------------------------------------------------------------

	/**
	 * For checking a condition in each group of the board.
	 * 
	 * @param allType The query type to perform.
	 * @param type        The type of the graph elements of the group.
	 * @param directions  The directions of the connection between elements in the
	 *                    group [Adjacent].
	 * @param of          The condition on the pieces to include in the group [(= (to) (mover))].
	 * @param If          The condition for each group to verify.
	 * 
	 * @example (all Groups if:(= 3 (count Sites in:(sites))))
	 */
	public static BooleanFunction construct
	(
			       final AllGroupsType    allType,
		@Opt       final SiteType         type,
		@Opt       final Direction        directions,
		@Opt @Name final BooleanFunction  of,
			 @Name final BooleanFunction  If
	)
	{
		switch (allType)
		{
		case Groups:
			return new AllGroups(type,directions, of, If);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("All(): A AllGroupsType is not implemented.");
	}
	
	/**
	 * For checking a condition in each value of an integer array.
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
		throw new IllegalArgumentException("All(): A AllValuesType is not implemented.");
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
		throw new IllegalArgumentException("All(): A AllSitesType is not implemented.");
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
		throw new IllegalArgumentException("All(): A AllSimpleType is not implemented.");
	}

	//-------------------------------------------------------------------------

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
	public boolean eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("All.eval(): Should never be called directly.");

		// return false;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "all of the following is true:";
	}

}
