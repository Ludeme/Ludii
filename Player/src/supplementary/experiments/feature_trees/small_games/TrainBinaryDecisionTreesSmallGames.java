package supplementary.experiments.feature_trees.small_games;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import decision_trees.classifiers.DecisionTreeNode;
import decision_trees.classifiers.ExperienceBinaryClassificationTreeLearner;
import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import game.types.play.RoleType;
import main.StringRoutines;
import metadata.ai.features.trees.FeatureTrees;
import other.GameLoader;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import utils.AIFactory;
import utils.ExperimentFileUtils;
import utils.data_structures.experience_buffers.ExperienceBuffer;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;
import utils.data_structures.experience_buffers.UniformExperienceBuffer;

/**
 * Train binary classification feature trees for our small games.
 * 
 * @author Dennis Soemers
 */
public class TrainBinaryDecisionTreesSmallGames
{

	//-------------------------------------------------------------------------
	
	/**
	 * Private constructor
	 */
	private TrainBinaryDecisionTreesSmallGames()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	private static final String RESULTS_DIR = "D:/Downloads/results.tar/results/Out/";
	
	/** Games we ran */
	private static final String[] GAMES = 
			new String[]
			{
				"Tic-Tac-Toe.lud",
				"Mu Torere.lud",
				"Mu Torere.lud",
				"Jeu Militaire.lud",
				"Pong Hau K'i.lud",
				"Akidada.lud",
				"Alquerque de Tres.lud",
				"Ho-Bag Gonu.lud",
				"Madelinette.lud",
				"Haretavl.lud",
				"Kaooa.lud",
				"Hat Diviyan Keliya.lud",
				"Three Men's Morris.lud"
			};
	
	/** Rulesets we ran */
	private static final String[] RULESETS = 
			new String[]
			{
				"",
				"Ruleset/Complete (Observed)",
				"Ruleset/Simple (Suggested)",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				""
			};
	
	private static final String[] POLICY_WEIGHT_TYPES = new String[] {"Playout", "TSPG"};
	private static final boolean[] BOOSTED = new boolean[] {false, true};
	
	private static int[] TREE_DEPTHS = new int[] {1, 2, 3, 4, 5, 10};

	//-------------------------------------------------------------------------
	
	/**
	 * Do the work
	 */
	@SuppressWarnings("static-method")
	public void run()
	{
		for (int i = 0; i < GAMES.length; ++i)
		{
			final Game game = GameLoader.loadGameFromName(GAMES[i], RULESETS[i]);
			
			if (game == null)
				throw new IllegalArgumentException("Cannot load game: " + GAMES[i] + " " + RULESETS[i]);
			
			final String cleanGameName = StringRoutines.cleanGameName(GAMES[i].replaceAll(Pattern.quote(".lud"), ""));
			final String cleanRulesetName = StringRoutines.cleanRulesetName(RULESETS[i]).replaceAll(Pattern.quote("/"), "_");
			
			for (int j = 0; j < POLICY_WEIGHT_TYPES.length; ++j)
			{
				// Construct a string to load an MCTS guided by features, from that we can then easily extract the
				// features again afterwards
				final StringBuilder playoutSb = new StringBuilder();
				playoutSb.append("playout=softmax");
		
				for (int p = 1; p <= game.players().count(); ++p)
				{
					final String policyFilepath = 
							ExperimentFileUtils.getLastFilepath
							(
								RESULTS_DIR + cleanGameName + "_" + cleanRulesetName + "/PolicyWeights" + 
								POLICY_WEIGHT_TYPES[j] + "_P" + p, 
								"txt"
							);
					
					playoutSb.append(",policyweights" + p + "=" + policyFilepath);
				}
				
				if (BOOSTED[j])
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
				
				final BaseFeatureSet[] featureSets = playoutSoftmax.featureSets();
				final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();
				
				playoutSoftmax.initAI(game, -1);
				
				for (final int depth : TREE_DEPTHS)
				{
					final metadata.ai.features.trees.classifiers.DecisionTree[] metadataTrees = 
							new metadata.ai.features.trees.classifiers.DecisionTree[featureSets.length - 1];
					
					for (int p = 1; p < featureSets.length; ++p)
					{
						// Load experience buffer for Player p
						final String bufferFilepath = 
								ExperimentFileUtils.getLastFilepath
								(
									RESULTS_DIR + cleanGameName + "_" + cleanRulesetName + "/" + 
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
						
						// Generate decision tree for Player p
						final DecisionTreeNode root = 
								ExperienceBinaryClassificationTreeLearner.buildTree(featureSets[p], linearFunctions[p], buffer, depth, 5);
						
						// Convert to metadata structure
						final metadata.ai.features.trees.classifiers.DecisionTreeNode metadataRoot = root.toMetadataNode();
						metadataTrees[p - 1] = new metadata.ai.features.trees.classifiers.DecisionTree(RoleType.roleForPlayerId(p), metadataRoot);
					}
					
					final String outFile = RESULTS_DIR + "Trees/" + cleanGameName + "_" + cleanRulesetName + 
							"/BinaryClassificationTree_" + POLICY_WEIGHT_TYPES[j] + "_" + depth + ".txt";
					System.out.println("Writing Binary Classification tree to: " + outFile);
					new File(outFile).getParentFile().mkdirs();
					
					try (final PrintWriter writer = new PrintWriter(outFile))
					{
						writer.println(new FeatureTrees(null, metadataTrees));
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final TrainBinaryDecisionTreesSmallGames task = new TrainBinaryDecisionTreesSmallGames();
		task.run();
	}
	
	//-------------------------------------------------------------------------
	
}
