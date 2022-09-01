package metadata.ai.agents.mcts.selection;

import metadata.ai.AIItem;

/**
 * Abstract class for Selection strategies for MCTS in AI metadata
 * 
 * @author Dennis Soemers
 */
public abstract class Selection implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Do we require a learned selection policy?
	 */
	@SuppressWarnings("static-method")
	public boolean requiresLearnedSelectionPolicy()
	{
		return false;
	}
	
	//-------------------------------------------------------------------------

}
