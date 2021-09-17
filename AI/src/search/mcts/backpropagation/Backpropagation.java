package search.mcts.backpropagation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;
import search.mcts.MCTS.ActionStatistics;
import search.mcts.MCTS.MoveKey;
import search.mcts.MCTS.NGramMoveKey;
import search.mcts.nodes.BaseNode;
import search.mcts.nodes.BaseNode.NodeStatistics;
import utils.AIUtils;

/**
 * Implements backpropagation of results for MCTS.
 * 
 * @author Dennis Soemers
 */
public final class Backpropagation
{
	
	//-------------------------------------------------------------------------
	
	/** Flags for things we have to backpropagate */
	public final int backpropFlags;
	
	/** AMAF stats per node for use by GRAVE (may be slightly different than stats used by RAVE/AMAF) */
	public final static int GRAVE_STATS					= 0x1;
	/** Global MCTS-wide action statistics (e.g., for Progressive History) */
	public final static int GLOBAL_ACTION_STATS			= (0x1 << 1);
	/** Global MCTS-wide N-gram action statistics (e.g., for NST) */
	public final static int GLOBAL_NGRAM_ACTION_STATS	= (0x2 << 1);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param backpropFlags
	 */
	public Backpropagation(final int backpropFlags)
	{
		this.backpropFlags = backpropFlags;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Updates the given node with statistics based on the given trial
	 * @param mcts
	 * @param startNode
	 * @param context
	 * @param utilities
	 * @param numPlayoutMoves
	 */
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
			// If we have heuristics, we mix 0.5 times the value function of the expanded
			// node with 0.5 times the playout's outcome (like AlphaGo)
			final double[] nodeHeuristicValues = node.heuristicValueEstimates();
			
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
		
		if (updateGlobalActionStats || updateGlobalNGramActionStats)
		{
			// Update global, MCTS-wide action statistics
			for (final MoveKey moveKey : moveKeysAMAF)
			{
				final ActionStatistics actionStats = mcts.getOrCreateActionStatsEntry(moveKey);
				//System.out.println("updating global action stats for move: " + moveKey);
				actionStats.visitCount += 1.0;
				actionStats.accumulatedScore += utilities[context.state().playerToAgent(moveKey.move.mover())];
			}
			
			if (updateGlobalNGramActionStats)
			{
				// Also do N-grams for N > 1
				// note: list of move keys is stored in reverse order
				for (int startMove = moveKeysAMAF.size() - 1; startMove >= 1; --startMove)
				{
					final int maxNGramLength = Math.min(mcts.maxNGramLength(), startMove + 1);
					final int nGramsDepth = moveKeysAMAF.get(startMove).moveDepth;
					final int nGramsMover = moveKeysAMAF.get(startMove).move.mover();
					
					// Start at 2, since the 1-length "n-grams" are already handled in normal action stats table
					for (int n = 2; n <= maxNGramLength; ++n)
					{
						final Move[] nGram = new Move[n];
						for (int i = 0; i < n; ++i)
						{
							nGram[i] = moveKeysAMAF.get(startMove - i).move;
						}
						final ActionStatistics nGramStats = mcts.getOrCreateNGramActionStatsEntry(new NGramMoveKey(nGram, nGramsDepth));
						nGramStats.visitCount += 1.0;
						nGramStats.accumulatedScore += utilities[context.state().playerToAgent(nGramsMover)];
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param player The player.
	 * @param context The context.
	 * 
	 * @return True if the player in entry is an ally of the mover.
	 */
	public static boolean ally(final int player, final Context context)
	{
		if (context.game().requiresTeams())
		{
			return context.state().getTeam(player) == context.state().getTeam(context.state().mover());
		}
		else
		{
			return context.state().mover() != player;
		}
	}

}
