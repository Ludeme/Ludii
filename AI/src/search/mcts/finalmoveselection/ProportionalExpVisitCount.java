package search.mcts.finalmoveselection;

import main.collections.FVector;
import other.move.Move;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Selects moves proportionally to exponentiated visit counts,
 * 
 * This strategy should never be used for "competitive" play, but can be useful
 * to generate more variety in experience in self-play.
 * 
 * @author Dennis Soemers
 */
public final class ProportionalExpVisitCount implements FinalMoveSelectionStrategy 
{
	
	//-------------------------------------------------------------------------
	
	/** Temperature parameter tau (all visit counts will be raised to this power to generate distribution) */
	protected double tau;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with temperature parameter tau 
	 * (1.0 = proportional to visit counts, 0.0 = greedy)
	 * @param tau
	 */
	public ProportionalExpVisitCount(final double tau)
	{
		this.tau = tau;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectMove(final MCTS mcts, final BaseNode rootNode) 
	{
		final FVector distribution = rootNode.computeVisitCountPolicy(tau);
		final int actionIndex = distribution.sampleProportionally();
		return rootNode.nthLegalMove(actionIndex);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void customise(final String[] inputs) 
	{
		for (final String input : inputs)
		{
			if (input.startsWith("tau="))
			{
				tau = Double.parseDouble(input.substring("tau=".length()));
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
