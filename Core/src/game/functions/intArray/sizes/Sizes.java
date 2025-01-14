package game.functions.intArray.sizes;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.intArray.sizes.group.SizesGroup;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.Direction;
import other.context.Context;

/**
 * Returns an array of sizes of many regions.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Sizes extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For returning the sizes of all the groups.
	 * 
	 * @param sizesType  The property to return the size.
	 * @param type       The graph element type [default SiteType of the board].
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param role       The role of the player [All].
	 * @param of         The index of the player.
	 * @param If         The condition on the pieces to include in the group.
	 * @param min        Minimum size of each group [0].
	 * @param isVisible  If all items of group have to be visible
	 * 
	 * @example (sizes Group Orthogonal P1)
	 */
	public static IntArrayFunction construct
	(
		                final SizesGroupType  sizesType,
		@Opt 	        final SiteType        type,
		@Opt            final Direction       directions,
		@Opt @Or	    final RoleType        role,
		@Opt @Or @Name  final IntFunction     of,
		@Opt @Or @Name  final BooleanFunction If,
		@Opt     @Name  final IntFunction     min,
		@Opt @Name     final BooleanFunction isVisible
	)
	{
		int numNonNull = 0;
		if (role != null)
			numNonNull++;
		if (of != null)
			numNonNull++;
		if (If != null)
			numNonNull++;
		
		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Sizes(): With SizesGroupType zero or one 'role' or 'of' or 'If' parameters must be non-null.");
		switch (sizesType)
		{
		case Group:
			return new SizesGroup(type, directions, role, of, If, min, isVisible);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sizes(): A SizeGroupType is not implemented.");
	}

	private Sizes()
	{
		// Make grammar pick up construct() and not default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Sizes.eval(): Should never be called directly.");
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
