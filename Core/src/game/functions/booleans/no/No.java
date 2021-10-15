package game.functions.booleans.no;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.no.moves.NoMoves;
import game.functions.booleans.no.pieces.NoPieces;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Returns whether a certain query about the game state is false.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public class No extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For checking if a piece type (or all piece types) are not placed.
	 * 
	 * @param noType  The type of query to perform.
	 * @param type    The graph element type [default SiteType of the board].
	 * @param role    The role of the player [All].
	 * @param of      The index of the player.
	 * @param name    The name of the container from which to count the number of
	 *                sites or the name of the piece to count only pieces of that
	 *                type.
	 * @param in      The region where to count the pieces.
	 * 
	 * @example (no Pieces Mover)
	 */
	public static BooleanFunction construct
	(
					   final NoPieceType    noType, 
		@Opt           final SiteType       type,
		@Opt @Or       final RoleType       role, 
		@Opt @Or @Name final IntFunction    of, 
		@Opt           final String         name,
		@Opt @Name     final RegionFunction in
	)
	{
		switch (noType)
		{
		case Pieces:
			return new NoPieces(type, role, of, name, in);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("No(): A NoPieceType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For checking if a specific player (or all players) have no moves.
	 * 
	 * @param noType   The type of query to perform.
	 * @param playerFn The role of the player.
	 * 
	 * @example (no Moves Mover)
	 */
	public static BooleanFunction construct
	(
		final NoMoveType noType, 
		final RoleType   playerFn
	)
	{
		switch (noType)
		{
		case Moves:
			return new NoMoves(playerFn);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("No(): A NoMoveType is not implemented.");
	}

	//-------------------------------------------------------------------------

	private No()
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
		throw new UnsupportedOperationException("No.eval(): Should never be called directly.");

		// return false;
	}
}
