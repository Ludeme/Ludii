package search.mcts.backpropagation;

import other.context.Context;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Standard backpropagation implementation for MCTS, performing Monte-Carlo backups
 * of playout outcomes.
 * 
 * @author Dennis Soemers
 */
public class MonteCarloBackprop extends BackpropagationStrategy
{
	
	@Override
	public void computeUtilities
	(
		final MCTS mcts,
		final BaseNode startNode, 
		final Context context, 
		final double[] utilities, 
		final int numPlayoutMoves
	)
	{
		// Do nothing
	}

}
