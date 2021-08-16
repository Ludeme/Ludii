package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ListUtils;
import main.options.Ruleset;
import other.GameLoader;
import search.mcts.MCTS;
import search.minimax.AlphaBetaSearch;
import search.minimax.BRSPlus;
import utils.AIFactory;
import utils.DBGameInfo;
import utils.RandomAI;
import utils.analysis.BestStartingHeuristics;
import utils.analysis.BestStartingHeuristics.Entry;

/**
 * Script to find best base agent in every game.
 *
 * @author Dennis Soemers
 */
public class FindBestBaseAgentScriptsGen
{
	/** Memory to assign per CPU, in MB */
	private static final String MEM_PER_CPU = "5120";
	
	/** Memory to assign to JVM, in MB */
	private static final String JVM_MEM = "4096";
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 6000;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private FindBestBaseAgentScriptsGen()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates our scripts
	 * @param argParse
	 */
	private static void generateScripts(final CommandLineArgParse argParse)
	{
		final List<String> jobScriptNames = new ArrayList<String>();
		final AlphaBetaSearch dummyAlphaBeta = new AlphaBetaSearch();
		final BRSPlus dummyBRSPlus = (BRSPlus) AIFactory.createAI("BRS+");
		final MCTS dummyUCT = MCTS.createUCT();
		final MCTS dummyGRAVE = (MCTS) AIFactory.createAI("MC-GRAVE");
		//final MCTS dummyBiased = MCTS.createBiasedMCTS(0.0);
		//final MCTS dummyBiasedUniformPlayouts = MCTS.createBiasedMCTS(1.0);
		final MCTS dummyProgressiveHistory = (MCTS) AIFactory.createAI("Progressive History");
		final MCTS dummyMAST = (MCTS) AIFactory.createAI("MAST");
		final RandomAI dummyRandom = (RandomAI) AIFactory.createAI("Random");
		
		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		final String userName = argParse.getValueString("--user-name");
		
		final BestStartingHeuristics bestStartingHeuristics = BestStartingHeuristics.loadData();
		
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
				final String dbGameName = DBGameInfo.getUniqueName(game);
				
				final List<String> relevantAIs = new ArrayList<String>();
				
				final String alphaBetaString;
				final String alphaBetaMetadataString = null;
				
				if (dummyAlphaBeta.supportsGame(game))
				{
					final Entry abHeuristicEntry = bestStartingHeuristics.getEntry(dbGameName);
					
					if (abHeuristicEntry != null)
					{
						final String heuristic = abHeuristicEntry.topHeuristic();
						alphaBetaString = StringRoutines.join
								(
									";", 
									"algorithm=Alpha-Beta",
									"heuristics=/home/" + userName + "/FindStartingHeuristic/" + heuristic + ".txt",
									"friendly_name=AlphaBeta"
								);
						relevantAIs.add("Alpha-Beta");
					}
					else
					{
						alphaBetaString = null;
					}
					
//					final Heuristics metadataHeuristics = game.metadata().ai().heuristics();
//					if (metadataHeuristics != null)
//					{
//						// Also include AlphaBeta with existing metadata heuristics
//						alphaBetaMetadataString = StringRoutines.join
//								(
//									";", 
//									"algorithm=Alpha-Beta",
//									"friendly_name=AlphaBetaMetadata"
//								);
//						relevantAIs.add("Alpha-Beta-Metadata");
//					}
//					else
//					{
//						alphaBetaMetadataString = null;
//					}
				}
				else
				{
					alphaBetaString = null;
					//alphaBetaMetadataString = null;
				}
				
				if (dummyUCT.supportsGame(game))
					relevantAIs.add("UCT");
				
				if (dummyGRAVE.supportsGame(game))
					relevantAIs.add("MC-GRAVE");
				
//				if (dummyBiased.supportsGame(game))
//					relevantAIs.add("BiasedMCTS");
//				
//				if (dummyBiasedUniformPlayouts.supportsGame(game))
//					relevantAIs.add("BiasedMCTSUniformPlayouts");
				
				if (dummyBRSPlus.supportsGame(game))
					relevantAIs.add("BRS+");
				
				if (dummyProgressiveHistory.supportsGame(game))
					relevantAIs.add("ProgressiveHistory");
				
				if (dummyMAST.supportsGame(game))
					relevantAIs.add("MAST");
				
				if (dummyRandom.supportsGame(game))
					relevantAIs.add("Random");
				
				if (relevantAIs.size() == 0)
				{
					System.err.println("Warning! No relevant AIs for: " + gameName);
					continue;
				}
				
				if (relevantAIs.size() == 1)
				{
					System.err.println("Warning! Only one relevant AI for " + gameName + ": " + relevantAIs.get(0));
					continue;
				}
				
				final int numPlayers = game.players().count();
				
				for (int evalAgentIdx = 0; evalAgentIdx < relevantAIs.size(); ++evalAgentIdx)
				{
					final String agentToEval = relevantAIs.get(evalAgentIdx);
					
					final TIntArrayList candidateOpponentIndices = new TIntArrayList(relevantAIs.size() - 1);
					for (int i = 0; i < relevantAIs.size(); ++i)
					{
						if (i != evalAgentIdx)
							candidateOpponentIndices.add(i);
					}
					
					final int numOpponents = numPlayers - 1;
					final List<TIntArrayList> opponentCombinations = new ArrayList<TIntArrayList>();
					
					final int opponentCombLength;
					
					if (candidateOpponentIndices.size() >= numOpponents)
						opponentCombLength = numOpponents;
					else
						opponentCombLength = candidateOpponentIndices.size();
					
					ListUtils.generateAllCombinations
					(
						candidateOpponentIndices, opponentCombLength, 0, 
						new int[opponentCombLength], 
						opponentCombinations
					);
					
					while (opponentCombinations.size() > 10)
					{
						// Too many combinations of opponents; we'll randomly remove some
						ListUtils.removeSwap(opponentCombinations, ThreadLocalRandom.current().nextInt(opponentCombinations.size()));
					}
					
					if (candidateOpponentIndices.size() < numOpponents)
					{
						// We don't have enough candidates to fill up all opponent slots;
						// we'll just fill up every combination with duplicates of its last entry
						for (final TIntArrayList combination : opponentCombinations)
						{
							while (combination.size() < numOpponents)
							{
								combination.add(combination.getQuick(combination.size() - 1));
							}
						}
					}
					
					final int numCombinations = opponentCombinations.size();
					int numGamesPerComb = 10;
					
					while (numCombinations * numGamesPerComb < 50)
					{
						numGamesPerComb += 10;
					}
					
					final String jobScriptFilename = "Eval" + filepathsGameName + filepathsRulesetName + agentToEval + ".sh";
					
					try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
					{
						writer.println("#!/usr/local_rwth/bin/zsh");
						writer.println("#SBATCH -J Eval" + filepathsGameName + filepathsRulesetName + agentToEval);
						writer.println("#SBATCH -o /work/" + userName + "/FindBestBaseAgent/Out"
								+ filepathsGameName + filepathsRulesetName + agentToEval + "_%J.out");
						writer.println("#SBATCH -e /work/" + userName + "/FindBestBaseAgent/Err"
								+ filepathsGameName + filepathsRulesetName + agentToEval + "_%J.err");
						writer.println("#SBATCH -t " + MAX_WALL_TIME);
						writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
						writer.println("#SBATCH -A " + argParse.getValueString("--project"));
						writer.println("unset JAVA_TOOL_OPTIONS");
						
						for (final TIntArrayList agentsCombination : opponentCombinations)
						{
							// Add the index of the heuristic to eval in front
							agentsCombination.insert(0, evalAgentIdx);
							
							assert (agentsCombination.size() == numPlayers);
							
							final String[] agentStrings = new String[numPlayers];
							String matchupStr = "";
							for (int i = 0; i < numPlayers; ++i)
							{
								final String agent = relevantAIs.get(agentsCombination.getQuick(i));
								final String agentCommandString;
								
								if (agent.equals("Alpha-Beta"))
									agentCommandString = StringRoutines.quote(alphaBetaString);
								else if (agent.equals("Alpha-Beta-Metadata"))
									agentCommandString = StringRoutines.quote(alphaBetaMetadataString);
								else if (agent.equals("BiasedMCTS"))
									agentCommandString = StringRoutines.quote("Biased MCTS");
								else if (agent.equals("BiasedMCTSUniformPlayouts"))
									agentCommandString = StringRoutines.quote("Biased MCTS (Uniform Playouts)");
								else if (agent.equals("ProgressiveHistory"))
									agentCommandString = StringRoutines.quote("Progressive History");
								else
									agentCommandString = StringRoutines.quote(agent);
								
								agentStrings[i] = agentCommandString;
								matchupStr += agent;
							}
							
							final String javaCall = StringRoutines.join
									(
										" ", 
										"java",
										"-Xms" + JVM_MEM + "M",
										"-Xmx" + JVM_MEM + "M",
										"-XX:+HeapDumpOnOutOfMemoryError",
				                        "-da",
				                        "-dsa",
				                        "-XX:+UseStringDeduplication",
				                        "-jar",
				                        StringRoutines.quote("/home/" + userName + "/FindBestBaseAgent/Ludii.jar"),
				                        "--eval-agents",
				                        "--game",
				                        StringRoutines.quote(gameName + ".lud"),
				                        "--ruleset",
				                        StringRoutines.quote(fullRulesetName),
				                        "-n",
				                        "" + numGamesPerComb,
				                        "--game-length-cap 1000",
				                        "--thinking-time 1",
				                        "--warming-up-secs 30",
				                        "--out-dir",
				                        StringRoutines.quote
				                        (
				                        	"/work/" + 
				                        	userName + 
				                        	"/FindBestBaseAgent/" + 
				                        	filepathsGameName + filepathsRulesetName +
				                        	"/" + matchupStr + "/"
				                        ),
				                        "--agents",
				                        StringRoutines.join(" ", agentStrings),
				                        "--max-wall-time",
				                        "" + Math.max((MAX_WALL_TIME / opponentCombinations.size()), 60),
				                        "--output-alpha-rank-data",
				                        "--no-print-out",
				                        "--round-to-next-permutations-divisor"
									);
							
							writer.println(javaCall);
						}
						
						jobScriptNames.add(jobScriptFilename);
					}
					catch (final FileNotFoundException | UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		final List<List<String>> jobScriptsLists = new ArrayList<List<String>>();
		List<String> remainingJobScriptNames = jobScriptNames;

		while (remainingJobScriptNames.size() > 0)
		{
			if (remainingJobScriptNames.size() > MAX_JOBS_PER_BATCH)
			{
				final List<String> subList = new ArrayList<String>();

				for (int i = 0; i < MAX_JOBS_PER_BATCH; ++i)
				{
					subList.add(remainingJobScriptNames.get(i));
				}

				jobScriptsLists.add(subList);
				remainingJobScriptNames = remainingJobScriptNames.subList(MAX_JOBS_PER_BATCH, remainingJobScriptNames.size());
			}
			else
			{
				jobScriptsLists.add(remainingJobScriptNames);
				remainingJobScriptNames = new ArrayList<String>();
			}
		}

		for (int i = 0; i < jobScriptsLists.size(); ++i)
		{
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "SubmitJobs_Part" + i + ".sh"), "UTF-8"))
			{
				for (final String jobScriptName : jobScriptsLists.get(i))
				{
					writer.println("sbatch " + jobScriptName);
				}
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
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
					"Evaluate playing strength of different base agents against each other"
					+ " (Alpha-Beta, UCT, MC-GRAVE)."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--project")
				.help("Project for which to submit the job on cluster.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--user-name")
				.help("Username on the cluster.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--scripts-dir")
				.help("Directory in which to store generated scripts.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateScripts(argParse);
	}
	
	//-------------------------------------------------------------------------

}
