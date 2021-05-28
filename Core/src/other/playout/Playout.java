package other.playout;

import java.util.List;
import java.util.Random;

import other.AI;
import other.context.Context;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * Custom playout types.
 * 
 * @author cambolbro
 */
public interface Playout
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Play out game to conclusion from current state.
	 * 
	 * @param context
	 * @param ais
	 * @param thinkingTime The maximum number of seconds that AIs are allowed 
	 * to spend per turn
	 * @param playoutMoveSelector A playout move selector to select moves non-uniformly
	 * @param maxNumBiasedActions Maximum number of actions for which to bias
	 * selection using features (-1 for no limit)
	 * @param maxNumPlayoutActions Maximum number of actions to be applied,
	 * after which we will simply return a null result (-1 for no limit)
	 * @param random RNG for selecting actions
	 * @return Fully played-out Trial object.
	 */
	public abstract Trial playout
	(
		final Context context, 
		final List<AI> ais, 
		final double thinkingTime,
		final PlayoutMoveSelector playoutMoveSelector,
		final int maxNumBiasedActions,
		final int maxNumPlayoutActions,
		final Random random
	);
	
	/**
	 * @return Should return true if the playout implementation still calls
	 * game.moves() to compute the list of legal moves in every game state, 
	 * or false otherwise.
	 */
	public abstract boolean callsGameMoves();
	
	//-------------------------------------------------------------------------
	
}
