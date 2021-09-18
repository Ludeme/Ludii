package search.mcts.backpropagation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;
import search.mcts.MCTS.MoveKey;
import search.mcts.nodes.BaseNode;
import search.mcts.nodes.BaseNode.NodeStatistics;
import utils.AIUtils;

/**
 * Standard backpropagation implementation for MCTS, performing Monte-Carlo backups
 * of playout outcomes.
 * 
 * @author Dennis Soemers
 */
public class MonteCarloBackprop extends BackpropagationStrategy
{
	
	@Override
	public void update
	(
		final MCTS mcts,
		final BaseNode startNode, 
		final Context context, 
		final double[] utilities, 
		final int numPlayoutMoves
	)
	{
		BaseNode node = startNode;
		final double playoutValueWeight = mcts.playoutValueWeight();
		
		if (mcts.heuristics() != null)
		{
			final double[] nodeHeuristicValues;
			
			if (playoutValueWeight < 1.0)
			{
				// Mix value function of expanded node with playout outcome (like AlphaGo)
				nodeHeuristicValues = node.heuristicValueEstimates();
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
		
		//System.out.println("utilities = " + Arrays.toString(utilities));
		final boolean updateGRAVE = ((backpropFlags & GRAVE_STATS) != 0);
		final boolean updateGlobalActionStats = ((backpropFlags & GLOBAL_ACTION_STATS) != 0);
		final boolean updateGlobalNGramActionStats = ((backpropFlags & GLOBAL_NGRAM_ACTION_STATS) != 0);
		final List<MoveKey> moveKeysAMAF = new ArrayList<MoveKey>();
		final Iterator<Move> reverseMovesIterator = context.trial().reverseMoveIterator();
		final int numTrialMoves = context.trial().numMoves();
		int movesIdxAMAF = numTrialMoves - 1;
		
		if (updateGRAVE || updateGlobalActionStats || updateGlobalNGramActionStats)
		{
			// collect all move keys for playout moves
			while (movesIdxAMAF >= (numTrialMoves - numPlayoutMoves))
			{
				moveKeysAMAF.add(new MoveKey(reverseMovesIterator.next(), movesIdxAMAF));
				--movesIdxAMAF;
			}
		}
		
		while (node != null)
		{
			synchronized(node)
			{
				node.update(utilities);
				
				if (updateGRAVE)
				{
					for (final MoveKey moveKey : moveKeysAMAF)
					{
						final NodeStatistics graveStats = node.getOrCreateGraveStatsEntry(moveKey);
						//System.out.println("updating GRAVE stats in " + node + " for move: " + moveKey);
						graveStats.visitCount += 1;
						graveStats.accumulatedScore += utilities[context.state().playerToAgent(moveKey.move.mover())];
	
						// the below would be sufficient for RAVE, but for GRAVE we also need moves
						// made by the "incorrect" colour in higher-up nodes
	
						/*
						final int mover = moveKey.move.mover();
						if (nodeColour == 0 || nodeColour == mover)
						{
							final NodeStatistics graveStats = node.getOrCreateGraveStatsEntry(moveKey);
							graveStats.visitCount += 1;
							graveStats.accumulatedScore += utilities[mover];
						}*/
					}
				}
			}
				
			if (updateGRAVE || updateGlobalActionStats)
			{
				// we're going up one level, so also one more move to count as AMAF-move
				if (movesIdxAMAF >= 0)
				{
					moveKeysAMAF.add(new MoveKey(reverseMovesIterator.next(), movesIdxAMAF));
					--movesIdxAMAF;
				}
			}
			
			node = node.parent();
		}
		
		BackpropagationStrategy.updateGlobalActionStats
		(
			mcts, updateGlobalActionStats, updateGlobalNGramActionStats, moveKeysAMAF, context, utilities
		);
	}

}
