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
 * Generates a CSV file with the best base agent per ruleset
 *
 * @author Dennis Soemers
 */
public class EvaluateBaseAgents
{
	/**
	 * Constructor (don't need this)
	 */
	private EvaluateBaseAgents()
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
					final List<String> rowStringList = new ArrayList<String>();
					
					// First column: just game name
					rowStringList.add(gameName);
					
					// Second column: just ruleset name
					rowStringList.add(fullRulesetName);
					
					// Third column: game + ruleset name
					rowStringList.add(DBGameInfo.getUniqueName(game));
					
					// Map from agent names to sum of scores for this ruleset
					final TObjectDoubleMap<String> agentScoreSums = new TObjectDoubleHashMap<String>();
					// Map from agent names to how often we observed this agent in this ruleset
					final TObjectIntMap<String> agentCounts = new TObjectIntHashMap<String>();
					
					final File[] matchupDirs = rulesetResultsDir.listFiles();
					for (final File matchupDir : matchupDirs)
					{
						if (matchupDir.isDirectory())
						{
							final String[] resultLines = 
									FileHandling.loadTextContentsFromFile
									(
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
								
								final String agentsTuple = 
										line
										.substring(idxQuote1 + 2, idxQuote2 - 1)
										.replaceAll(Pattern.quote(" "), "")
										.replaceAll(Pattern.quote("'"), "");
								final String scoresTuple = 
										line
										.substring(idxQuote3 + 2, idxQuote4 - 1)
										.replaceAll(Pattern.quote(" "), "");
								
								final String[] agentNames = agentsTuple.split(Pattern.quote(","));
								final String[] scores = scoresTuple.split(Pattern.quote(","));
								
								for (int j = 0; j < agentNames.length; ++j)
								{
									// Convert score to "win percentage"
									final double score = ((Double.parseDouble(scores[j]) + 1.0) / 2.0) * 100.0;
									
									agentScoreSums.adjustOrPutValue(agentNames[j], score, score);
									agentCounts.adjustOrPutValue(agentNames[j], 1, 1);
								}
							}
						}
					}
					
					// Figure out which agent has best score
					double bestScore = -1.0;
					String bestAgent = "";
					
					for (final String agent : agentScoreSums.keySet())
					{
						final double score = agentScoreSums.get(agent) / agentCounts.get(agent);
						if (score > bestScore)
						{
							bestScore = score;
							bestAgent = agent;
						}
					}
					
					// Fourth column: top agent
					rowStringList.add(bestAgent);
					
					// Fifth column: top score
					rowStringList.add("" + bestScore);
					
					// All strings for this row are complete
					rowStringLists.add(rowStringList);
				}
				else
				{
					System.out.println(rulesetResultsDir + " does not exist");
				}
			}
		}
		
		String outDir = argParse.getValueString("--out-dir");
		if (!outDir.endsWith("/"))
			outDir += "/";
		
		final String outFilename = "BestBaseAgents.csv";
		try (final PrintWriter writer = new PrintWriter(new File(outDir + outFilename), "UTF-8"))
		{
			// First write the headings
			final List<String> headings = new ArrayList<String>();
			headings.add("Game");
			headings.add("Ruleset");
			headings.add("GameRuleset");
			headings.add("Top Agent");
			headings.add("Top Score");
			
			writer.println(StringRoutines.join(",", headings));
			
			// Now write all the rows
			for (int i = 0; i < rowStringLists.size(); ++i)
			{
				writer.println(StringRoutines.join(",", rowStringLists.get(i)));
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
					"Generates a CSV file containing top base agent per ruleset."
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
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateCSV(argParse);
	}
	
	//-------------------------------------------------------------------------

}
