package search.mcts.backpropagation;

import other.context.Context;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;
import utils.AIUtils;

/**
 * An AlphaGo-style backpropagation, that returns a convex combination 
 * of a heuristic value function evaluated at the expanded node and
 * a heuristic value function evaluated at the end of a playout.
 * 
 * Can also be used for Alpha(Go) Zero style backpropagations
 * by simply using a weight of 0.0 for playout value, and 1.0 
 * for the expanded node's value (plus, for efficiency, using
 * 0-length playouts).
 * 
 * @author Dennis Soemers
 */
public class AlphaGoBackprop extends BackpropagationStrategy
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
		
		final double playoutValueWeight = mcts.playoutValueWeight();
		
		final double[] nodeHeuristicValues;
		
		if (playoutValueWeight < 1.0)
		{
			// Mix value function of expanded node with playout outcome (like AlphaGo)
			nodeHeuristicValues = startNode.heuristicValueEstimates();
		}
		else
		{
			// This array is irrelevant
			nodeHeuristicValues = new double[utilities.length];
		}
		
		if (context.active() && playoutValueWeight > 0.0)
		{
			// Playout did not terminate, so should also run heuristics at end of playout
			final double[] playoutHeuristicValues = AIUtils.heuristicValueEstimates(context, mcts.heuristics());
			for (int p = 1; p < utilities.length; ++p)
			{
				utilities[p] = playoutHeuristicValues[p];
			}
		}
		
		for (int p = 1; p < utilities.length; ++p)
		{
			// Mix node and playout values
			utilities[p] = playoutValueWeight * utilities[p] + (1.0 - playoutValueWeight) * nodeHeuristicValues[p];
		}
	}

}
