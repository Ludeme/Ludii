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
 * A UCB1 variant of Generalized Rapid Action Value Estimation (GRAVE).
 * This variant differs from MC-GRAVE in that it also uses a UCB1-style
 * exploration term.
 * 
 * Note that Subsection 5.2 of Gelly and Silver's 2011 paper in Artificial Intelligence
 * describes that they found MC-RAVE (their MC-variant of RAVE, without exploration) to
 * outperform UCT-RAVE (their UCT-variant of RAVE, with exploration). 
 * 
 * With ref = 0, this is equivalent to Gelly and Silver's UCT-RAVEe.
 * 
 * @author Dennis Soemers
 */
public class UCB1GRAVE implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Threshold number of playouts that a node must have had for its AMAF values to be used */
	protected final int ref;
	
	/** Hyperparameter used in computation of weight for AMAF term */
	protected final double bias;
	
	/** Exploration constant */
	protected double explorationConstant;
	
	/** Reference node in current MCTS simulation (one per thread, in case of multi-threaded MCTS) */
	protected ThreadLocal<BaseNode> currentRefNode = ThreadLocal.withInitial(() -> null);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default values of ref = 100 and bias = 10^(-6),
	 * loosely based on hyperparameter tuning in GRAVE paper (though
	 * that paper found many different optimal values for different games).
	 */
	public UCB1GRAVE()
	{
		this.ref = 100;
		this.bias = 10.0e-6;
		this.explorationConstant = Math.sqrt(2.0);
	}
	
	/**
	 * Constructor
	 * @param ref
	 * @param bias
	 * @param explorationConstant
	 */
	public UCB1GRAVE(final int ref, final double bias, final double explorationConstant)
	{
		this.ref = ref;
		this.bias = bias;
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

        if (currentRefNode.get() == null || current.numVisits() > ref || current.parent() == null)
        	currentRefNode.set(current);

        //System.out.println("selecting for current node = " + current + ". Mover = " + current.contextRef().state().mover());

        for (int i = 0; i < numChildren; ++i) 
        {
        	final BaseNode child = current.childForNthLegalMove(i);
        	final double explore;
        	final double meanScore;
        	final double meanAMAF;
        	final double beta;

        	if (child == null)
        	{
        		meanScore = unvisitedValueEstimate;
        		meanAMAF = 0.0;
        		beta = 0.0;
        		explore = Math.sqrt(parentLog);
        	}
        	else
        	{
        		meanScore = child.exploitationScore(moverAgent);
        		final Move move = child.parentMove();
        		final NodeStatistics graveStats = currentRefNode.get().graveStats(new MoveKey(move, current.contextRef().trial().numMoves()));
//        		if (graveStats == null)
//        		{
//        			System.out.println("currentRefNode = " + currentRefNode.get());
//        			System.out.println("stats for " + new MoveKey(move) + " in " + currentRefNode.get() + " = " + graveStats);
//        			System.out.println("child visits = " + child.numVisits());
//        			System.out.println("current.who = " + current.contextRef().containerState(0).cloneWho().toChunkString());
//        			System.out.println("current legal actions = " + Arrays.toString(((Node) current).legalActions()));
//        			System.out.println("current context legal moves = " + current.contextRef().activeGame().moves(current.contextRef()));
//        		}
        		
        		final int childVisits = Math.max(child.numVisits() + child.numVirtualVisits(), 1);
        		
        		if (graveStats == null)
        		{
        			// In single-threaded MCTS this should always be a bug, 
        			// but in multi-threaded MCTS it can happen
        			meanAMAF = 0.0;
        			beta = 0.0;
        		}
        		else 
        		{
        			final double graveScore = graveStats.accumulatedScore;
            		final int graveVisits = graveStats.visitCount;
            		meanAMAF = graveScore / graveVisits;
            		beta = graveVisits / (graveVisits + childVisits + bias * graveVisits * childVisits);
        		}
        		

        		explore = Math.sqrt(parentLog / childVisits);
        	}

        	final double graveValue = (1.0 - beta) * meanScore + beta * meanAMAF;
        	final double ucb1GraveValue = graveValue + explorationConstant * explore;

        	if (ucb1GraveValue > bestValue)
        	{
        		bestValue = ucb1GraveValue;
        		bestIdx = i;
        		numBestFound = 1;
        	}
        	else if 
        	(
        		ucb1GraveValue == bestValue 
        		&& 
        		ThreadLocalRandom.current().nextInt() % ++numBestFound == 0
        	)
        	{
        		bestIdx = i;
        	}
        }

        // This can help garbage collector to clean up a bit more easily
        if (current.childForNthLegalMove(bestIdx) == null)
        	currentRefNode.set(null);

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
					System.err.println("UCB1GRAVE ignores unknown customisation: " + input);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
