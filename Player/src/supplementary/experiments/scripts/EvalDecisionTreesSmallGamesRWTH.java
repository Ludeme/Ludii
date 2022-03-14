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
import main.collections.ListUtils;
import other.GameLoader;
import supplementary.experiments.analysis.RulesetConceptsUCT;
import utils.RulesetNames;

/**
 * Script to generate scripts for evaluation of training runs
 *
 * @author Dennis Soemers
 */
public class EvalDecisionTreesSmallGamesRWTH
{
	
	/** Memory to assign to JVM */
	private static final String JVM_MEM = "4096";
	
	/** Memory to assign per CPU, in MB */
	private static final String MEM_PER_CPU = "5120";
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 3000;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;
	
	/** Num trials per matchup */
	private static final int NUM_TRIALS = 50;
	
	/** Games we want to run */
	private static final String[] GAMES = 
			new String[]
			{
				"Tic-Tac-Toe.lud",
				"Mu Torere.lud",
				"Mu Torere.lud",
				"Jeu Militaire.lud",
				"Pong Hau K'i.lud",
				"Akidada.lud",
				"Alquerque de Tres.lud",
				"Ho-Bag Gonu.lud",
				"Madelinette.lud",
				"Haretavl.lud",
				"Kaooa.lud",
				"Hat Diviyan Keliya.lud",
				"Three Men's Morris.lud"
			};
	
	/** Rulesets we want to run */
	private static final String[] RULESETS = 
			new String[]
			{
				"",
				"Ruleset/Complete (Observed)",
				"Ruleset/Simple (Suggested)",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				""
			};
	
	private static final String[] TREE_TYPES =
			new String[]
			{
				"BinaryClassificationTree_Playout",
				"BinaryClassificationTree_TSPG",
				"ImbalancedBinaryClassificationTree_Playout",
				"ImbalancedBinaryClassificationTree_TSPG",
				"IQRTree_Playout",
				"IQRTree_TSPG",
				"LogitRegressionTree_Playout",
				"LogitRegressionTree_TSPG"
			};
	
	private static final int[] TREE_DEPTHS = new int[] {1, 2, 3, 4, 5, 10};
	
	private static final String[] EXPERIMENT_TYPES = new String[] {"Greedy", "Sampling"};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private EvalDecisionTreesSmallGamesRWTH()
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
			final Game game = GameLoader.loadGameFromName(GAMES[i], RULESETS[i]);

			if (game == null)
				throw new IllegalArgumentException("Cannot load game: " + GAMES[i] + " " + RULESETS[i]);

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
		
		// All the "algorithms" we want to evaluate
		final List<String> algorithms = new ArrayList<String>();
		algorithms.add("Random");
		algorithms.add("CE");
		algorithms.add("TSPG");
		algorithms.add("UCT");
		
		for (final String treeType : TREE_TYPES)
		{
			for (final int treeDepth : TREE_DEPTHS)
			{
				algorithms.add(treeType + "_" + treeDepth);
			}
		}
		
		final List<Object[][]> matchupsPerPlayerCount = new ArrayList<Object[][]>();

		// First create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		for (int idx : sortedGameIndices)
		{
			final Game game = compiledGames[idx];
			final String gameName = GAMES[idx];
			final String rulesetName = RULESETS[idx];
			
			final int numPlayers = game.players().count();
			
			// Check if we already have a matrix of matchup-lists for this player count
			while (matchupsPerPlayerCount.size() <= numPlayers)
			{
				matchupsPerPlayerCount.add(null);
			}
			
			if (matchupsPerPlayerCount.get(numPlayers) == null)
				matchupsPerPlayerCount.set(numPlayers, ListUtils.generateCombinationsWithReplacement(algorithms.toArray(), numPlayers));
			
			// We already ran most matchups on another cluster, only still need to do the ones
			// that contain at least one out of UCT, CE and TSPG
			for (final Object[] matchup : matchupsPerPlayerCount.get(numPlayers))
			{
				boolean keep = false;
				for (final Object obj : matchup)
				{
					final String s = (String) obj;
					if (s.equals("UCT") || s.equals("CE") || s.equals("TSPG"))
					{
						keep = true;
						break;
					}
				}
				
				if (keep)
				{
					for (final String experimentType : EXPERIMENT_TYPES)
					{
						processDataList.add(new ProcessData(gameName, rulesetName, matchup, experimentType, numPlayers));
					}
				}
			}
		}
		
		for (int processIdx = 0; processIdx < processDataList.size(); ++processIdx)
		{
			// Start a new job script
			final String jobScriptFilename = "EvalDecisionTrees_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				final ProcessData processData = processDataList.get(processIdx);
				final String cleanGameName = StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), ""));
				final String cleanRulesetName = StringRoutines.cleanRulesetName(processData.rulesetName).replaceAll(Pattern.quote("/"), "_");
				
				writer.println("#!/usr/local_rwth/bin/zsh");
				writer.println("#SBATCH -J Eval" + cleanGameName + cleanRulesetName);
				writer.println("#SBATCH -o /work/" + userName + "/EvalSmallGames/Out/Out"
						+ cleanGameName + cleanRulesetName + processData.matchup[0] + processData.matchup[1] + "_%J.out");
				writer.println("#SBATCH -e /work/" + userName + "/EvalSmallGames/Out/Err"
						+ cleanGameName + cleanRulesetName + processData.matchup[0] + processData.matchup[1] + "_%J.err");
				writer.println("#SBATCH -t " + MAX_WALL_TIME);
				writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
				writer.println("#SBATCH -A " + argParse.getValueString("--project"));
				writer.println("unset JAVA_TOOL_OPTIONS");
				
				final List<String> agentStrings = new ArrayList<String>();
				for (final Object agent : processData.matchup)
				{
					final String s = (String) agent;
					
					final String agentStr;
					
					if (s.equals("Random"))
					{
						agentStr = "Random";
					}
					else if (s.equals("UCT"))
					{
						agentStr = "UCT";
					}
					else if (s.equals("CE") || s.equals("TSPG"))
					{
						final String algName = (processData.experimentType.equals("Greedy")) ? "Greedy" : "Softmax";
						final String weightsFileName = (s.equals("CE")) ? "PolicyWeightsPlayout_P" : "PolicyWeightsTSPG_P";
						
						final List<String> policyStrParts = new ArrayList<String>();
						policyStrParts.add("algorithm=" + algName);
						for (int p = 1; p <= processData.numPlayers; ++p)
						{
							policyStrParts.add
							(
								"policyweights" + 
								p + 
								"=/work/" + 
								userName + 
								"/SmallGames/Out/" + 
								cleanGameName + "_" + cleanRulesetName +
								"/" + weightsFileName + p + "_00201.txt"
							);
						}
						policyStrParts.add("friendly_name=" + s);
						
						if (s.equals("TSPG"))
							policyStrParts.add("boosted=true");
						
						agentStr = 
								StringRoutines.join
								(
									";", 
									policyStrParts
								);
					}
					else if (s.startsWith("LogitRegressionTree"))
					{
						agentStr = 
								StringRoutines.join
								(
									";", 
									"algorithm=SoftmaxPolicyLogitTree",
									"policytrees=/" + 
									StringRoutines.join
									(
										"/", 
										"work",
										userName,
										"SmallGames",
										"Out",
										"Trees",
										cleanGameName + "_" + cleanRulesetName,
										s + ".txt"
									),
									"friendly_name=" + s,
									"greedy=" + ((processData.experimentType.equals("Greedy")) ? "true" : "false")
								);
					}
					else
					{
						agentStr = 
								StringRoutines.join
								(
									";", 
									"algorithm=ProportionalPolicyClassificationTree",
									"policytrees=/" + 
									StringRoutines.join
									(
										"/", 
										"work",
										userName,
										"SmallGames",
										"Out",
										"Trees",
										cleanGameName + "_" + cleanRulesetName,
										s + ".txt"
									),
									"friendly_name=" + s,
									"greedy=" + ((processData.experimentType.equals("Greedy")) ? "true" : "false")
								);
					}

					agentStrings.add(StringRoutines.quote(agentStr));
				}
				
				// Write Java call for this process
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
							StringRoutines.quote("/home/" + userName + "/SmallGames/Ludii.jar"),
							"--eval-agents",
							"--game",
							StringRoutines.quote("/" + processData.gameName),
							"--ruleset",
							StringRoutines.quote(processData.rulesetName),
							"-n " + NUM_TRIALS,
							"--thinking-time 1",
							"--agents",
							StringRoutines.join(" ", agentStrings),
							"--warming-up-secs",
							String.valueOf(0),
							"--game-length-cap",
							String.valueOf(250),
							"--out-dir",
							StringRoutines.quote
							(
								"/work/" + 
								userName + 
								"/EvalSmallGames/Out/" + 
								processData.experimentType + "/" +
								cleanGameName +
								"_" +
								cleanRulesetName
								+ 
								"/" +
								StringRoutines.join("_", processData.matchup)
							),
							"--output-summary",
							"--output-alpha-rank-data",
							"--max-wall-time",
							String.valueOf(MAX_WALL_TIME)
						);
				
				writer.println(javaCall);

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
		public final String rulesetName;
		public final Object[] matchup;
		public final String experimentType;
		public final int numPlayers;
		
		/**
		 * Constructor
		 * @param gameName
		 * @param rulesetName
		 * @param matchup
		 * @param experimentType
		 * @param numPlayers
		 */
		public ProcessData(final String gameName, final String rulesetName, final Object[] matchup, final String experimentType, final int numPlayers)
		{
			this.gameName = gameName;
			this.rulesetName = rulesetName;
			this.matchup = matchup;
			this.experimentType = experimentType;
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
					"Creating eval job scripts."
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
