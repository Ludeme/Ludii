package search.mcts.nodes;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;

/**
 * Node class for Open-Loop implementations of MCTS.
 * This is primarily intended for nondeterministic games.
 * 
 * @author Dennis Soemers
 */
public final class OpenLoopNode extends BaseNode
{
	
	//-------------------------------------------------------------------------
	
	/** List of child nodes */
	protected final List<OpenLoopNode> children = new ArrayList<OpenLoopNode>(10);
	
	/** Context object for current iteration being run through this node */
	protected ThreadLocal<Context> currentItContext = ThreadLocal.withInitial(() -> {return null;});
	
	/** Our root nodes will keep a deterministic context reference */
	protected Context deterministicContext = null;
	
	/** Current list of legal moves */
	protected FastArrayList<Move> currentLegalMoves = null;
	
	/** 
	 * Distribution over legal moves in current iteration in this node,
	 * as computed by learned Selection policy
	 */
	protected FVector learnedSelectionPolicy = null;
	
	/** 
	 * Array in which we store, for every potential index of a currently-legal move, 
	 * the corresponding child node (or null if not yet expanded).
	 */
	protected OpenLoopNode[] moveIdxToNode = null;
	
	/** Cached logit computed according to learned selection policy */
	protected float logit = Float.NaN;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mcts
	 * @param parent
	 * @param parentMove
	 * @param parentMoveWithoutConseq
	 * @param game
	 */
	public OpenLoopNode
	(
		final MCTS mcts, 
		final BaseNode parent, 
		final Move parentMove, 
		final Move parentMoveWithoutConseq, 
		final Game game
	)
	{
		super(mcts, parent, parentMove, parentMoveWithoutConseq, game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
    public void addChild(final BaseNode child, final int moveIdx)
    {
    	children.add((OpenLoopNode) child);
    	
    	if (parent() == null && deterministicContext != null)
    	{
    		// in case of root node, we'll also want to make sure to call this
    		updateLegalMoveDependencies(true);
    	}
    }
	
	@Override
    public OpenLoopNode childForNthLegalMove(final int n)
    {
		return moveIdxToNode[n];
    }
	
	@Override
	public Context contextRef()
	{
		return currentItContext.get();
	}
	
	@Override
    public Context deterministicContextRef()
    {
    	return deterministicContext;
    }
	
	@Override
	public OpenLoopNode findChildForMove(final Move move)
	{
		OpenLoopNode result = null;
				
		for (final OpenLoopNode child : children)
		{
			if (child.parentMove().equals(move))
			{
				result = child;
				break;
			}
		}
		
		return result;
	}
	
    @Override
    public FVector learnedSelectionPolicy()
    {
    	return learnedSelectionPolicy;
    }
    
    @Override
    public FastArrayList<Move> movesFromNode()
    {
    	return currentLegalMoves;
    }
    
    @Override
    public int nodeColour()
    {
    	return 0;	// could be anyone
    }
    
    @Override
    public Move nthLegalMove(final int n)
    {
    	return currentLegalMoves.get(n);
    }
	
	@Override
	public int numLegalMoves()
	{
		return currentLegalMoves.size();
	}
	
	@Override
    public Context playoutContext()
    {
    	// Don't need to copy context
    	return currentItContext.get();
    }
	
	@Override
    public void rootInit(final Context context)
    {
		deterministicContext = context;
		currentItContext.set(mcts.copyContext(context));
    	updateLegalMoveDependencies(true);
    }
	
	@Override
    public void startNewIteration(final Context context)
    {
		// make a copy of given context
		currentItContext.set(mcts.copyContext(context));
    }
	
	@Override
    public int sumLegalChildVisits()
    {
    	// only collect visits of children that are currently legal, not
		// just the visit count of this node
		int sum = 0;
		
		for (int i = 0; i < numLegalMoves(); ++i)
		{
			final OpenLoopNode child = childForNthLegalMove(i);
			
			if (child != null)
			{
				sum += child.numVisits;
			}
		}
		
		return sum;
    }
	
	@Override
    public Context traverse(final int moveIdx)
    {
    	// No need to copy current context, just modify it
		final Context context = currentItContext.get();
		context.game().apply(context, currentLegalMoves.get(moveIdx));
    	return context;
    }
	
	@Override
	public void updateContextRef()
	{
		// we take the same reference as our parent node
		if (parent != null)
			currentItContext.set(parent.contextRef());
		
		// and update some computations based on legal moves
		updateLegalMoveDependencies(false);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Update any internal data that depends on the list of legal
	 * moves in the current Context reference.
	 * @param root Whether this node is (or just turned into) a root node
	 */
	private void updateLegalMoveDependencies(final boolean root)
	{
		final Context context = root ? deterministicContext : currentItContext.get();
		currentLegalMoves = new FastArrayList<Move>(context.game().moves(context).moves());
		
		if (root)
		{
			// now that this is a root node, we may be able to remove some 
			// children with moves that are not legal
			for (int i = 0; i < children.size(); /**/)
			{
				if (currentLegalMoves.contains(children.get(i).parentMoveWithoutConseq))
					++i;
				else
					children.remove(i);
			}
		}
		
		// update mapping from legal move index to child node
		moveIdxToNode = new OpenLoopNode[currentLegalMoves.size()];
		
		for (int i = 0; i < moveIdxToNode.length; ++i)
		{
			final Move move = currentLegalMoves.get(i);
			
			for (int j = 0; j < children.size(); ++j)
			{
				if (move.equals(children.get(j).parentMoveWithoutConseq))
				{
					moveIdxToNode[i] = children.get(j);
					break;
				}
			}
		}
		
		// update learned policy distribution
		if (mcts.learnedSelectionPolicy() != null)
		{
			final float[] logits = new float[moveIdxToNode.length];
			
			for (int i = 0; i < logits.length; ++i)
			{
				if (moveIdxToNode[i] != null && !Float.isNaN(moveIdxToNode[i].logit))
				{
					logits[i] = moveIdxToNode[i].logit;
				}
				else
				{
					logits[i] = mcts.learnedSelectionPolicy().computeLogit(
							context, currentLegalMoves.get(i));
					
					if (moveIdxToNode[i] != null)
					{
						moveIdxToNode[i].logit = logits[i];
					}
				}
			}
			
			learnedSelectionPolicy = FVector.wrap(logits);
			learnedSelectionPolicy.softmax();
		}
	}
	
	//-------------------------------------------------------------------------

}
