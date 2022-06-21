package metadata.ai.agents.mcts.selection;

import annotations.Opt;

/**
 * Describes the selection strategy also used by AlphaGo Zero (and AlphaZero).
 * Requires that a learned selection policy (based on features) has been 
 * described for the MCTS agent that uses this selection strategy.
 * 
 * @author Dennis Soemers
 */
public class Ag0 extends Selection
{
	
	//-------------------------------------------------------------------------
	
	/** The exploration constant */
	protected final double explorationConstant;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param explorationConstant The value to use for the exploration constant [2.5].
	 * 
	 * @example (ag0)
	 */
	public Ag0(@Opt final Float explorationConstant)
	{
		if (explorationConstant == null)
			this.explorationConstant = 2.5;
		else
			this.explorationConstant = explorationConstant.doubleValue();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(ag0 " + explorationConstant + ")";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean requiresLearnedSelectionPolicy()
	{
		return true;
	}
	
	//-------------------------------------------------------------------------

}
