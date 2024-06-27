package supplementary.experiments.game_files;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import compiler.Compiler;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.grammar.Description;
import main.grammar.Report;
import main.options.Ruleset;
import main.options.UserSelections;
import other.GameLoader;

/**
 * A little script to write CSV files with rule descriptions (in English and in .lud)
 * for all built-in games, or for a specified list of lud files.
 * 
 * @author Dennis Soemers
 */
public class WriteRulesEnglishAndLud 
{
	
	/**
	 * Games we're not interested in for our current dataset.
	 */
	private static final String[] SKIP_GAMES = new String[]
			{
				"Chinese Checkers.lud",
				"Li'b al-'Aqil.lud",
				"Li'b al-Ghashim.lud",
				"Mini Wars.lud",
				"Pagade Kayi Ata (Sixteen-handed).lud",
				"Taikyoku Shogi.lud"
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private WriteRulesEnglishAndLud()
	{
		// Do nothing
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Write our CSV files
	 * @param argParse
	 */
	@SuppressWarnings("unchecked")
	private static void writeCSVs(final CommandLineArgParse argParse)
	{
		String outDir = argParse.getValueString("--out-dir");
		outDir = outDir.replaceAll(Pattern.quote("\\"), "/");
		if (!outDir.endsWith("/"))
			outDir += "/";
				
		List<String> gamePaths = (List<String>) argParse.getValue("--game-paths");
		List<String> rulesetNames = new ArrayList<String>();
		
		if (gamePaths == null)
		{
			// Take built-in games
			gamePaths = new ArrayList<String>();
			
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
			
			for (final String fullGamePath : allGameNames)
			{
				final String[] gamePathParts = fullGamePath.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
				
				boolean skipGame = false;
				for (final String game : SKIP_GAMES)
				{
					if (gamePathParts[gamePathParts.length - 1].endsWith(game))
					{
						skipGame = true;
						break;
					}
				}
				
				if (skipGame)
					continue;
				
				final String gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
				final Game gameNoRuleset = GameLoader.loadGameFromName(gameName + ".lud");
				final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
				gameRulesets.add(null);
				boolean foundRealRuleset = false;
				
				for (final Ruleset ruleset : gameRulesets)
				{
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
						
						final Game game = GameLoader.loadGameFromName(gameName + ".lud", fullRulesetName);
						
						// A bunch of game types we'll skip:
						if (game.players().count() != 2)
						{
							continue;
						}
						if (game.isDeductionPuzzle())
						{
							continue;
						}
						if (game.isSimulationMoveGame())
						{
							continue;
						}
						if (!game.isAlternatingMoveGame())
						{
							continue;
						}
						if (game.hasSubgames())
						{
							continue;
						}
						if (game.hiddenInformation())
						{
							continue;
						}
						
						gamePaths.add(gameName + ".lud");
						rulesetNames.add(fullRulesetName);
					}
					else if (ruleset != null && ruleset.optionSettings().isEmpty())
					{
						// Skip empty ruleset
						continue;
					}
					else
					{
						final Game game = gameNoRuleset;
						
						// A bunch of game types we'll skip:
						if (game.players().count() != 2)
						{
							continue;
						}
						if (game.isDeductionPuzzle())
						{
							continue;
						}
						if (game.isSimulationMoveGame())
						{
							continue;
						}
						if (!game.isAlternatingMoveGame())
						{
							continue;
						}
						if (game.hasSubgames())
						{
							continue;
						}
						if (game.hiddenInformation())
						{
							continue;
						}
						
						gamePaths.add(gameName + ".lud");
						rulesetNames.add("");
					}
				}
			}
		}
		
		for (int i = 0; i < gamePaths.size(); ++i)
		{
			final String gamePath = gamePaths.get(i).replaceAll(Pattern.quote("\\"), "/");
			final String rulesetName;
			
			final Game game;
			
			boolean gameNameIsPath = (gamePath.contains("/"));
			
			if (gameNameIsPath)
			{
				// This is a filepath
				game = GameLoader.loadGameFromFile(new File(gamePath));
				rulesetName = "";
			}
			else
			{
				// This is a built-in game
				rulesetName = rulesetNames.get(i);
				game = GameLoader.loadGameFromName(gamePath, rulesetName);
			}
						
			if (game.players().count() != 2)
			{
				System.err.println("Error: " + gamePath + " does not have 2 players");
				continue;
			}
			
			if (game.isDeductionPuzzle())
			{
				System.err.println("Error: " + gamePath + " is a deduction puzzle");
				continue;
			}
			
			if (game.isSimulationMoveGame())
			{
				System.err.println("Error: " + gamePath + " is a simulation");
				continue;
			}
			
			if (!game.isAlternatingMoveGame())
			{
				System.err.println("Error: " + gamePath + " is a simultaneous-move game");
				continue;
			}
			
			if (game.hasSubgames())
			{
				System.err.println("Error: " + gamePath + " has subgames");
				continue;
			}
			
			if (game.hiddenInformation())
			{
				System.err.println("Error: " + gamePath + " has partial observability");
				continue;
			}
			
			String gameName = gamePath;
			final String[] gamePathParts = gameName.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			
			if (gameNameIsPath)
			{
				gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
			}
			else
			{
				gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
			}
			
			final String filepathsGameName = StringRoutines.cleanGameName(gameName);
			final String filepathsRulesetName = 
					StringRoutines.cleanRulesetName(rulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
			
			final File csvFile = new File(outDir + filepathsGameName + filepathsRulesetName + "/Rules.csv");
			csvFile.getParentFile().mkdirs();
			
			try (final PrintWriter writer = new UnixPrintWriter(csvFile, "UTF-8"))
			{
				// Write the header
				writer.println("EnglishRules,LudRules");
				
				// Write the actual data
				final String englishRules = StringRoutines.join(" ", game.metadata().info().getRules()).replaceAll(Pattern.quote("\n"), " ");
				final String ludDescription = StringRoutines.cleanWhitespace(game.description().expanded().replaceAll(Pattern.quote("\n"), " "));
				
				try 
				{
					@SuppressWarnings("unused")
					final Game testGame = (Game) Compiler.compile
					(
						new Description(ludDescription), 
						new UserSelections(new ArrayList<String>()), 
						new Report(),
						false
					);
				}
				catch (final Exception e)
				{
					System.out.println("Failed to compile game without newlines!");
					e.printStackTrace();
				}
				
				writer.println(StringRoutines.quote(englishRules) + "," + StringRoutines.quote(ludDescription));
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
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
					"Write a CSV file with English rule descriptions and .lud rule descriptions."
				);

		argParse.addOption(new ArgOption()
				.withNames("--out-dir")
				.help("Directory in which to write generated CSV files.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());

		argParse.addOption(new ArgOption()
				.withNames("--game-paths")
				.help("Filepaths for games we wish to process. "
						+ "If not provided, we use all built-in two-player "
						+ "sequential perfect-info zero-sum games.")
				.withNumVals("+")
				.withType(OptionTypes.String)
				.withDefault(null));

		// parse the args
		if (!argParse.parseArguments(args))
			return;

		writeCSVs(argParse);
	}

	//-------------------------------------------------------------------------

}
