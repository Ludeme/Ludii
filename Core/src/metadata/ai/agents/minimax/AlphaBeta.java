package metadata.ai.agents.minimax;

import metadata.ai.agents.Agent;

/**
 * Describes an Alpha-Beta search agent.
 * 
 * @author Dennis Soemers
 */
public class AlphaBeta implements Agent
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @example (alphaBeta)
	 */
	public AlphaBeta()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String constructAgentString()
	{
		return "algorithm=AlphaBeta";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(alphaBeta)";
	}
	
	//-------------------------------------------------------------------------

}
