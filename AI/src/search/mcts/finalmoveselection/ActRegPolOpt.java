package search.mcts.finalmoveselection;

import main.collections.FVector;
import other.move.Move;
import search.mcts.nodes.BaseNode;
import search.mcts.utils.RegPolOptMCTS;

/**
 * Act by sampling from pi-bar, as described in the
 * "Monte-Carlo tree search as regularized policy optimization" paper at 
 * ICML 2020: http://proceedings.mlr.press/v119/grill20a.html
 *
 * @author Dennis Soemers
 */
public class ActRegPolOpt implements FinalMoveSelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Exploration constant (should normally be the same one we use in Selection) */
	protected double explorationConstant;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with default exploration constant of 2.5.
	 */
	public ActRegPolOpt()
	{
		this(2.5);
	}
	
	/**
	 * Constructor with custom exploration constant
	 * @param explorationConstant
	 */
	public ActRegPolOpt(final double explorationConstant)
	{
		this.explorationConstant = explorationConstant;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectMove(final BaseNode rootNode)
	{
		final FVector distribution = RegPolOptMCTS.computePiBar(rootNode, explorationConstant);
		final int actionIndex = distribution.sampleProportionally();
		return rootNode.nthLegalMove(actionIndex);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void customise(final String[] inputs)
	{
		// Nothing to do
	}
	
	//-------------------------------------------------------------------------

}
