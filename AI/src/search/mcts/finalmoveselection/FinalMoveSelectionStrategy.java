package search.mcts.finalmoveselection;

import org.json.JSONObject;

import other.move.Move;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Interface for different strategies of finally selecting the move to play in the real game
 * (after searching finished)
 * 
 * @author Dennis Soemers
 *
 */
public interface FinalMoveSelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Should be implemented to select the move to play in the real game
	 * 
	 * @param mcts
	 * @param rootNode
	 * @return The move.
	 */
	public Move selectMove(final MCTS mcts, final BaseNode rootNode);

	//-------------------------------------------------------------------------
	
	/**
	 * Customise the final move selection strategy based on a list of given string inputs
	 * 
	 * @param inputs
	 */
	public void customise(final String[] inputs);

	//-------------------------------------------------------------------------
	
	/**
	 * @param json
	 * @return Final Move Selection strategy constructed from given JSON object
	 */
	public static FinalMoveSelectionStrategy fromJson(final JSONObject json)
	{
		FinalMoveSelectionStrategy selection = null;
		final String strategy = json.getString("strategy");
		
		if (strategy.equalsIgnoreCase("RobustChild"))
		{
			return new RobustChild();
		}
		
		return selection;
	}
	
	//-------------------------------------------------------------------------
	
}
