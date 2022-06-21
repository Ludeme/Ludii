package search.minimax;

import metadata.ai.heuristics.Heuristics;

/**
 * 
 * A special class of AI intendended to be used for generating training data.
 * It's the transposition table that contains all the useful data.
 * The principle was proposed by Quentin Sohal's "Learning to Play Perfect-Information Games
 * without Knowledge" paper in 2021.
 * 
 * @author cyprien
 */

public class DescentBFS extends BestFirstSearch
{
	
	/** Our heuristic value function estimator */
	protected Heuristics heuristicValueFunction = null;
	
	//-------------------------------------------------------------------------
	
	public DescentBFS (final Heuristics heuristics)
	{
		friendlyName = "Alpha-Beta";
		heuristicValueFunction = heuristics;
	}	
	
	public static DescentBFS generateDescentBFS (final Heuristics heuristics)
	{
		return new DescentBFS(heuristics);
	}
	
	//-------------------------------------------------------------------------
}
