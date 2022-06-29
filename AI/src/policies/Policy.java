package policies;

import main.collections.FVector;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import search.mcts.playout.PlayoutStrategy;

/**
 * A policy is something that can compute distributions over actions in a given
 * state (presumably using some form of function approximation).
 * 
 * Policies should also implement the methods required to function as
 * Play-out strategies for MCTS or function as a full AI agent.
 * 
 * @author Dennis Soemers
 */
public abstract class Policy extends AI implements PlayoutStrategy 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param actions
	 * @param thresholded
	 * @return Probability distribution over the given list of actions in the given state.
	 */
	public abstract FVector computeDistribution
	(
		final Context context, 
		final FastArrayList<Move> actions,
		final boolean thresholded
	);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param move
	 * @return Logit for a single move in a single state
	 */
	public abstract float computeLogit(final Context context, final Move move);
	
	//-------------------------------------------------------------------------

}
