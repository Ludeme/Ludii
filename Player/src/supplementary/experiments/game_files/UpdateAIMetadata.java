package supplementary.experiments.game_files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import features.feature_sets.BaseFeatureSet;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.collections.ListUtils;
import main.grammar.Report;
import main.options.Option;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.misc.BestAgent;
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
		final String[] allGameNames = FileHandling.listGames();
		
		String bestAgentsDataDirPath = argParse.getValueString("--best-agents-data-dir");
		bestAgentsDataDirPath = bestAgentsDataDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!bestAgentsDataDirPath.endsWith("/"))
			bestAgentsDataDirPath += "/";
		
		final File bestAgentsDataDir = new File(bestAgentsDataDirPath);
		
		// Loop through all the directories of best-agents-data we have
		final File[] gameDirs = bestAgentsDataDir.listFiles();
		
		for (final File gameDir : gameDirs)
		{
			if (gameDir.isDirectory())
			{
				final String gameDirName = gameDir.getName();
				final File[] files = gameDir.listFiles();
				
				boolean noOptions = false;
				
				for (final File file : files)
				{
					if 
					(
						file.getName().contains("BestAgent.txt") ||
						file.getName().contains("BestHeuristics.txt") || 
						file.getName().contains("BestFeatures.txt")
					)
					{
						noOptions = true;
						break;
					}
				}
				
				if (noOptions)
				{
					// This directory is the only one we need for this game
					updateMetadata(new File[]{gameDir}, gameDirName, allGameNames, argParse);
				}
				else
				{
					// We have subdirectories for options
					updateMetadata(files, gameDirName, allGameNames, argParse);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Update metadata based on array of directories for game (sometimes one
	 * for game as a whole, sometimes one per set of options)
	 * 
	 * @param gameDirs
	 * @param gameDirName
	 * @param allGameNames
	 * @param argParse
	 */
	private static void updateMetadata
	(
		final File[] gameDirs, 
		final String gameDirName,
		final String[] allGameNames, 
		final CommandLineArgParse argParse
	)
	{
		// First figure out the proper name of the game we're dealing with
		String gamePath = ""; 
		
		for (final String name : allGameNames)
		{
			final String[] gameNameSplit = name.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String cleanGameName = StringRoutines.cleanGameName(gameNameSplit[gameNameSplit.length - 1]);
			
			if (gameDirName.equals(cleanGameName))
			{
				gamePath = name;
				break;
			}
		}
		
		if (gamePath.equals(""))
		{
			System.err.println("Can't recognise game: " + gameDirName);
			return;
		}
		
		final String[] gamePathSplit = gamePath.split(Pattern.quote("/"));
		final String gameName = gamePathSplit[gamePathSplit.length - 1];
				
		// We'll need to figure out all of the combinations of options that exist
		final Game gameNoOptions = GameLoader.loadGameFromName(gameName);
		final List<List<String>> optionCategories = new ArrayList<List<String>>();

		for (int o = 0; o < gameNoOptions.description().gameOptions().numCategories(); o++)
		{
			final List<Option> options = gameNoOptions.description().gameOptions().categories().get(o).options();
			final List<String> optionCategory = new ArrayList<String>();

			for (int i = 0; i < options.size(); i++)
			{
				final Option option = options.get(i);

				final String categoryStr = StringRoutines.join("/", option.menuHeadings().toArray(new String[0]));

				if 
				(
					!categoryStr.contains("Board Size/") &&
					!categoryStr.contains("Rows/") &&
					!categoryStr.contains("Columns/")
				)
				{
					optionCategory.add(categoryStr);
				}
			}

			if (optionCategory.size() > 0)
				optionCategories.add(optionCategory);
		}

		final List<List<String>> allOptionCombinations = ListUtils.generateTuples(optionCategories);
		
		final List<String> stringsToWrite = new ArrayList<String>();
		boolean addedAIContents = false;
		
		// Now process every directory of results (1 per combination of options)
		for (final File dir : gameDirs)
		{
			//System.out.println("dir = " + dir.getAbsolutePath());
			
			// Figure out the list of options that match this dir
			final List<String> selectedOptions = new ArrayList<String>();
			
			if (gameDirs.length > 1)
			{
				// This is the only case in which we actually have selected options
				final String dirName = dir.getName();
				
				for (final List<String> optionCombination : allOptionCombinations)
				{
					final String optionCombinationString = 
						StringRoutines.join("-", optionCombination)
						.replaceAll(Pattern.quote(" "), "")
						.replaceAll(Pattern.quote("/"), "_")
						.replaceAll(Pattern.quote("("), "_")
						.replaceAll(Pattern.quote(")"), "_")
						.replaceAll(Pattern.quote(","), "_");
					
					if (dirName.equals(optionCombinationString))
					{
						selectedOptions.addAll(optionCombination);
						break;
					}
				}
				
				if (selectedOptions.isEmpty())
				{
					System.err.println("ERROR: could not find options matching dir name: " + dirName);
					continue;
				}
			}
			
			final File bestAgentsFile = new File(dir.getAbsolutePath() + "/BestAgent.txt");
			final File bestFeaturesFile = new File(dir.getAbsolutePath() + "/BestFeatures.txt");
			final File bestHeuristicsFile = new File(dir.getAbsolutePath() + "/BestHeuristics.txt");

			final Report report = new Report();
			
			try
			{
				if (!selectedOptions.isEmpty())
				{
					final StringBuilder sb = new StringBuilder();
					sb.append("(useFor {");
					for (final String opt : selectedOptions)
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
						"metadata.ai.misc.BestAgent",
						report
					);
					
					if (bestAgent.agent().equals("AlphaBetaMetadata"))
						bestAgent = new BestAgent("Alpha-Beta");

					stringsToWrite.add(bestAgent.toString());
					addedAIContents = true;
				}
				else
				{
					System.err.println("No best agents data found!");
					continue;
				}
				
				if (bestHeuristicsFile.exists())
				{
					final Heuristics heuristics = (Heuristics)compiler.Compiler.compileObject
					(
						FileHandling.loadTextContentsFromFile(bestHeuristicsFile.getAbsolutePath()), 
						"metadata.ai.heuristics.Heuristics",
						report
					);

					stringsToWrite.add(heuristics.toStringThresholded(AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD));
					addedAIContents = true;
				}
				
				if (bestFeaturesFile.exists())
				{
					final Features features = (Features)compiler.Compiler.compileObject
					(
						FileHandling.loadTextContentsFromFile(bestFeaturesFile.getAbsolutePath()), 
						"metadata.ai.features.Features",
						report
					);

					stringsToWrite.add(features.toStringThresholded(BaseFeatureSet.SPATIAL_FEATURE_WEIGHT_THRESHOLD));
					addedAIContents = true;
				}
				
				if (!selectedOptions.isEmpty())
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
			final File aiDefFile = new File(argParse.getValueString("--ai-defs-dir") + "/" + gameNoOptions.name() + "_ai.def");
			
			try (final PrintWriter writer = new PrintWriter(aiDefFile, "UTF-8"))
			{
				System.out.println("Writing to file: " + aiDefFile.getAbsolutePath());
				writer.println("(define " + StringRoutines.quote(gameNoOptions.name() + "_ai"));
				
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
			final File ludFile = new File(argParse.getValueString("--luds-dir") + gamePath);
			try
			{
				final String ludFileContents = FileHandling.loadTextContentsFromFile(ludFile.getAbsolutePath());
				final String defStr = StringRoutines.quote(gameNoOptions.name() + "_ai");
				
				if (gameNoOptions.metadata().ai().bestAgent() == null)
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
