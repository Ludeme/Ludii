package supplementary.experiments.feature_trees;

import java.io.File;
import java.util.List;

import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import utils.AIFactory;

/**
 * Class to convert a trained feature set + weights into an exact logit
 * tree (with exact meaning that it will be guaranteed to produce identical
 * outputs for identical inputs).
 * 
 * @author Dennis Soemers
 */
public class GenerateExactFeatureTree
{

	//-------------------------------------------------------------------------
	
	/**
	 * Private constructor
	 */
	private GenerateExactFeatureTree()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/** Filepaths for trained feature weights */
	protected List<String> featureWeightsFilepaths;
	
	/** File to write tree metadata to */
	protected File outFile;
	
	/** If true, we expect Playout policy weight files to be boosted */
	protected boolean boosted;
	
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
		final SoftmaxPolicy playoutSoftmax = (SoftmaxPolicy) mcts.playoutStrategy();
		
		final BaseFeatureSet[] featureSets = playoutSoftmax.featureSets();
		
		@SuppressWarnings("unused")		// TODO
		final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();
		
		for (int p = 1; p < featureSets.length; ++p)
		{
			// TODO generate logit tree for Player p
		}
		
		// TODO write
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
				.withNames("--out-file")
				.help("Filepath to write to.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--boosted")
				.help("Indicates that the policy weight files are expected to be boosted.")
				.withType(OptionTypes.Boolean));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;

		final GenerateExactFeatureTree task = new GenerateExactFeatureTree();
		
		task.featureWeightsFilepaths = (List<String>) argParse.getValue("--feature-weights-filepaths");
		task.outFile = new File(argParse.getValueString("--out-file"));
		task.boosted = argParse.getValueBool("--boosted");
		
		task.run();
	}
	
	//-------------------------------------------------------------------------
	
}
