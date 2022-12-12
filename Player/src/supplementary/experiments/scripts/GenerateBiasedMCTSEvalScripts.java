package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import decision_trees.classifiers.DecisionTreeNode;
import decision_trees.classifiers.ExperienceIQRTreeLearner;
import features.feature_sets.BaseFeatureSet;
import features.spatial.Walk;
import function_approx.LinearFunction;
import game.Game;
import game.types.play.RoleType;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ArrayUtils;
import main.collections.ListUtils;
import main.options.Ruleset;
import metadata.ai.features.trees.FeatureTrees;
import other.GameLoader;
import other.WeaklyCachingGameLoader;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import supplementary.experiments.analysis.RulesetConceptsUCT;
import utils.AIFactory;
import utils.ExperimentFileUtils;
import utils.RulesetNames;
import utils.data_structures.experience_buffers.ExperienceBuffer;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;
import utils.data_structures.experience_buffers.UniformExperienceBuffer;

/**
 * Class with main method to automatically generate all the appropriate evaluation
 * scripts for features on the Snellius cluster.
 *
 * @author Dennis Soemers
 */
public class GenerateBiasedMCTSEvalScripts
{
	
	/** Number of threads to use for our actual job that's generating eval scripts */
	private static final int NUM_GENERATION_THREADS = 96;
	
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
	
	/** Num trials per matchup */
	private static final int NUM_TRIALS = 100;
	
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
	private GenerateBiasedMCTSEvalScripts()
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

		final String userName = argParse.getValueString("--user-name");
		
		// Modify the ruleset filepaths for running on Snellius
		RulesetConceptsUCT.FILEPATH = "/home/" + userName + "/rulesetConceptsUCT.csv";
		RulesetNames.FILEPATH = "/home/" + userName + "/GameRulesets.csv";
		
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
		final TIntArrayList gamePlayerCounts = new TIntArrayList();
		final TDoubleArrayList expectedTrialDurations = new TDoubleArrayList();
		
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
				
				final double drawishness = RulesetConceptsUCT.getValue(RulesetNames.gameRulesetName(game), "Drawishness");
				
				if (drawishness == 1.0)
				{
					System.out.println("Skipping " + shortGameName + " (" + fullRulesetName + ") because of drawishness.");
					continue;
				}
				
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
				gamePlayerCounts.add(game.players().count());
				expectedTrialDurations.add(expectedTrialDuration);
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

		// Create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		for (int idx : sortedGameIndices)
		{
			final int numPlayers = gamePlayerCounts.getQuick(idx);
			processDataList.add(new ProcessData(gameNames.get(idx), rulesetNames.get(idx), numPlayers));
		}
		
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
							final Game game = WeaklyCachingGameLoader.SINGLETON.loadGameFromName(processData.gameName, processData.rulesetName);
							
							// Construct an MCTS object with trained CE selection policy, easiest way to extract
							// the features from files again
							final StringBuilder playoutSb = new StringBuilder();
							playoutSb.append("playout=softmax");
					
							for (int p = 1; p <= game.players().count(); ++p)
							{
								final String policyFilepath = 
										ExperimentFileUtils.getLastFilepath
										(
											"/home/" 
											+ 
											userName 
											+ 
											"/TrainFeaturesSnelliusAllGames/Out" 
											+
											StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) 
											+ 
											"_"
											+
											StringRoutines.cleanRulesetName(processData.rulesetName).replaceAll(Pattern.quote("/"), "_")
											+
											"/PolicyWeightsSelection" + "_P" + p, 
											"txt"
										);
								
								playoutSb.append(",policyweights" + p + "=" + policyFilepath);
							}
							
							final StringBuilder selectionSb = new StringBuilder();
							selectionSb.append("learned_selection_policy=playout");
					
							final String agentStr = StringRoutines.join
									(
										";", 
										"algorithm=MCTS",
										"selection=noisyag0selection",
										playoutSb.toString(),
										"final_move=robustchild",
										"tree_reuse=true",
										selectionSb.toString(),
										"friendly_name=BiasedMCTS"
									);
							
							final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
							final SoftmaxPolicyLinear playoutSoftmax = (SoftmaxPolicyLinear) mcts.playoutStrategy();
							
							final BaseFeatureSet[] featureSets = playoutSoftmax.featureSets();
							final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();
							
							playoutSoftmax.initAI(game, -1);
							
							// Now build trees (only depth 3)
							final int DEPTH = 3;
							final metadata.ai.features.trees.classifiers.DecisionTree[] metadataTrees = 
									new metadata.ai.features.trees.classifiers.DecisionTree[featureSets.length - 1];
							
							for (int p = 1; p < featureSets.length; ++p)
							{
								// Load experience buffer for Player p
								final String bufferFilepath = 
										ExperimentFileUtils.getLastFilepath
										(
											"/home/" 
											+ 
											userName 
											+ 
											"/TrainFeaturesSnelliusAllGames/Out" 
											+
											StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) 
											+ 
											"_"
											+
											StringRoutines.cleanRulesetName(processData.rulesetName).replaceAll(Pattern.quote("/"), "_")
											+
											"/ExperienceBuffer_P" + p, 
											"buf"
										);
								
								ExperienceBuffer buffer = null;
								try
								{
									buffer = PrioritizedReplayBuffer.fromFile(game, bufferFilepath);
								}
								catch (final Exception e)
								{
									if (buffer == null)
									{
										try
										{
											buffer = UniformExperienceBuffer.fromFile(game, bufferFilepath);
										}
										catch (final Exception e2)
										{
											e.printStackTrace();
											e2.printStackTrace();
										}
									}
								}
								
								// Generate decision tree for Player p
								final DecisionTreeNode root = 
										ExperienceIQRTreeLearner.buildTree(featureSets[p], linearFunctions[p], buffer, DEPTH, 5);
								
								// Convert to metadata structure
								final metadata.ai.features.trees.classifiers.DecisionTreeNode metadataRoot = root.toMetadataNode();
								metadataTrees[p - 1] = 
										new metadata.ai.features.trees.classifiers.DecisionTree(RoleType.roleForPlayerId(p), metadataRoot);
							}
							
							final String outFile = 
									"/home/" + 
									userName + 
									"/TrainFeaturesSnelliusAllGames/Out" + 
									StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) 
									+ 
									"_"
									+
									StringRoutines.cleanRulesetName(processData.rulesetName).replaceAll(Pattern.quote("/"), "_")
									+
									"/IQR_Tree_"
									+
									3
									+
									".txt";
							
							System.out.println("Writing IQR tree to: " + outFile);
							new File(outFile).getParentFile().mkdirs();
							
							try (final PrintWriter writer = new PrintWriter(outFile))
							{
								writer.println(new FeatureTrees(null, metadataTrees));
							}
							catch (final IOException e)
							{
								e.printStackTrace();
							}
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
		
		// Now write all the eval job scripts
		final List<EvalProcessData> evalProcessDataList = new ArrayList<EvalProcessData>();
		for (final ProcessData processData : processDataList)
		{
			final int numPlayers = processData.numPlayers;
			evalProcessDataList.add(new EvalProcessData(processData.gameName, processData.rulesetName, processData.numPlayers, "Biased"));
			
			if (numPlayers == 1)		// Need to evaluate UCT separately
				evalProcessDataList.add(new EvalProcessData(processData.gameName, processData.rulesetName, processData.numPlayers, "UCT"));
		}
		
		final DoubleAdder totalCoreHoursRequested = new DoubleAdder();
		final int numProcessBatches = (int) Math.ceil((double)evalProcessDataList.size() / PROCESSES_PER_JOB);
		final TIntArrayList batchIndices = ListUtils.range(numProcessBatches);
		
		try
		{
			final CountDownLatch latch = new CountDownLatch(batchIndices.size());
			
			for (int i = 0; i < batchIndices.size(); ++i)
			{
				final int batchIdx = batchIndices.getQuick(i);
				final int evalProcessStartIdx = batchIdx * PROCESSES_PER_JOB;
				final int evalProcessEndIdx = Math.min((batchIdx + 1) * PROCESSES_PER_JOB, evalProcessDataList.size());
				
				// Start a new job script
				final String jobScriptFilename = "EvalBiasedMCTS_" + batchIdx + ".sh";

				executor.submit
				(
					() ->
					{
						try
						(
							final PrintWriter writer = 
								new UnixPrintWriter
								(
									new File("/home/" + userName + "/EvalBiasedMCTSAllGames/" + jobScriptFilename), "UTF-8"
								)
						)
						{
							
							writer.println("#!/bin/bash");
							writer.println("#SBATCH -J EvalBiasedMCTS");
							writer.println("#SBATCH -p thin");
							writer.println("#SBATCH -o /home/" + userName + "/EvalBiasedMCTSAllGames/Out/Out_%J.out");
							writer.println("#SBATCH -e /home/" + userName + "/EvalBiasedMCTSAllGames/Out/Err_%J.err");
							writer.println("#SBATCH -t " + MAX_WALL_TIME);
							writer.println("#SBATCH -N 1");		// 1 node, no MPI/OpenMP/etc
							
							// Compute memory and core requirements
							final int numProcessesThisJob = evalProcessEndIdx - evalProcessStartIdx;
							final boolean exclusive = (numProcessesThisJob > EXCLUSIVE_PROCESSES_THRESHOLD);
							final int jobMemRequestGB;
							if (exclusive)
								jobMemRequestGB = Math.min(MEM_PER_NODE, MAX_REQUEST_MEM);	// We're requesting full node anyway, might as well take all the memory
							else
								jobMemRequestGB = Math.min(numProcessesThisJob * MEM_PER_PROCESS, MAX_REQUEST_MEM);
							
							totalCoreHoursRequested.add(CORES_PER_NODE * (MAX_WALL_TIME / 60.0));
							
							writer.println("#SBATCH --cpus-per-task=" + numProcessesThisJob * CORES_PER_PROCESS);
							writer.println("#SBATCH --mem=" + jobMemRequestGB + "G");		// 1 node, no MPI/OpenMP/etc
							
							if (exclusive)
								writer.println("#SBATCH --exclusive");
							else
								writer.println("#SBATCH --exclusive");
							
							// load Java modules
							writer.println("module load 2021");
							writer.println("module load Java/11.0.2");
							
							// Put up to PROCESSES_PER_JOB processes in this job
							int numJobProcesses = 0;
							for (int processIdx = evalProcessStartIdx; processIdx < evalProcessEndIdx; ++processIdx)
							{
								final EvalProcessData evalProcessData = evalProcessDataList.get(processIdx);
								
								final List<String> agentStrings = new ArrayList<String>();
								final String agentStr1;
								
								if (evalProcessData.evalAgent.equals("UCT"))
								{
									agentStr1 = "algorithm=UCT";
								}
								else
								{
									final List<String> playoutStrParts = new ArrayList<String>();
									playoutStrParts.add("playout=classificationtreepolicy");
									for (int p = 1; p <= evalProcessData.numPlayers; ++p)
									{
										playoutStrParts.add
										(
											"policytrees=/" + 
											StringRoutines.join
											(
												"/", 
												"home",
												userName,
												"TrainFeaturesSnelliusAllGames",
												"Out"
												+
												StringRoutines.cleanGameName(evalProcessData.gameName.replaceAll(Pattern.quote(".lud"), "")) 
												+ 
												"_"
												+
												StringRoutines.cleanRulesetName(evalProcessData.rulesetName).replaceAll(Pattern.quote("/"), "_"),
												"IQR_Tree_3" + ".txt"
											)
											+ "," +
											"greedy=false"
										);
									}
									
									final List<String> learnedSelectionStrParts = new ArrayList<String>();
									learnedSelectionStrParts.add("learned_selection_policy=playout");
									
									agentStr1 = 
											StringRoutines.join
											(
												";", 
												"algorithm=MCTS",
												"selection=noisyag0selection",		// Noisy variant here because we're using playout policy in selection
												StringRoutines.join
												(
													",", 
													playoutStrParts
												),
												"tree_reuse=true",
												"use_score_bounds=true",
												"num_threads=3",
												"final_move=robustchild",
												StringRoutines.join
												(
													",", 
													learnedSelectionStrParts
												),
												"friendly_name=BiasedMCTS"
											);
								}
			
								while (agentStrings.size() < evalProcessData.numPlayers)
								{
									agentStrings.add(StringRoutines.quote(agentStr1));
									
									if (agentStrings.size() < evalProcessData.numPlayers)
										agentStrings.add(StringRoutines.quote("algorithm=UCT"));
								}
								
								// Write Java call for this process
								String javaCall = StringRoutines.join
										(
											" ", 
											"taskset",			// Assign specific cores to each process
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
											StringRoutines.quote("/home/" + userName + "/EvalBiasedMCTSAllGames/Ludii.jar"),
											"--eval-agents",
											"--game",
											StringRoutines.quote(evalProcessData.gameName),
											"--ruleset",
											StringRoutines.quote(evalProcessData.rulesetName),
											"-n " + NUM_TRIALS,
											"--thinking-time 1",
											"--agents",
											StringRoutines.join(" ", agentStrings),
											"--warming-up-secs",
											String.valueOf(30),
											"--game-length-cap",
											String.valueOf(1000),
											"--out-dir",
											StringRoutines.quote
											(
												"/home/" + 
												userName + 
												"/EvalBiasedMCTSAllGames/Out" + 
												StringRoutines.cleanGameName(evalProcessData.gameName.replaceAll(Pattern.quote(".lud"), "")) 
												+ 
												"_"
												+
												StringRoutines.cleanRulesetName(evalProcessData.rulesetName).replaceAll(Pattern.quote("/"), "_")
												+
												"/" 
												+
												evalProcessData.evalAgent
											),
											"--output-summary",
											"--output-alpha-rank-data",
											"--max-wall-time",
											String.valueOf(MAX_WALL_TIME),
											">",
											"/home/" + userName + "/EvalBiasedMCTSAllGames/Out/Out_${SLURM_JOB_ID}_" + numJobProcesses + ".out",
											"&"		// Run processes in parallel
										);
								
								writer.println(javaCall);
								
								++numJobProcesses;
							}
							
							writer.println("wait");		// Wait for all the parallel processes to finish

							jobScriptNames.add(jobScriptFilename);
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
		
		executor.shutdown();
		
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
			try (final PrintWriter writer = new UnixPrintWriter(new File("/home/" + userName + "/EvalBiasedMCTSAllGames/SubmitJobs_Part" + i + ".sh"), "UTF-8"))
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
	
	/**
	 * Wrapper around data for a single eval process (multiple processes per job)
	 *
	 * @author Dennis Soemers
	 */
	private static class EvalProcessData
	{
		public final String gameName;
		public final String rulesetName;
		public final int numPlayers;
		public final String evalAgent;
		
		/**
		 * Constructor
		 * @param gameName
		 * @param rulesetName
		 * @param numPlayers
		 * @param evalAgent
		 */
		public EvalProcessData(final String gameName, final String rulesetName, final int numPlayers, final String evalAgent)
		{
			this.gameName = gameName;
			this.rulesetName = rulesetName;
			this.numPlayers = numPlayers;
			this.evalAgent = evalAgent;
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
					"Generates scripts to run on cluster for evaluation of MCTS with features."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--user-name")
				.help("Username on the cluster.")
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
