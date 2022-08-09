package supplementary.experiments.feature_importance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import decision_trees.classifiers.DecisionConditionNode;
import decision_trees.classifiers.DecisionTreeNode;
import decision_trees.classifiers.ExperienceUrgencyTreeLearner;
import features.Feature;
import features.aspatial.AspatialFeature;
import features.feature_sets.BaseFeatureSet;
import features.spatial.SpatialFeature;
import function_approx.LinearFunction;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import other.GameLoader;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import utils.AIFactory;
import utils.ExperimentFileUtils;
import utils.data_structures.experience_buffers.ExperienceBuffer;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;
import utils.data_structures.experience_buffers.UniformExperienceBuffer;

/**
 * Script to identify small collection of top features
 * 
 * @author Dennis Soemers
 */
public class IdentifyTopFeatures
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runs the process
	 * @param parsedArgs
	 */
	private static void identifyTopFeatures(final CommandLineArgParse parsedArgs)
	{
		final String gameName = parsedArgs.getValueString("--game");
		final Game game = GameLoader.loadGameFromName(gameName);
			
		if (game == null)
			throw new IllegalArgumentException("Cannot load game: " + gameName);
		
		final int numPlayers = game.players().count();
		
		String trainingOutDirPath = parsedArgs.getValueString("--training-out-dir");
		if (!trainingOutDirPath.endsWith("/"))
			trainingOutDirPath += "/";
		
		// Load feature sets and policies
		final BaseFeatureSet[] featureSets = new BaseFeatureSet[numPlayers + 1];
		final LinearFunction[] linearFunctionsPlayout = new LinearFunction[numPlayers + 1];
		final LinearFunction[] linearFunctionsTSPG = new LinearFunction[numPlayers + 1];
		
		loadFeaturesAndWeights(game, trainingOutDirPath, featureSets, linearFunctionsPlayout, linearFunctionsTSPG);
		
		for (int p = 1; p < featureSets.length; ++p)
		{
			// Add simplified versions of existing spatial features
			final BaseFeatureSet featureSet = featureSets[p];
			final SpatialFeature[] origSpatialFeatures = featureSet.spatialFeatures();
			final List<SpatialFeature> featuresToAdd = new ArrayList<SpatialFeature>();
			
			for (final SpatialFeature feature : origSpatialFeatures)
			{
				featuresToAdd.addAll(feature.generateGeneralisers(game));
			}
			
			featureSets[p] = featureSet.createExpandedFeatureSet(game, featuresToAdd);
			featureSets[p].init(game, new int[] {p}, null);
		}
		
		// Load experience buffers
		final ExperienceBuffer[] experienceBuffers = new ExperienceBuffer[numPlayers + 1];
		loadExperienceBuffers(game, trainingOutDirPath, experienceBuffers);
		
		// Build trees for all players
		final DecisionTreeNode[] playoutTreesPerPlayer = new DecisionTreeNode[numPlayers + 1];
		final DecisionTreeNode[] tspgTreesPerPlayer = new DecisionTreeNode[numPlayers + 1];
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			playoutTreesPerPlayer[p] = 
					ExperienceUrgencyTreeLearner.buildTree(featureSets[p], linearFunctionsPlayout[p], experienceBuffers[p], 4, 10);
			tspgTreesPerPlayer[p] = 
					ExperienceUrgencyTreeLearner.buildTree(featureSets[p], linearFunctionsTSPG[p], experienceBuffers[p], 4, 10);
		}
		
		// For every player, extract candidate features from the decision trees for that player
		final List<List<Feature>> candidateFeaturesPerPlayer = new ArrayList<List<Feature>>();
		for (int p = 1; p <= numPlayers; ++p)
		{
			// Collect all the features in our trees
			final List<Feature> featuresList = new ArrayList<Feature>();
			candidateFeaturesPerPlayer.add(featuresList);
			
			collectFeatures(playoutTreesPerPlayer[p], featuresList);
			collectFeatures(tspgTreesPerPlayer[p], featuresList);
			
			// Get rid of any sorts of duplicates/redundancies
			List<SpatialFeature> spatialFeatures = new ArrayList<SpatialFeature>(featuresList.size());
			final List<AspatialFeature> aspatialFeatures = new ArrayList<AspatialFeature>();
			
			for (final Feature feature : featuresList)
			{
				if (feature instanceof AspatialFeature)
					aspatialFeatures.add((AspatialFeature) feature);
				else
					spatialFeatures.add((SpatialFeature) feature);
			}
			
			spatialFeatures = SpatialFeature.deduplicate(spatialFeatures);
			spatialFeatures = SpatialFeature.simplifySpatialFeaturesList(game, spatialFeatures);
			
			featuresList.clear();
			featuresList.addAll(aspatialFeatures);
			featuresList.addAll(spatialFeatures);
		}
		
		// Clear some memory
		Arrays.fill(experienceBuffers, null);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Collects all features under a given decision tree node
	 * @param node Root of (sub)tree from which to collect features
	 * @param outList List in which to place features
	 */
	private static void collectFeatures(final DecisionTreeNode node, final List<Feature> outList)
	{
		if (node instanceof DecisionConditionNode)
		{
			final DecisionConditionNode conditionNode = (DecisionConditionNode) node;
			outList.add(conditionNode.feature());
			
			collectFeatures(conditionNode.trueNode(), outList);
			collectFeatures(conditionNode.falseNode(), outList);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Loads feature sets and weights into the provided arrays
	 * @param game
	 * @param trainingOutDirPath
	 * @param outFeatureSets
	 * @param outLinearFunctionsPlayout
	 * @param outLinearFunctionsTSPG
	 */
	private static void loadFeaturesAndWeights
	(
		final Game game,
		final String trainingOutDirPath,
		final BaseFeatureSet[] outFeatureSets,
		final LinearFunction[] outLinearFunctionsPlayout,
		final LinearFunction[] outLinearFunctionsTSPG
	)
	{
		// First Playout policy
		{
			// Construct a string to load an MCTS guided by features, from that we can then easily extract the
			// features again afterwards
			final StringBuilder playoutSb = new StringBuilder();
			playoutSb.append("playout=softmax");
	
			for (int p = 1; p <= game.players().count(); ++p)
			{
				final String playoutPolicyFilepath = 
						ExperimentFileUtils.getLastFilepath
						(
							trainingOutDirPath + "PolicyWeightsPlayout_P" + p, 
							"txt"
						);
	
				playoutSb.append(",policyweights" + p + "=" + playoutPolicyFilepath);
			}
	
			final StringBuilder selectionSb = new StringBuilder();
			selectionSb.append("learned_selection_policy=playout");
	
			final String agentStr = StringRoutines.join
					(
						";", 
						"algorithm=MCTS",
						"selection=noisyag0selection",
						playoutSb.toString(),
						"final_move=robustchild",
						"tree_reuse=true",
						selectionSb.toString(),
						"friendly_name=BiasedMCTS"
					);
	
			final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
			final SoftmaxPolicyLinear playoutSoftmax = (SoftmaxPolicyLinear) mcts.playoutStrategy();
	
			final BaseFeatureSet[] featureSets = playoutSoftmax.featureSets();
			final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();
	
			playoutSoftmax.initAI(game, -1);
			
			System.arraycopy(featureSets, 0, outFeatureSets, 0, featureSets.length);
			System.arraycopy(linearFunctions, 0, outLinearFunctionsPlayout, 0, linearFunctions.length);
		}
		
		// Repeat the entire process again for TSPG
		{
			// Construct a string to load an MCTS guided by features, from that we can then easily extract the
			// features again afterwards
			final StringBuilder playoutSb = new StringBuilder();
			playoutSb.append("playout=softmax");
	
			for (int p = 1; p <= game.players().count(); ++p)
			{
				final String playoutPolicyFilepath = 
						ExperimentFileUtils.getLastFilepath
						(
							trainingOutDirPath + "PolicyWeightsTSPG_P" + p, 
							"txt"
						);
	
				playoutSb.append(",policyweights" + p + "=" + playoutPolicyFilepath);
			}
	
			playoutSb.append(",boosted=true");
	
			final StringBuilder selectionSb = new StringBuilder();
			selectionSb.append("learned_selection_policy=playout");
	
			final String agentStr = StringRoutines.join
					(
							";", 
							"algorithm=MCTS",
							"selection=noisyag0selection",
							playoutSb.toString(),
							"final_move=robustchild",
							"tree_reuse=true",
							selectionSb.toString(),
							"friendly_name=BiasedMCTS"
							);
	
			final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
			final SoftmaxPolicyLinear playoutSoftmax = (SoftmaxPolicyLinear) mcts.playoutStrategy();
	
			final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();
	
			playoutSoftmax.initAI(game, -1);
			
			System.arraycopy(linearFunctions, 0, outLinearFunctionsTSPG, 0, linearFunctions.length);
		}
	}
	
	/**
	 * Loads experience buffers into the provided array
	 * @param game
	 * @param trainingOutDirPath
	 * @param experienceBuffers
	 */
	private static void loadExperienceBuffers
	(
		final Game game,
		final String trainingOutDirPath,
		final ExperienceBuffer[] experienceBuffers
	)
	{
		for (int p = 1; p < experienceBuffers.length; ++p)
		{
			// Load experience buffer for Player p
			final String bufferFilepath = 
					ExperimentFileUtils.getLastFilepath
					(
						trainingOutDirPath + 
						"ExperienceBuffer_P" + p, 
						"buf"
					);

			ExperienceBuffer buffer = null;
			try
			{
				buffer = PrioritizedReplayBuffer.fromFile(game, bufferFilepath);
			}
			catch (final Exception e)
			{
				if (buffer == null)
				{
					try
					{
						buffer = UniformExperienceBuffer.fromFile(game, bufferFilepath);
					}
					catch (final Exception e2)
					{
						e.printStackTrace();
						e2.printStackTrace();
					}
				}
			}
			
			experienceBuffers[p] = buffer;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to generate all our scripts
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Identift top features for a game."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--training-out-dir")
				.help("Directory with training results (features, weights, experience buffers, ...).")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--out-dir")
				.help("Directory in which to write our outputs.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		identifyTopFeatures(argParse);
	}
	
	//-------------------------------------------------------------------------

}
