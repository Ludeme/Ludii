package search.mcts.selection;

import java.util.concurrent.ThreadLocalRandom;

import other.state.State;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * UCB1 Selection Strategy, as commonly used in UCT.
 * 
 * @author Dennis Soemers
 */
public final class UCB1 implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Exploration constant */
	protected double explorationConstant;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default value sqrt(2.0) for exploration constant
	 */
	public UCB1()
	{
		this(Math.sqrt(2.0));
	}
	
	/**
	 * Constructor with parameter for exploration constant
	 * @param explorationConstant
	 */
	public UCB1(final double explorationConstant)
	{
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

        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	final double exploit;
        	final double explore;

        	if (child == null)
        	{
        		exploit = unvisitedValueEstimate;
        		explore = Math.sqrt(parentLog);
        	}
        	else
        	{
        		exploit = child.exploitationScore(moverAgent);
        		final int numVisits = Math.max(child.numVisits() + child.numVirtualVisits(), 1);
        		explore = Math.sqrt(parentLog / numVisits);
        	}

        	final double ucb1Value = exploit + explorationConstant * explore;
        	//System.out.println("ucb1Value = " + ucb1Value);
        	//System.out.println("exploit = " + exploit);
        	//System.out.println("explore = " + explore);

        	if (ucb1Value > bestValue)
        	{
        		bestValue = ucb1Value;
        		bestIdx = i;
        		numBestFound = 1;
        	}
        	else if 
        	(
        		ucb1Value == bestValue 
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
					explorationConstant = Double.parseDouble(
							input.substring("explorationconstant=".length()));
				}
				else
				{
					System.err.println("UCB1 ignores unknown customisation: " + input);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
