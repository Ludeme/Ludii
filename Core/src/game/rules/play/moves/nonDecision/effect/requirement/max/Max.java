package game.rules.play.moves.nonDecision.effect.requirement.max;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.requirement.max.distance.MaxDistance;
import game.rules.play.moves.nonDecision.effect.requirement.max.moves.MaxCaptures;
import game.rules.play.moves.nonDecision.effect.requirement.max.moves.MaxMoves;
import game.types.play.RoleType;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Filters a list of legal moves to keep only the moves allowing the maximum
 * number of moves in a turn.
 * 
 * @author Eric.Piette and cambolbro
 */
@SuppressWarnings("javadoc")
public final class Max extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For getting the moves with the max captures or the max number of legal moves
	 * in the turn.
	 * 
	 * @param maxType    The type of property to maximise.
	 * @param moves      The moves to filter.
	 * @param withValue  If true, the capture has to maximise the values of the capturing pieces too.
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (max Moves (forEach Piece))
	 * 
	 * @example (max Captures (forEach Piece))
	 */
	public static Moves construct
	(
			       final MaxMovesType    maxType,
		@Opt @Name final BooleanFunction withValue,
			       final Moves           moves, 
		@Opt       final Then            then
	)
	{
		switch (maxType)
		{
		case Captures:
			return new MaxCaptures(withValue, moves, then);
		case Moves:
			return new MaxMoves(withValue, moves, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Max(): A MaxMovesType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting the moves with the max distance.
	 * 
	 * @param maxType   The type of property to maximise.
	 * @param trackName The name of the track.
	 * @param owner     The owner of the track.
	 * @param moves     The moves to filter.
	 * @param then      The moves applied after that move is applied.
	 * 
	 * @example (max Distance (forEach Piece))
	 */
	public static Moves construct
	(
			 final MaxDistanceType maxType,
		@Opt final String          trackName,
		@Opt final RoleType        owner, 
			 final Moves           moves, 
		@Opt final Then            then
	)
	{
		switch (maxType)
		{
		case Distance:
			return new MaxDistance(trackName, owner, moves, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Max(): A MaxDistanceType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	private Max()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Max.eval(): Should never be called directly.");
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

	@Override
	public boolean canMoveTo(Context context, int target)
	{
		// Should never be there
		throw new UnsupportedOperationException("Max.canMoveTo(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------

}
