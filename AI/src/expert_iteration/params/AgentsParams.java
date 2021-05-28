package expert_iteration.params;

/**
 * Wrapper around params for agents setup/configuration in training runs.
 *
 * @author Dennis Soemers
 */
public class AgentsParams
{
	
	//-------------------------------------------------------------------------
	
	/** Type of AI to use as expert */
	public String expertAI;
	
	/** Filepath for best agents data directory for this specific game (+ options) */
	public String bestAgentsDataDir;
	
	/** Max allowed thinking time per move (in seconds) */
	public double thinkingTime;
	
	/** Max allowed number of MCTS iterations per move */
	public int iterationLimit;
	
	/** Search depth limit (for e.g. Alpha-Beta experts) */
	public int depthLimit;
	
	/** Maximum number of actions per playout which we'll bias using features (-1 for no limit) */
	public int maxNumBiasedPlayoutActions;
	
	/** If true, use tournament mode (similar to the one in Polygames) */
	public boolean tournamentMode;
	
	/** Epsilon for epsilon-greedy features-based playouts */
	public double playoutFeaturesEpsilon;
	
	//-------------------------------------------------------------------------

}
