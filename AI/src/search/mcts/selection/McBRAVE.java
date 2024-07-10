package search.mcts.selection;

import java.util.concurrent.ThreadLocalRandom;

import other.move.Move;
import other.state.State;
import search.mcts.MCTS;
import search.mcts.MCTS.MoveKey;
import search.mcts.backpropagation.BackpropagationStrategy;
import search.mcts.nodes.BaseNode;
import search.mcts.nodes.BaseNode.NodeStatistics;

/**
 * TODO
 * 
 * @author Dennis Soemers
 */
public class McBRAVE implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Hyperparameter used in computation of weight for AMAF term */
	protected final double bias;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default value of bias = 10^(-6),
	 * loosely based on hyperparameter tuning in GRAVE paper (though
	 * that paper found many different optimal values for different games).
	 */
	public McBRAVE()
	{
		this.bias = 10.0e-6;
	}
	
	/**
	 * Constructor
	 * @param bias
	 */
	public McBRAVE(final double bias)
	{
		this.bias = bias;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int select(final MCTS mcts, final BaseNode current)
	{
		int bestIdx = 0;
        double bestValue = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;
        
        final int numChildren = current.numLegalMoves();
        final State state = current.contextRef().state();
        final int moverAgent = state.playerToAgent(state.mover());
        final double unvisitedValueEstimate = current.valueEstimateUnvisitedChildren(moverAgent);

        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	final double meanScore;
        	final double meanAMAF;
        	final double beta;

        	if (child == null)
        	{
        		meanScore = unvisitedValueEstimate;
        		meanAMAF = 0.0;
        		beta = 0.0;
        	}
        	else
        	{
        		meanScore = child.exploitationScore(moverAgent);
        		final Move move = child.parentMove();
        		
        		int accumVisits = 0;
        		double accumScore = 0.0;
        		final MoveKey moveKey = new MoveKey(move, current.contextRef().trial().numMoves());
        		
        		BaseNode raveNode = current;
        		while (raveNode != null)
        		{
        			final NodeStatistics graveStats = raveNode.graveStats(moveKey);
        			
        			if (graveStats != null)
        			{
        				accumScore += graveStats.accumulatedScore;
        				accumVisits += graveStats.visitCount;
        			}
        			
        			raveNode = raveNode.parent();
        		}
        		
        		if (accumVisits == 0)
        		{
        			meanAMAF = 0.0;
        			beta = 0.0;
        		}
        		else
        		{
	        		final int childVisits = child.numVisits() + child.numVirtualVisits();
	        		meanAMAF = accumScore / accumVisits;
	        		beta = accumVisits / (accumVisits + childVisits + bias * accumVisits * childVisits);
        		}
        	}

        	final double graveValue = (1.0 - beta) * meanScore + beta * meanAMAF;

        	if (graveValue > bestValue)
        	{
        		bestValue = graveValue;
        		bestIdx = i;
        		numBestFound = 1;
        	}
        	else if 
        	(
        		graveValue == bestValue 
        		&& 
        		ThreadLocalRandom.current().nextInt() % ++numBestFound == 0
        	)
        	{
        		bestIdx = i;
        	}
        }

        return bestIdx;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int backpropFlags()
	{
		return BackpropagationStrategy.GRAVE_STATS;
	}
	
	@Override
	public int expansionFlags()
	{
		return 0;
	}

	@Override
	public void customise(final String[] inputs)
	{
		// TODO 
	}
	
	//-------------------------------------------------------------------------

}
