package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;
import supplementary.experiments.concepts.ParallelComputeConceptsMultipleGames.GameRulesetToCompute;

/**
 * Generates scripts to run on the Lemaitre4 cluster to compute concepts for
 * many games.
 *
 * @author Dennis Soemers
 */
public class ComputeConceptsScriptsGenLemaitre4
{
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 200;

	/** Memory to assign to JVM */
	private static final String JVM_MEM_MIN = "512g";
	
	/** Memory to assign to JVM */
	private static final String JVM_MEM_MAX = "512g";
	
	// TODO no idea what this should be on Lemaitre4
	/** Cluster doesn't seem to let us request more memory than this for any single job (on a single node) */
	private static final int MAX_REQUEST_MEM = 600;
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 1500;
	
	/** Number of cores per node (this is for Lemaitre4) */
	private static final int CORES_PER_NODE = 128;
	
	/** Number of cores per Java call */
	private static final int CORES_PER_PROCESS = 4;
	
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
	private ComputeConceptsScriptsGenLemaitre4()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates our scripts
	 * @param argParse
	 */
	@SuppressWarnings("unchecked")
	private static void generateScripts(final CommandLineArgParse argParse)
	{
		final List<String> jobScriptNames = new ArrayList<String>();
		
		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		final String userName = argParse.getValueString("--user-name");
		
		List<String> gamePaths = (List<String>) argParse.getValue("--game-paths");
		List<String> rulesetNames = new ArrayList<String>();
		
		if (gamePaths == null)
		{
			// Take built-in games
			gamePaths = new ArrayList<String>();
			
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
						
						final Game game = GameLoader.loadGameFromName(gameName + ".lud", fullRulesetName);
						
						// A bunch of game types we'll skip:
						if (game.players().count() != 2)
						{
							continue;
						}
						if (game.isDeductionPuzzle())
						{
							continue;
						}
						if (game.isSimulationMoveGame())
						{
							continue;
						}
						if (!game.isAlternatingMoveGame())
						{
							continue;
						}
						if (game.hasSubgames())
						{
							continue;
						}
						if (game.hiddenInformation())
						{
							continue;
						}
						
						gamePaths.add(gameName + ".lud");
						rulesetNames.add(fullRulesetName);
					}
					else if (ruleset != null && ruleset.optionSettings().isEmpty())
					{
						// Skip empty ruleset
						continue;
					}
					else
					{
						final Game game = gameNoRuleset;
						
						// A bunch of game types we'll skip:
						if (game.players().count() != 2)
						{
							continue;
						}
						if (game.isDeductionPuzzle())
						{
							continue;
						}
						if (game.isSimulationMoveGame())
						{
							continue;
						}
						if (!game.isAlternatingMoveGame())
						{
							continue;
						}
						if (game.hasSubgames())
						{
							continue;
						}
						if (game.hiddenInformation())
						{
							continue;
						}
						
						gamePaths.add(gameName + ".lud");
						rulesetNames.add("");
					}
				}
			}
		}
		
		long callID = argParse.getValueInt("--first-call-id");
		
		// First create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		
		for (int i = 0; i < gamePaths.size(); ++i)
		{
			final String gamePath = gamePaths.get(i).replaceAll(Pattern.quote("\\"), "/");
			final String rulesetName;
			
			final Game game;
			
			if (gamePath.contains("/"))
			{
				// This is a filepath
				game = GameLoader.loadGameFromFile(new File(gamePath));
				rulesetName = "";
			}
			else
			{
				// This is a built-in game
				rulesetName = rulesetNames.get(i);
				game = GameLoader.loadGameFromName(gamePath, rulesetName);
			}
						
			if (game.players().count() != 2)
			{
				System.err.println("Error: " + gamePath + " does not have 2 players");
				continue;
			}
			
			if (game.isDeductionPuzzle())
			{
				System.err.println("Error: " + gamePath + " is a deduction puzzle");
				continue;
			}
			
			if (game.isSimulationMoveGame())
			{
				System.err.println("Error: " + gamePath + " is a simulation");
				continue;
			}
			
			if (!game.isAlternatingMoveGame())
			{
				System.err.println("Error: " + gamePath + " is a simultaneous-move game");
				continue;
			}
			
			if (game.hasSubgames())
			{
				System.err.println("Error: " + gamePath + " has subgames");
				continue;
			}
			
			if (game.hiddenInformation())
			{
				System.err.println("Error: " + gamePath + " has partial observability");
				continue;
			}
			
			processDataList.add
			(
				new ProcessData
				(
					gamePath, rulesetName, callID++
				)
			);
		}
		
		// Write scripts with all the processes
		Collections.shuffle(processDataList);

		long totalRequestedCoreHours = 0L;
		
		int processIdx = 0;
		while (processIdx < processDataList.size())
		{
			// Start a new job script and collection of JSON files
			final String jobScriptFilename = 
					"ComputeConcepts_" + String.valueOf(System.currentTimeMillis()) + "_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("#SBATCH -J ComputeConcepts");
				writer.println("#SBATCH -p batch");
				writer.println("#SBATCH -o /home/ucl/ingi/" + userName + "/ComputeConcepts/Out/Out_%J.out");
				writer.println("#SBATCH -e /home/ucl/ingi/" + userName + "/ComputeConcepts/Err/Err_%J.err");
				writer.println("#SBATCH -t " + MAX_WALL_TIME);
				writer.println("#SBATCH -N 1");		// 1 node, no MPI/OpenMP/etc
				
				// Compute memory and core requirements
				final int numProcessesThisJob = Math.min(processDataList.size() - processIdx, PROCESSES_PER_JOB);
				final int jobMemRequestGB = MAX_REQUEST_MEM;

				writer.println("#SBATCH --cpus-per-task=" + (numProcessesThisJob * CORES_PER_PROCESS));
				writer.println("#SBATCH --mem=" + jobMemRequestGB + "G");		// 1 node, no MPI/OpenMP/etc
				
				totalRequestedCoreHours += (CORES_PER_NODE * (MAX_WALL_TIME / 60));
				
				writer.println("#SBATCH --exclusive");
				
				// load Java module
				writer.println("module load Java/11.0.20");
				
				// Start writing the first part of the Java call line
				writer.print
				(
					StringRoutines.join
					(
						" ",
						"java",
						"-Xms" + JVM_MEM_MIN,
						"-Xmx" + JVM_MEM_MAX,
						"-XX:+HeapDumpOnOutOfMemoryError",
						"-XX:HeapDumpPath=" + StringRoutines.quote("/home/ucl/ingi/" + userName + "/ComputeConcepts/Err/java_pid%p.hprof"),
						"-da",
						"-dsa",
						"-XX:+UseStringDeduplication",
						"-jar",
						StringRoutines.quote("/home/ucl/ingi/" + userName + "/ComputeConcepts/Ludii.jar"),
						"--parallel-compute-concepts-multiple-games",
						"--max-wall-time",
						String.valueOf(MAX_WALL_TIME),
						"--num-cores-total",
						String.valueOf(CORES_PER_NODE),
						"--num-threads-per-job",
						String.valueOf(CORES_PER_PROCESS),
						"--json-files"
					)
				);
				
				// Put up to 48 jsons in this job
				int numJobProcesses = 0;
				while (numJobProcesses < 48 && processIdx < processDataList.size())
				{
					final ProcessData processData = processDataList.get(processIdx);
					
					String gameName = processData.gamePath;
					boolean gameNameIsPath = (gameName.contains("/"));
					final String[] gamePathParts = gameName.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
					
					final String gamePath;
					final String conceptsOutDir;
					if (gameNameIsPath)
					{
						gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
						gamePath = "/home/ucl/ingi/" + userName + "/Kaggle-Game-Dataset/" + gameName + ".lud";
					}
					else
					{
						gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
						gamePath = gameName + ".lud";
					}

					final String filepathsGameName = StringRoutines.cleanGameName(gameName);
					final String filepathsRulesetName = 
							StringRoutines.cleanRulesetName(
									processData.rulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
					
					if (gameNameIsPath)
					{
						conceptsOutDir = "/globalscratch/ucl/ingi/" + userName + "/ComputeConcepts/Concepts/Private" + filepathsGameName + filepathsRulesetName + "/";
					}
					else
					{
						conceptsOutDir = "/globalscratch/ucl/ingi/" + userName + "/ComputeConcepts/Concepts/Public/" + filepathsGameName + filepathsRulesetName + "/";
					}
					
					final GameRulesetToCompute job = 
							new GameRulesetToCompute
							(
								gamePath,
								processData.rulesetName, 
								100, 
								"/globalscratch/ucl/ingi/" + userName + "/ComputeConcepts/Trials/" + filepathsGameName + filepathsRulesetName + "/", 
								conceptsOutDir, 
								gameNameIsPath
							);
					
					job.toJson(scriptsDir + "GameConceptsJob_" + processData.callID + ".json");
					writer.print(" " + "/home/ucl/ingi/" + userName + "/ComputeConcepts/Scripts/" + "GameConceptsJob_" + processData.callID + ".json");
					
					++processIdx;
					++numJobProcesses;
				}
				
				// Write the end of the Java call line
				writer.println
				(
					" " + StringRoutines.join
					(
						" ",
						">",
						"/home/ucl/ingi/" + userName + "/ComputeConcepts/Out/Out_${SLURM_JOB_ID}" + ".out",
						"2>",
						"/home/ucl/ingi/" + userName + "/ComputeConcepts/Err/Err_${SLURM_JOB_ID}" + ".err",
						"&"		// Run processes in parallel
					)
				);
				
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
		
		final int firstJobsBatchIndex = argParse.getValueInt("--first-jobs-batch-index");

		for (int i = 0; i < jobScriptsLists.size(); ++i)
		{
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "SubmitJobs_Part" + (i + firstJobsBatchIndex) + ".sh"), "UTF-8"))
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
		public final String gamePath;
		public final String rulesetName;
		public final long callID;
		
		/**
		 * Constructor
		 * @param gamePath
		 * @param rulesetName
		 * @param callID
		 */
		public ProcessData
		(
			final String gamePath, 
			final String rulesetName,
			final long callID
		)
		{
			this.gamePath = gamePath;
			this.rulesetName = rulesetName;
			this.callID = callID;
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
					"Generate Lemaitre4 scripts to compute concepts."
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
		
		argParse.addOption(new ArgOption()
				.withNames("--game-paths")
				.help("Filepaths for games we wish to run. "
						+ "If not provided, we use all built-in two-player "
						+ "sequential perfect-info zero-sum games.")
				.withNumVals("+")
				.withType(OptionTypes.String)
				.withDefault(null));
		
		argParse.addOption(new ArgOption()
				.withNames("--first-call-id")
				.help("Call ID to use for the first JSON.")
				.withNumVals(1)
				.withType(OptionTypes.Int)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--first-jobs-batch-index")
				.help("Index to use in SubmitJobs filename for the first batch of jobs.")
				.withNumVals(1)
				.withType(OptionTypes.Int)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateScripts(argParse);
	}
	
	//-------------------------------------------------------------------------

}
