package training.expert_iteration.menageries;

import java.io.IOException;

import game.Game;
import main.FileHandling;
import main.grammar.Report;
import metadata.ai.agents.BestAgent;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import search.mcts.backpropagation.AlphaGoBackprop;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.NoisyAG0Selection;
import search.minimax.AlphaBetaSearch;
import training.expert_iteration.ExpertPolicy;
import training.expert_iteration.params.AgentsParams;
import utils.AIFactory;

/**
 * A checkpoint containing all the data required to reproduce a version of an
 * agent at some point in a training process.
 *
 * @author Dennis Soemers
 */
public class AgentCheckpoint
{
	
	//-------------------------------------------------------------------------
	
	/** Type of agent */
	protected final String agentName;
	
	/** Descriptor of this agent in population */
	protected final String checkpointName;
	
	/** Features metadata (can be null if this checkpoint doesn't use features) */
	protected final Features featuresMetadata;
	
	/** Heuristics metadata (can be null if this checkpoint doesn't use heuristics) */
	protected final Heuristics heuristicsMetadata;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param agentName
	 * @param checkpointName
	 * @param featuresMetadata
	 * @param heuristicsMetadata
	 */
	public AgentCheckpoint
	(
		final String agentName,
		final String checkpointName,
		final Features featuresMetadata,
		final Heuristics heuristicsMetadata
	)
	{
		this.agentName = agentName;
		this.checkpointName = checkpointName;
		this.featuresMetadata = featuresMetadata;
		this.heuristicsMetadata = heuristicsMetadata;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @param agentsParams 
	 * @return An agent generated based on this checkpoint
	 */
	public ExpertPolicy generateAgent(final Game game, final AgentsParams agentsParams)
	{
		final ExpertPolicy ai;
		
		if (agentName.equals("BEST_AGENT"))
		{
			try
			{
				final BestAgent bestAgent = (BestAgent)compiler.Compiler.compileObject
						(
							FileHandling.loadTextContentsFromFile(agentsParams.bestAgentsDataDir + "/BestAgent.txt"), 
							"metadata.ai.agents.BestAgent",
							new Report()
						);

				if (bestAgent.agent().equals("AlphaBeta") || bestAgent.agent().equals("Alpha-Beta"))
				{
					ai = new AlphaBetaSearch(agentsParams.bestAgentsDataDir + "/BestHeuristics.txt");
				}
				else if (bestAgent.agent().equals("AlphaBetaMetadata"))
				{
					ai = new AlphaBetaSearch();
				}
				else if (bestAgent.agent().equals("UCT"))
				{
					ai = (ExpertPolicy) AIFactory.createAI("UCT");
				}
				else if (bestAgent.agent().equals("MC-GRAVE"))
				{
					ai = (ExpertPolicy) AIFactory.createAI("MC-GRAVE");
				}
				else if (bestAgent.agent().equals("MC-BRAVE"))
				{
					ai = (ExpertPolicy) AIFactory.createAI("MC-BRAVE");
				}
				else if (bestAgent.agent().equals("Biased MCTS"))
				{
					final Features features = (Features)compiler.Compiler.compileObject
							(
								FileHandling.loadTextContentsFromFile(agentsParams.bestAgentsDataDir + "/BestFeatures.txt"), 
								"metadata.ai.features.Features",
								new Report()
							);

					// TODO compare features string to features string in training process, use that if same?
					ai = MCTS.createBiasedMCTS(features, agentsParams.playoutFeaturesEpsilon);
				}
				else if (bestAgent.agent().equals("Biased MCTS (Uniform Playouts)"))
				{
					final Features features = (Features)compiler.Compiler.compileObject
							(
								FileHandling.loadTextContentsFromFile(agentsParams.bestAgentsDataDir + "/BestFeatures.txt"), 
								"metadata.ai.features.Features",
								new Report()
							);

					ai = MCTS.createBiasedMCTS(features, 1.0);
				}
				else if (bestAgent.agent().equals("Random"))
				{
					// Don't want to train with Random, so we'll take UCT instead
					ai = MCTS.createUCT();
				}
				else
				{
					System.err.println("Unrecognised best agent: " + bestAgent.agent());
					return null;
				}
			}
			catch (final IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else if (agentName.equals("FROM_METADATA"))
		{
			ai = (ExpertPolicy) AIFactory.fromMetadata(game);

			if (ai == null)
			{
				System.err.println("AI from metadata is null!");
				return null;
			}
		}
		else if (agentName.equals("Biased MCTS"))
		{
			ai = MCTS.createBiasedMCTS(featuresMetadata, agentsParams.playoutFeaturesEpsilon);
			ai.setFriendlyName("Biased MCTS");
		}
		else if (agentName.equals("PVTS"))
		{
			final MCTS mcts = 
					new MCTS
					(
						new NoisyAG0Selection(), 
						new RandomPlayout(0),
						new AlphaGoBackprop(),
						new RobustChild()
					);

			mcts.setLearnedSelectionPolicy(SoftmaxPolicyLinear.constructSelectionPolicy(featuresMetadata, 0.0));
			mcts.setPlayoutValueWeight(0.0);
			mcts.setWantsMetadataHeuristics(false);
			mcts.setHeuristics(heuristicsMetadata);
			mcts.setFriendlyName("PVTS");
			ai = mcts;
		}
		else if (agentName.equals("UCT"))
		{
			ai = MCTS.createUCT();
		}
		else if (agentName.equals("MC-GRAVE"))
		{
			ai = (ExpertPolicy) AIFactory.createAI("MC-GRAVE");
		}
		else if (agentName.equals("MC-BRAVE"))
		{
			ai = (ExpertPolicy) AIFactory.createAI("MC-BRAVE");
		}
		else
		{
			System.err.println("Cannot recognise expert AI: " + agentsParams.expertAI);
			return null;
		}

		if (ai instanceof MCTS)
		{
			// Need to preserve root node such that we can extract distributions from it
			((MCTS) ai).setPreserveRootNode(true);
		}
		
		return ai;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Descriptor of this agent in the population
	 */
	public String checkpointName()
	{
		return checkpointName;
	}
	
	//-------------------------------------------------------------------------

}
