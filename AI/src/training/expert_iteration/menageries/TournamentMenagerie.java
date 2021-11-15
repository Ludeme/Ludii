package training.expert_iteration.menageries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import other.RankUtils;
import other.context.Context;
import training.expert_iteration.ExpertPolicy;
import training.expert_iteration.params.AgentsParams;

/**
 * Menagerie for Elo-based tournament mode (like in Polygames)
 *
 * @author Dennis Soemers
 */
public class TournamentMenagerie implements Menagerie
{
	
	//-------------------------------------------------------------------------
	
	/** Our dev checkpoint */
	private AgentCheckpoint dev;
	
	/** Population of checkpoints */
	private final List<AgentCheckpoint> population = new ArrayList<AgentCheckpoint>();
	
	/** Indexed by index of population list; Elo rating (one for every agent in population) */
	private TFloatArrayList populationElosTable;
	
	/** Elo rating for dev */
	private float devElo = 0.f;
	
	/** First indexed by Player ID (in game), secondly indexed by index of population list */
	private TIntArrayList[] agentPickCounts;
	
	/** How many checkpoints do we have? */
	private int checkpointCounter = 0;
	
	/** Do we have to add our new dev to the population? */
	private boolean shouldAddDev = false;
	
	//-------------------------------------------------------------------------

	@Override
	public TournamentDrawnAgentsData drawAgents(final Game game, final AgentsParams agentsParams)
	{
		if (shouldAddDev)
		{
			population.add(dev);
			shouldAddDev = false;
			
			// Initialise Elo rating for new checkpoint
			if (checkpointCounter > 0)
				populationElosTable.add(devElo);
			else
				populationElosTable.add(0.f);
			
			for (int p = 1; p < agentPickCounts.length; ++p)
			{
				agentPickCounts[p].add(0);
			}
			
			++checkpointCounter;
		}
		
		final int[] agentIndices = new int[agentPickCounts.length];
		agentIndices[0] = -1;
		final List<ExpertPolicy> agents = new ArrayList<ExpertPolicy>(agentPickCounts.length);
		agents.add(null);
		
		// We will always use dev for at least one of the players
		final int devIndex = ThreadLocalRandom.current().nextInt(1, agentPickCounts.length);
		
		for (int p = 1; p < agentPickCounts.length; ++p)
		{
			if (p == devIndex)
			{
				agents.add(dev.generateAgent(game, agentsParams));
				agentIndices[p] = -1;
			}
			else
			{
				// Compute vector of probabilities for this player for all checkpoints based on Elo ratings
				final FVector probs = new FVector(populationElosTable);
				final float max = probs.max();
	
				for (int i = 0; i < probs.dim(); ++i)
				{
					probs.set(i, (float) Math.exp((probs.get(i) - max) / 400.f));
				}
	
				final int sampledAgentIdx = probs.sampleProportionally();
				agents.add(population.get(sampledAgentIdx).generateAgent(game, agentsParams));
				agentIndices[p] = sampledAgentIdx;
				agentPickCounts[p].setQuick(sampledAgentIdx, agentPickCounts[p].getQuick(sampledAgentIdx) + 1);
			}
		}
		
		return new TournamentDrawnAgentsData(agents, devIndex, agentIndices);
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
		dev = new AgentCheckpoint(agentsParams.expertAI, "Checkpoint " + checkpointCounter, features, heuristics);
		devElo = 0.f;
		population.clear();
		shouldAddDev = true;
		
		final int numPlayers = game.players().count();
		populationElosTable = new TFloatArrayList();
		agentPickCounts = new TIntArrayList[numPlayers + 1];
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			agentPickCounts[p] = new TIntArrayList();
		}
		
		// Start out with plain UCT and MC-GRAVE agents
		for (final String startingAgent : new String[]{"UCT", "MC-GRAVE"})
		{
			population.add(new AgentCheckpoint(startingAgent, startingAgent, null, null));
			populationElosTable.add(0.f);
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				agentPickCounts[p].add(0);
			}
		}
	}
	
	@Override
	public void updateDevFeatures(final Features features)
	{
		dev = new AgentCheckpoint(dev.agentName, "Checkpoint " + checkpointCounter, features, dev.heuristicsMetadata);
		shouldAddDev = true;
	}
	
	@Override
	public void updateDevHeuristics(final Heuristics heuristics)
	{
		dev = new AgentCheckpoint(dev.agentName, "Checkpoint " + checkpointCounter, dev.featuresMetadata, heuristics);
		shouldAddDev = true;
	}
	
	@Override
	public void updateOutcome(final Context context, final DrawnAgentsData drawnAgentsData)
	{
		final TournamentDrawnAgentsData d = (TournamentDrawnAgentsData) drawnAgentsData;
		final double[] utilities = RankUtils.agentUtilities(context);
		
		float sumElos = 0.f;
		
		for (int p = 1; p < agentPickCounts.length; ++p)
		{
			if (p == d.devIdx())
				sumElos += devElo;
			else
				sumElos += populationElosTable.getQuick(d.agentIndices()[p]);
		}
		
		// Compute by how much to adjust all the Elo ratings
		final float[] elosToAdd = new float[agentPickCounts.length];
		
		for (int p = 1; p < agentPickCounts.length; ++p)
		{
			final double pUtility = utilities[p];
			final float pElo;
			
			if (p == d.devIdx())
				pElo = devElo;
			else
				pElo = populationElosTable.getQuick(d.agentIndices()[p]);
			
			final float avgOpponentsElo = (sumElos - pElo) / (agentPickCounts.length - 1);
			
			final double expectedWinProb = 1.0 / (1.0 + (Math.pow(10.0, (pElo - avgOpponentsElo) / 400.0)));
			final double expectedUtil = 2.0 * expectedWinProb - 1.0;
			elosToAdd[p] += 15 * (pUtility - expectedUtil);
		}
		
		// Do the actual Elo updates
		for (int p = 1; p < agentPickCounts.length; ++p)
		{
			if (p == d.devIdx())
				devElo += elosToAdd[p];
			else
				populationElosTable.setQuick(d.agentIndices()[p], populationElosTable.getQuick(d.agentIndices()[p]) + elosToAdd[p]);
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String generateLog()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("\nDev Elo: " + devElo + "\n");

		sb.append("Checkpoint Elos:\n");
		for (int i = 0; i < population.size(); ++i)
		{
			sb.append(population.get(i).checkpointName() + ": " + populationElosTable.getQuick(i) + "\n");
		}
		sb.append("\n");
		
		sb.append("Checkpoint Pick Counts:\n");
		for (int i = 0; i < population.size(); ++i)
		{
			sb.append(population.get(i).checkpointName() + ": ");
			for (int p = 1; p < agentPickCounts.length; ++p)
			{
				sb.append(agentPickCounts[p].getQuick(i));
				if (p + 1 < agentPickCounts.length)
					sb.append(", ");
			}
			sb.append("\n");
		}
		sb.append("\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Subclass of DrawnAgentsData; additionally remembers indexes of agents
	 * that were drawn, such that we can correctly update Elo ratings when
	 * trial is done.
	 *
	 * @author Dennis Soemers
	 */
	public static class TournamentDrawnAgentsData extends DrawnAgentsData
	{
		
		/** Player index for which we picked the dev checkpoint */
		private final int devIdx;

		/** For every player ID (except devIdx), the index of the checkpoint we used there */
		private final int[] agentIndices;

		/**
		 * Constructor
		 * @param agents
		 * @param devIdx
		 * @param agentIndices 
		 */
		public TournamentDrawnAgentsData(final List<ExpertPolicy> agents, final int devIdx, final int[] agentIndices)
		{
			super(agents);
			this.devIdx = devIdx;
			this.agentIndices = agentIndices;
		}
		
		/**
		 * @return For every player ID (except devIdx), the index of the checkpoint we used there
		 */
		public int[] agentIndices()
		{
			return agentIndices;
		}
		
		/**
		 * @return Player index for which we picked the dev checkpoint
		 */
		public int devIdx()
		{
			return devIdx;
		}
		
	}
	
	//-------------------------------------------------------------------------

}
