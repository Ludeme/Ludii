package supplementary.experiments.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.options.Ruleset;
import other.GameLoader;

/**
 * For every game-ruleset subdirectory in some directory, write a little
 * file telling us what category that game is listed under.
 * 
 * @author Dennis Soemers
 */
public class WriteGameRulesetCategories
{
	
	//-------------------------------------------------------------------------
	
	private static void writeGameRulesetCategories(final CommandLineArgParse argParse) throws FileNotFoundException, IOException
	{
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
		
		final Map<String, String> gameCategories = new HashMap<String, String>();
		
		final String gameCategoriesFileContents = FileHandling.loadTextContentsFromFile("../../LudiiPrivate/DataMiningScripts/Dennis/GameCategories.csv");
		final String[] gameCategoryLines = gameCategoriesFileContents.split(Pattern.quote("\n"));
		for (final String line : gameCategoryLines)
		{
			final String[] splitLine = line.replaceAll(Pattern.quote("\""), "").split(Pattern.quote(","));
			String gameName = StringRoutines.cleanGameName(splitLine[0]);
			final String category = splitLine[1];
			
			if	(gameCategories.containsKey(gameName))
				gameCategories.put(gameName, gameCategories.get(gameName) + "/" + category);
			else
				gameCategories.put(gameName, category);
		}
		
		for (final String gameName : allGameNames)
		{
			final String[] gameNameSplit = gameName.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String shortGameName = gameNameSplit[gameNameSplit.length - 1];
			
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName);
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
					game = GameLoader.loadGameFromName(gameName, fullRulesetName);
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
				
				if (game.hasSubgames())
					continue;
				
				if (game.isDeductionPuzzle())
					continue;
				
				if (game.isSimulationMoveGame())
					continue;
				
				if (!game.isAlternatingMoveGame())
					continue;
				
				if (game.isStacking())
					continue;
				
				if (game.isBoardless())
					continue;
				
				if (game.hiddenInformation())
					continue;
				
				String baseDir = argParse.getValueString("--base-dir");
				if (baseDir.endsWith("/"))
					baseDir = baseDir.substring(0, baseDir.length() - 1);
				final File gameRulesetDir = 
						new File
						(
							baseDir 
							+ 
							StringRoutines.cleanGameName(("/" + shortGameName).replaceAll(Pattern.quote(".lud"), "")) 
							+
							"_"
							+
							StringRoutines.cleanRulesetName(fullRulesetName).replaceAll(Pattern.quote("/"), "_")
							+
							"/"
						);
				
				if (!gameRulesetDir.exists() || !gameRulesetDir.isDirectory())
					continue;
				
				final String categoryFilepath = gameRulesetDir.getAbsolutePath() + "/Category.txt";
				System.out.println("Writing: " + categoryFilepath + "...");
				try (final PrintWriter writer = new PrintWriter(categoryFilepath, "UTF-8"))
				{
					writer.println(gameCategories.get(StringRoutines.cleanGameName((shortGameName).replaceAll(Pattern.quote(".lud"), ""))));
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
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
					"In every game-ruleset subdirectory of a larger directory, writes a file with the game's category."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--base-dir")
				.help("The base directory (with game-ruleset subdirectories).")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		writeGameRulesetCategories(argParse);
	}
	
	//-------------------------------------------------------------------------

}
