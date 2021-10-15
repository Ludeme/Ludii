package game.functions.booleans.can;

import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.Moves;
import other.context.Context;

/**
 * Returns whether a given property can be achieved in the current game state.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public class Can extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param canType Type of query.
	 * @param moves   List of moves.
	 * 
	 * @example (can Move (forEach Piece))
	 */
	public static BooleanFunction construct
	(
		final CanType canType, 
		final Moves   moves
	)
	{
		switch (canType)
		{
		case Move:
			return new CanMove(moves);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Can(): A CanType is not implemented.");
	}

	//-------------------------------------------------------------------------

	private Can()
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
		throw new UnsupportedOperationException("Can.eval(): Should never be called directly.");
	}
}
