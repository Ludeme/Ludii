package search.minimax;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.collections.Pair;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import other.move.Move;
import other.state.State;

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
