package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ArrayUtils;
import other.GameLoader;
import supplementary.experiments.analysis.RulesetConceptsUCT;
import utils.RulesetNames;

/**
 * Script to generate scripts for evaluation of training runs.
 *
 * @author Dennis Soemers
 */
public class EvalTrainedFeaturesSnellius4Reinforce
{
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;

	/** Memory to assign to JVM, in MB (2 GB per core --> we take 2 cores per job, 4GB per job, use 3GB for JVM) */
	private static final String JVM_MEM = "3072";
	
	/** Memory to assign per process (in GB) */
	private static final int MEM_PER_PROCESS = 4;
	
	/** Memory available per node in GB (this is for Thin nodes on Snellius) */
	private static final int MEM_PER_NODE = 256;
	
	/** Cluster doesn't seem to let us request more memory than this for any single job (on a single node) */
	private static final int MAX_REQUEST_MEM = 234;
	
	/** Max number of self-play trials */
	private static final int NUM_TRIALS = 100;
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 2880;
	
	/** Number of cores per node (this is for Thin nodes on Snellius) */
	private static final int CORES_PER_NODE = 128;
	
	/** Number of cores per Java call */
	private static final int CORES_PER_PROCESS = 2;
	
	/** If we request more cores than this in a job, we get billed for the entire node anyway, so should request exclusive */
	private static final int EXCLUSIVE_CORES_THRESHOLD = 96;
	
	/** If we have more processes than this in a job, we get billed for the entire node anyway, so should request exclusive  */
	private static final int EXCLUSIVE_PROCESSES_THRESHOLD = EXCLUSIVE_CORES_THRESHOLD / CORES_PER_PROCESS;
	
	/**Number of processes we can put in a single job (on a single node) */
	private static final int PROCESSES_PER_JOB = CORES_PER_NODE / CORES_PER_PROCESS;
	
	/** Games we want to run */
	private static final String[] GAMES = 
			new String[]
			{
				"Alquerque.lud",
				"Amazons.lud",
				"ArdRi.lud",
				"Arimaa.lud",
				"Ataxx.lud",
				"Bao Ki Arabu (Zanzibar 1).lud",
				"Bizingo.lud",
				"Breakthrough.lud",
				"Chess.lud",
				//"Chinese Checkers.lud",
				"English Draughts.lud",
				"Fanorona.lud",
				"Fox and Geese.lud",
				"Go.lud",
				"Gomoku.lud",
				"Gonnect.lud",
				"Havannah.lud",
				"Hex.lud",
				"Knightthrough.lud",
				"Konane.lud",
				//"Level Chess.lud",
				"Lines of Action.lud",
				"Omega.lud",
				"Pentalath.lud",
				"Pretwa.lud",
				"Reversi.lud",
				"Royal Game of Ur.lud",
				"Surakarta.lud",
				"Shobu.lud",
				"Tablut.lud",
				//"Triad.lud",
				"XII Scripta.lud",
				"Yavalath.lud"
			};
	
	/** Descriptors of several variants we want to test */
	private static final String[] VARIANTS =
			new String[]
			{
				"Baseline",
				//"TournamentMode",
				//"ReinforceGamma1",
				//"ReinforceGamma099",
				//"ReinforceGamma09",
				"SpecialMovesExpander",
				"SpecialMovesExpanderSplit",
				//"SignCorrelationExpander",
				//"RandomExpander",
				//"All"
				"NoHandleAliasing",
				"HandleAliasingPlayouts",
				"NoWED",
				"NoPER"
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private EvalTrainedFeaturesSnellius4Reinforce()
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
		
		// Sort games in decreasing order of expected duration (in moves per trial)
		// This ensures that we start with the slow games, and that games of similar
		// durations are likely to end up in the same job script (and therefore run
		// on the same node at the same time).
		final Game[] compiledGames = new Game[GAMES.length];
		final double[] expectedTrialDurations = new double[GAMES.length];
		for (int i = 0; i < compiledGames.length; ++i)
		{
			final Game game = GameLoader.loadGameFromName(GAMES[i]);

			if (game == null)
				throw new IllegalArgumentException("Cannot load game: " + GAMES[i]);

			compiledGames[i] = game;
			expectedTrialDurations[i] = RulesetConceptsUCT.getValue(RulesetNames.gameRulesetName(game), "DurationMoves");

			System.out.println("expected duration per trial for " + GAMES[i] + " = " + expectedTrialDurations[i]);
		}

		final List<Integer> sortedGameIndices = ArrayUtils.sortedIndices(GAMES.length, new Comparator<Integer>()
		{

			@Override
			public int compare(final Integer i1, final Integer i2) 
			{
				final double delta = expectedTrialDurations[i2.intValue()] - expectedTrialDurations[i1.intValue()];
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
			final Game game = compiledGames[idx];
			final String gameName = GAMES[idx];
			
			final int numPlayers = game.players().count();
			
			if (numPlayers != 2)
				System.err.println("ERROR! Only expected 2-player games!");
			
			for (final String opponent : VARIANTS)
			{
				processDataList.add(new ProcessData(gameName, numPlayers, opponent));
			}
		}
		
		long totalRequestedCoreHours = 0L;
		
		int processIdx = 0;
		while (processIdx < processDataList.size())
		{
			// Start a new job script
			final String jobScriptFilename = "EvalFeaturesReinforce_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("#SBATCH -J EvalFeatures");
				writer.println("#SBATCH -p thin");
				writer.println("#SBATCH -o /home/" + userName + "/EvalFeaturesSnellius4/Out/Out_%J.out");
				writer.println("#SBATCH -e /home/" + userName + "/EvalFeaturesSnellius4/Out/Err_%J.err");
				writer.println("#SBATCH -t " + MAX_WALL_TIME);
				writer.println("#SBATCH -N 1");		// 1 node, no MPI/OpenMP/etc
				
				// Compute memory and core requirements
				final int numProcessesThisJob = Math.min(processDataList.size() - processIdx, PROCESSES_PER_JOB);
				final boolean exclusive = (numProcessesThisJob > EXCLUSIVE_PROCESSES_THRESHOLD);
				final int jobMemRequestGB;
				if (exclusive)
					jobMemRequestGB = Math.min(MEM_PER_NODE, MAX_REQUEST_MEM);	// We're requesting full node anyway, might as well take all the memory
				else
					jobMemRequestGB = Math.min(numProcessesThisJob * MEM_PER_PROCESS, MAX_REQUEST_MEM);
				
				writer.println("#SBATCH --cpus-per-task=" + numProcessesThisJob * CORES_PER_PROCESS);
				writer.println("#SBATCH --mem=" + jobMemRequestGB + "G");		// 1 node, no MPI/OpenMP/etc
				
				totalRequestedCoreHours += (CORES_PER_NODE * (MAX_WALL_TIME / 60));
				
				if (exclusive)
					writer.println("#SBATCH --exclusive");
				else
					writer.println("#SBATCH --exclusive");	// Just making always exclusive for now because otherwise taskset doesn't work
				
				// load Java modules
				writer.println("module load 2021");
				writer.println("module load Java/11.0.2");
				
				// Put up to PROCESSES_PER_JOB processes in this job
				int numJobProcesses = 0;
				while (numJobProcesses < PROCESSES_PER_JOB && processIdx < processDataList.size())
				{
					final ProcessData processData = processDataList.get(processIdx);
					
					final List<String> agentStrings = new ArrayList<String>();
					final String[] matchup = new String[] {"ReinforceOnly", processData.opponent};
					
					for (final String agent : matchup)
					{
						final List<String> playoutStrParts = new ArrayList<String>();
						playoutStrParts.add("playout=softmax");
						for (int p = 1; p <= processData.numPlayers; ++p)
						{
							playoutStrParts.add
							(
								"policyweights" + 
								p + 
								"=/home/" + 
								userName + 
								"/TrainFeaturesSnellius4/Out/" + 
								StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + 
								"_" + agent + 
								"/PolicyWeightsPlayout_P" + p + "_00201.txt"
							);
						}
						
						final List<String> learnedSelectionStrParts = new ArrayList<String>();
						learnedSelectionStrParts.add("learned_selection_policy=softmax");
						for (int p = 1; p <= processData.numPlayers; ++p)
						{
							learnedSelectionStrParts.add
							(
								"policyweights" + 
								p + 
								"=/home/" + 
								userName + 
								"/TrainFeaturesSnellius4/Out/" + 
								StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + 
								"_" + agent + 
								"/PolicyWeightsSelection_P" + p + "_00201.txt"
							);
						}
						
						final String agentStr = 
								StringRoutines.join
								(
									";", 
									"algorithm=MCTS",
									"selection=noisyag0selection",
									StringRoutines.join
									(
										",", 
										playoutStrParts
									),
									"tree_reuse=true",
									"use_score_bounds=true",
									"num_threads=2",
									"final_move=robustchild",
									StringRoutines.join
									(
										",", 
										learnedSelectionStrParts
									),
									"friendly_name=" + agent
								);
						
						agentStrings.add(StringRoutines.quote(agentStr));
					}
					
					// Write Java call for this process
					final String javaCall = StringRoutines.join
							(
								" ", 
								"taskset",			// Assign specific cores to each process
								"-c",
								StringRoutines.join(",", String.valueOf(numJobProcesses * 2), String.valueOf(numJobProcesses * 2 + 1)),
								"java",
								"-Xms" + JVM_MEM + "M",
								"-Xmx" + JVM_MEM + "M",
								"-XX:+HeapDumpOnOutOfMemoryError",
								"-da",
								"-dsa",
								"-XX:+UseStringDeduplication",
								"-jar",
								StringRoutines.quote("/home/" + userName + "/EvalFeaturesSnellius4/Ludii.jar"),
								"--eval-agents",
								"--game",
								StringRoutines.quote("/" + processData.gameName),
								"-n " + NUM_TRIALS,
								"--thinking-time 1",
								"--agents",
								StringRoutines.join(" ", agentStrings),
								"--out-dir",
								StringRoutines.quote
								(
									"/home/" + 
									userName + 
									"/EvalFeaturesSnellius4/Out/" + 
									StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + "/" + 
									StringRoutines.join("_", matchup)
								),
								"--output-summary",
								"--output-alpha-rank-data",
								"--max-wall-time",
								String.valueOf(MAX_WALL_TIME),
								">",
								"/home/" + userName + "/EvalFeaturesSnellius4/Out/Out_${SLURM_JOB_ID}_" + numJobProcesses + ".out",
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
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "SubmitReinforceOnlyJobs_Part" + i + ".sh"), "UTF-8"))
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
	 * Wrapper around data for a single process (multiple processes per job)
	 *
	 * @author Dennis Soemers
	 */
	private static class ProcessData
	{
		public final String gameName;
		public final int numPlayers;
		public final String opponent;
		
		/**
		 * Constructor
		 * @param gameName
		 * @param numPlayers
		 * @param opponent
		 */
		public ProcessData(final String gameName, final int numPlayers, final String opponent)
		{
			this.gameName = gameName;
			this.numPlayers = numPlayers;
			this.opponent = opponent;
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
					"Creating eval job scripts."
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
