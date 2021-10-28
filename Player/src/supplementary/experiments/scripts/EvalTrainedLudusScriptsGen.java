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
 * Script to generate scripts for ExIt training runs for various
 * Ludus Latrunculorum rulesets.
 *
 * @author Dennis Soemers
 */
public class EvalTrainedLudusScriptsGen
{
	/** Memory to assign per CPU, in MB */
	private static final String MEM_PER_CPU = "5120";
	
	/** Memory to assign to JVM, in MB */
	private static final String JVM_MEM = "4096";
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 3600;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;
	
	/** Games and rulesets we want to use */
	private static final String[][] GAMES_RULESETS = new String[][]
			{
				{"/Ludus Latrunculorum.lud", "Ruleset/6x6 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/6x6 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/6x7 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/6x7 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/6x8 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/6x8 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/7x8 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/7x8 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/8x8 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/8x8 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/8x9 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/8x9 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/10x10 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/10x10 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/11x16 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/11x16 (Kharebga Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/9x10 (Seega Rules) (Suggested)"},
				{"/Ludus Latrunculorum.lud", "Ruleset/9x10 (Kharebga Rules) (Suggested)"},
				
				{"/Poprad Game.lud", "Ruleset/17x17 (Seega Rules) (Suggested)"},
				{"/Poprad Game.lud", "Ruleset/17x17 (Kharebga Rules) (Suggested)"},
				{"/Poprad Game.lud", "Ruleset/17x17 (Tablut Rules) (Suggested)"},
				{"/Poprad Game.lud", "Ruleset/17x18 (Seega Rules) (Suggested)"},
				{"/Poprad Game.lud", "Ruleset/17x18 (Kharebga Rules) (Suggested)"},
				{"/Poprad Game.lud", "Ruleset/17x17 (Tablut Rules More Pieces) (Suggested)"},
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private EvalTrainedLudusScriptsGen()
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
		
		for (final String[] gameRulesetArray : GAMES_RULESETS)
		{
			final Game game = GameLoader.loadGameFromName(gameRulesetArray[0], gameRulesetArray[1]);
			
			if (game == null)
				System.err.println("ERROR! Failed to compile " + gameRulesetArray[0] + ", " + gameRulesetArray[1]);
			
			final String filepathsGameName = StringRoutines.cleanGameName(gameRulesetArray[0].replaceAll(Pattern.quote("/"), ""));
			final String filepathsRulesetName = StringRoutines.cleanRulesetName(gameRulesetArray[1].replaceAll(Pattern.quote("Ruleset/"), ""));
			
			final String agentToEval = 
					StringRoutines.join
					(
						";", 
						"algorithm=MCTS",
						"selection=noisyag0selection",
						"playout=random,playoutturnlimit=0",
						"tree_reuse=true",
						StringRoutines.join
						(
							",",
							"learned_selection_policy=softmax",
							"policyweights1=/work/" + userName + "/TrainLudus/" + filepathsGameName + filepathsRulesetName + "/PolicyWeightsCE_P1_00201.txt",
							"policyweights2=/work/" + userName + "/TrainLudus/" + filepathsGameName + filepathsRulesetName + "/PolicyWeightsCE_P2_00201.txt"
						),
						"heuristics=value-func-dir-/work/" + userName + "/TrainLudus/" + filepathsGameName + filepathsRulesetName + "/",
						"friendly_name=Trained"
					);
			
			final List<String> opponentAgents = new ArrayList<String>();
			final List<String> opponentAgentShortNames = new ArrayList<String>();
			opponentAgents.add("UCT");
			opponentAgentShortNames.add("UCT");
			opponentAgents.add
			(
				StringRoutines.join
				(
					";",
					"algorithm=AlphaBeta",
					"heuristics=value-func-dir-/work/" + userName + "/TrainLudus/" + filepathsGameName + filepathsRulesetName + "/"
				)
			);
			opponentAgentShortNames.add("AlphaBeta");
			opponentAgents.add
			(
				StringRoutines.join
				(
					";", 
					"algorithm=MCTS",
					"selection=ucb1",
					"playout=random,playoutturnlimit=0",
					"tree_reuse=true",
					"heuristics=/work/" + userName + "/TrainLudus/" + filepathsGameName + filepathsRulesetName + "/ValueFunction_00000.txt",
					"friendly_name=AlphaBeta"
				)
			);
			opponentAgentShortNames.add("Untrained");
			opponentAgents.add
			(
				StringRoutines.join
				(
					";", 
					"algorithm=MCTS",
					"selection=ucb1",
					"playout=random,playoutturnlimit=0",
					"tree_reuse=true",
					"heuristics=value-func-dir-/work/" + userName + "/TrainLudus/" + filepathsGameName + filepathsRulesetName + "/",
					"friendly_name=Untrained"
				)
			);
			opponentAgentShortNames.add("NoFeatures");
			
			for (int oppIdx = 0; oppIdx < opponentAgents.size(); ++oppIdx)
			{
				final String jobScriptFilename = 
						"EvalTrainedLudus" + filepathsGameName + filepathsRulesetName + 
						"_vs_" + opponentAgentShortNames.get(oppIdx) + ".sh";
	
				try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
				{
					writer.println("#!/usr/local_rwth/bin/zsh");
					writer.println("#SBATCH -J EvalTrainedLudus_" + filepathsGameName + filepathsRulesetName);
					writer.println("#SBATCH -o /work/" + userName + "/EvalTrainedLudus/Out"
							+ filepathsGameName + filepathsRulesetName + "_vs_" + opponentAgentShortNames.get(oppIdx) + "_%J.out");
					writer.println("#SBATCH -e /work/" + userName + "/EvalTrainedLudus/Err"
							+ filepathsGameName + filepathsRulesetName + "_vs_" + opponentAgentShortNames.get(oppIdx) + "_%J.err");
					writer.println("#SBATCH -t " + MAX_WALL_TIME);
					writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
					writer.println("#SBATCH -A " + argParse.getValueString("--project"));
					writer.println("unset JAVA_TOOL_OPTIONS");
	
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
								StringRoutines.quote("/home/" + userName + "/EvalTrainedLudus/Ludii.jar"),
								"--eval-agents",
								"--game",
								StringRoutines.quote(gameRulesetArray[0]),
								"--ruleset",
								StringRoutines.quote(gameRulesetArray[1]),
								"--agents",
								StringRoutines.quote(agentToEval),
								StringRoutines.quote(opponentAgents.get(oppIdx)),
								"-n 100",
								"--thinking-time 1.1",
								"--out-dir",
								StringRoutines.quote
								(
									"/work/" + 
									userName + 
									"/EvalTrainedLudus/" + 
									filepathsGameName + filepathsRulesetName + "_vs_" + opponentAgentShortNames.get(oppIdx) + "/"
								),
								"--output-summary",
								"--output-alpha-rank-data"
							);
	
					writer.println(javaCall);
	
					jobScriptNames.add(jobScriptFilename);
				}
				catch (final FileNotFoundException | UnsupportedEncodingException e)
				{
					e.printStackTrace();
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
					"Generates heuristic optimisation scripts for Ludus Latrunculorum / Poprad Game rulesets"
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
