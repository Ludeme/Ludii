package supplementary.experiments.debugging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.collections.ListUtils;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import utils.AIFactory;
import utils.experiments.InterruptableExperiment;

/**
 * Implementation of an experiment that runs trials until one crashes,
 * and then saves the trial right before the crash.
 * 
 * @author Dennis Soemers
 */
public class FindCrashingTrial
{
	/* Game setup */

	/** Name of the game to play. Should end with .lud */
	private String gameName;
	
	/** List of game options to use when compiling game */
	private List<String> gameOptions;


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

	// ------------------------------------------------------------------------------
	
	/* Agents setup */
	
	/** Strings describing agents to use */
	private List<String> agentStrings;

	// ------------------------------------------------------------------------------
	
	/** File saving stuff and other outputs */
	
	/** Output directory */
	private File outTrialFile;
	
	/** Whether we allow printing some messages to System.out */
	protected boolean printOut = true;
	
	// ------------------------------------------------------------------------------
	
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
	public FindCrashingTrial()
	{
		// all defaults already set above
	}

	/**
	 * Constructor. No wall time limit.
	 * @param useGUI
	 */
	public FindCrashingTrial(final boolean useGUI)
	{
		this.useGUI = useGUI;
	}
	
	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	public FindCrashingTrial(final boolean useGUI, final int maxWallTime)
	{
		this.useGUI = useGUI;
		this.maxWallTime = maxWallTime;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Starts the experiment
	 */
	@SuppressWarnings("unused")
	public void startExperiment()
	{
		final List<AI> ais = new ArrayList<AI>(agentStrings.size());
		for (final String agent : agentStrings)
		{
			ais.add(AIFactory.createAI(agent));
		}
		
		final Game game = GameLoader.loadGameFromName(gameName, gameOptions);
		
		if (game == null)
		{
			System.err.println("Could not instantiate game. Aborting match. Game name = " + gameName + ".");
			return;
		}

		final int numPlayers = game.players().count();
		
		if (ais.size() != numPlayers)
		{
			System.err.println
			(
				"Expected " + numPlayers + 
				" agents, but received list of " + ais.size() + 
				" agents. Aborting match."
			);
			return;
		}
		
		if (gameLengthCap >= 0)
			game.setMaxTurns(Math.min(gameLengthCap, game.getMaxTurnLimit()));
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final byte[] gameStartRngState = new byte[((RandomProviderDefaultState) context.rng().saveState()).getState().length];

		try
		{
			new InterruptableExperiment(useGUI, maxWallTime)
			{
	
				@Override
				public void runExperiment()
				{
					final int numGamesToPlay = numGames;
					List<TIntArrayList> aiListPermutations = new ArrayList<TIntArrayList>();
					
					if (rotateAgents)
					{
						// compute all possible permutations of indices for the list of AIs
						aiListPermutations = ListUtils.generatePermutations(
							TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray())
							);
					}
					else
					{
						// only need a single permutation; order in which AIs were given to us
						aiListPermutations.add(TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()));
					}
					
					for (int gameCounter = 0; gameCounter < numGamesToPlay; ++gameCounter)
					{
						if (printOut)
							System.out.println("starting game " + gameCounter);
						
						checkWallTime(0.05);
						
						if (interrupted)
						{
							// time to abort the experiment due to wall time
							break;
						}
						
						// compute list of AIs to use for this game
						// (we rotate every game)
						final List<AI> currentAIList = new ArrayList<AI>(numPlayers);
						final int currentAIsPermutation = gameCounter % aiListPermutations.size();
						
						final TIntArrayList currentPlayersPermutation = aiListPermutations.get(currentAIsPermutation);
						currentAIList.add(null); // 0 index not used
	
						for (int i = 0; i < currentPlayersPermutation.size(); ++i)
						{
							currentAIList.add
							(
								ais.get(currentPlayersPermutation.getQuick(i) % ais.size())
							);
						}
	
						// play a game
						final byte[] newRNGState = ((RandomProviderDefaultState) context.rng().saveState()).getState();
						for (int i = 0; i < gameStartRngState.length; ++i)
						{
							gameStartRngState[i] = newRNGState[i];
						}
						game.start(context);
	
						for (int p = 1; p < currentAIList.size(); ++p)
						{
							currentAIList.get(p).initAI(game, p);
						}
						
						final Model model = context.model();
	
						while (!context.trial().over())
						{
							if (interrupted)
							{
								// time to abort the experiment due to wall time						
								break;
							}
							
							model.startNewStep(context, currentAIList, thinkingTime, iterationLimit, depthLimit, 0.0);
						}
						
						// Close AIs
						for (int p = 1; p < currentAIList.size(); ++p)
						{
							currentAIList.get(p).closeAI();
						}
					}
				}	
			};
		}
		catch (final Exception | Error e)
		{
			e.printStackTrace();
			
			try
			{
				System.out.println("Saving to file: " + outTrialFile.getAbsolutePath());
				trial.saveTrialToTextFile(outTrialFile, gameName, gameOptions, new RandomProviderDefaultState(gameStartRngState));
			}
			catch (final IOException ioException)
			{
				ioException.printStackTrace();
			}
			
			return;
		}
		
		if (printOut)
			System.out.println("No game crashed!");
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
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Run games until one crashes, and save the trial that causes a crash. "
					+ "Only intended for debugging purposes."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game to play. Should end with \".lud\".")
				.setRequired()
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--game-options")
				.help("Game Options to load.")
				.withDefault(new ArrayList<String>(0))
				.withNumVals("*")
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--agents")
				.help("Agents which should be evaluated")
				.withDefault(Arrays.asList("Random", "Random"))
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
				.withNames("--out-trial-file")
				.help("Filepath for output trial")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--no-print-out")
				.help("Suppress print messages to System.out")
				.withNumVals(0)
				.withType(OptionTypes.Boolean));
		
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
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final FindCrashingTrial eval = 
				new FindCrashingTrial
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		eval.gameName = argParse.getValueString("--game");
		eval.gameOptions = (List<String>) argParse.getValue("--game-options"); 
		
		eval.agentStrings = (List<String>) argParse.getValue("--agents");
		
		eval.numGames = argParse.getValueInt("-n");
		eval.gameLengthCap = argParse.getValueInt("--game-length-cap");
		eval.thinkingTime = argParse.getValueDouble("--thinking-time");
		eval.iterationLimit = argParse.getValueInt("--iteration-limit");
		eval.depthLimit = argParse.getValueInt("--depth-limit");
		eval.rotateAgents = !argParse.getValueBool("--no-rotate-agents");
		eval.printOut = !argParse.getValueBool("--no-print-out");

		final String outTrialFilepath = argParse.getValueString("--out-trial-file");
		if (outTrialFilepath != null)
			eval.outTrialFile = new File(outTrialFilepath);
		else
			eval.outTrialFile = null;
		
		eval.startExperiment();
	}

}
