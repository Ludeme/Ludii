package utils;
import game.Game;
import other.AI;
import other.context.Context;
import other.move.Move;

/**
 * Default Ludii AI. This is an agent that attempts to automatically
 * switch to different algorithms based on the metadata in a game's
 * .lud file.
 * 
 * If no best AI can be discovered from the metadata, this will default to:
 * 	- Flat Monte-Carlo for simultaneous-move games
 * 	- UCT for everything else
 *
 * @author Dennis Soemers
 */
public final class LudiiAI extends AI
{
	
	//-------------------------------------------------------------------------
	
	/** The current agent we use for the current game */
	private AI currentAgent = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public LudiiAI()
	{
		this.friendlyName = "Ludii";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds, 
		final int maxIterations, 
		final int maxDepth
	)
	{
		return currentAgent.selectAction(game, context, maxSeconds, maxIterations, maxDepth);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		if (currentAgent != null)
			currentAgent.closeAI();
		
		currentAgent = AIFactory.fromMetadata(game);
		
		if (currentAgent == null)
		{
			if (!game.isAlternatingMoveGame())
				currentAgent = AIFactory.createAI("Flat MC");
			else
				currentAgent = AIFactory.createAI("UCT");
		}
		
		this.friendlyName = "Ludii (" + currentAgent.friendlyName + ")";
		
		if (!currentAgent.supportsGame(game))
		{
			System.err.println
			(
				"Warning! Default AI (" + currentAgent + ")"
				+ " does not support game (" + game.name() + ")"
			);
		}
		
		assert(currentAgent.supportsGame(game));
		
		currentAgent.initAI(game, playerID);
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		return true;
	}
	
	@Override
	public double estimateValue()
	{
		if (currentAgent != null)
			return currentAgent.estimateValue();
		else
			return 0.0;
	}
	
	@Override
	public String generateAnalysisReport()
	{
		if (currentAgent != null)
			return currentAgent.generateAnalysisReport();
		else
			return null;
	}
	
	@Override
	public AIVisualisationData aiVisualisationData()
	{
		if (currentAgent != null)
			return currentAgent.aiVisualisationData();
		else
			return null;
	}
	
	@Override
	public void setWantsInterrupt(final boolean val)
	{
		super.setWantsInterrupt(val);
		if (currentAgent != null)
			currentAgent.setWantsInterrupt(val);
	}
	
	//-------------------------------------------------------------------------

}
