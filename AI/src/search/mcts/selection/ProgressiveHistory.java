package search.mcts.selection;

import java.util.concurrent.ThreadLocalRandom;

import other.move.Move;
import other.state.State;
import search.mcts.MCTS;
import search.mcts.MCTS.ActionStatistics;
import search.mcts.MCTS.MoveKey;
import search.mcts.backpropagation.BackpropagationStrategy;
import search.mcts.nodes.BaseNode;

/**
 * Progressive History, as described by Nijssen and Winands (2011)
 * 
 * @author Dennis Soemers
 */
public class ProgressiveHistory implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** The W hyperparameter / weight for Progressive History */
	protected final double progressiveBiasInfluence;
	
	/** Exploration constant */
	protected double explorationConstant;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default value of W = 3.0,
	 * loosely based on results from (Nijssen and Winands, 2011).
	 */
	public ProgressiveHistory()
	{
		this.progressiveBiasInfluence = 3.0;
		this.explorationConstant = Math.sqrt(2.0);
	}
	
	/**
	 * Constructor
	 * @param progressiveBiasInfluence
	 * @param explorationConstant
	 */
	public ProgressiveHistory(final double progressiveBiasInfluence, final double explorationConstant)
	{
		this.progressiveBiasInfluence = progressiveBiasInfluence;
		this.explorationConstant = explorationConstant;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int select(final MCTS mcts, final BaseNode current)
	{
		int bestIdx = -1;
        double bestValue = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;
        
        final double parentLog = Math.log(Math.max(1, current.sumLegalChildVisits()));
        final int numChildren = current.numLegalMoves();
        final State state = current.contextRef().state();
        final int moverAgent = state.playerToAgent(state.mover());
        final double unvisitedValueEstimate = current.valueEstimateUnvisitedChildren(moverAgent);

        //System.out.println("selecting for current node = " + current + ". Mover = " + current.contextRef().state().mover());

        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	final double explore;
        	final double meanScore;
        	final double meanGlobalActionScore;
        	final int childNumVisits = child == null ? 0 : Math.max(child.numVisits() + child.numVirtualVisits(), 1);

        	final Move move = current.nthLegalMove(i);
        	final ActionStatistics actionStats = mcts.getOrCreateActionStatsEntry(new MoveKey(move, current.contextRef().trial().numMoves()));
        	if (actionStats.visitCount == 0)
        		meanGlobalActionScore = unvisitedValueEstimate;
        	else
        		meanGlobalActionScore = actionStats.accumulatedScore / actionStats.visitCount;

        	if (child == null)
        	{
        		meanScore = unvisitedValueEstimate;
        		explore = Math.sqrt(parentLog);
        	}
        	else
        	{
        		meanScore = child.exploitationScore(moverAgent);
        		explore = Math.sqrt(parentLog / childNumVisits);
        	}

        	final double ucb1PhValue = meanScore + explorationConstant * explore 
        			+ meanGlobalActionScore * (progressiveBiasInfluence / ((1.0 - meanScore) * childNumVisits + 1));

        	if (ucb1PhValue > bestValue)
        	{
        		bestValue = ucb1PhValue;
        		bestIdx = i;
        		numBestFound = 1;
        	}
        	else if 
        	(
        		ucb1PhValue == bestValue 
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
		return BackpropagationStrategy.GLOBAL_ACTION_STATS;
	}
	
	@Override
	public int expansionFlags()
	{
		return 0;
	}

	@Override
	public void customise(final String[] inputs)
	{
		if (inputs.length > 1)
		{
			// We have more inputs than just the name of the strategy
			for (int i = 1; i < inputs.length; ++i)
			{
				final String input = inputs[i];
				
				if (input.startsWith("explorationconstant="))
				{
					explorationConstant = Double.parseDouble(input.substring("explorationconstant=".length()));
				}
				else
				{
					System.err.println("ProgressiveHistory ignores unknown customisation: " + input);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
