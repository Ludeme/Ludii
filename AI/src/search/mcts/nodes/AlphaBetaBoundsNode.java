package search.mcts.nodes;

import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;

/**
 * Node for MCTS tree that includes alpha-beta style bounds per node.
 * 
 * @author Dennis Soemers
 */
public class AlphaBetaBoundsNode extends DeterministicNode 
{

	//-------------------------------------------------------------------------
	
	/** The alpha bound to use for selection of children from this node */
	private double alphaBound = Double.NEGATIVE_INFINITY;

	/** The beta bound to use for selection of children from this node */
	private double betaBound = Double.POSITIVE_INFINITY;

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
	public AlphaBetaBoundsNode
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
	
	/**
	 * @return alpha bound
	 */
	public double alphaBound()
	{
		return alphaBound;
	}
	
	/**
	 * @return beta bound
	 */
	public double betaBound()
	{
		return betaBound;
	}
	
	/**
	 * Sets the alpha bound
	 * @param newBound
	 */
	public void setAlphaBound(final double newBound)
	{
		alphaBound = newBound;
	}
	
	/**
	 * Sets the beta bound
	 * @param newBound
	 */
	public void setBetaBound(final double newBound)
	{
		betaBound = newBound;
	}
	
	//-------------------------------------------------------------------------
	
}
