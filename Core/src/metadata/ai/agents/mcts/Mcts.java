package metadata.ai.agents.mcts;

import annotations.Opt;
import metadata.ai.agents.Agent;
import metadata.ai.agents.mcts.selection.Selection;
import metadata.ai.agents.mcts.selection.Ucb1;

/**
 * Describes a Monte-Carlo tree search agent.
 * 
 * @author Dennis Soemers
 */
public class Mcts implements Agent
{
	// WARNING: The weird capitalisation of the class name is INTENTIONAL!
	// This makes the type name in the grammar and documentation look better,
	// as just "<mcts>" instead of the really silly "<mCTS>" that we would get 
	// otherwise.
	
	
	//-------------------------------------------------------------------------
	
	/** Our Selection strategy */
	protected final Selection selection;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param selection The Selection strategy to be used by this MCTS agent [UCB1].
	 * 
	 * @example (mcts)
	 */
	public Mcts
	(
		@Opt final Selection selection
	)
	{
		if (selection != null)
			this.selection = selection;
		else
			this.selection = new Ucb1(null);
		
		if (this.selection.requiresLearnedSelectionPolicy())
		{
			// TODO check whether a learned selection policy was specified
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(mcts " + selection + ")";
	}
	
	//-------------------------------------------------------------------------

}
