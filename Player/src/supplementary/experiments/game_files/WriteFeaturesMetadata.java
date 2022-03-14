package supplementary.experiments.game_files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import metadata.ai.features.Features;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import utils.AIFactory;
import utils.AIUtils;

/**
 * Class to write a set of features and weights to a file
 *
 * @author Dennis Soemers
 */
public class WriteFeaturesMetadata
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Private constructor
	 */
	private WriteFeaturesMetadata()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/** Filepaths for Selection feature weights to write */
	protected List<String> featureWeightsFilepathsSelection;
	
	/** Filepaths for Playout feature weights to write */
	protected List<String> featureWeightsFilepathsPlayout;
	
	/** File to write features metadata to */
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

		for (int p = 1; p <= featureWeightsFilepathsPlayout.size(); ++p)
		{
			playoutSb.append(",policyweights" + p + "=" + featureWeightsFilepathsPlayout.get(p - 1));
		}
		
		if (boosted)
			playoutSb.append(",boosted=true");
		
		final StringBuilder selectionSb = new StringBuilder();
		selectionSb.append("learned_selection_policy=softmax");

		for (int p = 1; p <= featureWeightsFilepathsSelection.size(); ++p)
		{
			selectionSb.append(",policyweights" + p + "=" + featureWeightsFilepathsSelection.get(p - 1));
		}

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
		final SoftmaxPolicyLinear selectionSoftmax = (SoftmaxPolicyLinear) mcts.learnedSelectionPolicy();
		final SoftmaxPolicyLinear playoutSoftmax = (SoftmaxPolicyLinear) mcts.playoutStrategy();

		// Generate our features metadata and write it
		final Features features = AIUtils.generateFeaturesMetadata(selectionSoftmax, playoutSoftmax);

		try (final PrintWriter writer = new PrintWriter(outFile))
		{
			writer.println(features.toString());
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
				.withNames("--selection-feature-weights-filepaths")
				.help("Filepaths for feature weights for Selection.")
				.withNumVals("+")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--playout-feature-weights-filepaths")
				.help("Filepaths for feature weights for Selection.")
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

		final WriteFeaturesMetadata task = new WriteFeaturesMetadata();
		
		task.featureWeightsFilepathsSelection = (List<String>) argParse.getValue("--selection-feature-weights-filepaths");
		task.featureWeightsFilepathsPlayout = (List<String>) argParse.getValue("--playout-feature-weights-filepaths");
		task.outFile = new File(argParse.getValueString("--out-file"));
		task.boosted = argParse.getValueBool("--boosted");
		
		task.run();
	}
	
	//-------------------------------------------------------------------------

}
