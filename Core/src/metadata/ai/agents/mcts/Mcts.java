package metadata.ai.agents.mcts;

import metadata.ai.agents.Agent;

/**
 * Describes a Monte-Carlo tree search agent.
 * 
 * @author Dennis Soemers
 */
public class Mcts implements Agent
{
	// WARNING: The weird capitalisation of of the class name is INTENTIONAL!
	// This makes the type name in the grammar and documentation look better,
	// as just "<mcts>" instead of the really silly "<mCTS>" that we would get 
	// otherwise.
	
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @example (mcts)
	 */
	public Mcts()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String constructAgentString()
	{
		return "algorithm=MCTS";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(mcts)";
	}
	
	//-------------------------------------------------------------------------

}
