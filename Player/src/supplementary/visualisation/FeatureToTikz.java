package supplementary.visualisation;

import features.Feature;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;

public class FeatureToTikz
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Private constructor
	 */
	private FeatureToTikz()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/** Feature to write tikz code for*/
	protected String feature;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Do the work
	 */
	public void run()
	{
		final Feature f = Feature.fromString(feature);
		System.out.println("% Code generated for feature: " + StringRoutines.quote(feature));
		System.out.println(f.generateTikzCode(null));
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// Define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Print tikz code for a given feature."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--feature")
				.help("Feature to write tikz code for.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;

		final FeatureToTikz task = new FeatureToTikz();
		
		task.feature = argParse.getValueString("--feature");
		
		task.run();
	}
	
	//-------------------------------------------------------------------------

}
