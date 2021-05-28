package utils.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ListUtils;
import main.options.Option;
import main.options.Ruleset;
import other.GameLoader;

/**
 * Code to export CSV files for features to database
 *
 * @author Dennis Soemers
 */
public class ExportFeaturesDB
{
	
	//-------------------------------------------------------------------------
	
	/** The path of the csv with the id of the rulesets for each game. */
	private static final String GAME_RULESET_PATH = "/concepts/input/GameRulesets.csv";
	
	/** The path of the csv with Feature strings and IDs */
	private static final String FEATURES_CSV_PATH = "/features/Features.csv";
	
	/** ID of Cross-Entropy objective in database */
	private static final int CROSS_ENTROPY_ID = 1;
	
	/** ID of the TSPG objective in database */
	@SuppressWarnings("unused")		// NOT USED YET, but probably will be used later!!!!
	private static final int TSPG_ID = 2;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private ExportFeaturesDB()
	{
		// No need to instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Export the CSVs
	 * @param argParse
	 */
	private static void exportCSVs(final CommandLineArgParse argParse)
	{
		final String[] allGameNames = FileHandling.listGames();
		final List<String> games = new ArrayList<String>();
		final List<String> rulesets = new ArrayList<String>();
		final TIntArrayList ids = new TIntArrayList();
		final InputStream inGameRulesets = ExportFeaturesDB.class.getResourceAsStream(GAME_RULESET_PATH);

		try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(inGameRulesets)))
		{
			String line = rdr.readLine();
			while (line != null)
			{
				String lineNoQuote = line.replaceAll(Pattern.quote("\""), "");

				int separatorIndex = lineNoQuote.indexOf(',');
				final String gameName = lineNoQuote.substring(0, separatorIndex);
				games.add(gameName);
				lineNoQuote = lineNoQuote.substring(gameName.length() + 1);

				separatorIndex = lineNoQuote.indexOf(',');
				final String rulesetName = lineNoQuote.substring(0, separatorIndex);
				rulesets.add(rulesetName);
				lineNoQuote = lineNoQuote.substring(rulesetName.length() + 1);
				final int id = Integer.parseInt(lineNoQuote);
				ids.add(id);

				line = rdr.readLine();
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		final TObjectIntHashMap<String> featureIDsMap = new TObjectIntHashMap<String>();
		final List<String> featureStrings = new ArrayList<String>();
		
		// First collect all the already-known features and their IDs
		final InputStream inFeatures = ExportFeaturesDB.class.getResourceAsStream(FEATURES_CSV_PATH);

		try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(inFeatures)))
		{
			String line = rdr.readLine();
			while (line != null)
			{
				final String[] split = line.split(Pattern.quote(","));
				final int featureID = Integer.parseInt(split[0]);
				final String featureString = split[1];
				
				if (featureIDsMap.containsKey(featureString))
				{
					if (featureIDsMap.get(featureString) != featureID)
						System.err.println("ERROR: feature ID mismatch!");
					
					System.err.println("ERROR: duplicate feature in old CSV");
				}
				else
				{
					featureIDsMap.put(featureString, featureID);
					featureStrings.add(featureString);
				}

				line = rdr.readLine();
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		final File outDir = new File(argParse.getValueString("--out-dir"));
		outDir.mkdirs();
		
		@SuppressWarnings("resource")	// NOTE: not using try-with-resource because we need multiple writers, and pass them around functions
		PrintWriter featuresWriter = null;
		@SuppressWarnings("resource")	// NOTE: not using try-with-resource because we need multiple writers, and pass them around functions
		PrintWriter rulesetFeaturesWriter = null;
		try
		{
			// First re-write the features we already know about
			featuresWriter = new UnixPrintWriter(new File(argParse.getValueString("--out-dir") + "/Features.csv"), "UTF-8");
			for (int i = 0; i < featureStrings.size(); ++i)
			{
				featuresWriter.println((i + 1) + "," + StringRoutines.quote(featureStrings.get(i)));
			}
			
			rulesetFeaturesWriter = new UnixPrintWriter(new File(argParse.getValueString("--out-dir") + "/RulesetFeatures.csv"), "UTF-8");
			
			// Start processing training results
			final File gamesDir = new File(argParse.getValueString("--games-dir"));
			final File[] gameDirs = gamesDir.listFiles();
			
			// First counter is Feature ID, second counter is Ruleset-Feature-ID
			final int[] idCounters = new int[]{featureStrings.size() + 1, 1};
			
			for (final File gameDir : gameDirs)
			{
				// We will either directly find features in here, or subdirectories of combinations of options
				final File[] files = gameDir.listFiles();
				
				if (files.length == 0)
					continue;
				
				if (files[0].isDirectory())
				{
					// Subdirectories for combinations of options
					for (final File optionsCombDir : files)
					{
						System.out.println("Processing: " + gameDir.getName() + "/" + optionsCombDir.getName());
						processTrainingResultsDir(
								gameDir.getName(), 
								optionsCombDir.getName(), 
								optionsCombDir.listFiles(), 
								featureIDsMap, 
								allGameNames,
								games,
								ids,
								rulesets,
								featuresWriter,
								rulesetFeaturesWriter,
								idCounters
						);
					}
				}
				else
				{
					// Just a game-wide directory
					System.out.println("Processing: " + gameDir.getName());
					processTrainingResultsDir(
							gameDir.getName(), 
							null, 
							files, 
							featureIDsMap, 
							allGameNames,
							games,
							ids,
							rulesets,
							featuresWriter,
							rulesetFeaturesWriter,
							idCounters
					);
				}
			}
		} 
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (featuresWriter != null)
				featuresWriter.close();
			
			if (rulesetFeaturesWriter != null)
				rulesetFeaturesWriter.close();
		}
	}
	
	//-------------------------------------------------------------------------
	
	private static void processTrainingResultsDir
	(
		final String gameDirName,
		final String optionsCombDirName,
		final File[] trainingOutFiles,
		final TObjectIntHashMap<String> knownFeaturesMap,
		final String[] allGameNames,
		final List<String> gameNames,
		final TIntArrayList ids,
		final List<String> rulesetNames,
		final PrintWriter featuresWriter,
		final PrintWriter rulesetFeaturesWriter,
		final int[] idCounters		// First counter is Feature ID, second counter is Ruleset-Feature-ID
	)
	{
		// First figure out the proper name of the game we're dealing with
		String gameName = "";
		
		for (final String name : allGameNames)
		{
			final String[] gameNameSplit = name.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String cleanGameName = StringRoutines.cleanGameName(gameNameSplit[gameNameSplit.length - 1]);
			
			if (gameDirName.equals(cleanGameName))
			{
				gameName = name;
				break;
			}
		}
		
		if (gameName.equals(""))
		{
			System.err.println("Can't recognise game: " + gameDirName);
			return;
		}
		
		// Compile game without options
		final Game gameDefault = GameLoader.loadGameFromName(gameName);
		
		List<String> optionsToCompile = null;
		if (optionsCombDirName == null)
		{
			optionsToCompile = new ArrayList<String>();
		}
		else
		{
			// Figure out all combinations of options
			final List<List<String>> optionCategories = new ArrayList<List<String>>();
	
			for (int o = 0; o < gameDefault.description().gameOptions().numCategories(); o++)
			{
				final List<Option> options = gameDefault.description().gameOptions().categories().get(o).options();
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
	
			final List<List<String>> optionCombinations = ListUtils.generateTuples(optionCategories);
	
			// Figure out which combination of options is the correct one that we wish to compile
			for (final List<String> optionCombination : optionCombinations)
			{
				final String optionCombinationString = 
						StringRoutines.join("-", optionCombination)
						.replaceAll(Pattern.quote(" "), "")
						.replaceAll(Pattern.quote("/"), "_")
						.replaceAll(Pattern.quote("("), "_")
						.replaceAll(Pattern.quote(")"), "_")
						.replaceAll(Pattern.quote(","), "_");
	
				if (optionsCombDirName.equals(optionCombinationString))
				{
					optionsToCompile = optionCombination;
					break;
				}
			}
	
			if (optionsToCompile == null)
			{
				System.err.println("Couldn't find options to compile!");
				return;
			}
		}
		
		// See if we can find a ruleset that matches our list of options to compile with
		final List<Ruleset> rulesetsInGame = gameDefault.description().rulesets();
		int rulesetID = -1;
		if (rulesetsInGame != null && !rulesetsInGame.isEmpty())
		{
			final List<String> specifiedOptions =
					gameDefault.description().gameOptions().allOptionStrings(optionsToCompile);
			
			for (int rs = 0; rs < rulesetsInGame.size(); rs++)
			{
				final Ruleset ruleset = rulesetsInGame.get(rs);
				if (!ruleset.optionSettings().isEmpty())
				{
					final List<String> rulesetOptions = 
							gameDefault.description().gameOptions().allOptionStrings(ruleset.optionSettings());
					
					if (rulesetOptions.equals(specifiedOptions))
					{
						final String rulesetHeading = ruleset.heading();
						final String startString = "Ruleset/";
						final String rulesetNameCSV = rulesetHeading.substring(startString.length(),
								rulesetHeading.lastIndexOf('(') - 1);
						
						for (int i = 0; i < gameNames.size(); i++)
						{
							if (gameNames.get(i).equals(gameDefault.name()) && rulesetNames.get(i).equals(rulesetNameCSV))
							{
								rulesetID = ids.getQuick(i);
								break;
							}
						}
						
						if (rulesetID != -1)
							break;
					}
				}
			}
		}
		else
		{
			// No rulesets; see if these options are just the default for the game
			final List<String> defaultOptions = 
					gameDefault.description().gameOptions().allOptionStrings(new ArrayList<String>());
			final List<String> specifiedOptions =
					gameDefault.description().gameOptions().allOptionStrings(optionsToCompile);
			
			if (defaultOptions.equals(specifiedOptions))
			{
				for (int i = 0; i < gameNames.size(); i++)
				{
					if (gameNames.get(i).equals(gameDefault.name()))
					{
						rulesetID = ids.getQuick(i);
						break;
					}
				}
			}
			else
			{
				// We're skipping these options, they're not the default
				return;
			}
		}
		
		if (rulesetID == -1)
			return;		// Didn't find matching ruleset
		
		final Game game = GameLoader.loadGameFromName(gameName, optionsToCompile);
		final int numPlayers = game.players().count();
		
		// Find latest FeatureSet and PolicyWeightsCE files per player
		final File[] latestFeatureSetFiles = new File[numPlayers + 1];
		final File[] latestPolicyWeightFiles = new File[numPlayers + 1];
		final int[] latestCheckpoints = new int[numPlayers + 1];
		Arrays.fill(latestCheckpoints, -1);
		
		for (final File trainingOutFile : trainingOutFiles)
		{
			final String outFilename = trainingOutFile.getName();
			
			if (!outFilename.startsWith("FeatureSet_"))
				continue;
			
			// We're dealing with a featureset file
			final String[] outFilenameSplit = outFilename.split(Pattern.quote("_"));
			final int player = Integer.parseInt(outFilenameSplit[1].substring(1));
			final String checkpointStr = outFilenameSplit[2].replaceFirst(Pattern.quote(".fs"), "");
			final int checkpoint = Integer.parseInt(checkpointStr);
			
			if (checkpoint > latestCheckpoints[player])
			{
				// New latest checkpoint
				latestCheckpoints[player] = checkpoint;
				latestFeatureSetFiles[player] = trainingOutFile;
				
				// Find matching CE weights file
				final File weightsFile = new File(
						trainingOutFile.getParentFile().getAbsolutePath() +
						"/PolicyWeightsCE_P" + player + "_" + checkpointStr + ".txt");
				latestPolicyWeightFiles[player] = weightsFile;
			}
		}
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			if (latestFeatureSetFiles[p] != null)
			{
				// Read the list of feature strings
				final List<String> features = new ArrayList<String>();
				
				try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(latestFeatureSetFiles[p]))))
				{
					String line = rdr.readLine();
					while (line != null)
					{
						features.add(line);
						line = rdr.readLine();
					}
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
				
				// Read the list of feature weights
				final TFloatArrayList weights = new TFloatArrayList();
				
				try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(latestPolicyWeightFiles[p]))))
				{
					String line = rdr.readLine();
					while (line != null)
					{
						if (line.startsWith("FeatureSet="))
							break;
						
						weights.add(Float.parseFloat(line));
						line = rdr.readLine();
					}
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
				
				// Write results to CSVs
				for (int i = 0; i < features.size(); ++i)
				{
					final String feature = features.get(i);
					final float weight = weights.getQuick(i);
					final int featureID;
					
					if (!knownFeaturesMap.containsKey(feature))
					{
						featureID = idCounters[0]++;
						knownFeaturesMap.put(feature, featureID);
						featuresWriter.println(featureID + "," + StringRoutines.quote(feature));
					}
					else
					{
						featureID = knownFeaturesMap.get(feature);
					}
					
					rulesetFeaturesWriter.println(StringRoutines.join(",", new String[]{
							Integer.toString(idCounters[1]++),
							Integer.toString(rulesetID),
							Integer.toString(featureID),
							Integer.toString(CROSS_ENTROPY_ID),
							Integer.toString(p),
							Float.toString(weight)
					}));
				}
			}
		}
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
					"Export CSVs for features in database."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--games-dir")
				.help("Directory that contains one subdirectory for every game.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--out-dir")
				.help("Filepath for directory to write new CSVs to.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// Parse the args
		if (!argParse.parseArguments(args))
			return;
		
		exportCSVs(argParse);
	}
	
	//-------------------------------------------------------------------------

}
