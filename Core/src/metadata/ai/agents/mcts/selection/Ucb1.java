package metadata.ai.agents.mcts.selection;

import annotations.Opt;

/**
 * Describes the UCB1 selection strategy, which is one of the most straightforward
 * and simple Selection strategies, used by the standard UCT variant of MCTS.
 * 
 * @author Dennis Soemers
 */
public class Ucb1 extends Selection
{
	// WARNING: The weird capitalisation of of the class name is INTENTIONAL!
	// This makes the type name in the grammar and documentation look better,
	// as just "<ucb1>" instead of the really silly "<uCB1>" that we would get 
	// otherwise.
	
	
	//-------------------------------------------------------------------------
	
	/** The exploration constant */
	protected final double explorationConstant;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param explorationConstant The value to use for the exploration constant [square root of 2].
	 * 
	 * @example (ucb1)
	 * @example (ucb1 0.6)
	 */
	public Ucb1(@Opt final Float explorationConstant)
	{
		if (explorationConstant == null)
			this.explorationConstant = Math.sqrt(2.0);
		else
			this.explorationConstant = explorationConstant.doubleValue();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(ucb1 " + explorationConstant + ")";
	}
	
	//-------------------------------------------------------------------------

}
