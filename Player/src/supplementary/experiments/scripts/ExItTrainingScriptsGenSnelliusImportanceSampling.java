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
 * Generates job scripts to submit to SLURM for running ExIt training runs
 * 
 * Script generation currently made for Snellius cluster (not RWTH Aachen)
 *
 * @author Dennis Soemers
 */
public class ExItTrainingScriptsGenSnelliusImportanceSampling
{
	
	//-------------------------------------------------------------------------
	
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
	private static final int MAX_WALL_TIME = 2880;
	
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
	
	/** Games we want to run */
	private static final String[] GAMES = 
			new String[]
			{
				"Amazons.lud",
				"ArdRi.lud",
				"Breakthrough.lud",
				"English Draughts.lud",
				"Fanorona.lud",
				"Fox and Geese.lud",
				"Gomoku.lud",
				"Groups.lud",
				"Hex.lud",
				"Knightthrough.lud",
				"Konane.lud",
				"Pentalath.lud",
				"Reversi.lud",
				"Royal Game of Ur.lud",
				"Surakarta.lud",
				"Tablut.lud",
				"Yavalath.lud"
			};
	
	/** Descriptors of several variants we want to test */
	private static final String[] VARIANTS =
			new String[]
			{
				"None",
				"EpisodeDurations",
				"PER",
				//"CEExplore",
				//"CEExploreNoIS",
				"All"
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private ExItTrainingScriptsGenSnelliusImportanceSampling()
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
			
			for (final String variant : VARIANTS)
			{
				processDataList.add(new ProcessData(gameName, game.players().count(), variant));
			}
		}
		
		int processIdx = 0;
		while (processIdx < processDataList.size())
		{
			// Start a new job script
			final String jobScriptFilename = "TrainFeaturesIS_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("#SBATCH -J TrainFeaturesIS");
				writer.println("#SBATCH -p thin");
				writer.println("#SBATCH -o /home/" + userName + "/TrainFeaturesIS/Out/Out_%J.out");
				writer.println("#SBATCH -e /home/" + userName + "/TrainFeaturesIS/Out/Err_%J.err");
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
								StringRoutines.quote("/home/" + userName + "/TrainFeaturesIS/Ludii.jar"),
								"--expert-iteration",
								"--game",
								StringRoutines.quote("/" + processData.gameName),
								"-n",
								String.valueOf(MAX_SELFPLAY_TRIALS),
								"--out-dir",
								StringRoutines.quote
								(
									"/home/" + 
									userName + 
									"/TrainFeaturesIS/Out/" + 
									StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) 
									+ 
									"_" 
									+ 
									processData.trainingVariant + "/"
								),
								"--iteration-limit 800",
								"--max-wall-time",
								String.valueOf(MAX_WALL_TIME),
								"--checkpoint-freq 5",
								"--wis",
								"--game-length-cap 1000",
								"--no-value-learning",
								"--num-agent-threads",
								String.valueOf(numPlayingThreads),
								"--num-feature-discovery-threads",
								String.valueOf(numFeatureDiscoveryThreads),
								"--no-logging"
							);
					
					if (processData.trainingVariant.equals("EpisodeDurations"))
					{
						javaCall += " --is-episode-durations";
					}
					else if (processData.trainingVariant.equals("PER"))
					{
						javaCall += " --prioritized-experience-replay";
					}
					else if (processData.trainingVariant.equals("CEExplore"))
					{
						javaCall += " --ce-explore";
					}
					else if (processData.trainingVariant.equals("CEExploreNoIS"))
					{
						javaCall += " --ce-explore --no-ce-explore-is";
					}
					else if (processData.trainingVariant.equals("All"))
					{
						javaCall += " --is-episode-durations";
                        javaCall += " --prioritized-experience-replay";
                        //javaCall += " --ce-explore --no-ce-explore-is";
					}
					
					javaCall += " " + StringRoutines.join
							(
								" ",
								">",
								"/home/" + userName + "/TrainFeaturesIS/Out/Out_${SLURM_JOB_ID}_" + numJobProcesses + ".out",
								"2>",
								"/home/" + userName + "/TrainFeaturesIS/Out/Out_${SLURM_JOB_ID}_" + numJobProcesses + ".out",
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
		public final String trainingVariant;
		
		/**
		 * Constructor
		 * @param gameName
		 * @param numPlayers
		 * @param trainingVariant
		 */
		public ProcessData(final String gameName, final int numPlayers, final String trainingVariant)
		{
			this.gameName = gameName;
			this.numPlayers = numPlayers;
			this.trainingVariant = trainingVariant;
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
					"Creating feature training job scripts for Snellius cluster."
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
