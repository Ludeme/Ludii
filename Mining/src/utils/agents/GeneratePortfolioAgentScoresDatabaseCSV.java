package utils.agents;

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
 * Generates CSV files for database, describing scores of all building blocks
 * of agents for portfolio.
 *
 * @author Dennis Soemers
 */
public class GeneratePortfolioAgentScoresDatabaseCSV
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private GeneratePortfolioAgentScoresDatabaseCSV()
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
				
//				if (game.isDeductionPuzzle())
//					continue;
//				
//				if (game.isSimulationMoveGame())
//					continue;
//				
//				if (!game.isAlternatingMoveGame())
//					continue;
//				
//				if (game.hasSubgames())
//					continue;
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final File rulesetResultsDir = new File(resultsDir + filepathsGameName + filepathsRulesetName);
				if (rulesetResultsDir.exists())
				{
					final int rulesetID = IdRuleset.get(game);
					
					// Map from agent names to sum of scores for this ruleset
					final TObjectDoubleMap<String> agentScoreSums = new TObjectDoubleHashMap<String>();
					// Map from agent names to how often we observed this heuristic in this ruleset
					final TObjectIntMap<String> agentCounts = new TObjectIntHashMap<String>();
					
					final File[] jobDirs = rulesetResultsDir.listFiles();
					for (final File jobDir : jobDirs)
					{
						if (jobDir.isDirectory())
						{
							final String[] resultLines = 
									FileHandling.loadTextContentsFromFile(
											jobDir.getAbsolutePath() + "/alpha_rank_data.csv"
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
									final String agentName = agentNames[j].replaceAll(Pattern.quote("'"), "");
									
									if (Double.parseDouble(scores[j]) < -1.0 || Double.parseDouble(scores[j]) > 1.0)
									{
										System.out.println(scores[j]);
										System.out.println("Line " + i + " of " + jobDir.getAbsolutePath() + "/alpha_rank_data.csv");
									}
									
									// Convert score to "win percentage"
									final double score = ((Double.parseDouble(scores[j]) + 1.0) / 2.0) * 100.0;
									
									agentScoreSums.adjustOrPutValue(agentName, score, score);
									agentCounts.adjustOrPutValue(agentName, 1, 1);
								}
							}
						}
					}
					
					final List<ScoreData> rulesetScoreData = new ArrayList<ScoreData>();
					
					for (final String agent : agentScoreSums.keySet())
					{
						final double score = agentScoreSums.get(agent) / agentCounts.get(agent);
						rulesetScoreData.add(new ScoreData(rulesetID, agent, score, agentCounts.get(agent)));
					}
					
					scoreDataList.addAll(rulesetScoreData);
				}
			}
		}
		
		try (final PrintWriter writer = new PrintWriter(new File("../Mining/res/agents/RulesetPortfolioAgents.csv"), "UTF-8"))
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
	 * Data for the table of ruleset+agent scores
	 *
	 * @author Dennis Soemers
	 */
	private static class ScoreData
	{
		private static int nextID = 1;
		
		protected final int id;
		protected final int rulesetID;
		protected final String agent;
		protected double score;
		protected int numMatchups;
		
		public ScoreData(final int rulesetID, final String agent, final double score, final int numMatchups)
		{
			id = nextID++;
			this.rulesetID = rulesetID;
			this.agent = agent;
			this.score = score;
			this.numMatchups = numMatchups;
		}
		
		@Override
		public String toString()
		{
			final String[] agentParts = Arrays.copyOf(agent.split(Pattern.quote("-")), 5);
			for (int i = 0; i < agentParts.length; ++i)
			{
				if (agentParts[i] == null)
					agentParts[i] = "NULL";
			}
			return id + "," + rulesetID + "," + StringRoutines.join(",", agentParts) + "," + score + "," + numMatchups;
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
					"Generates CSV files for database, describing scores of all building blocks for agents for portfolio."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--results-dir")
				.help("Filepath for directory with per-game subdirectories.")
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
