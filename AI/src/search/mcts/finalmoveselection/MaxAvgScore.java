package search.mcts.finalmoveselection;

import java.util.concurrent.ThreadLocalRandom;

import other.move.Move;
import other.state.State;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Selects move corresponding to the child with the highest average score
 * 
 * @author Dennis Soemers
 */
public final class MaxAvgScore implements FinalMoveSelectionStrategy 
{
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectMove(final MCTS mcts, final BaseNode rootNode)
	{
		int bestIdx = -1;
        double maxAvgScore = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;
        
        final State state = rootNode.contextRef().state();
        final int numChildren = rootNode.numLegalMoves();
        final int moverAgent = state.playerToAgent(state.mover());
        
        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = rootNode.childForNthLegalMove(i);
        	final double avgScore;
        	
        	if (child == null)
        		avgScore = rootNode.valueEstimateUnvisitedChildren(moverAgent);
        	else
        		avgScore = child.expectedScore(moverAgent);
        	
            if (avgScore > maxAvgScore)
            {
            	maxAvgScore = avgScore;
                bestIdx = i;
                numBestFound = 1;
            }
            else if (avgScore == maxAvgScore && 
            		ThreadLocalRandom.current().nextInt() % ++numBestFound == 0)
            {
            	bestIdx = i;
            }
        }
        
        return rootNode.nthLegalMove(bestIdx);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void customise(final String[] inputs)
	{
		// do nothing
	}

	//-------------------------------------------------------------------------

}
