package search.mcts.selection;

import java.util.concurrent.ThreadLocalRandom;

import main.collections.FVector;
import other.state.State;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Selection strategy used by Anthony, Tian, and Barber (2017) for 
 * Expert Iteration
 * 
 * @author Dennis Soemers
 *
 */
public final class ExItSelection implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** The standard exploration constant of UCB1 */
	protected double explorationConstant;
	
	/** 
	 * Weight parameter for the prior policy term (w_a in the ExIt paper) 
	 * 
	 * Note: paper mentions a good value for this hyperparameter may be
	 * close to the average number of simulations per action at the root...
	 * which is going to wildly vary per game and per time-control setting.
	 */
	protected double priorPolicyWeight;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param priorPolicyWeight
	 */
	public ExItSelection(final double priorPolicyWeight)
	{
		this(Math.sqrt(2.0), priorPolicyWeight);
	}
	
	/**
	 * Constructor
	 * @param explorationConstant
	 * @param priorPolicyWeight
	 */
	public ExItSelection(final double explorationConstant, final double priorPolicyWeight)
	{
		this.explorationConstant = explorationConstant;
		this.priorPolicyWeight = priorPolicyWeight;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int select(final MCTS mcts, final BaseNode current)
	{
		int bestIdx = -1;
        double bestValue = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;
        
        final FVector distribution = current.learnedSelectionPolicy();
        final double parentLog = Math.log(Math.max(1, current.sumLegalChildVisits()));

        final int numChildren = current.numLegalMoves();
        final State state = current.contextRef().state();
        final int moverAgent = state.playerToAgent(state.mover());
        final double unvisitedValueEstimate = 
        		current.valueEstimateUnvisitedChildren(moverAgent);

        for (int i = 0; i < numChildren; ++i)
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	final double exploit;
        	final double explore;
        	final int numVisits;

        	if (child == null)
        	{
        		exploit = unvisitedValueEstimate;
        		numVisits = 0;
        		explore = Math.sqrt(parentLog);
        	}
        	else
        	{
        		exploit = child.exploitationScore(moverAgent);
        		numVisits = Math.max(child.numVisits() + child.numVirtualVisits(), 1);
        		explore = Math.sqrt(parentLog / numVisits);
        	}

        	final float priorProb = distribution.get(i);
        	final double priorTerm = priorProb / (numVisits + 1);

        	final double ucb1pValue = 
        			exploit + 
        			explorationConstant * explore + 
        			priorPolicyWeight * priorTerm;

        	if (ucb1pValue > bestValue)
        	{
        		bestValue = ucb1pValue;
        		bestIdx = i;
        		numBestFound = 1;
        	}
        	else if 
        	(
        		ucb1pValue == bestValue 
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
			// we have more inputs than just the name of the strategy
			for (int i = 1; i < inputs.length; ++i)
			{
				final String input = inputs[i];
				
				if (input.startsWith("explorationconstant="))
				{
					explorationConstant = Double.parseDouble(
							input.substring("explorationconstant=".length()));
				}
				else
				{
					System.err.println("ExItSelection ignores unknown customisation: " + input);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
