package training.expert_iteration.menageries;

import java.util.List;

import game.Game;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import training.expert_iteration.ExpertPolicy;
import training.expert_iteration.params.AgentsParams;

/**
 * Interface for "menageries": objects that can tell us which agents to use
 * in self-play games of a self-play training process. The term "menagier" is
 * inspired by "A Generalized Framework for Self-Play Training" by Hernandez et al., 2019.
 * However, in our case the menagerie actually also fulfills the roles of the
 * policy sampling distribution and the curator.
 *
 * @author Dennis Soemers
 */
public interface Menagerie
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @param agentsParams
	 * @return List of agents to use in a single self-play game
	 */
	public DrawnAgentsData drawAgents(final Game game, final AgentsParams agentsParams);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Initialise our population of checkpoints (+ dev)
	 * 
	 * @param game
	 * @param agentsParams
	 * @param features
	 * @param heuristics
	 */
	public void initialisePopulation
	(
		final Game game,
		final AgentsParams agentsParams, 
		final Features features, 
		final Heuristics heuristics
	);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Update the dev checkpoint's features
	 * @param features
	 */
	public void updateDevFeatures(final Features features);
	
	/**
	 * Update the dev checkpoint's heuristics
	 * @param heuristics
	 */
	public void updateDevHeuristics(final Heuristics heuristics);
	
	/**
	 * Update the menagerie based on an outcome of a self-play trial
	 * @param context
	 * @param drawnAgentsData
	 */
	public void updateOutcome(final Context context, final DrawnAgentsData drawnAgentsData);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return String describing the menagerie's data, for log (or null if nothing to log)
	 */
	public String generateLog();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Data describing a collection of agents that has been drawn. Specific
	 * implementations of Menageries may use subclasses of this to include
	 * additional data that they might need.
	 *
	 * @author Dennis Soemers
	 */
	public static class DrawnAgentsData
	{
		
		/** List of experts */
		protected final List<ExpertPolicy> agents;
		
		/**
		 * Constructor
		 * @param agents
		 */
		public DrawnAgentsData(final List<ExpertPolicy> agents)
		{
			this.agents = agents;
		}
		
		/**
		 * @return List of expert agents
		 */
		public List<ExpertPolicy> getAgents()
		{
			return agents;
		}
		
	}
	
	//-------------------------------------------------------------------------

}
