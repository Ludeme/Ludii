package supplementary.experiments.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import agentPrediction.external.AgentPredictionExternal;
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
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import other.GameLoader;
import search.mcts.MCTS;
import search.minimax.AlphaBetaSearch;
import search.minimax.BRSPlus;
import utils.AIFactory;
import utils.AIUtils;
import utils.RandomAI;

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
	private static final int MAX_WALL_TIME = 7000;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;
	
	/** Max number of trials we want to be running from a single script */
	private static final int NUM_TRIALS_PER_SCRIPT = 150;
	
	/** All our hyperparameters for MCTS */
	private static final String[] mctsHyperparamNames = new String[] 
			{
				"ExplorationConstant",
				"Selection",
				"Backpropagation",
				"Playout",
				"ScoreBounds"
			};
	
	/** Indices for our MCTS hyperparameter types */
	private static final int IDX_EXPLORATION_CONSTANT = 0;
	private static final int IDX_SELECTION = 1;
	private static final int IDX_BACKPROPAGATION = 2;
	private static final int IDX_PLAYOUT = 3;
	private static final int IDX_SCORE_BOUNDS = 4;

	/** All the values our hyperparameters for MCTS can take */
	private static final String[][] mctsHyperParamValues = new String[][]
			{
				{"0.1", "0.6", "1.41421356237"},
				{"ProgressiveBias", "ProgressiveHistory", "UCB1", "UCB1GRAVE", "UCB1Tuned"},
				{"AlphaGo05", "AlphaGo0", "Heuristic", "MonteCarlo", "QualitativeBonus"},
				{"MAST", "NST", "Random200", "Random4", "Random0"},
				{"true", "false"}
			};
		
	/** For every MCTS hyperparameter value, an indication of whether stochastic games are supported */
	private static final boolean[][] mctsSupportsStochastic = new boolean[][]
			{
				{true, true, true},
				{true, true, true, true, true},
				{true, true, true, true, true},
				{true, true, true, true, true},
				{false, true},
			};
			
	/** For every MCTS hyperparameter value, an indication of whether heuristics are required */
	private static final boolean[][] mctsRequiresHeuristic = new boolean[][]
			{
				{false, false, false},
				{true, false, false, false, false},
				{true, true, true, false, true},
				{false, false, false, false, false},
				{false, false},
			};
			
	/**
	 * Games we should skip since they never end anyway (in practice), but do
	 * take a long time.
	 */
	private static final String[] SKIP_GAMES = new String[]
			{
				"Chinese Checkers.lud",
				"Li'b al-'Aqil.lud",
				"Li'b al-Ghashim.lud",
				"Pagade Kayi Ata (Sixteen-handed).lud"
			};
	
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
	 * @param supportStochasticGames Do we need to support stochastic games?
	 * @return All combinations of indices for MCTS hyperparameter values
	 */
	public static int[][] generateMCTSCombos(final boolean supportStochasticGames)
	{
		if (mctsHyperparamNames.length != 5)
		{
			System.err.println("generateMCTSCombos() code currently hardcoded for exactly 5 hyperparams.");
			return null;
		}
		
		final List<TIntArrayList> combos = new ArrayList<TIntArrayList>();
		
		// Hyperparam 1
		for (int i1 = 0; i1 < mctsHyperParamValues[0].length; ++i1)
		{
			if (supportStochasticGames && !mctsSupportsStochastic[0][i1])
				continue;
			
			// Hyperparam 2
			for (int i2 = 0; i2 < mctsHyperParamValues[1].length; ++i2)
			{
				if (supportStochasticGames && !mctsSupportsStochastic[1][i2])
					continue;
				
				// Hyperparam 3
				for (int i3 = 0; i3 < mctsHyperParamValues[2].length; ++i3)
				{
					if (supportStochasticGames && !mctsSupportsStochastic[2][i3])
						continue;
					
					final boolean alphaGo0Backprop = (mctsHyperParamValues[2][i3].equals("AlphaGo0"));
					
					// Hyperparam 4
					for (int i4 = 0; i4 < mctsHyperParamValues[3].length; ++i4)
					{
						if (supportStochasticGames && !mctsSupportsStochastic[3][i4])
							continue;
						
						// With AlphaGo-0 backprops, we only support 0-length playouts
						if (alphaGo0Backprop && !(mctsHyperParamValues[3][i4].equals("Random0")))
							continue;
						
						// Hyperparam 5
						for (int i5 = 0; i5 < mctsHyperParamValues[4].length; ++i5)
						{
							if (supportStochasticGames && !mctsSupportsStochastic[4][i5])
								continue;
							
							combos.add(TIntArrayList.wrap(i1, i2, i3, i4, i5));
						}
					}
				}
			}
		}
		
		final int[][] ret = new int[combos.size()][];
		for (int i = 0; i < ret.length; ++i)
		{
			ret[i] = combos.get(i).toArray();
		}
		
		return ret;
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
		//final MCTS dummyBiased = MCTS.createBiasedMCTS(0.0);
		//final MCTS dummyBiasedUniformPlayouts = MCTS.createBiasedMCTS(1.0);
		final RandomAI dummyRandom = (RandomAI) AIFactory.createAI("Random");
		
		final int[][] deterministicMCTSCombos = generateMCTSCombos(false);
		final int[][] stochasticMCTSCombos = generateMCTSCombos(true);
		
		// Construct all the strings for MCTS variants in deterministic games
		final String[] mctsNamesDeterministic = new String[deterministicMCTSCombos.length];
		final String[] mctsStringsDeterministic = new String[deterministicMCTSCombos.length];
		final boolean[] mctsRequiresHeuristicsDeterministic = new boolean[deterministicMCTSCombos.length];
		for (int i = 0; i < deterministicMCTSCombos.length; ++i)
		{
			final int[] combo = deterministicMCTSCombos[i];
			mctsRequiresHeuristicsDeterministic[i] = 
					mctsRequiresHeuristic[0][combo[0]] ||
					mctsRequiresHeuristic[1][combo[1]] ||
					mctsRequiresHeuristic[2][combo[2]] ||
					mctsRequiresHeuristic[3][combo[3]] ||
					mctsRequiresHeuristic[4][combo[4]];
						
			final List<String> nameParts = new ArrayList<String>();
			final List<String> algStringParts = new ArrayList<String>();
			
			nameParts.add("MCTS");
			algStringParts.add("algorithm=MCTS");
			algStringParts.add("tree_reuse=true");
			
			final String selectionType = mctsHyperParamValues[IDX_SELECTION][combo[IDX_SELECTION]];
			final String explorationConstant = mctsHyperParamValues[IDX_EXPLORATION_CONSTANT][combo[IDX_EXPLORATION_CONSTANT]];
			nameParts.add(selectionType);
			nameParts.add(explorationConstant);
			algStringParts.add("selection=" + selectionType + ",explorationconstant=" + explorationConstant);
			
			String qinitString = "PARENT";
			if (Double.parseDouble(explorationConstant) == 0.1)
				qinitString = "INF";
			else if (selectionType.equals("ProgressiveBias"))
				qinitString = "INF";
			algStringParts.add("qinit=" + qinitString);
			
			final String playoutType = mctsHyperParamValues[IDX_PLAYOUT][combo[IDX_PLAYOUT]];
			nameParts.add(playoutType);
			switch (playoutType)
			{
			case "MAST":
				algStringParts.add("playout=mast,playoutturnlimit=200");
				break;
			case "NST":
				algStringParts.add("playout=nst,playoutturnlimit=200");
				break;
			case "Random200":
				algStringParts.add("playout=random,playoutturnlimit=200");
				break;
			case "Random4":
				algStringParts.add("playout=random,playoutturnlimit=4");
				break;
			case "Random0":
				algStringParts.add("playout=random,playoutturnlimit=0");
				break;
			default:
				System.err.println("Unrecognised playout type: " + playoutType);
				break;
			}
			
			final String backpropType = mctsHyperParamValues[IDX_BACKPROPAGATION][combo[IDX_BACKPROPAGATION]];
			nameParts.add(backpropType);
			switch (backpropType)
			{
			case "AlphaGo05":
				algStringParts.add("backprop=alphago");
				algStringParts.add("playout_value_weight=0.5");
				break;
			case "AlphaGo0":
				algStringParts.add("backprop=alphago");
				algStringParts.add("playout_value_weight=0.0");
				break;
			case "Heuristic":
				algStringParts.add("backprop=heuristic");
				break;
			case "MonteCarlo":
				algStringParts.add("backprop=montecarlo");
				break;
			case "QualitativeBonus":
				algStringParts.add("backprop=qualitativebonus");
				break;
			default:
				System.err.println("Unrecognised backprop type: " + backpropType);
				break;
			}
			
			if (mctsHyperParamValues[IDX_SCORE_BOUNDS][combo[IDX_SCORE_BOUNDS]].equals("true"))
				algStringParts.add("use_score_bounds=true");
			
			algStringParts.add("friendly_name=" + StringRoutines.join("-", nameParts));
			mctsNamesDeterministic[i] = StringRoutines.join("-", nameParts);
			mctsStringsDeterministic[i] = StringRoutines.join(";", algStringParts);
		}
		
		// all the same once more for stochastic games
		final String[] mctsNamesStochastic = new String[stochasticMCTSCombos.length];
		final String[] mctsStringsStochastic = new String[stochasticMCTSCombos.length];
		final boolean[] mctsRequiresHeuristicsStochastic = new boolean[stochasticMCTSCombos.length];
		for (int i = 0; i < stochasticMCTSCombos.length; ++i)
		{
			final int[] combo = stochasticMCTSCombos[i];
			mctsRequiresHeuristicsStochastic[i] = 
					mctsRequiresHeuristic[0][combo[0]] ||
					mctsRequiresHeuristic[1][combo[1]] ||
					mctsRequiresHeuristic[2][combo[2]] ||
					mctsRequiresHeuristic[3][combo[3]] ||
					mctsRequiresHeuristic[4][combo[4]];
						
			final List<String> nameParts = new ArrayList<String>();
			final List<String> algStringParts = new ArrayList<String>();
			
			nameParts.add("MCTS");
			algStringParts.add("algorithm=MCTS");
			algStringParts.add("tree_reuse=true");
			
			final String selectionType = mctsHyperParamValues[IDX_SELECTION][combo[IDX_SELECTION]];
			final String explorationConstant = mctsHyperParamValues[IDX_EXPLORATION_CONSTANT][combo[IDX_EXPLORATION_CONSTANT]];
			nameParts.add(selectionType);
			nameParts.add(explorationConstant);
			algStringParts.add("selection=" + selectionType + ",explorationconstant=" + explorationConstant);
			
			String qinitString = "PARENT";
			if (Double.parseDouble(explorationConstant) == 0.0)
				qinitString = "INF";
			else if (selectionType.equals("ProgressiveBias"))
				qinitString = "INF";
			algStringParts.add("qinit=" + qinitString);
			
			final String playoutType = mctsHyperParamValues[IDX_PLAYOUT][combo[IDX_PLAYOUT]];
			nameParts.add(playoutType);
			switch (playoutType)
			{
			case "MAST":
				algStringParts.add("playout=mast,playoutturnlimit=200");
				break;
			case "NST":
				algStringParts.add("playout=nst,playoutturnlimit=200");
				break;
			case "Random200":
				algStringParts.add("playout=random,playoutturnlimit=200");
				break;
			case "Random4":
				algStringParts.add("playout=random,playoutturnlimit=4");
				break;
			case "Random0":
				algStringParts.add("playout=random,playoutturnlimit=0");
				break;
			default:
				System.err.println("Unrecognised playout type: " + playoutType);
				break;
			}
			
			final String backpropType = mctsHyperParamValues[IDX_BACKPROPAGATION][combo[IDX_BACKPROPAGATION]];
			nameParts.add(backpropType);
			switch (backpropType)
			{
			case "AlphaGo05":
				algStringParts.add("backprop=alphago");
				algStringParts.add("playout_value_weight=0.5");
				break;
			case "AlphaGo0":
				algStringParts.add("backprop=alphago");
				algStringParts.add("playout_value_weight=0.0");
				break;
			case "Heuristic":
				algStringParts.add("backprop=heuristic");
				break;
			case "MonteCarlo":
				algStringParts.add("backprop=montecarlo");
				break;
			case "QualitativeBonus":
				algStringParts.add("backprop=qualitativebonus");
				break;
			default:
				System.err.println("Unrecognised backprop type: " + backpropType);
				break;
			}
			
			if (mctsHyperParamValues[IDX_SCORE_BOUNDS][combo[IDX_SCORE_BOUNDS]].equals("true"))
				System.err.println("Should never have score bounds in MCTSes for stochastic games!");
			
			algStringParts.add("friendly_name=" + StringRoutines.join("-", nameParts));
			mctsNamesStochastic[i] = StringRoutines.join("-", nameParts);
			mctsStringsStochastic[i] = StringRoutines.join(";", algStringParts);
		}
		
		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		final String userName = argParse.getValueString("--user-name");
		
		//final BestStartingHeuristics bestStartingHeuristics = BestStartingHeuristics.loadData();
		
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
		
		long callID = 0L;
		
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
				
				final String bestPredictedHeuristicName = predictBestHeuristic(game);
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				//final String dbGameName = DBGameInfo.getUniqueName(game);
				//final Entry abHeuristicEntry = bestStartingHeuristics.getEntry(dbGameName);
				
				final List<String> relevantNonMCTSAIs = new ArrayList<String>();
				
				final String alphaBetaString;
				final String brsPlusString;
				
				if (dummyAlphaBeta.supportsGame(game))
				{		
					if (bestPredictedHeuristicName != null)
					{
						//final String heuristic = abHeuristicEntry.topHeuristic();
						alphaBetaString = StringRoutines.join
								(
									";", 
									"algorithm=Alpha-Beta",
									"heuristics=/home/" + userName + "/FindStartingHeuristic/" + bestPredictedHeuristicName + ".txt",
									"friendly_name=AlphaBeta"
								);
						relevantNonMCTSAIs.add("Alpha-Beta");
					}
					else
					{
						System.err.println("Null predicted heuristic!");
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
				
				if (dummyBRSPlus.supportsGame(game))
				{
					if (bestPredictedHeuristicName != null)
					{
						//final String heuristic = abHeuristicEntry.topHeuristic();
						brsPlusString = StringRoutines.join
								(
									";", 
									"algorithm=BRS+",
									"heuristics=/home/" + userName + "/FindStartingHeuristic/" + bestPredictedHeuristicName + ".txt",
									"friendly_name=BRS+"
								);
						relevantNonMCTSAIs.add("BRS+");
					}
					else
					{
						brsPlusString = null;
					}
				}
				else
				{
					brsPlusString = null;
				}
				
				if (dummyRandom.supportsGame(game))
					relevantNonMCTSAIs.add("Random");
				
				final int numPlayers = game.players().count();
				
				int numTrialsThisJob = 0;
				int jobCounterThisGame = 0;
				
				@SuppressWarnings("resource")	// Not using try-catch to control this resource because we need to sometimes switch to different files
				PrintWriter writer = null;
				
				try
				{
					// Evaluate all of the non-MCTSes against each other
					for (int evalAgentIdxNonMCTS = 0; evalAgentIdxNonMCTS < relevantNonMCTSAIs.size(); ++evalAgentIdxNonMCTS)
					{
						final String agentToEval = relevantNonMCTSAIs.get(evalAgentIdxNonMCTS);
						
						for (int oppIdx = 0; oppIdx < relevantNonMCTSAIs.size(); ++oppIdx)
						{
							if (writer == null || numTrialsThisJob + numPlayers > NUM_TRIALS_PER_SCRIPT)
							{
								// Time to open a new job script
								if (writer != null)
								{
									writer.close();
									writer = null;
								}
								
								final String jobScriptFilename = "Eval" + filepathsGameName + filepathsRulesetName + jobCounterThisGame + ".sh";
								jobScriptNames.add(jobScriptFilename);
								numTrialsThisJob = 0;
								writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8");
								writer.println("#!/usr/local_rwth/bin/zsh");
								writer.println("#SBATCH -J Eval" + filepathsGameName + filepathsRulesetName + jobCounterThisGame);
								writer.println("#SBATCH -o /work/" + userName + "/FindBestBaseAgent/Out"
										+ filepathsGameName + filepathsRulesetName + jobCounterThisGame + "_%J.out");
								writer.println("#SBATCH -e /work/" + userName + "/FindBestBaseAgent/Err"
										+ filepathsGameName + filepathsRulesetName + jobCounterThisGame + "_%J.err");
								writer.println("#SBATCH -t " + MAX_WALL_TIME);
								writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
								writer.println("#SBATCH -A " + argParse.getValueString("--project"));
								writer.println("unset JAVA_TOOL_OPTIONS");
								++jobCounterThisGame;
							}
							
							numTrialsThisJob += numPlayers;
							
							// Take (k - 1) copies of same opponent for a k-player game
							final String[] agentStrings = new String[numPlayers];
							for (int i = 0; i < numPlayers - 1; ++i)
							{
								final String agent = relevantNonMCTSAIs.get(oppIdx);
								final String agentCommandString;
								
								if (agent.equals("Alpha-Beta"))
									agentCommandString = StringRoutines.quote(alphaBetaString);
								else if (agent.equals("BRS+"))
									agentCommandString = StringRoutines.quote(brsPlusString);
								else
									agentCommandString = StringRoutines.quote(agent);
								
								agentStrings[i] = agentCommandString;
							}
							
							// and finally add the eval agent
							final String evalAgentCommandString;
							
							if (agentToEval.equals("Alpha-Beta"))
								evalAgentCommandString = StringRoutines.quote(alphaBetaString);
							else if (agentToEval.equals("BRS+"))
								evalAgentCommandString = StringRoutines.quote(brsPlusString);
							else
								evalAgentCommandString = StringRoutines.quote(agentToEval);
							
							agentStrings[numPlayers - 1] = evalAgentCommandString;
							
							final String javaCall = generateJavaCall
									(
										userName,
										gameName,
										fullRulesetName,
										numPlayers,
										filepathsGameName,
										filepathsRulesetName,
										callID,
										agentStrings
									);
							++callID;
							
							writer.println(javaCall);
						}
					}
					
					if (dummyUCT.supportsGame(game))
					{
						// Evaluate all MCTSes...
						final String[] relevantMCTSNames = (game.isStochasticGame()) ? mctsNamesStochastic : mctsNamesDeterministic;
						final String[] relevantMCTSStrings = (game.isStochasticGame()) ? mctsStringsStochastic : mctsStringsDeterministic;
						final boolean[] relevantMCTSHeuristicRequirements = (game.isStochasticGame()) ? mctsRequiresHeuristicsStochastic : mctsRequiresHeuristicsDeterministic;
						
						for (int evalAgentIdxMCTS = 0; evalAgentIdxMCTS < relevantMCTSNames.length; ++evalAgentIdxMCTS)
						{				
							// ... against all non-MCTSes...
							for (int oppIdx = 0; oppIdx < relevantNonMCTSAIs.size(); ++oppIdx)
							{
								if (writer == null || numTrialsThisJob + numPlayers > NUM_TRIALS_PER_SCRIPT)
								{
									// Time to open a new job script
									if (writer != null)
									{
										writer.close();
										writer = null;
									}
									
									final String jobScriptFilename = "Eval" + filepathsGameName + filepathsRulesetName + jobCounterThisGame + ".sh";
									jobScriptNames.add(jobScriptFilename);
									numTrialsThisJob = 0;
									writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8");
									writer.println("#!/usr/local_rwth/bin/zsh");
									writer.println("#SBATCH -J Eval" + filepathsGameName + filepathsRulesetName + jobCounterThisGame);
									writer.println("#SBATCH -o /work/" + userName + "/FindBestBaseAgent/Out"
											+ filepathsGameName + filepathsRulesetName + jobCounterThisGame + "_%J.out");
									writer.println("#SBATCH -e /work/" + userName + "/FindBestBaseAgent/Err"
											+ filepathsGameName + filepathsRulesetName + jobCounterThisGame + "_%J.err");
									writer.println("#SBATCH -t " + MAX_WALL_TIME);
									writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
									writer.println("#SBATCH -A " + argParse.getValueString("--project"));
									writer.println("unset JAVA_TOOL_OPTIONS");
									++jobCounterThisGame;
								}
								
								numTrialsThisJob += numPlayers;
								
								// Take (k - 1) copies of same opponent for a k-player game
								final String[] agentStrings = new String[numPlayers];
								for (int i = 0; i < numPlayers - 1; ++i)
								{
									final String agent = relevantNonMCTSAIs.get(oppIdx);
									final String agentCommandString;
									
									if (agent.equals("Alpha-Beta"))
										agentCommandString = StringRoutines.quote(alphaBetaString);
									else if (agent.equals("BRS+"))
										agentCommandString = StringRoutines.quote(brsPlusString);
									else
										agentCommandString = StringRoutines.quote(agent);
									
									agentStrings[i] = agentCommandString;
								}
								
								// and finally add the eval agent
								String evalAgentCommandString = relevantMCTSStrings[evalAgentIdxMCTS];
								
								if (relevantMCTSHeuristicRequirements[evalAgentIdxMCTS])
								{
									if (bestPredictedHeuristicName != null)
									{
										evalAgentCommandString += ";heuristics=/home/" + userName + "/FindStartingHeuristic/" + bestPredictedHeuristicName + ".txt";
									}
									else
									{
										// Our MCTS requires heuristics but we don't have any, so skip
										continue;
									}
								}
								
								evalAgentCommandString = StringRoutines.quote(evalAgentCommandString);
								
								agentStrings[numPlayers - 1] = evalAgentCommandString;
								
								final String javaCall = generateJavaCall
										(
											userName,
											gameName,
											fullRulesetName,
											numPlayers,
											filepathsGameName,
											filepathsRulesetName,
											callID,
											agentStrings
										);
								++callID;
								
								writer.println(javaCall);
							}
							
							// ... and once against a randomly selected set of (k - 1) other MCTSes for a k-player game
							if (writer == null || numTrialsThisJob + numPlayers > NUM_TRIALS_PER_SCRIPT)
							{
								// Time to open a new job script
								if (writer != null)
								{
									writer.close();
									writer = null;
								}
								
								final String jobScriptFilename = "Eval" + filepathsGameName + filepathsRulesetName + jobCounterThisGame + ".sh";
								jobScriptNames.add(jobScriptFilename);
								numTrialsThisJob = 0;
								writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8");
								writer.println("#!/usr/local_rwth/bin/zsh");
								writer.println("#SBATCH -J Eval" + filepathsGameName + filepathsRulesetName + jobCounterThisGame);
								writer.println("#SBATCH -o /work/" + userName + "/FindBestBaseAgent/Out"
										+ filepathsGameName + filepathsRulesetName + jobCounterThisGame + "_%J.out");
								writer.println("#SBATCH -e /work/" + userName + "/FindBestBaseAgent/Err"
										+ filepathsGameName + filepathsRulesetName + jobCounterThisGame + "_%J.err");
								writer.println("#SBATCH -t " + MAX_WALL_TIME);
								writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
								writer.println("#SBATCH -A " + argParse.getValueString("--project"));
								writer.println("unset JAVA_TOOL_OPTIONS");
								++jobCounterThisGame;
							}
							
							numTrialsThisJob += numPlayers;
							
							final String[] agentStrings = new String[numPlayers];
							for (int i = 0; i < numPlayers - 1; ++i)
							{
								boolean success = false;
								
								final TIntArrayList sampleIndices = ListUtils.range(relevantMCTSNames.length);
								while (!success)
								{
									final int randIdx = ThreadLocalRandom.current().nextInt(sampleIndices.size());
									final int otherMCTSIdx = sampleIndices.getQuick(randIdx);
									ListUtils.removeSwap(sampleIndices, randIdx);
									String agentCommandString = relevantMCTSStrings[otherMCTSIdx];
									
									if (relevantMCTSHeuristicRequirements[otherMCTSIdx])
									{
										if (bestPredictedHeuristicName != null)
										{
											agentCommandString += ";heuristics=/home/" + userName + "/FindStartingHeuristic/" + bestPredictedHeuristicName + ".txt";
											success = true;
										}
										else
										{
											// Our MCTS requires heuristics but we don't have any, so try again
											//System.out.println("no heuristics in game: " + dbGameName);
											//System.out.println("picked index " + otherMCTSIdx + " out of " + relevantMCTSNames.length + " options");
											continue;
										}
									}
									else
									{
										success = true;
									}
									
									agentCommandString = StringRoutines.quote(agentCommandString);
									
									agentStrings[i] = agentCommandString;
								}
								
								if (!success)
								{
									System.err.println("No suitable MCTS found at all");
									continue;
								}
							}
							
							// add the eval agent
							String evalAgentCommandString = relevantMCTSStrings[evalAgentIdxMCTS];
							if (relevantMCTSHeuristicRequirements[evalAgentIdxMCTS])
							{
								if (bestPredictedHeuristicName != null)
								{
									evalAgentCommandString += ";heuristics=/home/" + userName + "/FindStartingHeuristic/" + bestPredictedHeuristicName + ".txt";
								}
								else
								{
									// Our MCTS requires heuristics but we dont have any, so skip
									continue;
								}
							}
							
							evalAgentCommandString = StringRoutines.quote(evalAgentCommandString);
							
							agentStrings[numPlayers - 1] = evalAgentCommandString;
							
							final String javaCall = generateJavaCall
									(
										userName,
										gameName,
										fullRulesetName,
										numPlayers,
										filepathsGameName,
										filepathsRulesetName,
										callID,
										agentStrings
									);
							++callID;
							
							writer.println(javaCall);
						}
					}
					
					if (writer != null)
					{
						writer.close();
						writer = null;
					}
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		final List<List<String>> jobScriptsLists = new ArrayList<List<String>>();
		List<String> remainingJobScriptNames = jobScriptNames;
		Collections.shuffle(remainingJobScriptNames);

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
	
	public static String predictBestHeuristic(final Game game)
	{
		final boolean useClassifier = true;
		final boolean useCompilationOnly = true;

		String modelFilePath = "RandomForestClassifier";

		if (useClassifier)
			modelFilePath += "-Classification";
		else
			modelFilePath += "-Regression";

		modelFilePath += "-Heuristics";

		if (useCompilationOnly)
			modelFilePath += "-True";
		else
			modelFilePath += "-False";
		
		String sInput = null;
		String sError = null;

        try 
        {
        	final String conceptNameString = "RulesetName," + AgentPredictionExternal.conceptNameString(useCompilationOnly);
        	final String conceptValueString = "UNUSED," + AgentPredictionExternal.conceptValueString(game, useCompilationOnly);
        	
        	final String arg1 = modelFilePath;
        	final String arg2 = "Classification";
        	final String arg3 = conceptNameString;
        	final String arg4 = conceptValueString;
        	final Process p = Runtime.getRuntime().exec("conda.bat activate DefaultEnv && python3 ../../LudiiPrivate/DataMiningScripts/Sklearn/External/GetBestPredictedAgent.py " + arg1 + " " + arg2 + " " + arg3 + " " + arg4);

        	// Read file output
        	final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        	while ((sInput = stdInput.readLine()) != null) 
        	{
        		System.out.println(sInput);
        		if (sInput.contains("PREDICTION"))
        		{
        			// Check if returning probabilities for each class.
        			try
        			{
        				final String[] classNamesAndProbas = sInput.split("=")[1].split("_:_");
        				final String[] classNames = classNamesAndProbas[0].split("_;_");
        				for (int i = 0; i < classNames.length; i++)
        					classNames[i] = classNames[i];
        				final String[] valueStrings = classNamesAndProbas[1].split("_;_");
        				final double[] values = new double[valueStrings.length];
        				for (int i = 0; i < valueStrings.length; i++)
        					values[i] = Double.parseDouble(valueStrings[i]);
        				if (classNames.length != values.length)
        					System.out.println("ERROR! Class Names and Values should be the same length.");

        				double highestProbabilityValue = -1.0;
        				String highestProbabilityName = "Random";
        				for (int i = 0; i < classNames.length; i++)
        				{
        					if (values[i] > highestProbabilityValue)
        					{
        						// Check that the heuristic is actually applicable
        						final Heuristics heuristics = AIUtils.convertStringtoHeuristic(classNames[i]);
        						boolean applicable = true;
        						for (final HeuristicTerm term : heuristics.heuristicTerms())
        						{
        							if (!term.isApplicable(game))
        							{
        								applicable = false;
        								break;
        							}
        						}
        						
        						if (applicable)
        						{
        							highestProbabilityValue = values[i];
	        						highestProbabilityName = classNames[i];
        						}
	        						
        					}
        				}

        				return highestProbabilityName;
        			}
        			catch (final Exception e)
        			{
        				return sInput.split("=")[1];
        			}
        		}	
        	}

        	// Read any errors.
        	final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        	while ((sError = stdError.readLine()) != null) 
        	{
        		System.out.println("Python Error\n");
        		System.out.println(sError);
        	}
        }
        catch (final IOException e) 
        {
            e.printStackTrace();
        }
        
        return null;
	}
	
	/**
	 * @param userName
	 * @param gameName
	 * @param fullRulesetName
	 * @param numTrialsPerOpponent
	 * @param filepathsGameName
	 * @param filepathsRulesetName
	 * @param callID
	 * @param agentStrings
	 * @return A complete string for a Java call
	 */
	public static String generateJavaCall
	(
		final String userName,
		final String gameName,
		final String fullRulesetName,
		final int numTrialsPerOpponent,
		final String filepathsGameName,
		final String filepathsRulesetName,
		final long callID,
		final String[] agentStrings
	)
	{
		return StringRoutines.join
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
					String.valueOf(numTrialsPerOpponent),
					"--game-length-cap 1000",
					"--thinking-time 1",
					"--iteration-limit 100000",				// 100K iterations per move should be good enough
					"--warming-up-secs 10",
					"--out-dir",
					StringRoutines.quote
					(
						"/work/" + 
						userName + 
						"/FindBestBaseAgent/" + 
						filepathsGameName + filepathsRulesetName +
						"/" + callID + "/"
					),
					"--agents",
					StringRoutines.join(" ", agentStrings),
					"--max-wall-time",
					String.valueOf(500),
					"--output-alpha-rank-data",
					"--no-print-out",
					"--suppress-divisor-warning"
				);
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
