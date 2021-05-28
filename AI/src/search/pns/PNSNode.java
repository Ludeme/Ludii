package search.pns;

import java.util.Arrays;

import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import search.pns.ProofNumberSearch.ProofGoals;

/**
 * Node for search trees in PNS
 * 
 * @author Dennis Soemers
 */
public class PNSNode
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Nodes types in search trees in PNS
	 * 
	 * @author Dennis Soemers
	 */
	public enum PNSNodeTypes
	{
		/** An OR node */
		OR_NODE,
		
		/** An AND node */
		AND_NODE
	}
	
	/**
	 * Values of nodes in search trees in PNS
	 * 
	 * @author Dennis Soemers
	 */
	public enum PNSNodeValues
	{
		/** A proven node */
		TRUE,
		
		/** A disproven node */
		FALSE,
		
		/** Unknown node (yet to prove or disprove) */
		UNKNOWN
	}
	
	//-------------------------------------------------------------------------
	
	/** Our parent node */
	protected final PNSNode parent;
	
	/** Our node type */
	protected final PNSNodeTypes nodeType;
	
	/** Context for this node (contains game state) */
	protected final Context context;
    
    /** Array of child nodes. */
    protected final PNSNode[] children;
    
    /** Array of legal moves in this node's state */
    protected final Move[] legalMoves;
    
    /** Whether we have expanded (generated child nodes) */
    private boolean expanded = false;
    
    /** Our proof number */
    private int proofNumber = -1;
    
    /** Our disproof number */
    private int disproofNumber = -1;
    
    /** Our node's value */
    private PNSNodeValues value = PNSNodeValues.UNKNOWN;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param context
	 * @param proofGoal
	 * @param proofPlayer
	 */
	public PNSNode
	(
		final PNSNode parent, 
		final Context context, 
		final ProofGoals proofGoal, 
		final int proofPlayer
	)
	{
		this.parent = parent;
		this.context = context;
		
		final int mover = context.state().mover();
		
		if (mover == proofPlayer)
		{
			if (proofGoal == ProofGoals.PROVE_WIN)
				nodeType = PNSNodeTypes.OR_NODE;
			else
				nodeType = PNSNodeTypes.AND_NODE;
		}
		else
		{
			if (proofGoal == ProofGoals.PROVE_WIN)
				nodeType = PNSNodeTypes.AND_NODE;
			else
				nodeType = PNSNodeTypes.OR_NODE;
		}
    	
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
    	
    	children = new PNSNode[legalMoves.length];
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Array of child nodes (contains null entries if not expanded)
	 */
	public PNSNode[] children()
	{
		return children;
	}
	
	/**
	 * @return Context in this node
	 */
	public Context context()
	{
		return context;
	}
	
	/**
	 * Deletes subtree below this node
	 */
	public void deleteSubtree()
	{
		Arrays.fill(children, null);
	}
	
	/**
	 * @return Our disproof number
	 */
	public int disproofNumber()
	{
		assert (disproofNumber >= 0);
		return disproofNumber;
	}
	
	/**
	 * @return True if and only if this node has been expanded
	 */
	public boolean isExpanded()
	{
		return expanded;
	}
	
	/**
	 * @return Our node type
	 */
	public PNSNodeTypes nodeType()
	{
		return nodeType;
	}
	
	/**
	 * @return Our proof number
	 */
	public int proofNumber()
	{
		assert (proofNumber >= 0);
		return proofNumber;
	}
	
	/**
	 * Sets our disproof number
	 * @param disproofNumber
	 */
	public void setDisproofNumber(final int disproofNumber)
	{
		this.disproofNumber = disproofNumber;
	}
	
	/**
	 * Sets whether or not we're expanded
	 * @param expanded
	 */
	public void setExpanded(final boolean expanded)
	{
		this.expanded = expanded;
	}
	
	/**
	 * Sets our proof number
	 * @param proofNumber
	 */
	public void setProofNumber(final int proofNumber)
	{
		this.proofNumber = proofNumber;
	}
	
	/**
	 * Sets our node's value
	 * @param value
	 */
	public void setValue(final PNSNodeValues value)
	{
		this.value = value;
	}
	
	/**
	 * @return Our node's value
	 */
	public PNSNodeValues value()
	{
		return value;
	}
	
	//-------------------------------------------------------------------------

}
