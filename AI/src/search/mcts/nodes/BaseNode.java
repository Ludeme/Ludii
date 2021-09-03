package search.mcts.nodes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import expert_iteration.ExItExperience;
import expert_iteration.ExItExperience.ExItExperienceState;
import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.state.State;
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import search.mcts.MCTS.MoveKey;
import search.mcts.backpropagation.Backpropagation;

/**
 * Abstract base class for nodes in MCTS search trees.
 * 
 * @author Dennis Soemers
 */
public abstract class BaseNode
{
	
	//-------------------------------------------------------------------------
	
	/** Parent node */
	protected BaseNode parent;
	
	/** Move leading from parent to this node */
    protected final Move parentMove;
    
    /** Move leading from parent to this node, without consequents evaluated */
    protected final Move parentMoveWithoutConseq;
	
	/** Reference back to our MCTS algorithm */
	protected final MCTS mcts;
	
	/** Total number of times this node was visited. */
    protected int numVisits = 0;
    
    /** Number of virtual visits to this node (for Tree Parallelisation) */
    protected AtomicInteger numVirtualVisits = new AtomicInteger();
    
    /** Total scores backpropagated into this node (one per player, 0 index unused). */
    protected final double[] totalScores;
    
    /** MinMax scores backpropagated into this node (one per player, 0 index unused). */
    protected final double[] minMaxScores;
    
    /** Value estimates based on heuristic score function, normalised to appropriate range in [-1, 1]. Can be null. */
    protected double[] heuristicValueEstimates;
    
    /** Table of AMAF stats for GRAVE */
    protected final Map<MoveKey, NodeStatistics> graveStats;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mcts
	 * @param parent
	 * @param parentMove
	 * @param parentMoveWithoutConseq
	 * @param game
	 */
	public BaseNode
	(
		final MCTS mcts, 
		final BaseNode parent, 
		final Move parentMove, 
		final Move parentMoveWithoutConseq, 
		final Game game
	)
	{
		this.mcts = mcts;
		this.parent = parent;
		this.parentMove = parentMove;
		this.parentMoveWithoutConseq = parentMoveWithoutConseq;

		totalScores = new double[game.players().count() + 1];
		minMaxScores = new double[totalScores.length];
		heuristicValueEstimates = null;
		
		final int backpropFlags = mcts.backpropFlags();
		
		if ((backpropFlags & Backpropagation.GRAVE_STATS) != 0)
			graveStats = new ConcurrentHashMap<MoveKey, NodeStatistics>();
		else
			graveStats = null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Adds the given new child, resulting from the move at the given move index
	 * @param child
	 * @param moveIdx
	 */
	public abstract void addChild(final BaseNode child, final int moveIdx);
	
	/**
	 * @param n
	 * @return Child node for the nth legal move in this node (in current iteration)
	 */
	public abstract BaseNode childForNthLegalMove(final int n);
	
	/**
	 * @return Reference to Context object for this node. Callers are
	 * expected NOT to modify this object (i.e. not directly apply moves on it)
	 */
	public abstract Context contextRef();
	
	/**
	 * @return Deterministic reference to Context object. This will be
	 * null for non-root nodes in open-loop trees
	 */
	public abstract Context deterministicContextRef();
	
	/**
	 * @param move
	 * @return Child node of this node corresponding to given move.
	 * Null if there is no child matching the given move.
	 */
	public abstract BaseNode findChildForMove(final Move move);
	
	/**
	 * @return Distribution over legal moves in this node (in current iteration)
	 * computed by learned Selection policy
	 */
	public abstract FVector learnedSelectionPolicy();
	
	/**
	 * @return List of all moves that MCTS believes may be valid from this node
	 */
	public abstract FastArrayList<Move> movesFromNode();
	
	/**
	 * @return "colour" (= player ID) for this node. Returns 0 if it could be
	 * any player (i.e. in nondeterministic games).
	 */
	public abstract int nodeColour();
	
	/**
	 * @param n
	 * @return nth legal move in this node (in current iteration)
	 */
	public abstract Move nthLegalMove(final int n);
	
	/**
	 * @return Number of legal moves in this node (in current iteration)
	 */
	public abstract int numLegalMoves();
	
	/**
	 * @return Context object that MCTS can use to run a full playout
	 */
	public abstract Context playoutContext();
	
	/**
	 * Perform any required computations when a node becomes the root node.
	 * @param context
	 */
	public abstract void rootInit(final Context context);
	
	/**
	 * Tells the node that we're starting a new iteration in it,
	 * with the current state encapsulated in the given context object.
	 * @param context
	 */
	public abstract void startNewIteration(final Context context);
	
	/**
	 * @return Sum of visits among the currently-legal children
	 */
	public abstract int sumLegalChildVisits();
	
	/**
	 * Traverse the tree by playing the move at the given index
	 * @param moveIdx
	 * @return Context object resulting from application of given move
	 */
	public abstract Context traverse(final int moveIdx);
	
	/**
	 * Called when we reach this node in an MCTS iteration, indicating
	 * that it may have to update its Context reference
	 */
	public abstract void updateContextRef();
	
	//-------------------------------------------------------------------------
	
	/**
     * @param player Player index
     * @param state
     * 
     * @return Average score backpropagated into this node for player. 
     * 	Also accounts for virtual visits (treating them as losses)
     */
    public double averageScore(final int player, final State state)
    {
    	return (numVisits == 0) ? 0.0 : (totalScores[state.playerToAgent(player)] - numVirtualVisits.get()) / (numVisits + numVirtualVisits.get());
    }
    
    /**
     * @return Array of heuristic value estimates: one per player. Array can be null if MCTS
     * 	has no heuristics.
     */
    public double[] heuristicValueEstimates()
    {
    	return heuristicValueEstimates;
    }
    
	/**
     * @param player Player index
     * @param state
     * 
     * @return MinMax score backpropagated into this node for player
     */
    public double minMaxScore(final int player, final State state)
    {
    	return (numVisits == 0) ? 0.0 : minMaxScores[state.playerToAgent(player)] / numVisits;
    }
    
    /**
     * @return Num visits (i.e. MCTS iterations) for this node
     */
    public int numVisits()
    {
    	return numVisits;
    }
    
    /**
     * @return Number of virtual visits
     */
    public int numVirtualVisits()
    {
    	return numVirtualVisits.get();
    }
    
    /**
     * Adds one virtual visit to this node (will be subtracted again during backpropagation)
     */
    public void addVirtualVisit()
    {
    	numVirtualVisits.incrementAndGet();
    }
	
	/**
     * @return Parent node, or null if this is the root
     */
    public BaseNode parent()
    {
    	return parent;
    }
    
    /**
     * @return Move leading from parent node to this node
     */
    public Move parentMove()
    {
    	return parentMove;
    }
    
    /**
     * Sets the number of visits of this node to the given number
     * @param numVisits
     */
    public void setNumVisits(final int numVisits)
    {
    	this.numVisits = numVisits;
    }
	
	/**
	 * Set the parent node of this node
	 * @param newParent
	 */
	public void setParent(final BaseNode newParent)
	{
		this.parent = newParent;
	}
	
	/**
	 * Sets the array of heuristic value estimates for this node 
	 * NOTE: (value estimates per player, not per child node).
	 * @param heuristicValueEstimates
	 */
	public void setHeuristicValueEstimates(final double[] heuristicValueEstimates)
	{
		this.heuristicValueEstimates = heuristicValueEstimates;
	}
	
	/**
     * @param player Player index
     * @return Total score (sum of scores) backpropagated into this node for player
     */
    public double totalScore(final int player)
    {
    	return totalScores[player];
    }
    
	/**
     * @param player Player index
     * @return MinMax score backpropagated into this node for player
     */
    public double minMaxScores(final int player)
    {
    	return minMaxScores[player];
    }
    
    /**
     * Backpropagates result with vector of utilities
     * @param utilities The utilities.
     */
    public void update(final double[] utilities)
    {
    	++numVisits;
    	for (int p = 1; p < totalScores.length; ++p)
    	{
    		totalScores[p] += utilities[p];
    	}
    	numVirtualVisits.decrementAndGet();
    }
    
    /**
     * Backpropagates result with vector of utilities in using MinMax.
     * @param utilities The utilities.
     */
    public void updateMinMax(final double[] utilities, final boolean max)
    {
    	// FIXME if both minmax and average updates are run at the same time, we also double-increment visit count
    	++numVisits;
    	for (int p = 1; p < totalScores.length; ++p)
    	{
    		// FIXME taking same min or max across entire vector for all players seems... wrong? breaks zero-sum?
    		minMaxScores[p] = (max) ? Math.max(minMaxScores[p], utilities[p]) : Math.min(minMaxScores[p], utilities[p]);
    	}
    }
    
    /**
     * @param player Player index
     * @param state
     * 
     * @return Value estimate for unvisited children of this node
     */
    public double valueEstimateUnvisitedChildren(final int player, final State state)
    {
    	switch (mcts.qInit())
		{
		case DRAW:
			return 0.0;
		case INF:
			return 10000.0;
		case LOSS:
			return -1.0;
		case PARENT:
			if (numVisits == 0)
			{
				return 10000.0;
			}
			else
			{
				return averageScore(player, state);
			}
		case WIN:
			return 1.0;
		default:
			return 0.0;
		}
    }
	
	//-------------------------------------------------------------------------
    
    /**
     * @param moveKey
     * @return GRAVE's AMAF stats for given move key. Creates new entry if it
     * does not yet exist in this node's table.
     */
    public NodeStatistics getOrCreateGraveStatsEntry(final MoveKey moveKey)
    {
    	NodeStatistics stats = graveStats.get(moveKey);
    	
    	if (stats == null)
    	{
    		stats = new NodeStatistics();
    		graveStats.put(moveKey, stats);
    		//System.out.println("creating entry for " + moveKey + " in " + this);
    	}
    	
    	return stats;
    }
    
    /**
     * @param moveKey
     * @return GRAVE's AMAF stats for given move key.
     */
    public NodeStatistics graveStats(final MoveKey moveKey)
    {
//    	if (!graveStats.containsKey(moveKey))
//    	{
//    		System.out.println("will be returning null! Total num keys in this node = " + graveStats.keySet().size());
//    		for (final MoveKey key : graveStats.keySet())
//    		{
//    			System.out.println("key = " + key + ", stats = " + graveStats.get(key));
//    		}
//    	}
    	
    	return graveStats.get(moveKey);
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * Computes a policy over the list of children based on the visit counts 
     * collected through an MCTS search process. The policy assigns probability 
     * to actions proportional to the exponentiated visit counts 
     * N(s, a)^{1 / tau}, where tau is a temperature parameter, as described 
     * in the AlphaGo Zero paper.
     * 
     * Special cases:
     * 	- tau = 1.f results in a policy proportional to the raw visit counts
     * 	- tau = 0.f (or, mathematically, tau --> 0.f) results in a greedy 
     * 	policy w.r.t. visit counts
     * 
     * AlphaGo Zero used tau = 1.f for the first 30 moves of every game, 
     * and tau = 0.f afterwards.
     * Anthony, Tian, and Barber (2017 NIPS paper) used tau = 1.f everywhere.
     * 
     * Note that sampling from the distribution that would result from the 
     * tau = 0.f case (greedy) could technically be implemented slightly more 
     * efficiently by sampling directly, using an implementation as used by 
     * RobustChild, rather than first computing the distribution and then
     * sampling from it.
     * 
     * @param tau
     * @return Vector.
     */
    public FVector computeVisitCountPolicy(final double tau)
    {
    	final FVector policy = new FVector(numLegalMoves());
    	
    	if (tau == 0.0)
    	{
    		// special case: simply want to select greedily with respect to 
    		// visit count
    		
    		// first find what the maximum visit count is, 
    		// and which children have that visit count
    		int maxVisitCount = -1;
    		final TIntArrayList maxVisitCountChildren = new TIntArrayList();
    		
    		for (int i = 0; i < numLegalMoves(); ++i)
    		{
    			final BaseNode child = childForNthLegalMove(i);
    			final int visitCount;
    			
    			if (child == null)
    			{
    				visitCount = 0;
    			}
    			else
    			{
    				visitCount = child.numVisits;
    			}
    			    			
    			if (visitCount > maxVisitCount)
    			{
    				maxVisitCount = visitCount;
    				maxVisitCountChildren.reset();
    				maxVisitCountChildren.add(i);
    			}
    			else if (visitCount == maxVisitCount)
    			{
    				maxVisitCountChildren.add(i);
    			}
    		}
    		
    		// this is the probability we assign to all max children
    		final float maxProb = 1.f / maxVisitCountChildren.size();
    		
    		// now assign the probabilities to indices
    		for (int i = 0; i < maxVisitCountChildren.size(); ++i)
    		{
    			policy.set(maxVisitCountChildren.getQuick(i), maxProb);
    		}
    	}
    	else
    	{
    		// first collect visit counts in vector
    		for (int i = 0; i < numLegalMoves(); ++i)
    		{
    			final BaseNode child = childForNthLegalMove(i);
    			final int visitCount;
    			
    			if (child == null)
    			{
    				visitCount = 0;
    			}
    			else
    			{
    				visitCount = child.numVisits;
    			}
    			
    			policy.set(i, visitCount);
    		}
    		
    		if (tau != 1.0)	// need to use exponentiate visit counts
    		{
    			policy.raiseToPower(1.0 / tau);
    		}
    		
    		final float sumVisits = policy.sum();
    		
    		if (sumVisits > 0.f)
    			policy.mult(1.f / policy.sum());
    	}
    	
    	return policy;
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * @return The normalised entropy of the discrete distribution implied by
     * the MCTS visit counts among this node's children.
     */
    public double normalisedEntropy()
    {
    	// Compute distribution implied by visit counts
    	final FVector distribution = computeVisitCountPolicy(1.f);
    	
    	final int dim = distribution.dim();
    	
    	if (dim <= 1)
    	{
    		return 0.0;
    	}
    	
    	// Compute unnormalised entropy 
    	// (in nats, unit won't matter after normalisation)
    	double entropy = 0.0;
    	
    	for (int i = 0; i < dim; ++i)
    	{
    		final float prob = distribution.get(i);
    		
    		if (prob > 0.f)
    		{
    			entropy -= prob * Math.log(prob);
    		}
    	}
    	
    	// Normalise and return
    	return (entropy / Math.log(dim));
    }
    
    /**
     * @return The normalised entropy of the distribution computed by the
     * learned Selection policy for this node.
     */
    public double learnedSelectionPolicyNormalisedEntropy()
    {
    	// compute distribution using learned Selection policy
    	final FVector distribution = learnedSelectionPolicy();
    	
    	final int dim = distribution.dim();
    	
    	if (dim <= 1)
    	{
    		return 0.0;
    	}
    	
    	// Compute unnormalised entropy 
    	// (in nats, unit won't matter after normalisation)
    	double entropy = 0.0;
    	
    	for (int i = 0; i < dim; ++i)
    	{
    		final float prob = distribution.get(i);
    		
    		if (prob > 0.f)
    		{
    			entropy -= prob * Math.log(prob);
    		}
    	}
    	
    	// Normalise and return
    	return (entropy / Math.log(dim));
    }
    
    /**
     * @return The normalised entropy of the distribution computed by the
     * learned Play-out policy for this node.
     */
    public double learnedPlayoutPolicyNormalisedEntropy()
    {
    	// compute distribution using learned Play-out policy
		final FVector distribution = 
				((SoftmaxPolicy) mcts.playoutStrategy()).computeDistribution(
						contextRef(), contextRef().game().moves(contextRef()).moves(), true);
		
		final int dim = distribution.dim();
    	
    	if (dim <= 1)
    	{
    		return 0.0;
    	}
    	
    	// Compute unnormalised entropy 
    	// (in nats, unit won't matter after normalisation)
    	double entropy = 0.0;
    	
    	for (int i = 0; i < dim; ++i)
    	{
    		final float prob = distribution.get(i);
    		
    		if (prob > 0.f)
    		{
    			entropy -= prob * Math.log(prob);
    		}
    	}
    	
    	// Normalise and return
    	return (entropy / Math.log(dim));
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * @return A sample of experience for learning with Expert Iteration
     */
    public ExItExperience generateExItExperience()
    {
    	final FastArrayList<Move> actions = new FastArrayList<Move>(numLegalMoves());
    	final float[] valueEstimates = new float[numLegalMoves()];
    	
    	for (int i = 0; i < numLegalMoves(); ++i)
    	{
    		final BaseNode child = childForNthLegalMove(i);
    		final Move m = new Move(nthLegalMove(i));
    		m.setMover(nthLegalMove(i).mover());
    		m.then().clear();	// Can't serialise these, and won't need them
    		actions.add(m);
    		
    		if (child == null)
    			valueEstimates[i] = -1.f;
    		else
    			valueEstimates[i] = (float) child.averageScore(deterministicContextRef().state().mover(), deterministicContextRef().state());
       	}
    	
    	return new ExItExperience
    			(
    				new ExItExperienceState(deterministicContextRef()),
    				actions,
    				computeVisitCountPolicy(1.0),
    				FVector.wrap(valueEstimates)
    			);
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * Wrapper class for statistics we may want to store inside nodes
     * (accumulated scores + visit count)
     * 
     * @author Dennis Soemers
     */
    public static class NodeStatistics
    {
    	/** Visit count */
    	public int visitCount = 0;
    	
    	/** Accumulated score */
    	public double accumulatedScore = 0.0;
    	
    	@Override
    	public String toString()
    	{
    		return "[visits = " + visitCount + ", accum. score = " + accumulatedScore + "]";
    	}
    }
    
    //-------------------------------------------------------------------------

}
