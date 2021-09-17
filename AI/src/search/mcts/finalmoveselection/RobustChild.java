package search.mcts.finalmoveselection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import other.move.Move;
import other.state.State;
import search.mcts.nodes.BaseNode;

/**
 * Selects move corresponding to the most robust child (highest visit count),
 * with an additional tie-breaker based on value estimates
 * 
 * @author Dennis Soemers
 */
public final class RobustChild implements FinalMoveSelectionStrategy
{
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectMove(final BaseNode rootNode)
	{
		final List<Move> bestActions = new ArrayList<Move>();
		double bestActionValueEstimate = Double.NEGATIVE_INFINITY;
		final State rootState = rootNode.contextRef().state();
        int maxNumVisits = -1;
        
        final int numChildren = rootNode.numLegalMoves();
        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = rootNode.childForNthLegalMove(i);
            final int numVisits = child == null ? 0 : child.numVisits();
            final double childValueEstimate = child == null ? 0.0 : child.averageScore(rootState.mover(), rootState);

            if (numVisits > maxNumVisits)
            {
            	maxNumVisits = numVisits;
            	bestActions.clear();
            	bestActionValueEstimate = childValueEstimate;
            	bestActions.add(rootNode.nthLegalMove(i));
            }
            else if (numVisits == maxNumVisits)
            {
            	if (childValueEstimate > bestActionValueEstimate)
            	{
            		// Tie-breaker; prefer higher value estimates
            		bestActions.clear();
            		bestActionValueEstimate = childValueEstimate;
            		bestActions.add(rootNode.nthLegalMove(i));
            	}
            	else if (childValueEstimate == bestActionValueEstimate)
            	{
            		// Really a tie, both for num visits and also for estimated value
            		bestActions.add(rootNode.nthLegalMove(i));
            	}
            }
        }
        
        return bestActions.get(ThreadLocalRandom.current().nextInt(bestActions.size()));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void customise(final String[] inputs)
	{
		// Do nothing
	}

	//-------------------------------------------------------------------------

}
