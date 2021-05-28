package game.functions.ints.size;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.size.array.SizeArray;
import game.functions.ints.size.connection.SizeGroup;
import game.functions.ints.size.connection.SizeTerritory;
import game.functions.ints.size.largePiece.SizeLargePiece;
import game.functions.ints.size.site.SizeStack;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import other.context.Context;

/**
 * Returns the size of the specified property.
 * 
 * @author Eric Piette
 */
@SuppressWarnings("javadoc")
public final class Size extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For the size of a stack.
	 * 
	 * @param sizeType The property to return the size.
	 * @param array    The array.
	 * 
	 * @example (size Array (values Remembered))
	 */
	public static IntFunction construct
	(
		final SizeArrayType    sizeType,
		final IntArrayFunction array 
	)
	{
		switch (sizeType)
		{
		case Array:
			return new SizeArray(array);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Size(): A SizeArrayType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For the size of a stack.
	 * 
	 * @param sizeType The property to return the size.
	 * @param type     The graph element type [default SiteType of the board].
	 * @param in       The region to count.
	 * @param at       The site from which to compute the count [(last To)].
	 * 
	 * @example (size Stack at:(last To))
	 */
	public static IntFunction construct
	(
			           final SizeSiteType   sizeType,
		@Opt           final SiteType       type,
		@Opt @Or @Name final RegionFunction in, 
		@Opt @Or @Name final IntFunction    at 
	)
	{
		int numNonNull = 0;
		if (in != null)
			numNonNull++;
		if (at != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Size(): With SizeSiteType zero or one 'in' or 'at' parameters must be non-null.");

		switch (sizeType)
		{
		case Stack:
			return new SizeStack(type, in, at);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Size(): A SizeSiteType is not implemented.");
	}
	
	// -------------------------------------------------------------------------

	/**
	 * For the size of large pieces currently placed.
	 * 
	 * @param sizeType The property to return the size.
	 * @param type     The graph element type [default site type of the board].
	 * @param in       The region to look for large pieces.
	 * @param at       The site to look for large piece.
	 * 
	 * @example (size LargePiece at:(last To))
	 */
	public static IntFunction construct
	(
			           final SizeLargePieceType sizeType,
		@Opt           final SiteType           type,
		     @Or @Name final RegionFunction     in, 
		     @Or @Name final IntFunction        at 
	)
	{
		int numNonNull = 0;
		if (in != null)
			numNonNull++;
		if (at != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Size(): With SizeLargePiece one 'in' or 'at' parameters must be non-null.");

		switch (sizeType)
		{
		case LargePiece:
			return new SizeLargePiece(type, in, at);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Size(): A SizeLargePiece is not implemented.");
	}

	/**
	 * For the size of a group.
	 * 
	 * @param sizeType   The property to return the size.
	 * @param type       The graph element type [default SiteType of the board].
	 * @param at         The site to compute the group [(last To)].
	 * @param directions The type of directions from the site to compute the group
	 *                   [Adjacent].
	 * @param If         The condition of the members of the group [(= (mover) (who
	 *                   at:(to)))].
	 * 
	 * @example (size Group at:(last To) Orthogonal)
	 */
	public static IntFunction construct
	(
		@Opt       final SizeGroupType   sizeType, 
		@Opt       final SiteType        type, 
			 @Name final IntFunction     at,
		@Opt       final Direction       directions,
		@Opt @Name final BooleanFunction If
	)
	{
		switch (sizeType)
		{
		case Group:
			return new SizeGroup(type, at, directions, If);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Size(): A SizeGroupType is not implemented.");
	}
	
	/**
	 * For the size of a territory.
	 * 
	 * @param sizeType  The property to return the size.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param role      The roleType of the player owning the components in the
	 *                  territory.
	 * @param player    The index of the player owning the components in the
	 *                  territory.
	 * @param direction The type of directions from the site to compute the group
	 *                  [Adjacent].
	 * 
	 * @example (size Territory P1)
	 */
	public static IntFunction construct
	(
			@Opt final SizeTerritoryType      sizeType,
			@Opt final SiteType               type,
		@Or      final RoleType               role, 
		@Or      final game.util.moves.Player player,
			@Opt final AbsoluteDirection      direction
	)
	{
		int numNonNull = 0;
		if (role != null)
			numNonNull++;
		if (player != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Size(): With SizeTerritoryType only one role or player parameter must be non-null.");
		
		switch (sizeType)
		{
		case Territory:
			return new SizeTerritory(type, role, player, direction);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Size(): A SizeTerritoryType is not implemented.");
	}
	
	private Size()
	{
		// Make grammar pick up construct() and not default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Size.eval(): Should never be called directly.");

		// return new Region();
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
}