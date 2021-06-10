package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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
import search.mcts.MCTS;
import search.minimax.AlphaBetaSearch;

/**
 * Class with main method to automatically generate all the appropriate evaluation
 * scripts for gating in ExIt on the cluster.
 *
 * @author Dennis Soemers
 */
public class GenerateGatingScripts
{
	/** Memory to assign per CPU, in MB */
	private static final String MEM_PER_CPU = "5120";
	
	/** Memory to assign to JVM, in MB */
	private static final String JVM_MEM = "4096";
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 4000;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;
	
	// A bunch of dummy AIs, used to check which trainable algorithms are valid in which game
	private static final AlphaBetaSearch dummyAlphaBeta = new AlphaBetaSearch();
	private static final MCTS dummyUCT = MCTS.createUCT();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private GenerateGatingScripts()
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
		
		String scriptsDirPath = argParse.getValueString("--scripts-dir");
		scriptsDirPath = scriptsDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDirPath.endsWith("/"))
			scriptsDirPath += "/";
		final File scriptsDir = new File(scriptsDirPath);
		if (!scriptsDir.exists())
			scriptsDir.mkdirs();
		
		String trainingOutDirPath  = argParse.getValueString("--training-out-dir");
		trainingOutDirPath = trainingOutDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!trainingOutDirPath.endsWith("/"))
			trainingOutDirPath += "/";
		
		String bestAgentsDataDirPath = argParse.getValueString("--best-agents-data-dir");
		bestAgentsDataDirPath = bestAgentsDataDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!bestAgentsDataDirPath.endsWith("/"))
			bestAgentsDataDirPath += "/";
		
		final File bestAgentsDataDir = new File(bestAgentsDataDirPath);
		if (!bestAgentsDataDir.exists())
			bestAgentsDataDir.mkdirs();
		
		final String userName = argParse.getValueString("--user-name");
		
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter
				(
					s -> 
					(
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
						!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
					)
				).toArray(String[]::new);
		
		// Loop through all the games we have
		for (final String fullGamePath : allGameNames)
		{
			final String[] gamePathParts = fullGamePath.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
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
				
				if (game.isStacking())
					continue;
				
				if (game.hiddenInformation())
					continue;
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final File rulesetExItOutDir = new File(trainingOutDirPath + filepathsGameName + filepathsRulesetName);
				if (rulesetExItOutDir.exists() && rulesetExItOutDir.isDirectory())
				{
					final int numPlayers = game.players().count();
		
					final File bestAgentsDataDirForGame = 
							new File(bestAgentsDataDir.getAbsolutePath() + "/" + filepathsGameName + filepathsRulesetName);
					
					// Now write our gating experiment scripts
					final List<String> agentsToEval = new ArrayList<String>();
					final List<List<String>> evalFeatureWeightFilepaths = new ArrayList<List<String>>();
					final List<String> evalHeuristicsFilepaths = new ArrayList<String>();
					final List<List<String>> gateAgentTypes = new ArrayList<List<String>>();
					
					final File[] trainingOutFiles = rulesetExItOutDir.listFiles();
					if (trainingOutFiles == null || trainingOutFiles.length == 0)
					{
						System.err.println("No training out files for: " + rulesetExItOutDir.getAbsolutePath());
						return;
					}
					
					// Find latest value function and feature files
					File latestValueFunctionFile = null;
					int latestValueFunctionCheckpoint = 0;
					
					final File[] latestPolicyWeightFiles = new File[numPlayers + 1];
					final int[] latestPolicyWeightCheckpoints = new int[numPlayers + 1];
					boolean foundPolicyWeights = false;
					
					for (final File file : trainingOutFiles)
					{
						String filename = file.getName();
						// remove extension
						filename = filename.substring(0, filename.lastIndexOf('.'));
						
						final String[] filenameSplit = filename.split(Pattern.quote("_"));
						
						if (filename.startsWith("PolicyWeightsCE_P"))
						{
							final int checkpoint = Integer.parseInt(filenameSplit[2]);
							final int p = Integer.parseInt(filenameSplit[1].substring(1));
							
							if (checkpoint > latestPolicyWeightCheckpoints[p])
							{
								foundPolicyWeights = true;
								latestPolicyWeightFiles[p] = file;
								latestPolicyWeightCheckpoints[p] = checkpoint;
							}
						}
						else if (filename.startsWith("ValueFunction"))
						{
							final int checkpoint = Integer.parseInt(filenameSplit[1]);
							if (checkpoint > latestValueFunctionCheckpoint)
							{
								latestValueFunctionFile = file;
								latestValueFunctionCheckpoint = checkpoint;
							}
						}
					}
					
					int numMatchups = 0;
					
					if (dummyAlphaBeta.supportsGame(game))
					{
						if (latestValueFunctionCheckpoint > 0)
						{
							agentsToEval.add("Alpha-Beta");
							evalFeatureWeightFilepaths.add(new ArrayList<String>());
							evalHeuristicsFilepaths.add(latestValueFunctionFile.getAbsolutePath());
							
							final List<String> gateAgents = new ArrayList<String>();
							gateAgents.add("Alpha-Beta");
							gateAgents.add("BestAgent");
							gateAgentTypes.add(gateAgents);
							
							numMatchups += 2;
						}
					}
					
					if (dummyUCT.supportsGame(game))
					{
						if (foundPolicyWeights)
						{
							final List<String> policyWeightFiles = new ArrayList<String>();
							for (int p = 1; p < latestPolicyWeightFiles.length; ++p)
							{
								policyWeightFiles.add(latestPolicyWeightFiles[p].toString());
							}
							
							agentsToEval.add("BiasedMCTS");
							evalFeatureWeightFilepaths.add(policyWeightFiles);
							evalHeuristicsFilepaths.add(null);
							
							List<String> gateAgents = new ArrayList<String>();
							if (new File(bestAgentsDataDirForGame.getAbsolutePath() + "/BestFeatures.txt").exists())
							{
								gateAgents.add("BiasedMCTS");
								numMatchups += 1;
							}
							gateAgents.add("BestAgent");
							numMatchups += 1;
							
							gateAgentTypes.add(gateAgents);
							
							agentsToEval.add("BiasedMCTSUniformPlayouts");
							evalFeatureWeightFilepaths.add(policyWeightFiles);
							evalHeuristicsFilepaths.add(null);
							
							gateAgents = new ArrayList<String>();
							if (new File(bestAgentsDataDirForGame.getAbsolutePath() + "/BestFeatures.txt").exists())
							{
								gateAgents.add("BiasedMCTS");
								numMatchups += 1;
							}
							gateAgents.add("BestAgent");
							numMatchups += 1;
							
							gateAgentTypes.add(gateAgents);
						}
					}
					
					final List<String> javaCalls = new ArrayList<String>();
					
					for (int evalAgentIdx = 0; evalAgentIdx < agentsToEval.size(); ++evalAgentIdx)
					{
						final String agentToEval = agentsToEval.get(evalAgentIdx);
						final List<String> featureWeightFilepaths = evalFeatureWeightFilepaths.get(evalAgentIdx);
						final String heuristicFilepath = evalHeuristicsFilepaths.get(evalAgentIdx);
						final List<String> gateAgents = gateAgentTypes.get(evalAgentIdx);
			
						for (final String gateAgent : gateAgents)
						{
							String javaCall = StringRoutines.join
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
								StringRoutines.quote("/home/" + userName + "/Gating/Ludii.jar"),
								"--eval-gate",
								"--game",
								StringRoutines.quote(gameName),
								"--ruleset",
				                StringRoutines.quote(fullRulesetName),
								"--eval-agent",
								StringRoutines.quote(agentToEval),
								"-n 75",
								"--game-length-cap 800",
								"--thinking-time 1",
								"--best-agents-data-dir",
								StringRoutines.quote(bestAgentsDataDirForGame.getAbsolutePath()),
								"--gate-agent-type",
								gateAgent,
								"--max-wall-time",
								"" + Math.max(1000, (MAX_WALL_TIME / numMatchups))
							);
				
							if (featureWeightFilepaths.size() > 0)
								javaCall += " --eval-feature-weights-filepaths " + StringRoutines.join(" ", featureWeightFilepaths);
				
							if (heuristicFilepath != null)
								javaCall += " --eval-heuristics-filepath " + heuristicFilepath;
							
							javaCalls.add(javaCall);
						}
					}
					
					final String jobScriptFilename = "Gating_" + filepathsGameName + filepathsRulesetName + ".sh";
			
					try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "/" + jobScriptFilename), "UTF-8"))
					{
						writer.println("#!/usr/local_rwth/bin/zsh");
						writer.println("#SBATCH -J Gating_" + filepathsGameName + filepathsRulesetName);
						writer.println("#SBATCH -o /work/" + userName + "/Gating/Out"
								+ filepathsGameName + filepathsRulesetName + "_%J.out");
						writer.println("#SBATCH -e /work/" + userName + "/Gating/Err"
								+ filepathsGameName + filepathsRulesetName + "_%J.err");
						writer.println("#SBATCH -t " + MAX_WALL_TIME);
						writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
						writer.println("#SBATCH -A " + argParse.getValueString("--project"));
						writer.println("unset JAVA_TOOL_OPTIONS");
						
						for (final String javaCall : javaCalls)
						{
							writer.println(javaCall);
						}
						
						jobScriptNames.add(jobScriptFilename);
					}
					catch (final FileNotFoundException | UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
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
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "/SubmitJobs_Part" + i + ".sh"), "UTF-8"))
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
					"Generates gating scripts for cluster to evaluate which trained agents outperform current default agents."
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
		
		argParse.addOption(new ArgOption()
				.withNames("--training-out-dir")
				.help("Base output directory that contains all the results from training.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--best-agents-data-dir")
				.help("Base directory in which we store data about the best agents per game.")
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
