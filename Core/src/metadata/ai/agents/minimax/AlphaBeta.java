package metadata.ai.agents.minimax;

import annotations.Opt;
import metadata.ai.agents.Agent;
import metadata.ai.heuristics.Heuristics;

/**
 * Describes an Alpha-Beta search agent.
 * 
 * @author Dennis Soemers
 */
public class AlphaBeta implements Agent
{
	
	//-------------------------------------------------------------------------
	
	/** The heuristics we want to use. If null, will just use from game file's metadata */
	protected final Heuristics heuristics;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param heuristics The heuristics to be used by this agent. Will default
	 * to heuristics from the game file's metadata if left unspecified [null].
	 * 
	 * @example (alphaBeta)
	 */
	public AlphaBeta
	(
		@Opt final Heuristics heuristics
	)
	{
		this.heuristics = heuristics;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our heuristics (can be null if we just want to use from game 
	 * file's metadata)
	 */
	public Heuristics heuristics()
	{
		return heuristics;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		if (heuristics == null)
			return "(alphaBeta)";
		else
			return "(alphaBeta " + heuristics.toString() + ")";
	}
	
	//-------------------------------------------------------------------------

}
