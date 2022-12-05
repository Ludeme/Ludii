package supplementary.experiments.game_files;

import java.io.File;
import java.util.regex.Pattern;

import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;

/**
 * Main method to update AI Metadata with identified top features.
 * 
 * @author Dennis Soemers
 */
public class UpdateAIMetadataTopFeatures
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Updates our AI metadata
	 * @param argParse
	 */
	private static void updateMetadata(final CommandLineArgParse argParse)
	{
		String topFeaturesOutDirPath = argParse.getValueString("--top-features-out-dir");
		topFeaturesOutDirPath = topFeaturesOutDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!topFeaturesOutDirPath.endsWith("/"))
			topFeaturesOutDirPath += "/";
		
		final File topFeaturesOutDir = new File(topFeaturesOutDirPath);
		
		// TODO
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to update all our metadata
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Updates all our AI metadata to include identified top features."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--top-features-out-dir")
				.help("Output directory with identified top features.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--ai-defs-dir")
				.help("Directory containing AI metadata .def files.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withDefault("../Common/res/def_ai"));
		
		argParse.addOption(new ArgOption()
				.withNames("--luds-dir")
				.help("Directory that contains the /lud/** directory.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withDefault("../Common/res"));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		updateMetadata(argParse);
	}
	
	//-------------------------------------------------------------------------

}
