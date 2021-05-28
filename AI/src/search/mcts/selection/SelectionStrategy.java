package search.mcts.selection;

import org.json.JSONObject;

import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Interface for Selection strategies for MCTS
 * 
 * @author Dennis Soemers
 *
 */
public interface SelectionStrategy
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Should be implemented to select the index of a child of the current 
	 * node to traverse to.
	 * 
	 * @param mcts
	 * @param current
	 * @return Index of child.
	 */
	public int select(final MCTS mcts, final BaseNode current);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Flags indicating stats that should be backpropagated
	 */
	public int backpropFlags();
	
	/**
	 * Customize the selection strategy based on a list of given string inputs
	 * 
	 * @param inputs
	 */
	public void customise(final String[] inputs);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param json
	 * @return Selection strategy constructed from given JSON object
	 */
	public static SelectionStrategy fromJson(final JSONObject json)
	{
		SelectionStrategy selection = null;
		final String strategy = json.getString("strategy");
		
		if (strategy.equalsIgnoreCase("UCB1"))
		{
			return new UCB1();
		}
		
		return selection;
	}
	
	//-------------------------------------------------------------------------

}
