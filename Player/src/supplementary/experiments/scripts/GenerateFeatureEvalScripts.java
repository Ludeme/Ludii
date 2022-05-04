package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.regex.Pattern;

import features.spatial.Walk;
import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ArrayUtils;
import main.options.Ruleset;
import other.GameLoader;
import supplementary.experiments.analysis.RulesetConceptsUCT;
import utils.RulesetNames;

/**
 * Class with main method to automatically generate all the appropriate evaluation
 * scripts (and build decision trees) for features on the Snellius cluster.
 *
 * @author Dennis Soemers
 */
public class GenerateFeatureEvalScripts
{
	
	/** Number of threads to use for our actual job that's building decision trees and generating eval scripts */
	private static final int NUM_GENERATION_THREADS = 128;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;

	/** Memory to assign to JVM, in MB (2 GB per core --> we take 3 cores per job, 6GB per job, use 5GB for JVM) */
	private static final String JVM_MEM = "5120";
	
	/** Memory to assign per process (in GB) */
	private static final int MEM_PER_PROCESS = 6;
	
	/** Memory available per node in GB (this is for Thin nodes on Snellius) */
	private static final int MEM_PER_NODE = 256;
	
	/** Cluster doesn't seem to let us request more memory than this for any single job (on a single node) */
	private static final int MAX_REQUEST_MEM = 234;
	
	/** Max number of self-play trials */
	private static final int MAX_SELFPLAY_TRIALS = 200;
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 1445;
	
	/** Number of cores per node (this is for Thin nodes on Snellius) */
	private static final int CORES_PER_NODE = 128;
	
	/** Two cores is not enough since we want at least 5GB memory per job, so we take 3 cores (and 6GB memory) per job */
	private static final int CORES_PER_PROCESS = 3;
	
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
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private GenerateFeatureEvalScripts()
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

		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		final String userName = argParse.getValueString("--user-name");
		
		// Modify the RulesetConceptsUCT.csv filepath for running on Snellius
		RulesetConceptsUCT.FILEPATH = "/home/" + userName + "/RulesetConceptsUCT.csv";
		
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
		
		final List<String> gameNames = new ArrayList<String>();
		final List<String> rulesetNames = new ArrayList<String>();
		final TDoubleArrayList expectedTrialDurations = new TDoubleArrayList();
		final TIntArrayList playerCounts = new TIntArrayList();
		
		for (final String gameName : allGameNames)
		{
			final String[] gameNameSplit = gameName.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String shortGameName = gameNameSplit[gameNameSplit.length - 1];
			
			boolean skipGame = false;
			for (final String game : SKIP_GAMES)
			{
				if (shortGameName.endsWith(game))
				{
					skipGame = true;
					break;
				}
			}
			
			if (skipGame)
				continue;
			
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName);
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
					game = GameLoader.loadGameFromName(gameName, fullRulesetName);
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
				
				if (game.hasSubgames())
					continue;
				
				if (game.isDeductionPuzzle())
					continue;
				
				if (game.isSimulationMoveGame())
					continue;
				
				if (!game.isAlternatingMoveGame())
					continue;
				
				if (game.isStacking())
					continue;
				
				if (game.isBoardless())
					continue;
				
				if (game.hiddenInformation())
					continue;
				
				if (Walk.allGameRotations(game).length == 0)
					continue;
				
				// TODO skip games played on edges (maybe done by the Walk thing above?)
				
				final File trainingOutDir = 
						new File
						(
							"/home/" + 
							userName + 
							"/TrainFeaturesSnelliusAllGames/Out" + 
							StringRoutines.cleanGameName(("/" + shortGameName).replaceAll(Pattern.quote(".lud"), "")) 
							+ 
							"_"
							+
							StringRoutines.cleanRulesetName(fullRulesetName).replaceAll(Pattern.quote("/"), "_")
							+
							"/"
						);
				
				if (!trainingOutDir.exists() || !trainingOutDir.isDirectory())
					continue;	// No training results for this ruleset
				
				final String[] trainingOutDirFiles = trainingOutDir.list();
				
				if (trainingOutDirFiles.length == 0)
					continue;	// No training results for this ruleset
				
				boolean haveBuffers = false;
				
				for (final String s : trainingOutDirFiles)
				{
					if (s.contains("ExperienceBuffer"))
					{
						haveBuffers = true;
						break;
					}
				}
				
				if (!haveBuffers)
					continue;	// No training results for this ruleset
				
				double expectedTrialDuration = RulesetConceptsUCT.getValue(RulesetNames.gameRulesetName(game), "DurationMoves");
				if (Double.isNaN(expectedTrialDuration))
					expectedTrialDuration = Double.MAX_VALUE;
				
				gameNames.add("/" + shortGameName);
				rulesetNames.add(fullRulesetName);
				expectedTrialDurations.add(expectedTrialDuration);
				playerCounts.add(game.players().count());
			}
		}
		
		// Sort games in decreasing order of expected duration (in moves per trial)
		// This ensures that we start with the slow games, and that games of similar
		// durations are likely to end up in the same job script (and therefore run
		// on the same node at the same time).
		final List<Integer> sortedGameIndices = ArrayUtils.sortedIndices(gameNames.size(), new Comparator<Integer>()
		{

			@Override
			public int compare(final Integer i1, final Integer i2) 
			{
				final double delta = expectedTrialDurations.getQuick(i2.intValue()) - expectedTrialDurations.getQuick(i1.intValue());
				if (delta < 0.0)
					return -1;
				if (delta > 0.0)
					return 1;
				return 0;
			}
			
		});

		// First create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		for (int idx : sortedGameIndices)
		{
			processDataList.add(new ProcessData(gameNames.get(idx), rulesetNames.get(idx), playerCounts.getQuick(idx)));
		}
		
		// Build all the decision trees
		// TODO
		
		final ExecutorService executor = Executors.newFixedThreadPool(NUM_GENERATION_THREADS);
		
		try
		{
			final CountDownLatch latch = new CountDownLatch(processDataList.size());
			
			for (final ProcessData processData : processDataList)
			{
				executor.submit
				(
					() ->
					{
						try
						{
							// TODO
						}
						catch (final Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							latch.countDown();
						}
					}
				);
			}
			
			latch.await();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		
		// Now write all the job scripts
		// TODO
		final DoubleAdder totalCoreHoursRequested = new DoubleAdder();
		
		int processIdx = 0;		// TODO make this thread-safe
		while (processIdx < processDataList.size())
		{
			// Start a new job script
			final String jobScriptFilename = "TrainFeatures_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("#SBATCH -J TrainFeatures");
				writer.println("#SBATCH -p thin");
				writer.println("#SBATCH -o /home/" + userName + "/TrainFeaturesSnelliusAllGames/Out/Out_%J.out");
				writer.println("#SBATCH -e /home/" + userName + "/TrainFeaturesSnelliusAllGames/Out/Err_%J.err");
				writer.println("#SBATCH -t " + MAX_WALL_TIME);
				writer.println("#SBATCH -N 1");		// 1 node, no MPI/OpenMP/etc
				
				// Compute memory and core requirements
				final int numProcessesThisJob = Math.min(processDataList.size() - processIdx, PROCESSES_PER_JOB);
				final boolean exclusive = (numProcessesThisJob > EXCLUSIVE_PROCESSES_THRESHOLD);
				final int jobMemRequestGB;
				if (exclusive)
					jobMemRequestGB = Math.min(MEM_PER_NODE, MAX_REQUEST_MEM);	// We're requesting full node anyway, might as well take all the memory
				else
					jobMemRequestGB = Math.min(MEM_PER_NODE, MAX_REQUEST_MEM);	// We're requesting full node anyway, might as well take all the memory
					//jobMemRequestGB = Math.min(numProcessesThisJob * MEM_PER_PROCESS, MAX_REQUEST_MEM);
				
				totalCoreHoursRequested.add(CORES_PER_NODE * (MAX_WALL_TIME / 60.0));
				
				writer.println("#SBATCH --cpus-per-task=" + numProcessesThisJob * CORES_PER_PROCESS);
				writer.println("#SBATCH --mem=" + jobMemRequestGB + "G");		// 1 node, no MPI/OpenMP/etc
				
				if (exclusive)
					writer.println("#SBATCH --exclusive");
				else
					writer.println("#SBATCH --exclusive");	// Just making always exclusive for now because otherwise taskset doesn't work
				
				// load Java modules
				writer.println("module load 2021");
				writer.println("module load Java/11.0.2");
				
				// Put up to PROCESSES_PER_JOB processes in this job
				int numJobProcesses = 0;
				while (numJobProcesses < numProcessesThisJob)
				{
					final ProcessData processData = processDataList.get(processIdx);
					
					final int numFeatureDiscoveryThreads = Math.min(processData.numPlayers, CORES_PER_PROCESS);
					final int numPlayingThreads = CORES_PER_PROCESS;
					
					// Write Java call for this process
					String javaCall = StringRoutines.join
							(
								" ", 
								"taskset",			// Assign specific core to each process
								"-c",
								StringRoutines.join
								(
									",", 
									String.valueOf(numJobProcesses * 3), 
									String.valueOf(numJobProcesses * 3 + 1), 
									String.valueOf(numJobProcesses * 3 + 2)
								),
								"java",
								"-Xms" + JVM_MEM + "M",
								"-Xmx" + JVM_MEM + "M",
								"-XX:+HeapDumpOnOutOfMemoryError",
								"-da",
								"-dsa",
								"-XX:+UseStringDeduplication",
								"-jar",
								StringRoutines.quote("/home/" + userName + "/TrainFeaturesSnelliusAllGames/Ludii.jar"),
								"--expert-iteration",
								"--game",
								StringRoutines.quote(processData.gameName),
								"--ruleset",
								StringRoutines.quote(processData.rulesetName),
								"-n",
								String.valueOf(MAX_SELFPLAY_TRIALS),
								"--game-length-cap 1000",
								"--thinking-time 1",
								"--iteration-limit 12000",
								"--wis",
								"--playout-features-epsilon 0.5",
								"--no-value-learning",
								"--train-tspg",
								"--checkpoint-freq 5",
								"--num-agent-threads",
								String.valueOf(numPlayingThreads),
								"--num-feature-discovery-threads",
								String.valueOf(numFeatureDiscoveryThreads),
								"--out-dir",
								StringRoutines.quote
								(
									"/home/" + 
									userName + 
									"/TrainFeaturesSnelliusAllGames/Out" + 
									StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) 
									+ 
									"_"
									+
									StringRoutines.cleanRulesetName(processData.rulesetName).replaceAll(Pattern.quote("/"), "_")
									+
									"/"
								),
								"--no-logging",
								"--max-wall-time",
								String.valueOf(MAX_WALL_TIME)
							);
					
					javaCall += " --special-moves-expander-split";
					javaCall += " --handle-aliasing";
					javaCall += " --is-episode-durations";
					javaCall += " --prioritized-experience-replay";
					
					javaCall += " " + StringRoutines.join
							(
								" ",
								">",
								"/home/" + userName + "/TrainFeaturesSnelliusAllGames/Out/Out_${SLURM_JOB_ID}_" + numJobProcesses + ".out",
								"&"		// Run processes in parallel
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
		
		System.out.println("Total core hours requested = " + totalCoreHoursRequested.doubleValue());
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
//		final List<String> jobScriptNames = new ArrayList<String>();
//		
//		String scriptsDirPath = argParse.getValueString("--scripts-dir");
//		scriptsDirPath = scriptsDirPath.replaceAll(Pattern.quote("\\"), "/");
//		if (!scriptsDirPath.endsWith("/"))
//			scriptsDirPath += "/";
//		final File scriptsDir = new File(scriptsDirPath);
//		if (!scriptsDir.exists())
//			scriptsDir.mkdirs();
//		
//		String trainingOutDirPath  = argParse.getValueString("--training-out-dir");
//		trainingOutDirPath = trainingOutDirPath.replaceAll(Pattern.quote("\\"), "/");
//		if (!trainingOutDirPath.endsWith("/"))
//			trainingOutDirPath += "/";
//		
//		String bestAgentsDataDirPath = argParse.getValueString("--best-agents-data-dir");
//		bestAgentsDataDirPath = bestAgentsDataDirPath.replaceAll(Pattern.quote("\\"), "/");
//		if (!bestAgentsDataDirPath.endsWith("/"))
//			bestAgentsDataDirPath += "/";
//		
//		final File bestAgentsDataDir = new File(bestAgentsDataDirPath);
//		if (!bestAgentsDataDir.exists())
//			bestAgentsDataDir.mkdirs();
//		
//		final String userName = argParse.getValueString("--user-name");
//		
//		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter
//				(
//					s -> 
//					(
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
//						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
//					)
//				).toArray(String[]::new);
//		
//		// Loop through all the games we have
//		for (final String fullGamePath : allGameNames)
//		{
//			final String[] gamePathParts = fullGamePath.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
//			final String gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
//			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName + ".lud");
//			final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
//			gameRulesets.add(null);
//			boolean foundRealRuleset = false;
//			
//			for (final Ruleset ruleset : gameRulesets)
//			{
//				final Game game;
//				String fullRulesetName = "";
//				if (ruleset == null && foundRealRuleset)
//				{
//					// Skip this, don't allow game without ruleset if we do have real implemented ones
//					continue;
//				}
//				else if (ruleset != null && !ruleset.optionSettings().isEmpty())
//				{
//					fullRulesetName = ruleset.heading();
//					foundRealRuleset = true;
//					game = GameLoader.loadGameFromName(gameName + ".lud", fullRulesetName);
//				}
//				else if (ruleset != null && ruleset.optionSettings().isEmpty())
//				{
//					// Skip empty ruleset
//					continue;
//				}
//				else
//				{
//					game = gameNoRuleset;
//				}
//				
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
//				
//				if (game.isStacking())
//					continue;
//				
//				if (game.hiddenInformation())
//					continue;
//				
//				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
//				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
//				
//				final File rulesetExItOutDir = new File(trainingOutDirPath + filepathsGameName + filepathsRulesetName);
//				if (rulesetExItOutDir.exists() && rulesetExItOutDir.isDirectory())
//				{
//					final int numPlayers = game.players().count();
//		
//					final File bestAgentsDataDirForGame = 
//							new File(bestAgentsDataDir.getAbsolutePath() + "/" + filepathsGameName + filepathsRulesetName);
//					
//					// Now write our gating experiment scripts
//					final List<String> agentsToEval = new ArrayList<String>();
//					final List<List<String>> evalFeatureWeightFilepaths = new ArrayList<List<String>>();
//					final List<String> evalHeuristicsFilepaths = new ArrayList<String>();
//					final List<List<String>> gateAgentTypes = new ArrayList<List<String>>();
//					
//					final File[] trainingOutFiles = rulesetExItOutDir.listFiles();
//					if (trainingOutFiles == null || trainingOutFiles.length == 0)
//					{
//						System.err.println("No training out files for: " + rulesetExItOutDir.getAbsolutePath());
//						continue;
//					}
//					
//					// Find latest value function and feature files
//					File latestValueFunctionFile = null;
//					int latestValueFunctionCheckpoint = 0;
//					
//					final File[] latestPolicyWeightFiles = new File[numPlayers + 1];
//					final int[] latestPolicyWeightCheckpoints = new int[numPlayers + 1];
//					boolean foundPolicyWeights = false;
//					
//					for (final File file : trainingOutFiles)
//					{
//						String filename = file.getName();
//						// remove extension
//						filename = filename.substring(0, filename.lastIndexOf('.'));
//						
//						final String[] filenameSplit = filename.split(Pattern.quote("_"));
//						
//						if (filename.startsWith("PolicyWeightsCE_P"))
//						{
//							final int checkpoint = Integer.parseInt(filenameSplit[2]);
//							final int p = Integer.parseInt(filenameSplit[1].substring(1));
//							
//							if (checkpoint > latestPolicyWeightCheckpoints[p])
//							{
//								foundPolicyWeights = true;
//								latestPolicyWeightFiles[p] = file;
//								latestPolicyWeightCheckpoints[p] = checkpoint;
//							}
//						}
//						else if (filename.startsWith("ValueFunction"))
//						{
//							final int checkpoint = Integer.parseInt(filenameSplit[1]);
//							if (checkpoint > latestValueFunctionCheckpoint)
//							{
//								latestValueFunctionFile = file;
//								latestValueFunctionCheckpoint = checkpoint;
//							}
//						}
//					}
//					
//					int numMatchups = 0;
//					
//					if (dummyAlphaBeta.supportsGame(game))
//					{
//						if (latestValueFunctionCheckpoint > 0)
//						{
//							agentsToEval.add("Alpha-Beta");
//							evalFeatureWeightFilepaths.add(new ArrayList<String>());
//							evalHeuristicsFilepaths.add(latestValueFunctionFile.getAbsolutePath());
//							
//							final List<String> gateAgents = new ArrayList<String>();
//							gateAgents.add("Alpha-Beta");
//							gateAgents.add("BestAgent");
//							gateAgentTypes.add(gateAgents);
//							
//							numMatchups += 2;
//						}
//					}
//					
//					if (dummyUCT.supportsGame(game))
//					{
//						if (foundPolicyWeights)
//						{
//							final List<String> policyWeightFiles = new ArrayList<String>();
//							for (int p = 1; p < latestPolicyWeightFiles.length; ++p)
//							{
//								policyWeightFiles.add(latestPolicyWeightFiles[p].toString());
//							}
//							
//							agentsToEval.add("BiasedMCTS");
//							evalFeatureWeightFilepaths.add(policyWeightFiles);
//							evalHeuristicsFilepaths.add(null);
//							
//							List<String> gateAgents = new ArrayList<String>();
//							if (new File(bestAgentsDataDirForGame.getAbsolutePath() + "/BestFeatures.txt").exists())
//							{
//								gateAgents.add("BiasedMCTS");
//								numMatchups += 1;
//							}
//							gateAgents.add("BestAgent");
//							numMatchups += 1;
//							
//							gateAgentTypes.add(gateAgents);
//							
//							agentsToEval.add("BiasedMCTSUniformPlayouts");
//							evalFeatureWeightFilepaths.add(policyWeightFiles);
//							evalHeuristicsFilepaths.add(null);
//							
//							gateAgents = new ArrayList<String>();
//							if (new File(bestAgentsDataDirForGame.getAbsolutePath() + "/BestFeatures.txt").exists())
//							{
//								gateAgents.add("BiasedMCTS");
//								numMatchups += 1;
//							}
//							gateAgents.add("BestAgent");
//							numMatchups += 1;
//							
//							gateAgentTypes.add(gateAgents);
//						}
//					}
//					
//					final List<String> javaCalls = new ArrayList<String>();
//					
//					for (int evalAgentIdx = 0; evalAgentIdx < agentsToEval.size(); ++evalAgentIdx)
//					{
//						final String agentToEval = agentsToEval.get(evalAgentIdx);
//						final List<String> featureWeightFilepaths = evalFeatureWeightFilepaths.get(evalAgentIdx);
//						final String heuristicFilepath = evalHeuristicsFilepaths.get(evalAgentIdx);
//						final List<String> gateAgents = gateAgentTypes.get(evalAgentIdx);
//			
//						for (final String gateAgent : gateAgents)
//						{
//							String javaCall = StringRoutines.join
//							(
//								" ", 
//								"java",
//								"-Xms" + JVM_MEM + "M",
//								"-Xmx" + JVM_MEM + "M",
//								"-XX:+HeapDumpOnOutOfMemoryError",
//								"-da",
//								"-dsa",
//								"-XX:+UseStringDeduplication",
//								"-jar",
//								StringRoutines.quote("/home/" + userName + "/Gating/Ludii.jar"),
//								"--eval-gate",
//								"--game",
//								StringRoutines.quote(gameName + ".lud"),
//								"--ruleset",
//				                StringRoutines.quote(fullRulesetName),
//								"--eval-agent",
//								StringRoutines.quote(agentToEval),
//								"-n 70",
//								"--game-length-cap 800",
//								"--thinking-time 1",
//								"--best-agents-data-dir",
//								StringRoutines.quote(bestAgentsDataDirForGame.getAbsolutePath()),
//								"--gate-agent-type",
//								gateAgent,
//								"--max-wall-time",
//								"" + Math.max(1000, (MAX_WALL_TIME / numMatchups))
//							);
//				
//							if (featureWeightFilepaths.size() > 0)
//								javaCall += " --eval-feature-weights-filepaths " + StringRoutines.join(" ", featureWeightFilepaths);
//				
//							if (heuristicFilepath != null)
//								javaCall += " --eval-heuristics-filepath " + heuristicFilepath;
//							
//							javaCalls.add(javaCall);
//						}
//					}
//					
//					final String jobScriptFilename = "Gating_" + filepathsGameName + filepathsRulesetName + ".sh";
//			
//					try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "/" + jobScriptFilename), "UTF-8"))
//					{
//						writer.println("#!/usr/local_rwth/bin/zsh");
//						writer.println("#SBATCH -J Gating_" + filepathsGameName + filepathsRulesetName);
//						writer.println("#SBATCH -o /work/" + userName + "/Gating/Out"
//								+ filepathsGameName + filepathsRulesetName + "_%J.out");
//						writer.println("#SBATCH -e /work/" + userName + "/Gating/Err"
//								+ filepathsGameName + filepathsRulesetName + "_%J.err");
//						writer.println("#SBATCH -t " + MAX_WALL_TIME);
//						writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
//						writer.println("#SBATCH -A " + argParse.getValueString("--project"));
//						writer.println("unset JAVA_TOOL_OPTIONS");
//						
//						for (final String javaCall : javaCalls)
//						{
//							writer.println(javaCall);
//						}
//						
//						jobScriptNames.add(jobScriptFilename);
//					}
//					catch (final FileNotFoundException | UnsupportedEncodingException e)
//					{
//						e.printStackTrace();
//					}
//				}
//			}
//		}
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
		public final int numPlayers;

		/**
		 * Constructor
		 * @param gameName
		 * @param rulesetName
		 * @param numPlayers
		 */
		public ProcessData(final String gameName, final String rulesetName, final int numPlayers)
		{
			this.gameName = gameName;
			this.rulesetName = rulesetName;
			this.numPlayers = numPlayers;
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
					"Generates decision trees and scripts to run on cluster for feature evaluation."
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
