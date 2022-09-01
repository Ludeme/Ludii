package other.playout;

import java.util.Random;

import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;

/**
 * Abstract class for an object that can efficiently select moves in playouts
 * (including custom, optimised playout strategies)
 *
 * @author Dennis Soemers
 */
public abstract class PlayoutMoveSelector
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Method which should be overridden to return a move to play.
	 * 
	 * NOTE: this method is allowed to modify the maybeLegalMoves list
	 * 
	 * @param context
	 * @param maybeLegalMoves
	 * @param p Player for which to make a move
	 * @param isMoveReallyLegal If not null, a functor that tells us if a move is REALLY legal
	 * @return Move to play. Should return null if no legal move is found
	 */
	public abstract Move selectMove
	(
		final Context context, 
		final FastArrayList<Move> maybeLegalMoves, 
		final int p,
		final IsMoveReallyLegal isMoveReallyLegal
	);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Can be implemented to return true if a MoveSelector wants to play a move
	 * selected uniformly at random. When this happens, a playout implementation
	 * may be able to implement this more efficiently that the MoveSelector itself
	 * would.
	 * 
	 * @return True if the MoveSelector wants to select a move uniformly at random.
	 */
	@SuppressWarnings("static-method")
	public boolean wantsPlayUniformRandomMove()
	{
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Selects a move uniformly at random. NOTE: this method may modify the
	 * given list of maybe-legal-moves.
	 * 
	 * @param context
	 * @param maybeLegalMoves
	 * @param isMoveReallyLegal If not null, a functor that tells us if a move is REALLY legal
	 * @param random Random number generator to use for selecting moves
	 * @return Move selected uniformly at random, or null if no legal move found.
	 */
	public static Move selectUniformlyRandomMove
	(
		final Context context, 
		final FastArrayList<Move> maybeLegalMoves, 
		final IsMoveReallyLegal isMoveReallyLegal,
		final Random random
	)
	{
		while (!maybeLegalMoves.isEmpty())
		{
			final int moveIdx = random.nextInt(maybeLegalMoves.size());
			final Move move = maybeLegalMoves.removeSwap(moveIdx);

			if (isMoveReallyLegal.checkMove(move))
				return move;
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Interface for a functor that can be used to check if a move is really legal
	 * Normally used for moves that are already likely to be legal, but may turn out 
	 * to be illegal due to some expensive-to-evaluate test (like IsThreatened stuff)
	 *
	 * @author Dennis Soemers
	 */
	public interface IsMoveReallyLegal
	{
		/**
		 * The function
		 * 
		 * @param move
		 * @return True if the move is really legal
		 */
		boolean checkMove(final Move move);
	}
	
	//-------------------------------------------------------------------------

}
