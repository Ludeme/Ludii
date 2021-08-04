package supplementary.experiments.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import game.rules.phase.Phase;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;
import other.playout.Playout;
import other.playout.PlayoutAddToEmpty;
import other.playout.PlayoutFilter;
import other.playout.PlayoutNoRepetition;

/**
 * Generates a CSV with results from timing custom playouts vs. non-custom playouts
 *
 * @author Dennis Soemers
 */
public class CustomPlayoutsResultsCSV
{

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private CustomPlayoutsResultsCSV()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @param type
	 * @return True if the given game uses the given type of custom playout
	 */
	private static boolean gameUsesPlayoutType(final Game game, final Class<? extends Playout> type)
	{
		if (game.mode().playout() != null && game.mode().playout().getClass().isAssignableFrom(type))
			return true;

		for (final Phase phase : game.rules().phases())
			if (phase.playout() != null && phase.playout().getClass().isAssignableFrom(type))
				return true;

		return false;
	}
	
	/**
	 * Generates our CSV file
	 * @param argParse
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static void generateCSV(final CommandLineArgParse argParse) throws FileNotFoundException, IOException
	{
		String customResultsDir = argParse.getValueString("--custom-results-dir");
		customResultsDir = customResultsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!customResultsDir.endsWith("/"))
			customResultsDir += "/";
		
		String noCustomResultsDir = argParse.getValueString("--no-custom-results-dir");
		noCustomResultsDir = noCustomResultsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!noCustomResultsDir.endsWith("/"))
			noCustomResultsDir += "/";
		
		final String outFile = argParse.getValueString("--out-file");
		final List<String> rows = new ArrayList<String>();	// Rows to write in new CSV
		
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter(s -> (
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
			)).toArray(String[]::new);
		
		for (final String fullGamePath : allGameNames)
		{
			final String[] gamePathParts = fullGamePath.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName + ".lud");
			final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
			gameRulesets.add(null);
			boolean foundRealRuleset = false;
			
			for (final Ruleset ruleset : gameRulesets)
			{
				final Game game;
				String fullRulesetName = "";
				if (ruleset == null && foundRealRuleset)
				{
					// Skip this, don't allow game without ruleset if we do have real implemented ones
					continue;
				}
				else if (ruleset != null && !ruleset.optionSettings().isEmpty())
				{
					fullRulesetName = ruleset.heading();
					foundRealRuleset = true;
					game = GameLoader.loadGameFromName(gameName + ".lud", fullRulesetName);
				}
				else if (ruleset != null && ruleset.optionSettings().isEmpty())
				{
					// Skip empty ruleset
					continue;
				}
				else
				{
					game = gameNoRuleset;
				}
				
				if (game.isDeductionPuzzle())
					continue;
				
				if (game.isSimulationMoveGame())
					continue;
				
				if (!game.isAlternatingMoveGame())
					continue;
				
				if (game.hasSubgames())
					continue;
				
				if (game.isStacking())
					continue;
				
				if (game.hiddenInformation())
					continue;
				
				if (!game.hasCustomPlayouts())
					continue;
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final File customResultFile = new File(customResultsDir + filepathsGameName + filepathsRulesetName + ".csv");
				final File noCustomResultFile = new File(noCustomResultsDir + filepathsGameName + filepathsRulesetName + ".csv");
				
				if (customResultFile.exists() && noCustomResultFile.exists())
				{
					final String customContents = FileHandling.loadTextContentsFromFile(customResultFile.getAbsolutePath());
					final String noCustomContents = FileHandling.loadTextContentsFromFile(noCustomResultFile.getAbsolutePath());
					
					final String[] customLines = customContents.split(Pattern.quote("\n"));
					final String[] noCustomLines = noCustomContents.split(Pattern.quote("\n"));
					
					// Second line has results, first line is just column headers
					final String[] customRowSplit = customLines[1].split(Pattern.quote(","));
					final String[] noCustomRowSplit = noCustomLines[1].split(Pattern.quote(","));
					
					// Second result of row is playouts per second, we'll take that
					final double customResult = Double.parseDouble(customRowSplit[1]);
					final double noCustomResult = Double.parseDouble(noCustomRowSplit[1]);
					
					final double ratio = customResult / noCustomResult;
					
					if (gameUsesPlayoutType(game, PlayoutAddToEmpty.class))
						rows.add(StringRoutines.join(",", "Add-To-Empty", String.valueOf(ratio), String.valueOf(noCustomResult), String.valueOf(customResult), filepathsGameName + filepathsRulesetName));
					if (gameUsesPlayoutType(game, PlayoutFilter.class))
						rows.add(StringRoutines.join(",", "Filter", String.valueOf(ratio), String.valueOf(noCustomResult), String.valueOf(customResult), filepathsGameName + filepathsRulesetName));
					if (gameUsesPlayoutType(game, PlayoutNoRepetition.class))
						rows.add(StringRoutines.join(",", "No-Repetition", String.valueOf(ratio), String.valueOf(noCustomResult), String.valueOf(customResult), filepathsGameName + filepathsRulesetName));
				}
				else if (customResultFile.exists() || noCustomResultFile.exists())
				{
					System.err.println("One exists but the other doesn't!");
				}
			}
		}
		
		try (final PrintWriter writer = new UnixPrintWriter(new File(outFile), "UTF-8"))
		{
			// Write header
			writer.println(StringRoutines.join(",", "Playout", "Speedup", "BaselinePlayoutsPerSec", "CustomPlayoutsPerSec", "GameRuleset"));
			
			// Write rows
			for (final String row : rows)
			{
				writer.println(row);
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to generate all our scripts
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(final String[] args) throws FileNotFoundException, IOException
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Generates single CSV with results."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--custom-results-dir")
				.help("Directory with results for custom playouts.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--no-custom-results-dir")
				.help("Directory with results for no-custom playouts.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--out-file")
				.help("Filepath for CSV file to write")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateCSV(argParse);
	}
	
	//-------------------------------------------------------------------------
}
