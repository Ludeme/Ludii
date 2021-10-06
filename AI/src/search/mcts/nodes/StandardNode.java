package search.mcts.nodes;

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
public final class StandardNode extends DeterministicNode
{
	
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
    public StandardNode
    (
    	final MCTS mcts, 
    	final BaseNode parent, 
    	final Move parentMove, 
    	final Move parentMoveWithoutConseq,
    	final Context context
    )
    {
    	super(mcts, parent, parentMove, parentMoveWithoutConseq, context);
    }
    
    //-------------------------------------------------------------------------

}
