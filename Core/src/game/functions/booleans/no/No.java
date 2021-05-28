package game.functions.booleans.no;

import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
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

	// -------------------------------------------------------------------------

	/**
	 * @param noType   The type of query to perform.
	 * @param playerFn The role of the player.
	 * 
	 * @example (no Moves Mover)
	 */
	public static BooleanFunction construct
	(
		final NoType   noType, 
		final RoleType playerFn
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
		throw new IllegalArgumentException("No(): A NoType is not implemented.");
	}

	// -------------------------------------------------------------------------

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
