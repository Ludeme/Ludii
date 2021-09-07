package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.UnixPrintWriter;
import other.GameLoader;

/**
 * Script to generate scripts for evaluation of training runs with vs. without
 * --final-states-buffers.
 *
 * @author Dennis Soemers
 */
public class EvalTrainedFeaturesFinalStatesBuffersScriptsGen
{
	/** Memory to assign per CPU, in MB */
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;

	/** Memory to assign to JVM, in MB (64 GB per node, 24 cores per node --> 2.6GB per core, we take 2 cores) */
	private static final String JVM_MEM = "4096";
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 1440;
	
	/** We get 24 cores per job; we'll give 2 cores per process */
	private static final int PROCESSES_PER_JOB = 12;
	
	/** Only run on the Haswell nodes */
	private static final String PROCESSOR = "haswell";
	
	/** Games we want to run */
	private static final String[] GAMES = 
			new String[]
			{
				"Amazons.lud",
				"ArdRi.lud",
				"Breakthrough.lud",
				"English Draughts.lud",
				"Fanorona.lud",
				"Gomoku.lud",
				"Havannah.lud",
				"Hex.lud",
				"Knightthrough.lud",
				"Reversi.lud",
				"Yavalath.lud"
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private EvalTrainedFeaturesFinalStatesBuffersScriptsGen()
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

		// First create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		for (final String gameName : GAMES)
		{
			// Sanity check: make sure game with this name loads correctly
			System.out.println("gameName = " + gameName);
			final Game game = GameLoader.loadGameFromName(gameName);
			
			if (game == null)
				throw new IllegalArgumentException("Cannot load game: " + gameName);
			
			processDataList.add(new ProcessData(gameName));
		}
		
		int processIdx = 0;
		while (processIdx < processDataList.size())
		{
			// Start a new job script
			final String jobScriptFilename = "EvalFeatures_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("#SBATCH -J EvalFeatures");
				writer.println("#SBATCH -o /home/" + userName + "/EvalFeaturesFinalStatesBuffers/Out/Out_%J.out");
				writer.println("#SBATCH -e /home/" + userName + "/EvalFeaturesFinalStatesBuffers/Out/Err_%J.err");
				writer.println("#SBATCH -t " + MAX_WALL_TIME);
				writer.println("#SBATCH --constraint=" + PROCESSOR);
				
				// load Java modules
				writer.println("module load 2020");
				writer.println("module load Java/1.8.0_261");
				
				// Put up to PROCESSES_PER_JOB processes in this job
				int numJobProcesses = 0;
				while (numJobProcesses < PROCESSES_PER_JOB && processIdx < processDataList.size())
				{
					final ProcessData processData = processDataList.get(processIdx);
					
					final String agentToEval = 
							StringRoutines.join
							(
								";", 
								"algorithm=MCTS",
								"selection=ag0selection",
								StringRoutines.join
								(
									",", 
									"playout=softmax",
									"policyweights1=/home/" + userName + "TrainFeaturesFinalStatesBuffers/Out/" + StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + "_With/PolicyWeightsCE_P1_00201.txt",
									"policyweights2=/home/" + userName + "TrainFeaturesFinalStatesBuffers/Out/" + StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + "_With/PolicyWeightsCE_P2_00201.txt"
								),
								"tree_reuse=true",
								"num_threads=2",
								"final_move=robustchild",
								"learned_selection_policy=playout",
								"friendly_name=With"
							);
					
					final String opponent = 
							StringRoutines.join
							(
								";", 
								"algorithm=MCTS",
								"selection=ag0selection",
								StringRoutines.join
								(
									",", 
									"playout=softmax",
									"policyweights1=/home/" + userName + "TrainFeaturesFinalStatesBuffers/Out/" + StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + "_Without/PolicyWeightsCE_P1_00201.txt",
									"policyweights2=/home/" + userName + "TrainFeaturesFinalStatesBuffers/Out/" + StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + "_Without/PolicyWeightsCE_P2_00201.txt"
								),
								"tree_reuse=true",
								"num_threads=2",
								"final_move=robustchild",
								"learned_selection_policy=playout",
								"friendly_name=Without"
							);
					
					// Write Java call for this process
					final String javaCall = StringRoutines.join
							(
								" ", 
								"taskset",			// Assign specific core to each process
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
								StringRoutines.quote("/home/" + userName + "/EvalFeaturesFinalStatesBuffers/Ludii.jar"),
								"--eval-agents",
								"--game",
								StringRoutines.quote("/" + processData.gameName),
								"-n 150",
								"--thinking-time 1",
								"--agents",
								StringRoutines.quote(agentToEval),
								StringRoutines.quote(opponent),
								"--out-dir",
								StringRoutines.quote
								(
									"/home/" + 
									userName + 
									"/EvalFeaturesFinalStatesBuffers/Out/" + 
									StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), "")) + "/"
								),
								"--output-summary",
								"--output-alpha-rank-data",
								">",
								"/home/" + userName + "/EvalFeaturesFinalStatesBuffers/Out/Out_${SLURM_JOB_ID}_" + numJobProcesses + ".out",
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
		
		/**
		 * Constructor
		 * @param gameName
		 */
		public ProcessData(final String gameName)
		{
			this.gameName = gameName;
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
