package search.mcts.backpropagation;

import main.math.IncrementalStats;
import other.context.Context;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;
import utils.AIUtils;

/**
 * Implements a Qualitative bonus (based on heuristic value function estimates),
 * as described in "Quality-based Rewards for Monte-Carlo Tree Search Simulations"
 * 
 * @author Dennis Soemers
 */
public class QualitativeBonus extends BackpropagationStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Constant used in sigmoid squashing of bonus */
	private final double k = 1.4;
	
	/** Weight assigned to bonuses */
	private final double a = 0.25;
	
	//-------------------------------------------------------------------------
	
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
		
		final double[] heuristicValues = AIUtils.heuristicValueBonusEstimates(context, mcts.heuristics());
		final IncrementalStats[] heuristicStats = mcts.heuristicStats();
		
		for (int p = 1; p < heuristicValues.length; ++p)
		{
			final IncrementalStats stats = heuristicStats[p];
			final double q = heuristicValues[p];
			final double std = stats.getStd();
			
			if (std > 0.0)
			{
				// Apply bonus
				final double lambda = (q - stats.getMean()) / std;
				final double bonus = -1.0 + (2.0 / (1.0 + Math.exp(-k * lambda)));
				utilities[p] += a * bonus;	// Not including sign(r) since our bonuses are from p perspective, not from winner's perspective
			}
			
			// Update incremental stats tracker
			stats.observe(q);
		}
	}
	
	@Override
	public int backpropagationFlags()
	{
		return BackpropagationStrategy.GLOBAL_HEURISTIC_STATS;
	}
	
	//-------------------------------------------------------------------------

}
