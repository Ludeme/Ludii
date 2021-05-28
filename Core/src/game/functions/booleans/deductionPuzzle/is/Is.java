package game.functions.booleans.deductionPuzzle.is;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.deductionPuzzle.is.graph.IsUnique;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsCount;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsSum;
import game.functions.booleans.deductionPuzzle.is.simple.IsSolved;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Whether the specified query is true for a deduction puzzle.
 * 
 * @author Eric.Piette and cambolbro
 * 
 */
@SuppressWarnings("javadoc")
public class Is extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * For solving a puzzle.
	 * 
	 * @param isType The query type to perform.
	 * 
	 * @example (is Solved)
	 */
	public static BooleanFunction construct
	(
		final IsPuzzleSimpleType isType
	)
	{
		switch (isType)
		{
		case Solved:
			return new IsSolved();
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsPuzzleSimpleType is not implemented.");
	}
	
	// -------------------------------------------------------------------------

	/**
	 * For the unique constraint.
	 * 
	 * @param isType      The query type to perform.
	 * @param elementType The graph element type [Cell].
	 * 
	 * @example (is Unique)
	 */
	public static BooleanFunction construct
	(
		     final IsPuzzleGraphType isType,
		@Opt final SiteType          elementType
	)
	{
		switch (isType)
		{
		case Unique:
			return new IsUnique(elementType);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsPuzzleGraphType is not implemented.");
	}
	
	// -------------------------------------------------------------------------

	/**
	 * For a constraint related to count or sum.
	 * 
	 * @param isType     The query type to perform.
	 * @param type       The graph element of the region [Default SiteType of the board].
	 * @param region     The region [Regions].
	 * @param of         The index of the piece [1].
	 * @param nameRegion The name of the region to check.
	 * @param result     The result to check.
	 * 
	 * @example (is Count (sites All) of:1 8)
	 * @example (is Sum 5)
	 */
	public static BooleanFunction construct
	(
		           final IsPuzzleRegionResultType isType,
		@Opt       final SiteType                 type,
		@Opt       final RegionFunction           region,
		@Opt @Name final IntFunction              of,
		@Opt       final String                   nameRegion,
			       final IntFunction              result
	)
	{
		switch (isType)
		{
		case Count:
			return new IsCount(type, region, of, result);
		case Sum:
			return new IsSum(type, region, nameRegion, result);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsPuzzleRegionResultType is not implemented.");
	}

	// -------------------------------------------------------------------------

	private Is()
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
		throw new UnsupportedOperationException("Is.eval(): Should never be called directly.");

		// return false;
	}
}
