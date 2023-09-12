package supplementary.experiments.rulesets;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.options.Ruleset;
import other.GameLoader;
import utils.IdRuleset;

/**
 * Generates a CSV file that lets us easily go from game+ruleset name strings (in format
 * used for output directories of various experiments) to ruleset IDs.
 * 
 * @author Dennis Soemers
 */
public class GenerateRulesetIDsCSV 
{
	
	private static void generateFile(final CommandLineArgParse argParse) 
	{
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter(s -> (
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/puzzle/deduction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/subgame/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
			)).toArray(String[]::new);
		
		final List<String> cleanGameRulesetNames = new ArrayList<String>();
		final TIntArrayList ids = new TIntArrayList();
		
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
				
				final String filepathsGameName = 
						StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = 
						StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				cleanGameRulesetNames.add(filepathsGameName + filepathsRulesetName);
				
				ids.add(IdRuleset.get(game));
			}
		}
		
		String outDir = argParse.getValueString("--out-dir");
		outDir = outDir.replaceAll(Pattern.quote("\\"), "/");
		if (!outDir.endsWith("/"))
			outDir += "/";
		
		try (final PrintWriter writer = new PrintWriter(outDir + "GameRulesetIds.csv", "UTF-8"))
		{
			writer.write("GameRulesetName,Id\n");
			
			for (int i = 0; i < cleanGameRulesetNames.size(); ++i)
			{
				writer.write(cleanGameRulesetNames.get(i) + "," + ids.getQuick(i) + "\n");
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method to generate all our scripts
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Generates our CSV."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--out-dir")
				.help("Directory in which to generate our file.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateFile(argParse);
	}

}
