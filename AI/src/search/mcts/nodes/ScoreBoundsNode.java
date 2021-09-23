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
    			if (parent != null)
    			{
    				((ScoreBoundsNode) parent).updatePessBounds(p, currentUtils[p]);
	    			((ScoreBoundsNode) parent).updateOptBounds(p, currentUtils[p]);
    			}
    		}
    		else
    		{
    			pessimisticScores[p] = nextWorstScore;
    			optimisticScores[p] = nextBestScore;
    		}
    	}
    }
    
    //-------------------------------------------------------------------------
    
//    @Override
//    public double averageScore(final int player)
//    {
//    	return (numVisits == 0) ? 0.0 : (totalScores[state.playerToAgent(player)] - numVirtualVisits.get()) / (numVisits + numVirtualVisits.get());
//    }
    
  //-------------------------------------------------------------------------
    
    /**
     * One of our children has an updated pessimistic bound for the given agent; 
     * check if we should also update now
     * 
     * @param agent
     * @param pessBound
     */
    public void updatePessBounds(final int agent, final double pessBound)
    {
    	final double oldPess = pessimisticScores[agent];
    	
    	if (pessBound > oldPess)	// May be able to increase pessimistic bounds
    	{
    		final int moverAgent = contextRef().state().playerToAgent(contextRef().state().mover());
    		
    		if (moverAgent == agent)
    		{
    			// The agent for which one of our children has a new pessimistic bound
    			// is the agent to move in this node. Hence, we can update directly
    			pessimisticScores[agent] = pessBound;
    			if (parent != null)
    				((ScoreBoundsNode) parent).updatePessBounds(agent, pessBound);
    		}
    		else
    		{
    			// The agent for which one of our children has a new pessimistic bound
    			// is NOT the agent to move in this node. Hence, we only update to
    			// the minimum pessimistic bound over all children.
    			//
    			// Technically, if the real value (opt = pess) were proven for the
    			// agent to move, we could restrict the set of children over
    			// which we take the minimum to just those that have the optimal
    			// value for the agent to move.
    			//
    			// This is more expensive to implement though, and only relevant in
    			// games with more than 2 players, and there likely also only very
    			// rarely, so we don't bother doing this.
    			double minPess = pessBound;
    			
    			for (int i = 0; i < children.length; ++i)
    			{
    				final ScoreBoundsNode child = (ScoreBoundsNode) children[i];
    				
    				if (child == null)
    				{
    					return;		// Can't update anything if we have an unvisited child left
    				}
    				else
    				{
    					final double pess = child.pessBound(agent);
    					if (pess < minPess)
    					{
    						if (pess == oldPess)
    							return;		// Won't be able to update
    						
    						minPess = pess;
    					}
    				}
    			}
    			
    			if (minPess < oldPess)
    				System.err.println("ERROR in updatePessBounds()!");
    			
    			// We can update
    			pessimisticScores[agent] = minPess;
    			if (parent != null)
    				((ScoreBoundsNode) parent).updatePessBounds(agent, minPess);
    		}
    	}
    }
    
    /**
     * One of our children has an updated optimistic bound for the given agent; 
     * check if we should also update now
     * 
     * @param agent
     * @param optBound
     */
    public void updateOptBounds(final int agent, final double optBound)
    {
    	final double oldOpt = optimisticScores[agent];
    	
    	if (optBound < oldOpt)	// May be able to decrease optimistic bounds
    	{
    		// Regardless of who the mover in this node is, any given agent's optimistic
    		// bound should always just be the maximum over all their children
    		double maxOpt = optBound;
			
			for (int i = 0; i < children.length; ++i)
			{
				final ScoreBoundsNode child = (ScoreBoundsNode) children[i];
				
				if (child == null)
				{
					return;		// Can't update anything if we have an unvisited child left
				}
				else
				{
					final double opt = child.optBound(agent);
					if (opt > maxOpt)
					{
						if (opt == oldOpt)
							return;		// Won't be able to update
						
						maxOpt = opt;
					}
				}
			}
			
			if (maxOpt > oldOpt)
				System.err.println("ERROR in updateOptBounds()!");
			
			// We can update
			optimisticScores[agent] = maxOpt;
			if (parent != null)
				((ScoreBoundsNode) parent).updateOptBounds(agent, maxOpt);
    	}
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * @param agent
     * @return Current pessimistic bound for given agent
     */
    public double pessBound(final int agent)
    {
    	return pessimisticScores[agent];
    }
    
    /**
     * @param agent
     * @return Current optimistic bound for given agent
     */
    public double optBound(final int agent)
    {
    	return optimisticScores[agent];
    }
    
	//-------------------------------------------------------------------------

}
