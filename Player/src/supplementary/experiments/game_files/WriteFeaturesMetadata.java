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
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import utils.AIFactory;

/**
 * Class to write a set of features and weights to a file
 *
 * @author Dennis Soemers
 */
public class WriteFeaturesMetadata
{
	/** Filepaths for feature weights to write */
	protected List<String> featureWeightsFilepaths;
	
	/** File to write features metadata to */
	protected File outFile;
	
	/** If true, we expect policy weight files to be boosted */
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

		final String agentStr = StringRoutines.join
				(
						";", 
						"algorithm=MCTS",
						"selection=ag0selection",
						playoutSb.toString(),
						"final_move=robustchild",
						"tree_reuse=true",
						"learned_selection_policy=playout",
						"friendly_name=BiasedMCTS"
						);

		final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
		final SoftmaxPolicy softmax = (SoftmaxPolicy) mcts.playoutStrategy();

		// Generate our features metadata and write it
		final Features features = softmax.generateFeaturesMetadata();

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
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Write features to a file."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--feature-weights-filepaths")
				.help("Filepaths for feature weights.")
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
		
		task.featureWeightsFilepaths = (List<String>) argParse.getValue("--feature-weights-filepaths");
		task.outFile = new File(argParse.getValueString("--out-file"));
		task.boosted = argParse.getValueBool("--boosted");
		
		task.run();
	}
	
	//-------------------------------------------------------------------------

}
