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
 * A Monte-Carlo variant of Generalized Rapid Action Value Estimation (GRAVE).
 * This is basically exactly the variant described in the Tristan Cazenave's
 * IJCAI 2015 paper; with no exploration term, pure exploitation.
 * 
 * Note that Subsection 5.2 of Gelly and Silver's 2011 paper in Artificial Intelligence
 * describes that they found MC-RAVE (their MC-variant of RAVE, without exploration) to
 * outperform UCT-RAVE (their UCT-variant of RAVE, with exploration). 
 * 
 * With ref = 0, this is equivalent to Gelly and Silver's MC-RAVE.
 * 
 * @author Dennis Soemers
 */
public class McGRAVE implements SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Threshold number of playouts that a node must have had for its AMAF values to be used */
	protected final int ref;
	
	/** Hyperparameter used in computation of weight for AMAF term */
	protected final double bias;
	
	/** Reference node in current MCTS simulation (one per thread, in case of multi-threaded MCTS) */
	protected ThreadLocal<BaseNode> currentRefNode = ThreadLocal.withInitial(() -> null);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default values of ref = 100 and bias = 10^(-6),
	 * loosely based on hyperparameter tuning in GRAVE paper (though
	 * that paper found many different optimal values for different games).
	 */
	public McGRAVE()
	{
		this.ref = 100;
		this.bias = 10.0e-6;
	}
	
	/**
	 * Constructor
	 * @param ref
	 * @param bias
	 */
	public McGRAVE(final int ref, final double bias)
	{
		this.ref = ref;
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

        if (currentRefNode.get() == null || current.numVisits() > ref || current.parent() == null)
        	currentRefNode.set(current);

        //System.out.println("selecting for current node = " + current + ". Mover = " + current.contextRef().state().mover());

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
        		final NodeStatistics graveStats = currentRefNode.get().graveStats(new MoveKey(move, current.contextRef().trial().numMoves()));
//        		if (graveStats == null)
//        		{
//        			System.out.println("currentRefNode = " + currentRefNode.get());
//        			System.out.println("stats for " + new MoveKey(move, current.contextRef().trial().numMoves()) + " in " + currentRefNode.get() + " = " + graveStats);
//        			System.out.println("child visits = " + child.numVisits());
//        			System.out.println("current.who = " + current.contextRef().containerState(0).cloneWho().toChunkString());
//        			System.out.println("current legal actions = " + Arrays.toString(((Node) current).legalActions()));
//        			System.out.println("current context legal moves = " + current.contextRef().activeGame().moves(current.contextRef()));
//        		}
        		
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
	        		final int childVisits = child.numVisits() + child.numVirtualVisits();
	        		meanAMAF = graveScore / graveVisits;
	        		beta = graveVisits / (graveVisits + childVisits + bias * graveVisits * childVisits);
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
		// TODO 
	}
	
	//-------------------------------------------------------------------------

}
