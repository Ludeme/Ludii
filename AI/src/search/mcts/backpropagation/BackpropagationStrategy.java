package search.mcts.backpropagation;

import java.util.List;

import org.json.JSONObject;

import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;
import search.mcts.MCTS.ActionStatistics;
import search.mcts.MCTS.MoveKey;
import search.mcts.MCTS.NGramMoveKey;
import search.mcts.nodes.BaseNode;

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
	 * Updates the given node with statistics based on the given trial
	 * @param mcts
	 * @param startNode
	 * @param context
	 * @param utilities
	 * @param numPlayoutMoves
	 */
	public abstract void update
	(
		final MCTS mcts,
		final BaseNode startNode, 
		final Context context, 
		final double[] utilities, 
		final int numPlayoutMoves
	);
	
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
