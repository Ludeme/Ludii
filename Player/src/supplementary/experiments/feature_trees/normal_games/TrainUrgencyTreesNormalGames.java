package supplementary.experiments.feature_trees.normal_games;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import decision_trees.classifiers.DecisionTreeNode;
import decision_trees.classifiers.ExperienceUrgencyTreeLearner;
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
 * Train binary classification feature trees for our "normal" (not especially small) games.
 * 
 * @author Dennis Soemers
 */
public class TrainUrgencyTreesNormalGames
{

	//-------------------------------------------------------------------------
	
	/**
	 * Private constructor
	 */
	private TrainUrgencyTreesNormalGames()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	private static final String RESULTS_DIR = "D:/Apps/Ludii_Local_Experiments/TrainFeaturesSnellius4/";
	
	/** Games we ran */
	private static final String[] GAMES = 
			new String[]
			{
				"Alquerque.lud",
				"Amazons.lud",
				"ArdRi.lud",
				"Arimaa.lud",
				"Ataxx.lud",
				"Bao Ki Arabu (Zanzibar 1).lud",
				"Bizingo.lud",
				"Breakthrough.lud",
				"Chess.lud",
				//"Chinese Checkers.lud",
				"English Draughts.lud",
				"Fanorona.lud",
				"Fox and Geese.lud",
				"Go.lud",
				"Gomoku.lud",
				"Gonnect.lud",
				"Havannah.lud",
				"Hex.lud",
				"Knightthrough.lud",
				"Konane.lud",
				//"Level Chess.lud",
				"Lines of Action.lud",
				"Omega.lud",
				"Pentalath.lud",
				"Pretwa.lud",
				"Reversi.lud",
				"Royal Game of Ur.lud",
				"Surakarta.lud",
				"Shobu.lud",
				"Tablut.lud",
				//"Triad.lud",
				"XII Scripta.lud",
				"Yavalath.lud"
			};
	
	private static final String[] POLICY_WEIGHT_TYPES = new String[] {"Playout", "TSPG"};
	private static final boolean[] BOOSTED = new boolean[] {false, true};
	
	private static int[] TREE_DEPTHS = new int[] {1, 2, 3, 4, 5, 10};

	//-------------------------------------------------------------------------
	
	/**
	 * Do the work
	 */
	public void run()
	{
		for (int i = 0; i < GAMES.length; ++i)
		{
			final Game game = GameLoader.loadGameFromName(GAMES[i]);
			
			if (game == null)
				throw new IllegalArgumentException("Cannot load game: " + GAMES[i]);
			
			final String cleanGameName = StringRoutines.cleanGameName(GAMES[i].replaceAll(Pattern.quote(".lud"), ""));
			
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
								RESULTS_DIR + cleanGameName + "_Baseline/PolicyWeights" + 
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
									RESULTS_DIR + cleanGameName + "_Baseline/" + 
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
								ExperienceUrgencyTreeLearner.buildTree(featureSets[p], linearFunctions[p], buffer, depth, 10);
						
						// Convert to metadata structure
						final metadata.ai.features.trees.classifiers.DecisionTreeNode metadataRoot = root.toMetadataNode();
						metadataTrees[p - 1] = new metadata.ai.features.trees.classifiers.DecisionTree(RoleType.roleForPlayerId(p), metadataRoot);
					}
					
					final String outFile = RESULTS_DIR + "Trees/" + cleanGameName + 
							"/UrgencyTree_" + POLICY_WEIGHT_TYPES[j] + "_" + depth + ".txt";
					System.out.println("Writing Urgency tree to: " + outFile);
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
		final TrainUrgencyTreesNormalGames task = new TrainUrgencyTreesNormalGames();
		task.run();
	}
	
	//-------------------------------------------------------------------------
	
}
