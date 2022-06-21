package metadata.ai.agents;

import metadata.ai.AIItem;

/**
 * Describes an agent (either by name of expected best agent, or as a 
 * complete setup).
 * 
 * @author Dennis Soemers
 */
public interface Agent extends AIItem
{
	
	/** 
	 * Build a string that the AI Factory can use to create an AI 
	 * 
	 * @return The string to build the AI
	 */
	public String constructAgentString();

}
