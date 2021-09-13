package game.functions.booleans.deductionPuzzle.all;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Whether the specified query is all true for a deduction puzzle.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public class All extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * @param allType     The query type to perform.
	 * @param elementType Type of graph elements to return [Cell].
	 * @param region      The region to check [Regions].
	 * @param except      The exception on the test.
	 * @param excepts     The exceptions on the test.
	 * 
	 * @example (all Different)
	 * 
	 * @example (all Different except:0)
	 */
	public static BooleanFunction construct
	(
			           final AllPuzzleType  allType, 
		@Opt           final SiteType       elementType, 
		@Opt           final RegionFunction region,
		@Opt @Or @Name final IntFunction    except,
		@Opt @Or @Name final IntFunction[]  excepts
	)
	{
		int numNonNull = 0;
		if (except != null)
			numNonNull++;
		if (excepts != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"All(): With AllPuzzleType zero or one except or excepts parameter must be non-null.");

		switch (allType)
		{
		case Different:
			return new AllDifferent(elementType, region, except, excepts);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("All(): A AllPuzzleType is not implemented.");
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
