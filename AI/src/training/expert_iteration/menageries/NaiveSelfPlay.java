package training.expert_iteration.menageries;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import training.expert_iteration.ExpertPolicy;
import training.expert_iteration.params.AgentsParams;

/**
 * Naive self-play menagerie: always uses the latest version of the trained agent,
 * for all player IDs.
 *
 * @author Dennis Soemers
 */
public class NaiveSelfPlay implements Menagerie
{
	
	//-------------------------------------------------------------------------
	
	/** Our dev checkpoint (the only one we actually use) */
	private AgentCheckpoint dev;
	
	//-------------------------------------------------------------------------

	@Override
	public DrawnAgentsData drawAgents(final Game game, final AgentsParams agentsParams)
	{
		final List<ExpertPolicy> agents = new ArrayList<ExpertPolicy>(game.players().count() + 1);
		agents.add(null);
		for (int p = 1; p <= game.players().count(); ++p)
		{
			agents.add(dev.generateAgent(game, agentsParams));
		}
		return new DrawnAgentsData(agents);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initialisePopulation
	(
		final Game game,
		final AgentsParams agentsParams, 
		final Features features, 
		final Heuristics heuristics
	)
	{
		dev = new AgentCheckpoint(agentsParams.expertAI, "Dev", features, heuristics);
	}
	
	@Override
	public void updateDevFeatures(final Features features)
	{
		dev = new AgentCheckpoint(dev.agentName, "Dev", features, dev.heuristicsMetadata);
	}
	
	@Override
	public void updateDevHeuristics(final Heuristics heuristics)
	{
		dev = new AgentCheckpoint(dev.agentName, "Dev", dev.featuresMetadata, heuristics);
	}
	
	@Override
	public void updateOutcome(final Context context, final DrawnAgentsData drawnAgentsData)
	{
		// Nothing to do here
	}
	
	@Override
	public String generateLog()
	{
		return null;
	}
	
	//-------------------------------------------------------------------------

}
