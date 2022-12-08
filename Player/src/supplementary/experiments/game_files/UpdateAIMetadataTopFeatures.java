package supplementary.experiments.game_files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import features.spatial.Walk;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Report;
import main.options.Ruleset;
import metadata.ai.Ai;
import metadata.ai.features.Features;
import other.GameLoader;

/**
 * Main method to update AI Metadata with identified top features.
 * 
 * @author Dennis Soemers
 */
public class UpdateAIMetadataTopFeatures
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Games we should skip since they never end anyway (in practice), but do
	 * take a long time.
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
	 * Updates our AI metadata
	 * @param argParse
	 */
	private static void updateMetadata(final CommandLineArgParse argParse)
	{
		String topFeaturesOutDirPath = argParse.getValueString("--top-features-out-dir");
		topFeaturesOutDirPath = topFeaturesOutDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!topFeaturesOutDirPath.endsWith("/"))
			topFeaturesOutDirPath += "/";
		
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
		
		for (final String gameName : allGameNames)
		{
			final String[] gameNameSplit = gameName.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String shortGameName = gameNameSplit[gameNameSplit.length - 1];
			
			boolean skipGame = false;
			for (final String game : SKIP_GAMES)
			{
				if (shortGameName.endsWith(game))
				{
					skipGame = true;
					break;
				}
			}
			
			if (skipGame)
				continue;
			
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName);
			final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
			gameRulesets.add(null);
			boolean foundRealRuleset = false;
			
			final String thisGameName = "/" + shortGameName;
			
			// This will collect the strings we want to write for all rulesets of this game
			final List<String> stringsToWrite = new ArrayList<String>();
			
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
				
				if (Walk.allGameRotations(game).length == 0)
					continue;
				
				if (game.players().count() == 0)
					continue;
				
				if (game.isSimultaneousMoveGame())
					continue;
				
				final String thisRulesetName = fullRulesetName;
				
				// Figure out whether we have features for this ruleset
				final String cleanGameName = StringRoutines.cleanGameName(thisGameName.replaceAll(Pattern.quote(".lud"), ""));
				final String cleanRulesetName = StringRoutines.cleanRulesetName(thisRulesetName).replaceAll(Pattern.quote("/"), "_");
				
				final String rulesetFeaturesOutDirPath = topFeaturesOutDirPath + cleanGameName + "_" + cleanRulesetName;
				final File rulesetFeaturesOutDir = new File(rulesetFeaturesOutDirPath);
				
				if (!rulesetFeaturesOutDir.exists() || !rulesetFeaturesOutDir.isDirectory())
					continue;		// No features for this ruleset, move on
				
				final File bestFeaturesFile = new File(rulesetFeaturesOutDirPath + "/BestFeatures.txt");
				if (!bestFeaturesFile.exists())
					continue;		// No features for this ruleset, move on
				
				// Generate the strings we want to write for features metadata
				if (!cleanRulesetName.isEmpty())
				{
					stringsToWrite.add("(useFor { " + StringRoutines.quote(thisRulesetName) + " }");
				}
				
				// Get the current metadata out of game, if any exists
				Ai aiMetadata = game.metadata().ai();
				if (aiMetadata == null)
					aiMetadata = new Ai(null, null, null, null, null, null);
				
				try
				{
					// Load the features we've identified and put them in metadata
					final Features features = (Features)compiler.Compiler.compileObject
							(
								FileHandling.loadTextContentsFromFile(bestFeaturesFile.getAbsolutePath()), 
								"metadata.ai.features.Features",
								new Report()
							);
					
					aiMetadata.setTrainedFeatures(features);
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
				
				stringsToWrite.add(aiMetadata.toString());
				
				// Close the (useFor ...) block if we have one
				if (!cleanRulesetName.isEmpty())
				{
					stringsToWrite.add(")");
				}
			}
			
			// We have something to write for this game
			if (!stringsToWrite.isEmpty())
			{
				// Write our AI def file
				final File aiDefFile = new File(argParse.getValueString("--ai-defs-dir") + "/" + thisGameName.replaceAll(Pattern.quote(".lud"), "") + "_ai.def");
				
				try (final PrintWriter writer = new PrintWriter(aiDefFile, "UTF-8"))
				{
					System.out.println("Writing to file: " + aiDefFile.getAbsolutePath());
					writer.println("(define " + StringRoutines.quote((thisGameName.replaceAll(Pattern.quote(".lud"), "") + "_ai").substring(1)));
					
					for (final String toWrite : stringsToWrite)
					{
						writer.println(toWrite);
					}
					
					writer.println(")");
				}
				catch (final FileNotFoundException | UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				
				// Also update the .lud file to make sure it points to our AI def file
				final File ludFile = new File(argParse.getValueString("--luds-dir") + gameName, "");
				try
				{
					final String ludFileContents = FileHandling.loadTextContentsFromFile(ludFile.getAbsolutePath());
					final String defStr = StringRoutines.quote((thisGameName.replaceAll(Pattern.quote(".lud"), "") + "_ai").substring(1));
					
					if (gameNoRuleset.metadata().ai().agent() == null)
					{
						if (!StringRoutines.cleanWhitespace(ludFileContents.replaceAll(Pattern.quote("\n"), "")).contains(defStr))
						{
							// We need to write the AI metadata
							final StringBuffer sb = new StringBuffer(ludFileContents);
							final int startMetadataIdx = sb.indexOf("(metadata");
							final int endMetadataIdx = StringRoutines.matchingBracketAt(ludFileContents, startMetadataIdx);
							sb.insert(endMetadataIdx, "    (ai\n        " + defStr + "\n    )\n");
							
							try (final PrintWriter writer = new PrintWriter(ludFile, "UTF-8"))
							{
								System.out.println("Updating .lud file: " + ludFile.getAbsolutePath());
								writer.print(sb.toString());
							}
						}	
					}
					else if (!StringRoutines.cleanWhitespace(ludFileContents.replaceAll(Pattern.quote("\n"), "")).contains(defStr))
					{
						// Print warnings
						System.err.println("AI Metadata not null, but did not find the AI def: " + defStr);
						System.err.println(" looked at file: " + ludFile.getAbsolutePath());
						
						// Replace the (ai ...) metadata part from .lud file with reference to AI def
						final StringBuffer sb = new StringBuffer(ludFileContents);
						final int startMetadataIdx = sb.indexOf("(metadata");
						
						final int startAiMetadataIdx = sb.indexOf("(ai", startMetadataIdx);
						final int endAiMetadataIdx = StringRoutines.matchingBracketAt(ludFileContents, startAiMetadataIdx);
						
						sb.replace(startAiMetadataIdx, endAiMetadataIdx + 1, "    (ai\n        " + defStr + "\n    )\n");

						try (final PrintWriter writer = new PrintWriter(ludFile, "UTF-8"))
						{
							System.out.println("Updating .lud file: " + ludFile.getAbsolutePath());
							writer.print(sb.toString());
						}
					}
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
