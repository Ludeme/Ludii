package search.mcts.nodes;

import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;

/**
 * Nodes for "standard" MCTS search trees, for deterministic games.
 * This node implementation stores a game state in every node, and
 * assumes every node has a fixed list of legal actions.
 * 
 * @author Dennis Soemers
 */
public final class Node extends BaseNode
{
	
	//-------------------------------------------------------------------------
	
	/** Context for this node (contains game state) */
	protected final Context context;
    
    /** Array of child nodes. */
    protected final Node[] children;
    
    /** Array of legal moves in this node's state */
    protected final Move[] legalMoves;
    
    /** Cached policy over the list of children */
    protected FVector cachedPolicy = null;
    
    /** Indices of relevant children (for deterministic game, every child is always relevant) */
    protected final int[] childIndices;
    
    //-------------------------------------------------------------------------
    
    /**
     * Constructor 
     * 
     * @param mcts
     * @param parent
     * @param parentMove
     * @param parentMoveWithoutConseq
     * @param context
     */
    public Node
    (
    	final MCTS mcts, 
    	final BaseNode parent, 
    	final Move parentMove, 
    	final Move parentMoveWithoutConseq,
    	final Context context
    )
    {
    	super(mcts, parent, parentMove, parentMoveWithoutConseq, context.game());
    	this.context = context;
    	
    	if (context.trial().over())
    	{
    		// we just created a node for a terminal game state, 
    		// so create empty list of actions
    		legalMoves = new Move[0];
    	}
    	else
    	{
    		// non-terminal game state, so figure out list of actions we can 
    		// still take
    		final FastArrayList<Move> actions = context.game().moves(context).moves();
    		legalMoves = new Move[actions.size()];
    		actions.toArray(legalMoves);
    	}
    	
    	children = new Node[legalMoves.length];
    	childIndices = new int[children.length];
    	
    	for (int i = 0; i < childIndices.length; ++i)
    	{
    		childIndices[i] = i;
    	}
    }
    
    //-------------------------------------------------------------------------

    @Override
    public void addChild(final BaseNode child, final int moveIdx)
    {
    	children[moveIdx] = (Node) child;
    }
    
    @Override
    public Node childForNthLegalMove(final int n)
    {
    	return children[n];
    }
    
    @Override
    public Context contextRef()
    {
    	return context;
    }
    
    @Override
    public Context deterministicContextRef()
    {
    	return context;
    }
    
    @Override
    public Node findChildForMove(final Move move)
    {
    	Node result = null;
		
		for (final Node child : children)
		{
			if (child != null && child.parentMove().equals(move))
			{
				//System.out.println("found equal move: " + child.parentMove() + " equals " + move);
				result = child;
				break;
			}
		}
		
		return result;
    }
    
    @Override
    public FastArrayList<Move> movesFromNode()
    {
    	return new FastArrayList<Move>(legalMoves);
    }
    
    @Override
    public int nodeColour()
    {
    	return context.state().mover();
    }
    
    @Override
    public Move nthLegalMove(final int n)
    {
    	return legalMoves[n];
    }
    
    @Override
    public int numLegalMoves()
    {
    	return children.length;
    }
    
    @Override
    public Context playoutContext()
    {
    	// need to copy context
    	return mcts.copyContext(context);
    }
    
    @Override
    public void rootInit(final Context cont)
    {
    	// do nothing
    }
    
    @Override
    public void startNewIteration(final Context cont)
    {
    	// do nothing
    }
    
    @Override
    public int sumLegalChildVisits()
    {
    	// just the number of visits of this node
    	return numVisits;
    }
    
    @Override
    public Context traverse(final int moveIdx)
    {
    	final Context newContext;
    	
    	if (children[moveIdx] == null)
    	{
    		// need to copy context
        	newContext = mcts.copyContext(context);
        	newContext.game().apply(newContext, legalMoves[moveIdx]);
    	}
    	else
    	{
    		newContext = children[moveIdx].context;
    	}
    	
    	return newContext;
    }
    
    @Override
    public void updateContextRef()
    {
    	// do nothing
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * @return Array of child nodes
     */
    public Node[] children()
    {
    	return children;
    }
    
    /**
     * @return List of legal actions for this node's state
     */
    public Move[] legalActions()
    {
    	return legalMoves;
    }
    
    //-------------------------------------------------------------------------
    
    @Override
    public FVector learnedSelectionPolicy()
    {
    	// NOTE: by caching policy, we're assuming here that our learned policy 
    	// will never change in the middle of a single game. Have to change 
    	// this if we ever want to experiment with online learning in the 
    	// middle of a game.
    	if (cachedPolicy == null)
    	{
    		// didn't compute policy yet, so need to do so
    		cachedPolicy = 
    				mcts.learnedSelectionPolicy().computeDistribution(
    						context, new FastArrayList<Move>(legalMoves), true);
    	}
    	
    	return cachedPolicy;
    }
    
    //-------------------------------------------------------------------------

}
