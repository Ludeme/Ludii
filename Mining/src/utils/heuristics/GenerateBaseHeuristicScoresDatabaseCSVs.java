package utils.heuristics;

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
import utils.IdRuleset;

/**
 * Generates CSV files for database, describing scores of all base heuristics
 * for all games.
 *
 * @author Dennis Soemers
 */
public class GenerateBaseHeuristicScoresDatabaseCSVs
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Different types of heuristics for which we store data
	 *
	 * @author Dennis Soemers
	 */
	public enum HeuristicTypes
	{
		/** A standard, unparameterised heuristic */
		Standard,
		/** A parameterised heuristic with a specific parameter (e.g., region proximity for specific region ID) */
		Unmerged,
		/** Represents a collection of heuristics of the same type, but with different parameters */
		Merged
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private GenerateBaseHeuristicScoresDatabaseCSVs()
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
	private static void generateCSVs(final CommandLineArgParse argParse) throws FileNotFoundException, IOException
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
		
		final List<HeuristicData> heuristicsList = new ArrayList<HeuristicData>();
		final List<ScoreData> scoreDataList = new ArrayList<ScoreData>();

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
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final File rulesetResultsDir = new File(resultsDir + filepathsGameName + filepathsRulesetName);
				if (rulesetResultsDir.exists())
				{
					final int rulesetID = IdRuleset.get(game);
					
					if (rulesetID >= 0)
					{
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
										if (Double.parseDouble(scores[j]) < -1.0 || Double.parseDouble(scores[j]) > 1.0)
										{
											System.out.println(scores[j]);
											System.out.println("Line " + i + " of " + matchupDir.getAbsolutePath() + "/alpha_rank_data.csv");
										}
										
										// Convert score to "win percentage"
										final double score = ((Double.parseDouble(scores[j]) + 1.0) / 2.0) * 100.0;
										
										heuristicScoreSums.adjustOrPutValue(heuristicNames[j], score, score);
										heuristicCounts.adjustOrPutValue(heuristicNames[j], 1, 1);
									}
								}
							}
						}
						
						final List<ScoreData> rulesetScoreData = new ArrayList<ScoreData>();
						
						for (final String heuristic : heuristicScoreSums.keySet())
						{
							if (StringRoutines.isDigit(heuristic.charAt(heuristic.length() - 1)))
							{
								// Need to do both merged and unmerged
								final String truncatedName = heuristic.substring(0, heuristic.lastIndexOf("_"));
								
								// First do unmerged version
								HeuristicData heuristicData = null;
								for (final HeuristicData data : heuristicsList)
								{
									if (data.name.equals(heuristic))
									{
										heuristicData = data;
										break;
									}
								}
								
								if (heuristicData == null)
								{
									heuristicData = new HeuristicData(heuristic, HeuristicTypes.Unmerged);
									heuristicsList.add(heuristicData);
								}
								
								final int heuristicID = heuristicData.id;
								final double score = heuristicScoreSums.get(heuristic) / heuristicCounts.get(heuristic);
								rulesetScoreData.add(new ScoreData(rulesetID, heuristicID, score));
								
								// And now the merged version
								heuristicData = null;
								for (final HeuristicData data : heuristicsList)
								{
									if (data.name.equals(truncatedName))
									{
										heuristicData = data;
										break;
									}
								}
								
								if (heuristicData == null)
								{
									heuristicData = new HeuristicData(truncatedName, HeuristicTypes.Merged);
									heuristicsList.add(heuristicData);
								}
								
								final int mergedHeuristicID = heuristicData.id;
								
								// See if we need to update already-added score data, or add new data
								boolean shouldAdd = true;
								for (final ScoreData data : rulesetScoreData)
								{
									if (data.heuristicID == mergedHeuristicID)
									{
										if (score > data.score)
											data.score = score;
											
										shouldAdd = false;
										break;
									}
								}
								
								if (shouldAdd)
									rulesetScoreData.add(new ScoreData(rulesetID, mergedHeuristicID, score));
							}
							else
							{
								// No merged version
								HeuristicData heuristicData = null;
								for (final HeuristicData data : heuristicsList)
								{
									if (data.name.equals(heuristic))
									{
										heuristicData = data;
										break;
									}
								}
								
								if (heuristicData == null)
								{
									heuristicData = new HeuristicData(heuristic, HeuristicTypes.Standard);
									heuristicsList.add(heuristicData);
								}
								
								final int heuristicID = heuristicData.id;
								final double score = heuristicScoreSums.get(heuristic) / heuristicCounts.get(heuristic);
								rulesetScoreData.add(new ScoreData(rulesetID, heuristicID, score));
							}
						}
						
						scoreDataList.addAll(rulesetScoreData);
					}
				}
			}
		}
		
		try (final PrintWriter writer = new PrintWriter(new File("../Mining/res/heuristics/Heuristics.csv"), "UTF-8"))
		{
			for (final HeuristicData data : heuristicsList)
			{
				writer.println(data);
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		try (final PrintWriter writer = new PrintWriter(new File("../Mining/res/heuristics/RulesetHeuristics.csv"), "UTF-8"))
		{
			for (final ScoreData data : scoreDataList)
			{
				writer.println(data);
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Data for Heuristics table
	 *
	 * @author Dennis Soemers
	 */
	private static class HeuristicData
	{
		private static int nextID = 1;
		
		protected final int id;
		protected final String name;
		protected final HeuristicTypes type;
		
		public HeuristicData(final String name, final HeuristicTypes type)
		{
			this.id = nextID++;
			this.name = name;
			this.type = type;
		}
		
		@Override
		public String toString()
		{
			return id + "," + name + "," + type.ordinal();
		}
	}
	
	/**
	 * Data for the table of ruleset+heuristic scores
	 *
	 * @author Dennis Soemers
	 */
	private static class ScoreData
	{
		private static int nextID = 1;
		
		protected final int id;
		protected final int rulesetID;
		protected final int heuristicID;
		protected double score;
		
		public ScoreData(final int rulesetID, final int heuristicID, final double score)
		{
			this.id = nextID++;
			this.rulesetID = rulesetID;
			this.heuristicID = heuristicID;
			this.score = score;
		}
		
		@Override
		public String toString()
		{
			return id + "," + rulesetID + "," + heuristicID + "," + score;
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
					"Generates CSV files for database, describing scores of all base heuristics for all games."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--results-dir")
				.help("Filepath for directory with per-game subdirectories of matchup directories.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateCSVs(argParse);
	}
	
	//-------------------------------------------------------------------------

}
