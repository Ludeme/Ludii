package supplementary.experiments.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import features.feature_sets.network.JITSPatterNetFeatureSet;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import other.AI;
import supplementary.experiments.EvalGamesSet;
import utils.AIFactory;

/**
 * Implementation of an experiment that evaluates the performance
 * of different Agents.
 * 
 * @author Dennis Soemers
 */
public class EvalAgents
{
	/* Game setup */

	/** Name of the game to play. Should end with .lud */
	protected String gameName;
	
	/** List of game options to use when compiling game */
	protected List<String> gameOptions = new ArrayList<String>(0);
	
	/** Name of ruleset to compile. Any options will be ignored if ruleset is provided. */
	protected String ruleset;

	//-------------------------------------------------------------------------
	/* Basic experiment setup */

	/** Number of evaluation games to run */
	protected int numGames;
	
	/** Maximum game duration (in moves) */
	protected int gameLengthCap;

	/** Max allowed thinking time per move (in seconds) */
	protected double thinkingTime;

	/** Max allowed number of MCTS iterations per move */
	protected int iterationLimit;
	
	/** Max search depth (for e.g. alpha-beta) */
	protected int depthLimit;
	
	/** Whether to rotate through agent-to-player assignments */
	protected boolean rotateAgents;
	
	/** Number of seconds for warming-up of JVM */
	protected int warmingUpSecs;
	
	/** If true, increase number of games to play to next number that can be divided by number of permutations of agents */
	protected boolean roundToNextPermutationsDivisor;

	//-------------------------------------------------------------------------
	/* Agents setup */
	
	/** Strings describing agents to use */
	protected List<String> agentStrings;
	
	//-------------------------------------------------------------------------
	
	/** File saving stuff and other outputs */
	
	/** Output directory */
	protected File outDir;
	
	/** Whether we want to output a human-readable(ish) summary of results */
	protected boolean outputSummary;
	
	/** Whether we want to output data for alpha-rank */
	protected boolean outputAlphaRankData;
	
	/** Whether we want to output all raw results */
	protected boolean outputRawResults;
	
	/** Whether we want to print general messages to System.out */
	protected boolean printOut;
	
	/** Suppress warnings about number of trials not being divisible by number of permutations of agents */
	protected boolean suppressDivisorWarning = false;

	//-------------------------------------------------------------------------
	
	/* Auxiliary experiment setup */
	
	/** 
	 * Whether to create a small GUI that can be used to manually interrupt training run. 
	 * False by default. 
	 */
	protected boolean useGUI;
	
	/** Max wall time in minutes (or -1 for no limit) */
	protected int maxWallTime;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor. No GUI for interrupting experiment, no wall time limit.
	 */
	public EvalAgents()
	{
		// all defaults already set above
	}

	/**
	 * Constructor. No wall time limit.
	 * @param useGUI
	 */
	public EvalAgents(final boolean useGUI)
	{
		this.useGUI = useGUI;
	}
	
	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	public EvalAgents(final boolean useGUI, final int maxWallTime)
	{
		this.useGUI = useGUI;
		this.maxWallTime = maxWallTime;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Starts the experiment
	 */
	public void startExperiment()
	{
		final List<AI> ais = new ArrayList<AI>(agentStrings.size());
		for (final String agent : agentStrings)
		{
			ais.add(AIFactory.createAI(agent));
		}
		
		final EvalGamesSet gamesSet = 
				new EvalGamesSet(useGUI, maxWallTime)
				.setGameName(gameName)
				.setGameOptions(gameOptions)
				.setRuleset(ruleset)
				.setAgents(ais)
				.setNumGames(numGames)
				.setGameLengthCap(gameLengthCap)
				.setMaxSeconds(thinkingTime)
				.setMaxIterations(iterationLimit)
				.setMaxSearchDepth(depthLimit)
				.setRotateAgents(rotateAgents)
				.setWarmingUpSecs(warmingUpSecs)
				.setRoundToNextPermutationsDivisor(roundToNextPermutationsDivisor)
				.setOutDir(outDir)
				.setOutputAlphaRankData(outputAlphaRankData)
				.setOutputRawResults(outputRawResults)
				.setOutputSummary(outputSummary)
				.setPrintOut(printOut)
				.setSuppressDivisorWarning(suppressDivisorWarning);
		
		gamesSet.startGames();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Can be used for quick testing without command-line args, or proper
	 * testing with elaborate setup through command-line args
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{
		// Feature Set caching is safe in this main method
		JITSPatterNetFeatureSet.ALLOW_FEATURE_SET_CACHE = true;
		
		// Define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Evaluate playing strength of different agents against each other."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game to play. Should end with \".lud\".")
				.withDefault("Amazons.lud")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--game-options")
				.help("Game Options to load.")
				.withDefault(new ArrayList<String>(0))
				.withNumVals("*")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--ruleset")
				.help("Ruleset to compile.")
				.withDefault("")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--agents")
				.help("Agents which should be evaluated")
				.withDefault(Arrays.asList("UCT", "Biased MCTS"))
				.withNumVals("+")
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("-n", "--num-games", "--num-eval-games")
				.help("Number of training games to run.")
				.withDefault(Integer.valueOf(200))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--game-length-cap", "--max-num-actions")
				.help("Maximum number of actions that may be taken before a game is terminated as a draw (-1 for no limit).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--thinking-time", "--time", "--seconds")
				.help("Max allowed thinking time per move (in seconds).")
				.withDefault(Double.valueOf(1.0))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--iteration-limit", "--iterations")
				.help("Max allowed number of MCTS iterations per move.")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--depth-limit")
				.help("Max allowed search depth per move (for e.g. alpha-beta).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--no-rotate-agents")
				.help("Don't rotate through possible assignments of agents to Player IDs.")
				.withType(OptionTypes.Boolean)
				.withNumVals(0));
		argParse.addOption(new ArgOption()
				.withNames("--warming-up-secs")
				.help("Number of seconds for which to warm up JVM.")
				.withType(OptionTypes.Int)
				.withNumVals(1)
				.withDefault(Integer.valueOf(60)));
		argParse.addOption(new ArgOption()
				.withNames("--round-to-next-permutations-divisor")
				.help("Increase number of games to play to next number that can be divided by number of permutations of agents.")
				.withType(OptionTypes.Boolean)
				.withNumVals(0));

		argParse.addOption(new ArgOption()
				.withNames("--out-dir", "--output-directory")
				.help("Filepath for output directory")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--output-summary")
				.help("Output summary of results.")
				.withType(OptionTypes.Boolean)
				.withNumVals(0));
		argParse.addOption(new ArgOption()
				.withNames("--output-alpha-rank-data")
				.help("Output data for alpha-rank.")
				.withType(OptionTypes.Boolean)
				.withNumVals(0));
		argParse.addOption(new ArgOption()
				.withNames("--output-raw-results")
				.help("Output all raw results.")
				.withType(OptionTypes.Boolean)
				.withNumVals(0));
		argParse.addOption(new ArgOption()
				.withNames("--no-print-out")
				.help("Suppress general prints to System.out.")
				.withType(OptionTypes.Boolean)
				.withNumVals(0));
		argParse.addOption(new ArgOption()
				.withNames("--suppress-divisor-warning")
				.help("Suppress warning about number of trials not being divisible by number of permutations of agents.")
				.withType(OptionTypes.Boolean)
				.withNumVals(0));
		
		argParse.addOption(new ArgOption()
				.withNames("--useGUI")
				.help("Whether to create a small GUI that can be used to "
						+ "manually interrupt training run. False by default."));
		argParse.addOption(new ArgOption()
				.withNames("--max-wall-time")
				.help("Max wall time in minutes (or -1 for no limit).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		
		// Parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// Use the parsed args
		final EvalAgents eval = 
				new EvalAgents
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		eval.gameName = argParse.getValueString("--game");
		eval.gameOptions = (List<String>) argParse.getValue("--game-options"); 
		eval.ruleset = argParse.getValueString("--ruleset");
		
		eval.agentStrings = (List<String>) argParse.getValue("--agents");
		
		eval.numGames = argParse.getValueInt("-n");
		eval.gameLengthCap = argParse.getValueInt("--game-length-cap");
		eval.thinkingTime = argParse.getValueDouble("--thinking-time");
		eval.iterationLimit = argParse.getValueInt("--iteration-limit");
		eval.depthLimit = argParse.getValueInt("--depth-limit");
		eval.rotateAgents = !argParse.getValueBool("--no-rotate-agents");
		eval.warmingUpSecs = argParse.getValueInt("--warming-up-secs");
		eval.roundToNextPermutationsDivisor = argParse.getValueBool("--round-to-next-permutations-divisor");

		final String outDirFilepath = argParse.getValueString("--out-dir");
		if (outDirFilepath != null)
			eval.outDir = new File(outDirFilepath);
		else
			eval.outDir = null;
		
		eval.outputSummary = argParse.getValueBool("--output-summary");
		eval.outputAlphaRankData = argParse.getValueBool("--output-alpha-rank-data");
		eval.outputRawResults = argParse.getValueBool("--output-raw-results");
		eval.printOut = !argParse.getValueBool("--no-print-out");
		eval.suppressDivisorWarning = argParse.getValueBool("--suppress-divisor-warning");
		
		eval.startExperiment();
	}

}
