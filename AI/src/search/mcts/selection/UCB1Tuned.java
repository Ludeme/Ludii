package search.mcts.selection;

import java.util.concurrent.ThreadLocalRandom;

import other.state.State;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * UCB1-Tuned Selection strategy. The original paper by Auer et al. used 1/4 as the
 * upper bound on the variance of a Bernoulli random variable. We expect values
 * in the [-1, 1] range, rather than the [0, 1] range in our MCTS, so
 * we use 1 as an upper bound on the variance of this random variable.
 * 
 * @author Dennis Soemers
 */
public final class UCB1Tuned implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Upper bound on variance of random variable in [-1, 1] range */
	protected static final double VARIANCE_UPPER_BOUND = 1.0;
	
	/** Exploration constant */
	protected double explorationConstant;

	//-------------------------------------------------------------------------

	/**
	 * Constructor with default value sqrt(2.0) for exploration constant
	 */
	public UCB1Tuned()
	{
		this(Math.sqrt(2.0));
	}

	/**
	 * Constructor with parameter for exploration constant
	 * @param explorationConstant
	 */
	public UCB1Tuned(final double explorationConstant)
	{
		this.explorationConstant = explorationConstant;
	}

	//-------------------------------------------------------------------------

	@Override
	public int select(final MCTS mcts, final BaseNode current)
	{
		int bestIdx = 0;
        double bestValue = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;

        final double parentLog = Math.log(Math.max(1, current.sumLegalChildVisits()));
        final int numChildren = current.numLegalMoves();
        final State state = current.contextRef().state();
        final int moverAgent = state.playerToAgent(state.mover());
        final double unvisitedValueEstimate = current.valueEstimateUnvisitedChildren(moverAgent);

        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	final double exploit;
        	final double sampleVariance;
        	final double visitsFraction;

        	if (child == null)
        	{
        		exploit = unvisitedValueEstimate;
        		sampleVariance = VARIANCE_UPPER_BOUND;
        		visitsFraction = parentLog;
        	}
        	else
        	{
        		exploit = child.exploitationScore(moverAgent);
        		final int numChildVisits = Math.max(child.numVisits() + child.numVirtualVisits(), 1);
        		sampleVariance = Math.max(child.sumSquaredScores(moverAgent) / numChildVisits - exploit*exploit, 0.0);
        		visitsFraction = parentLog / numChildVisits;
        	}

        	final double ucb1TunedValue = exploit + 
        			Math.sqrt
        			(
        				visitsFraction * Math.min(VARIANCE_UPPER_BOUND, sampleVariance + explorationConstant * Math.sqrt(visitsFraction))
        			);

        	if (ucb1TunedValue > bestValue)
        	{
        		bestValue = ucb1TunedValue;
        		bestIdx = i;
        		numBestFound = 1;
        	}
        	else if 
        	(
        		ucb1TunedValue == bestValue 
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
		return 0;
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
					System.err.println("UCB1Tuned ignores unknown customisation: " + input);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
