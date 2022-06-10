package search.mcts.nodes;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import policies.softmax.SoftmaxPolicy;
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
	
	/** For the root, we no longer need thread-local current-legal move lists and can instead use a single fixed list */
	protected FastArrayList<Move> rootLegalMovesList = null;
	
	/** Current list of legal moves */
	protected ThreadLocal<FastArrayList<Move>> currentLegalMoves = ThreadLocal.withInitial(() -> {return null;});
	
	/** 
	 * Distribution over legal moves in current iteration in this node,
	 * as computed by learned Selection policy
	 */
	protected ThreadLocal<FVector> learnedSelectionPolicy = ThreadLocal.withInitial(() -> {return null;});
	
	/** Learned selection policy for root node, where we no longer need it to be thread-local */
	protected FVector rootLearnedSelectionPolicy = null;
	
	/** 
	 * Array in which we store, for every potential index of a currently-legal move, 
	 * the corresponding child node (or null if not yet expanded).
	 */
	protected ThreadLocal<OpenLoopNode[]> moveIdxToNode = ThreadLocal.withInitial(() -> {return null;});
	
	/** A mapping from move indices to nodes for the root (no longer want this to be thread-local) */
	protected OpenLoopNode[] rootMoveIdxToNode = null;
	
	/** Cached logit computed according to learned selection policy */
	protected ThreadLocal<Float> logit = ThreadLocal.withInitial(() -> {return Float.valueOf(Float.NaN);});
	
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
		if (rootMoveIdxToNode != null)
			return rootMoveIdxToNode[n];
		
		return moveIdxToNode.get()[n];
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
    	if (rootLearnedSelectionPolicy != null)
    		return rootLearnedSelectionPolicy;
    	
    	return learnedSelectionPolicy.get();
    }
    
    @Override
    public FastArrayList<Move> movesFromNode()
    {
    	if (rootLegalMovesList != null)
    		return rootLegalMovesList;
    	
    	return currentLegalMoves.get();
    }
    
    @Override
    public int nodeColour()
    {
    	return 0;	// could be anyone
    }
    
    @Override
    public Move nthLegalMove(final int n)
    {
    	return movesFromNode().get(n);
    }
	
	@Override
	public int numLegalMoves()
	{
		return movesFromNode().size();
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
		context.game().apply(context, movesFromNode().get(moveIdx));
    	return context;
    }
	
	@Override
	public void updateContextRef()
	{
		if (parent != null)
		{
			// We take the same reference as our parent node
			currentItContext.set(parent.contextRef());
			
			// and update some computations based on legal moves
			updateLegalMoveDependencies(false);
		}
	}
	
	@Override
	public void cleanThreadLocals()
	{
		currentItContext.remove();
		currentLegalMoves.remove();
		learnedSelectionPolicy.remove();
		moveIdxToNode.remove();
		logit.remove();
		
		getLock().lock();
		try
		{
			for (final OpenLoopNode child : children)
			{
				child.cleanThreadLocals();
			}
		}
		finally
		{
			getLock().unlock();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Update any internal data that depends on the list of legal
	 * moves in the current Context reference.
	 * @param root Whether this node is (or just turned into) a root node
	 */
	private void updateLegalMoveDependencies(final boolean root)
	{
		getLock().lock();
		try
		{
			final Context context = root ? deterministicContext : currentItContext.get();
			final FastArrayList<Move> legalMoves;
			
			if (root)
			{
				rootLegalMovesList = new FastArrayList<Move>(context.game().moves(context).moves());
				currentLegalMoves.set(null);
				legalMoves = rootLegalMovesList;
			}
			else
			{
				legalMoves = new FastArrayList<Move>(context.game().moves(context).moves());
				currentLegalMoves.set(legalMoves);
			}
						
			if (root)
			{
				// Now that this is a root node, we may be able to remove some 
				// children with moves that are not legal
				for (int i = children.size() - 1; i >= 0; --i)
				{
					if (!legalMoves.contains(children.get(i).parentMoveWithoutConseq))
						children.remove(i).cleanThreadLocals();
				}
			}
			
			// Update mapping from legal move index to child node
			final OpenLoopNode[] mapping = new OpenLoopNode[legalMoves.size()];
			if (root)
			{
				rootMoveIdxToNode = mapping;
				moveIdxToNode.set(null);
			}
			else
			{
				moveIdxToNode.set(mapping);
			}
			
			for (int i = 0; i < mapping.length; ++i)
			{
				final Move move = legalMoves.get(i);
				
				for (int j = 0; j < children.size(); ++j)
				{
					if (move.equals(children.get(j).parentMoveWithoutConseq))
					{
						mapping[i] = children.get(j);
						break;
					}
				}
			}
			
			// Update learned policy distribution
			if (mcts.learnedSelectionPolicy() != null)
			{
				final float[] logits = new float[mapping.length];
				
				for (int i = 0; i < logits.length; ++i)
				{
					if (mapping[i] != null && !Float.isNaN(mapping[i].logit.get().floatValue()))
					{
						logits[i] = mapping[i].logit.get().floatValue();
					}
					else
					{
						logits[i] = mcts.learnedSelectionPolicy().computeLogit(context, legalMoves.get(i));
						
						if (mapping[i] != null)
						{
							mapping[i].logit.set(Float.valueOf(logits[i]));
						}
					}
				}
				
				final FVector dist = FVector.wrap(logits);
				
				if (mcts.learnedSelectionPolicy() instanceof SoftmaxPolicy)
					dist.softmax();
				else
					dist.normalise();
				
				if (root)
				{
					rootLearnedSelectionPolicy = dist;
					learnedSelectionPolicy.set(null);
				}
				else
				{
					learnedSelectionPolicy.set(dist);
				}
			}
		}
		finally
		{
			getLock().unlock();
		}
	}
	
	//-------------------------------------------------------------------------

}
