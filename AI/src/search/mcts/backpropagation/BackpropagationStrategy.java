package search.mcts.backpropagation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;
import search.mcts.MCTS.ActionStatistics;
import search.mcts.MCTS.MoveKey;
import search.mcts.MCTS.NGramMoveKey;
import search.mcts.nodes.BaseNode;
import search.mcts.nodes.BaseNode.NodeStatistics;

/**
 * Abstract class for implementations of backpropagation in MCTS
 * 
 * @author Dennis Soemers
 */
public abstract class BackpropagationStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Flags for things we have to backpropagate */
	protected int backpropFlags = 0;
	
	/** AMAF stats per node for use by GRAVE (may be slightly different than stats used by RAVE/AMAF) */
	public final static int GRAVE_STATS					= 0x1;
	/** Global MCTS-wide action statistics (e.g., for Progressive History) */
	public final static int GLOBAL_ACTION_STATS			= (0x1 << 1);
	/** Global MCTS-wide N-gram action statistics (e.g., for NST) */
	public final static int GLOBAL_NGRAM_ACTION_STATS	= (0x2 << 1);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set backprop flags for this backpropagation implementation
	 * @param backpropFlags
	 */
	public void setBackpropFlags(final int backpropFlags)
	{
		this.backpropFlags = backpropFlags;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Computes the array of utilities that we want to backpropagate.
	 * This method is expected to modify the given utilities array in-place
	 * 
	 * @param mcts
	 * @param startNode
	 * @param context
	 * @param utilities
	 * @param numPlayoutMoves
	 */
	public abstract void computeUtilities
	(
		final MCTS mcts,
		final BaseNode startNode, 
		final Context context, 
		final double[] utilities, 
		final int numPlayoutMoves
	);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Updates the given node with statistics based on the given trial
	 * @param mcts
	 * @param startNode
	 * @param context
	 * @param utilities
	 * @param numPlayoutMoves
	 */
	public final void update
	(
		final MCTS mcts,
		final BaseNode startNode, 
		final Context context, 
		final double[] utilities, 
		final int numPlayoutMoves
	)
	{
		BaseNode node = startNode;
		computeUtilities(mcts, startNode, context, utilities, numPlayoutMoves);
		
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
		
		updateGlobalActionStats
		(
			mcts, updateGlobalActionStats, updateGlobalNGramActionStats, moveKeysAMAF, context, utilities
		);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method to update global (MCTS-wide) action stats for
	 * techniques such as RAVE, GRAVE, MAST, NST, etc.
	 * 
	 * Can be reused by various different backpropagation implementations.
	 * 
	 * @param mcts
	 * @param updateGlobalActionStats
	 * @param updateGlobalNGramActionStats
	 * @param moveKeysAMAF
	 * @param context
	 * @param utilities
	 */
	public static void updateGlobalActionStats
	(
		final MCTS mcts,
		final boolean updateGlobalActionStats,
		final boolean updateGlobalNGramActionStats,
		final List<MoveKey> moveKeysAMAF,
		final Context context,
		final double[] utilities
	)
	{
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
	 * @param json
	 * @return Playout strategy constructed from given JSON object
	 */
	public static BackpropagationStrategy fromJson(final JSONObject json)
	{
		BackpropagationStrategy backprop = null;
		final String strategy = json.getString("strategy");
		
		if (strategy.equalsIgnoreCase("MonteCarlo"))
		{
			return new MonteCarloBackprop();
		}
		
		return backprop;
	}
	
	//-------------------------------------------------------------------------

}
