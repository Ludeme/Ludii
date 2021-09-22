package search.mcts.backpropagation;

import other.context.Context;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;
import utils.AIUtils;

/**
 * Implementation of backpropagation that uses heuristic value estimates
 * for any player that is still active at the end of a playout, instead
 * of defaulting to 0.0
 * 
 * @author Dennis Soemers
 */
public class HeuristicBackprop extends BackpropagationStrategy
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
		assert (mcts.heuristics() != null);
		
		if (context.active())
		{
			// Playout did not terminate, so should run heuristics at end of playout
			final double[] playoutHeuristicValues = AIUtils.heuristicValueEstimates(context, mcts.heuristics());
			for (int p = 1; p < utilities.length; ++p)
			{
				utilities[p] = playoutHeuristicValues[p];
			}
		}
	}
	
	@Override
	public int backpropagationFlags()
	{
		return 0;
	}

}
