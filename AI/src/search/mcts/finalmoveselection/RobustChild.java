package search.mcts.finalmoveselection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import other.move.Move;
import search.mcts.nodes.BaseNode;

/**
 * Selects move corresponding to the most robust child (highest visit count)
 * 
 * @author Dennis Soemers
 *
 */
public final class RobustChild implements FinalMoveSelectionStrategy
{
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectMove(final BaseNode rootNode)
	{
		final List<Move> bestActions = new ArrayList<Move>();
        int maxNumVisits = -1;
        
        final int numChildren = rootNode.numLegalMoves();
        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = rootNode.childForNthLegalMove(i);
            final int numVisits = child == null ? 0 : child.numVisits();

            if (numVisits > maxNumVisits)
            {
            	maxNumVisits = numVisits;
            	bestActions.clear();
            	bestActions.add(rootNode.nthLegalMove(i));
            }
            else if (numVisits == maxNumVisits)
            {
            	bestActions.add(rootNode.nthLegalMove(i));
            }
        }
        
        return bestActions.get(ThreadLocalRandom.current().nextInt(bestActions.size()));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void customise(final String[] inputs)
	{
		// do nothing
	}

	//-------------------------------------------------------------------------

}
