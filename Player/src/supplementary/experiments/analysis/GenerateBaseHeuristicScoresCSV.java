package supplementary.experiments.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.options.Ruleset;
import other.GameLoader;
import utils.DBGameInfo;

/**
 * Generates a CSV file containing the scores for all base heuristics
 * for all games.
 *
 * @author Dennis Soemers
 */
public class GenerateBaseHeuristicScoresCSV
{
	/**
	 * Constructor (don't need this)
	 */
	private GenerateBaseHeuristicScoresCSV()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates our CSV
	 * @param argParse
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static void generateCSV(final CommandLineArgParse argParse) throws FileNotFoundException, IOException
	{
		String resultsDir = argParse.getValueString("--results-dir");
		resultsDir = resultsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!resultsDir.endsWith("/"))
			resultsDir += "/";
		
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
		
		final List<List<String>> rowStringLists = new ArrayList<List<String>>();
		final List<TObjectDoubleMap<String>> heuristicScoreSumsList = new ArrayList<TObjectDoubleMap<String>>();
		final List<TObjectIntMap<String>> heuristicCountsList = new ArrayList<TObjectIntMap<String>>();
		final Set<String> allHeuristicNames = new HashSet<String>();

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
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final File rulesetResultsDir = new File(resultsDir + filepathsGameName + filepathsRulesetName);
				if (rulesetResultsDir.exists())
				{
					final List<String> rowStringList = new ArrayList<String>();
					
					// First column: game name
					rowStringList.add(gameName);
					
					// Second column: ruleset name
					rowStringList.add(fullRulesetName);
					
					// Third column: game + ruleset name
					rowStringList.add(DBGameInfo.getUniqueName(game));
					
					// Map from heuristic names to sum of scores for this ruleset
					final TObjectDoubleMap<String> heuristicScoreSums = new TObjectDoubleHashMap<String>();
					// Map from heuristic names to how often we observed this heuristic in this ruleset
					final TObjectIntMap<String> heuristicCounts = new TObjectIntHashMap<String>();
					
					final File[] matchupDirs = rulesetResultsDir.listFiles();
					for (final File matchupDir : matchupDirs)
					{
						if (matchupDir.isDirectory())
						{
							final String[] resultLines = 
									FileHandling.loadTextContentsFromFile(
											matchupDir.getAbsolutePath() + "/alpha_rank_data.csv"
									).split(Pattern.quote("\n"));
							
							// Skip index 0, that's just the headings
							for (int i = 1; i < resultLines.length; ++i)
							{
								final String line = resultLines[i];
								final int idxQuote1 = 0;
								final int idxQuote2 = line.indexOf("\"", idxQuote1 + 1);
								final int idxQuote3 = line.indexOf("\"", idxQuote2 + 1);
								final int idxQuote4 = line.indexOf("\"", idxQuote3 + 1);
								
								final String heuristicsTuple = 
										line
										.substring(idxQuote1 + 2, idxQuote2 - 1)
										.replaceAll(Pattern.quote(" "), "")
										.replaceAll(Pattern.quote("'"), "");
								final String scoresTuple = 
										line
										.substring(idxQuote3 + 2, idxQuote4 - 1)
										.replaceAll(Pattern.quote(" "), "");
								
								final String[] heuristicNames = heuristicsTuple.split(Pattern.quote(","));
								final String[] scores = scoresTuple.split(Pattern.quote(","));
								
								for (int j = 0; j < heuristicNames.length; ++j)
								{
									// Convert score to "win percentage"
									final double score = ((Double.parseDouble(scores[j]) + 1.0) / 2.0) * 100.0;
									
									heuristicScoreSums.adjustOrPutValue(heuristicNames[j], score, score);
									heuristicCounts.adjustOrPutValue(heuristicNames[j], 1, 1);
									allHeuristicNames.add(heuristicNames[j]);
								}
							}
						}
					}
					
					// All strings for this row are complete...ish for now
					rowStringLists.add(rowStringList);
					heuristicScoreSumsList.add(heuristicScoreSums);
					heuristicCountsList.add(heuristicCounts);
				}
			}
		}
		
		final List<String> sortedHeuristicNames = new ArrayList<String>(allHeuristicNames);
		Collections.sort(sortedHeuristicNames);
		
		String outDir = argParse.getValueString("--out-dir");
		if (!outDir.endsWith("/"))
			outDir += "/";
		
		final String outFilename = argParse.getValueBool("--merge-region-heuristics") ? "BaseHeuristicScoresMerged.csv" : "BaseHeuristicScores.csv";
		try (final PrintWriter writer = new PrintWriter(new File(outDir + outFilename), "UTF-8"))
		{
			// First write the headings
			final List<String> headings = new ArrayList<String>();
			headings.add("Game");
			headings.add("Ruleset");
			headings.add("GameRuleset");
			headings.addAll(sortedHeuristicNames);
			
			if (argParse.getValueBool("--merge-region-heuristics"))
			{
				String lastMerged = "";
				
				for (int i = 3; i < headings.size(); /**/)
				{
					final String origHeading = headings.get(i);
					
					if (StringRoutines.isDigit(origHeading.charAt(origHeading.length() - 1)))
					{
						final String truncatedHeading = origHeading.substring(0, origHeading.lastIndexOf("_"));
						if (!lastMerged.equals(truncatedHeading))
						{
							lastMerged = truncatedHeading;
							headings.set(i, truncatedHeading);
						}
						else
						{
							headings.remove(i);
						}
					}
					else
					{
						++i;
					}
				}
			}
			
			writer.println(StringRoutines.join(",", headings));
			
			// Now write all the rows
			for (int i = 0; i < rowStringLists.size(); ++i)
			{
				final List<String> rowStringList = rowStringLists.get(i);
				final TObjectDoubleMap<String> scoreSumsMap = heuristicScoreSumsList.get(i);
				final TObjectIntMap<String> heuristicCountsMap = heuristicCountsList.get(i);
				
				String lastMerged = "";
				
				for (final String heuristicName : sortedHeuristicNames)
				{
					if (scoreSumsMap.containsKey(heuristicName))
					{
						if (argParse.getValueBool("--merge-region-heuristics") && StringRoutines.isDigit(heuristicName.charAt(heuristicName.length() - 1)))
						{
							final String truncatedName = heuristicName.substring(0, heuristicName.lastIndexOf("_"));
							if (!lastMerged.equals(truncatedName))
							{
								lastMerged = truncatedName;
								rowStringList.add("" + scoreSumsMap.get(heuristicName) / heuristicCountsMap.get(heuristicName));
							}
							else
							{
								final double prevScore;
								if (rowStringList.get(rowStringList.size() - 1).length() > 0)
									prevScore = Double.parseDouble(rowStringList.get(rowStringList.size() - 1));
								else
									prevScore = Double.NEGATIVE_INFINITY;
								
								final double newScore = scoreSumsMap.get(heuristicName) / heuristicCountsMap.get(heuristicName);
								
								if (newScore > prevScore)
									rowStringList.set(rowStringList.size() - 1, "" + newScore);
							}
						}
						else
						{
							rowStringList.add("" + scoreSumsMap.get(heuristicName) / heuristicCountsMap.get(heuristicName));
						}
					}
					else
					{
						if (argParse.getValueBool("--merge-region-heuristics") && StringRoutines.isDigit(heuristicName.charAt(heuristicName.length() - 1)))
						{
							final String truncatedName = heuristicName.substring(0, heuristicName.lastIndexOf("_"));
							
							if (!lastMerged.equals(truncatedName))
							{
								lastMerged = truncatedName;
								rowStringList.add("");
							}
						}
						else
						{
							rowStringList.add("");
						}
					}
				}
				
				writer.println(StringRoutines.join(",", rowStringList));
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
					"Generates a CSV file containing the scores for all base heuristics for all games."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--results-dir")
				.help("Filepath for directory with per-game subdirectories of matchup directories.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--out-dir")
				.help("Output directory to save output files to.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--merge-region-heuristics")
				.help("If true, we'll merge all region proximity heuristics with different region indices.")
				.withType(OptionTypes.Boolean));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateCSV(argParse);
	}
	
	//-------------------------------------------------------------------------

}
