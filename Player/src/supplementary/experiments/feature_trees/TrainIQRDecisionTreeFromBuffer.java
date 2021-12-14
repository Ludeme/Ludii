package supplementary.experiments.feature_trees;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import decision_trees.classifiers.DecisionTreeNode;
import decision_trees.classifiers.ExperienceIQRTreeLearner;
import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import game.types.play.RoleType;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import metadata.ai.features.trees.FeatureTrees;
import other.GameLoader;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import utils.AIFactory;
import utils.data_structures.experience_buffers.ExperienceBuffer;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;
import utils.data_structures.experience_buffers.UniformExperienceBuffer;

/**
 * Class to train a decision tree from existing self-play data in an experience
 * buffer. The goal is to predict whether actions are in the Interquartile Range 
 * (IQR) of logits / probabilities, below it, or above it.
 * 
 * 
 * @author Dennis Soemers
 */
public class TrainIQRDecisionTreeFromBuffer
{

	//-------------------------------------------------------------------------
	
	/**
	 * Private constructor
	 */
	private TrainIQRDecisionTreeFromBuffer()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/** Filepaths for trained feature weights */
	protected List<String> featureWeightsFilepaths;
	
	/** Filepaths for buffers with self-play experience */
	protected List<String> experienceBufferFilepaths;
	
	/** File to write tree metadata to */
	protected File outFile;
	
	/** If true, we expect Playout policy weight files to be boosted */
	protected boolean boosted;
	
	/** Name of game for which we're building tree */
	protected String gameName;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Do the work
	 */
	public void run()
	{
		// We'll first just use the command line args we got to build a Biased MCTS
		// Then we'll extract the features from that one
		final StringBuilder playoutSb = new StringBuilder();
		playoutSb.append("playout=softmax");

		for (int p = 1; p <= featureWeightsFilepaths.size(); ++p)
		{
			playoutSb.append(",policyweights" + p + "=" + featureWeightsFilepaths.get(p - 1));
		}
		
		if (boosted)
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
		
		final Game game = GameLoader.loadGameFromName(gameName);
		playoutSoftmax.initAI(game, -1);
		
		final metadata.ai.features.trees.classifiers.DecisionTree[] metadataTrees = 
				new metadata.ai.features.trees.classifiers.DecisionTree[featureSets.length - 1];
		
		for (int p = 1; p < featureSets.length; ++p)
		{
			// Load experience buffer for Player p
			ExperienceBuffer buffer = null;
			try
			{
				buffer = PrioritizedReplayBuffer.fromFile(game, experienceBufferFilepaths.get(p - 1));
			}
			catch (final Exception e)
			{
				if (buffer == null)
				{
					try
					{
						buffer = UniformExperienceBuffer.fromFile(game, experienceBufferFilepaths.get(p - 1));
					}
					catch (final Exception e2)
					{
						e.printStackTrace();
						e2.printStackTrace();
					}
				}
			}
			
			// Generate decision tree for Player p
			final DecisionTreeNode root = ExperienceIQRTreeLearner.buildTree(featureSets[p], linearFunctions[p], buffer, 10, 5);
			
			// Convert to metadata structure
			final metadata.ai.features.trees.classifiers.DecisionTreeNode metadataRoot = root.toMetadataNode();
			metadataTrees[p - 1] = new metadata.ai.features.trees.classifiers.DecisionTree(RoleType.roleForPlayerId(p), metadataRoot);
		}
		
		try (final PrintWriter writer = new PrintWriter(outFile))
		{
			writer.println(new FeatureTrees(null, metadataTrees));
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{
		// Define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Write features to a file."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--feature-weights-filepaths")
				.help("Filepaths for trained feature weights.")
				.withNumVals("+")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--experience-buffer-filepaths")
				.help("Filepaths for experience buffers.")
				.withNumVals("+")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--out-file")
				.help("Filepath to write to.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--boosted")
				.help("Indicates that the policy weight files are expected to be boosted.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of game.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;

		final TrainIQRDecisionTreeFromBuffer task = new TrainIQRDecisionTreeFromBuffer();
		
		task.featureWeightsFilepaths = (List<String>) argParse.getValue("--feature-weights-filepaths");
		task.experienceBufferFilepaths = (List<String>) argParse.getValue("--experience-buffer-filepaths");
		task.outFile = new File(argParse.getValueString("--out-file"));
		task.boosted = argParse.getValueBool("--boosted");
		task.gameName = argParse.getValueString("--game");
		
		task.run();
	}
	
	//-------------------------------------------------------------------------
	
}
