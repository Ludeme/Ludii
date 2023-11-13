package search.mcts.selection;

import java.util.concurrent.ThreadLocalRandom;

import other.state.State;
import search.mcts.MCTS;
import search.mcts.nodes.AlphaBetaBoundsNode;
import search.mcts.nodes.BaseNode;

/**
 * UCB1 Selection Strategy, with extra terms based on alpha-beta-style bounds.
 * 
 * Based on: https://github.com/tpepels/mcts_python
 * 
 * @author Dennis Soemers
 */
public final class UCB1WithAlphaBetaBounds implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Exploration constant */
	protected double explorationConstant;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default value sqrt(2.0) for exploration constant
	 */
	public UCB1WithAlphaBetaBounds()
	{
		this(Math.sqrt(2.0));
	}
	
	/**
	 * Constructor with parameter for exploration constant
	 * @param explorationConstant
	 */
	public UCB1WithAlphaBetaBounds(final double explorationConstant)
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
        
        final double alpha = ((AlphaBetaBoundsNode) current).alphaBound();
        final double beta = ((AlphaBetaBoundsNode) current).betaBound();
        final int abVersion = mcts.useAlphaBetaBoundsVersion();
        
        final int valAdj = abVersion / 10;
        final int ciAdjust = abVersion % 10;

        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	double exploit;
        	double explore;

        	if (child == null)
        	{
        		exploit = unvisitedValueEstimate;
        		explore = Math.sqrt(parentLog);
        	}
        	else
        	{
        		exploit = child.exploitationScore(moverAgent);
        		final int numVisits = child.numVisits() + child.numVirtualVisits();
        		explore = Math.sqrt(parentLog / numVisits);
        		
        		if (abVersion != 0 && Double.isFinite(alpha) && Double.isFinite(beta))
        		{
        			final double deltaAlpha = exploit - alpha;
        			final double k = beta - alpha;
        			
        			if (valAdj == 1)
        				exploit = exploit + (deltaAlpha * k);
        			else if (valAdj == 2)
        				exploit = (exploit * k) + deltaAlpha;
        			else if (valAdj == 3)
        				exploit = (exploit + deltaAlpha) * k;
        			else if (valAdj == 4)
        				exploit = deltaAlpha + (2.0 / k);
        			else if (valAdj == 5)
        				exploit = exploit + deltaAlpha + (2.0 / k);
        			else if (valAdj == 6)
        				exploit = exploit + deltaAlpha;
        			else if (valAdj == 7)
        				exploit = exploit + (deltaAlpha / k);
        			else if (valAdj == 8)
        				exploit = (exploit + deltaAlpha) / k;
        			else if (valAdj == 9)
        				exploit = (exploit / k) + deltaAlpha;
        			else
        				System.err.println("Unknown valAdj: " + valAdj);
        			
        			if (ciAdjust == 1)
        				explore = Math.sqrt(Math.log(k * Math.max(1, current.sumLegalChildVisits())) / numVisits);
        			else if (ciAdjust == 2)
        				explore = explore / Math.sqrt(k);
        			else
        				System.err.println("Unknown ciAdjust: " + ciAdjust);
        		}
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
	
	public double explorationConstant()
	{
		return explorationConstant;
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
					System.err.println("UCB1 with Alpha-Beta Bounds ignores unknown customisation: " + input);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
