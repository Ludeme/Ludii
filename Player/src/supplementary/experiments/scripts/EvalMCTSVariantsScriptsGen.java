package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import utils.AIFactory;
import utils.RandomAI;

/**
 * Generates scripts to run on Snellius cluster to evaluate many different variants
 * of MCTS in many different games.
 *
 * @author Dennis Soemers
 */
public class EvalMCTSVariantsScriptsGen
{
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;

	/** Memory to assign to JVM, in MB (7GB per process --> give 6GB for heap) */
	private static final String JVM_MEM = "6144";
	
	/** Memory to assign per process (in GB) */
	private static final int MEM_PER_PROCESS = 7;
	
	/** Cluster doesn't seem to let us request more memory than this for any single job (on a single node) */
	private static final int MAX_REQUEST_MEM = 224;
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 1000;
	
	/** Number of cores per node (this is for Thin nodes on Snellius) */
	private static final int CORES_PER_NODE = 128;
	
	/** Number of cores per Java call */
	private static final int CORES_PER_PROCESS = 4;
	
	/** If we request more cores than this in a job, we get billed for the entire node anyway, so should request exclusive */
	private static final int EXCLUSIVE_CORES_THRESHOLD = 96;
	
	/** If we have more processes than this in a job, we get billed for the entire node anyway, so should request exclusive  */
	private static final int EXCLUSIVE_PROCESSES_THRESHOLD = EXCLUSIVE_CORES_THRESHOLD / CORES_PER_PROCESS;
	
	/**Number of processes we can put in a single job (on a single node) */
	private static final int PROCESSES_PER_JOB = CORES_PER_NODE / CORES_PER_PROCESS;
	
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
	
	/** All our hyperparameters for MCTS */
	private static final String[] mctsHyperparamNames = new String[] 
			{
				"ExplorationConstant",
				"Selection",
				"Playout",
				"ScoreBounds"
			};
	
	/** Indices for our MCTS hyperparameter types */
	private static final int IDX_EXPLORATION_CONSTANT = 0;
	private static final int IDX_SELECTION = 1;
	private static final int IDX_PLAYOUT = 2;
	private static final int IDX_SCORE_BOUNDS = 3;

	/** All the values our hyperparameters for MCTS can take */
	private static final String[][] mctsHyperParamValues = new String[][]
			{
				{"0.1", "0.6", "1.41421356237"},
				{"ProgressiveHistory", "UCB1", "UCB1GRAVE", "UCB1Tuned"},
				{"MAST", "NST", "Random200"},
				{"true", "false"}
			};
		
	/** For every MCTS hyperparameter value, an indication of whether stochastic games are supported */
	private static final boolean[][] mctsSupportsStochastic = new boolean[][]
			{
				{true, true, true},
				{true, true, true, true},
				{true, true, true},
				{false, true},
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private EvalMCTSVariantsScriptsGen()
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
		if (mctsHyperparamNames.length != 4)
		{
			System.err.println("generateMCTSCombos() code currently hardcoded for exactly 4 hyperparams.");
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
					
					// Hyperparam 4
					for (int i4 = 0; i4 < mctsHyperParamValues[3].length; ++i4)
					{
						if (supportStochasticGames && !mctsSupportsStochastic[3][i4])
							continue;
						
						combos.add(TIntArrayList.wrap(i1, i2, i3, i4));
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
		final MCTS dummyUCT = MCTS.createUCT();
		final RandomAI dummyRandom = (RandomAI) AIFactory.createAI("Random");
		
		final int[][] deterministicMCTSCombos = generateMCTSCombos(false);
		final int[][] stochasticMCTSCombos = generateMCTSCombos(true);
		
		// Construct all the strings for MCTS variants in deterministic games
		final String[] mctsNamesDeterministic = new String[deterministicMCTSCombos.length];
		final String[] mctsStringsDeterministic = new String[deterministicMCTSCombos.length];
		for (int i = 0; i < deterministicMCTSCombos.length; ++i)
		{
			final int[] combo = deterministicMCTSCombos[i];
						
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
			
			nameParts.add(mctsHyperParamValues[IDX_SCORE_BOUNDS][combo[IDX_SCORE_BOUNDS]]);
			if (mctsHyperParamValues[IDX_SCORE_BOUNDS][combo[IDX_SCORE_BOUNDS]].equals("true"))
				algStringParts.add("use_score_bounds=true");
			
			algStringParts.add("num_threads=" + CORES_PER_PROCESS);
			
			algStringParts.add("friendly_name=" + StringRoutines.join("-", nameParts));
			mctsNamesDeterministic[i] = StringRoutines.join("-", nameParts);
			mctsStringsDeterministic[i] = StringRoutines.join(";", algStringParts);
		}
		
		// all the same once more for stochastic games
		final String[] mctsNamesStochastic = new String[stochasticMCTSCombos.length];
		final String[] mctsStringsStochastic = new String[stochasticMCTSCombos.length];
		for (int i = 0; i < stochasticMCTSCombos.length; ++i)
		{
			final int[] combo = stochasticMCTSCombos[i];
						
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
			default:
				System.err.println("Unrecognised playout type: " + playoutType);
				break;
			}
			
			nameParts.add(mctsHyperParamValues[IDX_SCORE_BOUNDS][combo[IDX_SCORE_BOUNDS]]);
			if (mctsHyperParamValues[IDX_SCORE_BOUNDS][combo[IDX_SCORE_BOUNDS]].equals("true"))
				System.err.println("Should never have score bounds in MCTSes for stochastic games!");
			
			algStringParts.add("num_threads=" + CORES_PER_PROCESS);
			
			algStringParts.add("friendly_name=" + StringRoutines.join("-", nameParts));
			mctsNamesStochastic[i] = StringRoutines.join("-", nameParts);
			mctsStringsStochastic[i] = StringRoutines.join(";", algStringParts);
		}
		
		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		final String userName = argParse.getValueString("--user-name");
				
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter(s -> (
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/puzzle/deduction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/subgame/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
			)).toArray(String[]::new);
		
		long callID = 0L;
		
		// First create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		
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
								
				final List<String> relevantNonMCTSAIs = new ArrayList<String>();

				if (dummyRandom.supportsGame(game))
					relevantNonMCTSAIs.add("Random");
				
				final int numPlayers = game.players().count();
				
				if (dummyUCT.supportsGame(game))
				{
					// Evaluate all MCTSes...
					final String[] relevantMCTSNames = (game.isStochasticGame()) ? mctsNamesStochastic : mctsNamesDeterministic;
					final String[] relevantMCTSStrings = (game.isStochasticGame()) ? mctsStringsStochastic : mctsStringsDeterministic;

					for (int evalAgentIdxMCTS = 0; evalAgentIdxMCTS < relevantMCTSNames.length; ++evalAgentIdxMCTS)
					{				
						// ... against all non-MCTSes...
						for (int oppIdx = 0; oppIdx < relevantNonMCTSAIs.size(); ++oppIdx)
						{
							// Take (k - 1) copies of same opponent for a k-player game
							final String[] agentStrings = new String[numPlayers];
							for (int i = 0; i < numPlayers - 1; ++i)
							{
								final String agent = relevantNonMCTSAIs.get(oppIdx);
								final String agentCommandString = StringRoutines.quote(agent);

								agentStrings[i] = agentCommandString;
							}

							// and finally add the eval agent
							String evalAgentCommandString = relevantMCTSStrings[evalAgentIdxMCTS];

							evalAgentCommandString = StringRoutines.quote(evalAgentCommandString);

							agentStrings[numPlayers - 1] = evalAgentCommandString;
							
							processDataList.add
							(
								new ProcessData
								(
									gameName, fullRulesetName, callID++, 
									agentStrings
								)
							);
						}

						// ... and once against a randomly selected set of (k - 1) other MCTSes for a k-player game
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

								success = true;

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

						evalAgentCommandString = StringRoutines.quote(evalAgentCommandString);

						agentStrings[numPlayers - 1] = evalAgentCommandString;

						processDataList.add
						(
							new ProcessData
							(
								gameName, fullRulesetName, callID++, 
								agentStrings
							)
						);
					}
				}
			}
		}
		
		// Write scripts with all the processes
		Collections.shuffle(processDataList);

		long totalRequestedCoreHours = 0L;
		
		int processIdx = 0;
		while (processIdx < processDataList.size())
		{
			// Start a new job script
			final String jobScriptFilename = "EvalMCTSVariants_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("#SBATCH -J EvalMCTSVariants");
				writer.println("#SBATCH -p thin");
				writer.println("#SBATCH -o /home/" + userName + "/EvalMCTSVariants/Out/Out_%J.out");
				writer.println("#SBATCH -e /home/" + userName + "/EvalMCTSVariants/Out/Err_%J.err");
				writer.println("#SBATCH -t " + MAX_WALL_TIME);
				writer.println("#SBATCH -N 1");		// 1 node, no MPI/OpenMP/etc
				
				// Compute memory and core requirements
				final int numProcessesThisJob = Math.min(processDataList.size() - processIdx, PROCESSES_PER_JOB);
				final boolean exclusive = (numProcessesThisJob > EXCLUSIVE_PROCESSES_THRESHOLD);
				final int jobMemRequestGB;
				if (exclusive)
					jobMemRequestGB = MAX_REQUEST_MEM;	// We're requesting full node anyway, might as well take all the memory
				else
					jobMemRequestGB = Math.min(numProcessesThisJob * MEM_PER_PROCESS, MAX_REQUEST_MEM);
				
				writer.println("#SBATCH --cpus-per-task=" + numProcessesThisJob * CORES_PER_PROCESS);
				writer.println("#SBATCH --mem=" + jobMemRequestGB + "G");		// 1 node, no MPI/OpenMP/etc
				
				totalRequestedCoreHours += (CORES_PER_NODE * (MAX_WALL_TIME / 60));
				
				if (exclusive)
					writer.println("#SBATCH --exclusive");
				
				// load Java modules
				writer.println("module load 2021");
				writer.println("module load Java/11.0.2");
				
				// Put up to PROCESSES_PER_JOB processes in this job
				int numJobProcesses = 0;
				while (numJobProcesses < PROCESSES_PER_JOB && processIdx < processDataList.size())
				{
					final ProcessData processData = processDataList.get(processIdx);
					final String filepathsGameName = 
							StringRoutines.cleanGameName(processData.gameName);
					final String filepathsRulesetName = 
							StringRoutines.cleanRulesetName(
									processData.rulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
					// Write Java call for this process
					final String javaCall = 
								generateJavaCall
								(
									userName,
									processData.gameName,
									processData.rulesetName,
									processData.agentStrings.length * 10,
									filepathsGameName,
									filepathsRulesetName,
									processData.callID,
									processData.agentStrings,
									numJobProcesses
								);
					
					writer.println(javaCall);
					
					++processIdx;
					++numJobProcesses;
				}
				
				writer.println("wait");		// Wait for all the parallel processes to finish

				jobScriptNames.add(jobScriptFilename);
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		System.out.println("Total requested core hours = " + totalRequestedCoreHours);
		
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
	
	/**
	 * @param userName
	 * @param gameName
	 * @param fullRulesetName
	 * @param numTrialsPerOpponent
	 * @param filepathsGameName
	 * @param filepathsRulesetName
	 * @param callID
	 * @param agentStrings
	 * @param numJobProcesses
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
		final String[] agentStrings,
		final int numJobProcesses
	)
	{
		return StringRoutines.join
				(
					" ", 
					"taskset",			// Assign specific core to each process
					"-c",
					StringRoutines.join
					(
						",", 
						String.valueOf(numJobProcesses * 4), 
						String.valueOf(numJobProcesses * 4 + 1),
						String.valueOf(numJobProcesses * 4 + 2),
						String.valueOf(numJobProcesses * 4 + 3)
					),
					"java",
					"-Xms" + JVM_MEM + "M",
					"-Xmx" + JVM_MEM + "M",
					"-XX:+HeapDumpOnOutOfMemoryError",
					"-da",
					"-dsa",
					"-XX:+UseStringDeduplication",
					"-jar",
					StringRoutines.quote("/home/" + userName + "/EvalMCTSVariants/Ludii.jar"),
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
						"/home/" + 
						userName + 
						"/EvalMCTSVariants/Out/" + 
						filepathsGameName + filepathsRulesetName +
						"/" + callID + "/"
					),
					"--agents",
					StringRoutines.join(" ", agentStrings),
					"--max-wall-time",
					String.valueOf(500),
					"--output-raw-results",
					"--no-print-out",
					"--suppress-divisor-warning",
					">",
					"/home/" + userName + "/EvalMCTSVariants/Out/Out_${SLURM_JOB_ID}_" + callID + ".out",
					"2>",
					"/home/" + userName + "/EvalMCTSVariants/Out/Err_${SLURM_JOB_ID}_" + callID + ".err",
					"&"		// Run processes in parallel
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper around data for a single process (multiple processes per job)
	 *
	 * @author Dennis Soemers
	 */
	private static class ProcessData
	{
		public final String gameName;
		public final String rulesetName;
		public final long callID;
		public final String[] agentStrings;
		
		/**
		 * Constructor
		 * @param gameName
		 * @param rulesetName
		 * @param callID
		 * @param agentStrings
		 */
		public ProcessData
		(
			final String gameName, 
			final String rulesetName, 
			final long callID,
			final String[] agentStrings
		)
		{
			this.gameName = gameName;
			this.rulesetName = rulesetName;
			this.callID = callID;
			this.agentStrings = agentStrings;
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
					"Generate Snellius scripts to evaluate different MCTS combinations."
				);
		
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
