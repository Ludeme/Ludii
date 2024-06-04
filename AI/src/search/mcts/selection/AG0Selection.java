package search.mcts.selection;

import java.util.concurrent.ThreadLocalRandom;

import main.collections.FVector;
import other.state.State;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Selection strategy used by AlphaGo Zero (described there as a variant of
 * PUCB1 proposed by Rosin (2011), but it's a variant, not exactly the same).
 * 
 * @author Dennis Soemers
 *
 */
public final class AG0Selection implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Exploration constant for AlphaGo Zero's selection strategy */
	protected double explorationConstant;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default exploration constant of 2.5.
	 */
	public AG0Selection()
	{
		this(2.5);
	}
	
	/**
	 * Constructor with custom exploration constant
	 * @param explorationConstant
	 */
	public AG0Selection(final double explorationConstant)
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
		
        final FVector distribution = current.learnedSelectionPolicy();
        final double parentSqrt = Math.sqrt(current.sumLegalChildVisits());

        final int numChildren = current.numLegalMoves();
        final State state = current.contextRef().state();
        final int moverAgent = state.playerToAgent(state.mover());
        final double unvisitedValueEstimate = 
        		current.valueEstimateUnvisitedChildren(moverAgent);

        for (int i = 0; i < numChildren; ++i)
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	final double exploit;
        	final int numVisits;

        	if (child == null)
        	{
        		exploit = unvisitedValueEstimate;
        		numVisits = 0;
        	}
        	else
        	{
        		exploit = child.exploitationScore(moverAgent);
        		numVisits = child.numVisits() + child.numVirtualVisits();
        	}

        	final float priorProb = distribution.get(i);
        	final double explore = (parentSqrt == 0.0) ? 1.0 : parentSqrt / (1.0 + numVisits);

        	final double pucb1Value = exploit + explorationConstant * priorProb * explore;

        	if (pucb1Value > bestValue)
        	{
        		bestValue = pucb1Value;
        		bestIdx = i;
        		numBestFound = 1;
        	}
        	else if (pucb1Value == bestValue && ThreadLocalRandom.current().nextInt() % ++numBestFound == 0)
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
					explorationConstant = Double.parseDouble(input.substring("explorationconstant=".length()));
				}
				else
				{
					System.err.println("AG0Selection ignores unknown customisation: " + input);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
