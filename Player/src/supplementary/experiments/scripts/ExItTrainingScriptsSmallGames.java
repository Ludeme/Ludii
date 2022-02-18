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
 * Generates scripts for training runs on small games (generally ones
 * where we can probably easily reach (close to) perfect play.
 *
 * @author Dennis Soemers
 */
public class ExItTrainingScriptsSmallGames
{
	
	//-------------------------------------------------------------------------

	/** Memory to assign to JVM */
	private static final String JVM_MEM = "5120";

	/** Max number of self-play trials */
	private static final int MAX_SELFPLAY_TRIALS = 200;
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 2880;
	
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
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private ExItTrainingScriptsSmallGames()
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

		// First create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		for (int idx : sortedGameIndices)
		{
			final Game game = compiledGames[idx];
			final String gameName = GAMES[idx];
			final String rulesetName = RULESETS[idx];
			processDataList.add(new ProcessData(gameName, rulesetName, game.players().count()));
		}
		
		int processIdx = 0;
		while (processIdx < processDataList.size())
		{
			// Start a new job script
			final String jobScriptFilename = "TrainFeatures_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("mkdir /home/USERNAME/TrainFeaturesSmallGames/Out");

				int numJobProcesses = 0;
				while (numJobProcesses < processDataList.size())
				{
					final ProcessData processData = processDataList.get(processIdx);
					
					final int numFeatureDiscoveryThreads = processData.numPlayers;
					final String cleanGameName = StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), ""));
					final String cleanRulesetName = StringRoutines.cleanRulesetName(processData.rulesetName).replaceAll(Pattern.quote("/"), "_");
					
					// Write Java call for this process
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
								StringRoutines.quote("/home/" + "USERNAME" + "/TrainFeaturesSmallGames/Ludii.jar"),
								"--expert-iteration",
								"--game",
								StringRoutines.quote("/" + processData.gameName),
								"--ruleset",
								StringRoutines.quote(processData.rulesetName),
								"-n",
								String.valueOf(MAX_SELFPLAY_TRIALS),
								"--game-length-cap 1000",
								"--thinking-time 1",
								"--is-episode-durations",
								"--prioritized-experience-replay",
								"--wis",
								"--playout-features-epsilon 0.5",
								"--num-policy-gradient-epochs 100",
								"--pg-gamma 0.9",
								"--handle-aliasing",
								"--no-value-learning",
								"--train-tspg",
								"--checkpoint-freq 5",
								"--num-agent-threads",
								String.valueOf(1),
								"--num-policy-gradient-threads",
								String.valueOf(1),
								"--post-pg-weight-scalar 0.0",
								"--num-feature-discovery-threads",
								String.valueOf(numFeatureDiscoveryThreads),
								"--out-dir",
								StringRoutines.quote
								(
									"/home/" + 
									"USERNAME" + 
									"/TrainFeaturesSmallGames/Out/" + 
									cleanGameName +
									"_" +
									cleanRulesetName
									+ 
									"/"
								),
								"--no-logging",
								"--max-wall-time",
								String.valueOf(MAX_WALL_TIME)
							);
					
					javaCall += " " + StringRoutines.join
							(
								" ",
								">",
								"/home/" + "USERNAME" + "/TrainFeaturesSmallGames/Out/Out_" + cleanGameName + "_" + cleanRulesetName + ".out",
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
					"Creating feature training job scripts for small games."
				);
		
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
