package search.mcts.nodes;

import other.RankUtils;
import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;

/**
 * Node for MCTS tree that tracks pessimistic and optimistic score bounds, for
 * solving of nodes.
 * 
 * @author Dennis Soemers
 */
public final class ScoreBoundsNode extends DeterministicNode
{
	
	//-------------------------------------------------------------------------
	
	/** For every agent, a pessimistic score bound */
	private final double[] pessimisticScores;
	
	/** For every agent, an optimistic score bound */
	private final double[] optimisticScores;
	
	//-------------------------------------------------------------------------
    
    /**
     * Constructor 
     * 
     * @param mcts
     * @param parent
     * @param parentMove
     * @param parentMoveWithoutConseq
     * @param context
     */
    public ScoreBoundsNode
    (
    	final MCTS mcts, 
    	final BaseNode parent, 
    	final Move parentMove, 
    	final Move parentMoveWithoutConseq,
    	final Context context
    )
    {
    	super(mcts, parent, parentMove, parentMoveWithoutConseq, context);
    	
    	final int numPlayers = context.game().players().count();
    	pessimisticScores = new double[numPlayers + 1];
    	optimisticScores = new double[numPlayers + 1];
    	
    	final double nextWorstScore = RankUtils.rankToUtil(context.computeNextLossRank(), numPlayers);
    	final double nextBestScore = RankUtils.rankToUtil(context.computeNextWinRank(), numPlayers);
    	final double[] currentUtils = RankUtils.agentUtilities(context);
    	
    	for (int p = 1; p <= numPlayers; ++p)
    	{
    		if (currentUtils[p] != 0.0)
    		{
    			pessimisticScores[p] = currentUtils[p];
    			optimisticScores[p] = currentUtils[p];
    			
    			// We've just assigned new proven scores, so our parent may want to update
    			((ScoreBoundsNode) parent).updateBounds(p, currentUtils[p]);
    		}
    		else
    		{
    			pessimisticScores[p] = nextWorstScore;
    			optimisticScores[p] = nextBestScore;
    		}
    	}
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * One of our children has a solved score for the given agent; check if
     * we should also update now
     * @param agent
     * @param util
     */
    public void updateBounds(final int agent, final double util)
    {
    	for (int i = 0; i < children.length; ++i)
    	{
    		// TODO
    	}
    }
    
    //-------------------------------------------------------------------------

}
