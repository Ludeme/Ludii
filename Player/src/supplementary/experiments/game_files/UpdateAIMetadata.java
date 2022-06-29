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

import features.feature_sets.BaseFeatureSet;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Report;
import main.options.Ruleset;
import metadata.ai.agents.BestAgent;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import other.GameLoader;
import search.minimax.AlphaBetaSearch;

/**
 * Script to update all our AI metadata defines based on a directory
 * of best agents data.
 *
 * @author Dennis Soemers
 */
public class UpdateAIMetadata
{
	/**
	 * Constructor
	 */
	private UpdateAIMetadata()
	{
		// Should not construct
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Updates our AI metadata
	 * @param argParse
	 */
	private static void updateMetadata(final CommandLineArgParse argParse)
	{
		String bestAgentsDataDirPath = argParse.getValueString("--best-agents-data-dir");
		bestAgentsDataDirPath = bestAgentsDataDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!bestAgentsDataDirPath.endsWith("/"))
			bestAgentsDataDirPath += "/";
		
		final File bestAgentsDataDir = new File(bestAgentsDataDirPath);
		
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter
				(
					s -> 
					(
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
					)
				).toArray(String[]::new);
		
		// Loop through all the games we have
		for (final String fullGamePath : allGameNames)
		{
			final String[] gamePathParts = fullGamePath.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName + ".lud");
			final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
			gameRulesets.add(null);
			boolean foundRealRuleset = false;
			
			final List<File> bestAgentDataDirsForGame = new ArrayList<File>();	// one per ruleset
			final List<Ruleset> rulesets = new ArrayList<Ruleset>();
			
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
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final File bestAgentsDataDirForRuleset = 
						new File(bestAgentsDataDir.getAbsolutePath() + "/" + filepathsGameName + filepathsRulesetName);
				
				if 
				(
					bestAgentsDataDirForRuleset.exists() 
					&& 
					bestAgentsDataDirForRuleset.isDirectory() 
					&& 
					bestAgentsDataDirForRuleset.list().length > 0
				)
				{
					bestAgentDataDirsForGame.add(bestAgentsDataDirForRuleset);
					rulesets.add(ruleset);
				}
			}
			
			if (!rulesets.isEmpty())
			{
				updateMetadata(bestAgentDataDirsForGame, gameName, rulesets, fullGamePath, argParse);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Update metadata based on array of directories for game (sometimes one
	 * for game as a whole, sometimes one per set of options)
	 * 
	 * @param gameDirs
	 * @param gameName
	 * @param allGameNames
	 * @param fullGamePath
	 * @param argParse
	 */
	private static void updateMetadata
	(
		final List<File> gameDirs, 
		final String gameName,
		final List<Ruleset> rulesets, 
		final String fullGamePath,
		final CommandLineArgParse argParse
	)
	{
		final List<String> stringsToWrite = new ArrayList<String>();
		boolean addedAIContents = false;
		final Game defaultGame = GameLoader.loadGameFromName(gameName + ".lud");
		
		// Now process every ruleset
		for (int i = 0; i < rulesets.size(); ++i)
		{
			final Ruleset ruleset = rulesets.get(i);
			final List<String> usedOptions = new ArrayList<String>();
			
			if (ruleset != null && !ruleset.optionSettings().isEmpty())
			{
				usedOptions.addAll(ruleset.optionSettings());
			}
			
			final File bestAgentsFile = new File(gameDirs.get(i).getAbsolutePath() + "/BestAgent.txt");
			final File bestFeaturesFile = new File(gameDirs.get(i).getAbsolutePath() + "/BestFeatures.txt");
			final File bestHeuristicsFile = new File(gameDirs.get(i).getAbsolutePath() + "/BestHeuristics.txt");
			
			try
			{
				if (!usedOptions.isEmpty())
				{
					final StringBuilder sb = new StringBuilder();
					sb.append("(useFor {");
					for (final String opt : usedOptions)
					{
						sb.append(" " + StringRoutines.quote(opt));
					}
					sb.append(" }");
					stringsToWrite.add(sb.toString());
				}	
				
				if (bestAgentsFile.exists())
				{
					BestAgent bestAgent = (BestAgent)compiler.Compiler.compileObject
					(
						FileHandling.loadTextContentsFromFile(bestAgentsFile.getAbsolutePath()), 
						"metadata.ai.agents.BestAgent",
						new Report()
					);
					
					if (bestAgent.agent().equals("AlphaBetaMetadata"))
						bestAgent = new BestAgent("Alpha-Beta");

					stringsToWrite.add(bestAgent.toString());
					addedAIContents = true;
				}
				else
				{
					System.err.println("No best agents data found at: " + bestAgentsFile.getAbsolutePath());
					continue;
				}
				
				if (bestHeuristicsFile.exists())
				{
					final Heuristics heuristics = (Heuristics)compiler.Compiler.compileObject
					(
						FileHandling.loadTextContentsFromFile(bestHeuristicsFile.getAbsolutePath()), 
						"metadata.ai.heuristics.Heuristics",
						new Report()
					);

					final String thresholdedString = heuristics.toStringThresholded(AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
					
					// Make sure heuristics are not empty after thresholding, possible due to null heuristic
					if (!thresholdedString.replaceAll(Pattern.quote("\n"), "").equals("(heuristics {})"))
					{
						stringsToWrite.add(thresholdedString);
						addedAIContents = true;
					}

				}
				
				if (bestFeaturesFile.exists())
				{
					final Features features = (Features)compiler.Compiler.compileObject
					(
						FileHandling.loadTextContentsFromFile(bestFeaturesFile.getAbsolutePath()), 
						"metadata.ai.features.Features",
						new Report()
					);

					stringsToWrite.add(features.toStringThresholded(BaseFeatureSet.SPATIAL_FEATURE_WEIGHT_THRESHOLD));
					addedAIContents = true;
				}
				
				if (!usedOptions.isEmpty())
				{
					stringsToWrite.add(")");
				}	
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if (addedAIContents)
		{
			// Find the AI metadata def file
			final File aiDefFile = new File(argParse.getValueString("--ai-defs-dir") + "/" + gameName + "_ai.def");
			
			try (final PrintWriter writer = new PrintWriter(aiDefFile, "UTF-8"))
			{
				System.out.println("Writing to file: " + aiDefFile.getAbsolutePath());
				writer.println("(define " + StringRoutines.quote(gameName + "_ai"));
				
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
			
			// Find the .lud file
			final File ludFile = new File(argParse.getValueString("--luds-dir") + fullGamePath);
			try
			{
				final String ludFileContents = FileHandling.loadTextContentsFromFile(ludFile.getAbsolutePath());
				final String defStr = StringRoutines.quote(gameName + "_ai");
				
				if (defaultGame.metadata().ai().agent() == null)
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
					System.err.println("AI Metadata not null, but did not find the AI def: " + defStr);
					System.err.println(" looked at file: " + ludFile.getAbsolutePath());
				}
			}
			catch (final IOException e)
			{
				e.printStackTrace();
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
					"Updates all our AI metadata to include the new best agents, features, and heuristics."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--best-agents-data-dir")
				.help("Directory containing our best agents data.")
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
