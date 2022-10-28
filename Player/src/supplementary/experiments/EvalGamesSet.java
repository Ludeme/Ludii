package supplementary.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.ListUtils;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import utils.experiments.InterruptableExperiment;
import utils.experiments.ResultsSummary;

/**
 * A set consisting of one or more games between a fixed set of agents
 * (which may rotate through permutations of assignments to player numbers)
 * 
 * @author Dennis Soemers
 */
public class EvalGamesSet
{
	/** The name of the game to play */
	protected String gameName = null;
	
	/** List of extra game options to compile */
	protected List<String> gameOptions = new ArrayList<String>();
	
	/** Ruleset to compile */
	protected String ruleset = null;
	
	/** List of AIs / agents */
	protected List<AI> agents = null;
	
	/** Number of games to run (by default 100) */
	protected int numGames = 100;
	
	/** If a game lasts for more turns than this, we'll terminate it as a draw (by default -1, i.e. no limit) */
	protected int gameLengthCap = -1;
	
	/** Max seconds per move for AI (by default 1.0 second) */
	protected double[] maxSeconds = new double[Constants.MAX_PLAYERS+1];
	
	/** Max iterations per move for AI (by default -1, i.e. no limit) */
	protected int maxIterations = -1;
	
	/** Max search depth per move for AI (by default -1, i.e. no search depth limit) */
	protected int maxSearchDepth = -1;
	
	/** Whether to rotate assignments of agents to player numbers (by default true) */
	protected boolean rotateAgents = true;
	
	/** Number of seconds for which to run a warming-up */
	protected int warmingUpSecs = 60;
	
	/** If true, we'll increase number of games to play to next number that can be divided by number of permutations of agents */
	protected boolean roundToNextPermutationsDivisor = false;
	
	/** If true, we'll print intermediate results to System.out */
	protected boolean printOut = true;
	
	/** Directory in which to save output files */
	protected File outDir = null;
	
	/** Whether we want to output a summary of results */
	protected boolean outputSummary = true;
	
	/** Whether we want to output results in a format convenient for subsequently computing alpha-ranks using OpenSpiel */
	protected boolean outputAlphaRankData = false;
	
	/** Whether to create a small GUI that can be used to manually interrupt experiment */
	protected boolean useGUI = false;
	
	/** Max wall time in minutes (or -1 for no limit) */
	protected int maxWallTime = -1;
	
	/** The results of the last experiment run. */
	protected ResultsSummary resultsSummary = null;
	
	/** Suppress warnings about number of trials not being divisible by number of permutations of agents */
	protected boolean suppressDivisorWarning = false;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor. No GUI for interrupting experiment, no wall time limit.
	 */
	public EvalGamesSet()
	{
		Arrays.fill(maxSeconds, 1.0);
	}

	/**
	 * Constructor. No wall time limit.
	 * @param useGUI
	 */
	public EvalGamesSet(final boolean useGUI)
	{
		this.useGUI = useGUI;
	}
	
	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	public EvalGamesSet(final boolean useGUI, final int maxWallTime)
	{
		this.useGUI = useGUI;
		this.maxWallTime = maxWallTime;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Starts running the set of games
	 */
	public void startGames()
	{
		final Game game;
		
		if (ruleset != null && !ruleset.equals(""))
			game = GameLoader.loadGameFromName(gameName, ruleset);
		else
			game = GameLoader.loadGameFromName(gameName, gameOptions);
		
		startGames(game);
	}
	
	/**
	 * Starts running the set of games
	 */
	
	/**
	 * Starts running the set of games, using the specified Game object
	 * @param game
	 */
	@SuppressWarnings("unused")
	public void startGames(final Game game)
	{
		if (game == null)
		{
			System.err.println("Could not instantiate game. Aborting set of games. Game name = " + gameName + ".");
			return;
		}
		
		if (agents == null)
		{
			System.err.println("No list of agents provided. Aborting set of games.");
			return;
		}
		
		final int numPlayers = game.players().count();
		
		if (agents.size() != numPlayers)
		{
			System.err.println
			(
				"Expected " + numPlayers + 
				" agents, but received list of " + agents.size() + 
				" agents. Aborting set of games."
			);
			return;
		}
		
		if (gameLengthCap >= 0)
			game.setMaxTurns(Math.min(gameLengthCap, game.getMaxTurnLimit()));
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);

		new InterruptableExperiment(useGUI, maxWallTime)
		{

			@Override
			public void runExperiment()
			{
				int numGamesToPlay = numGames;
				List<TIntArrayList> aiListPermutations = new ArrayList<TIntArrayList>();
				
				if (rotateAgents)
				{
					
					if (numPlayers <= 5)
					{
						// Compute all possible permutations of indices for the list of AIs
						aiListPermutations = ListUtils.generatePermutations(
								TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()));
						
						Collections.shuffle(aiListPermutations);
					}
					else
					{
						// Randomly generate some permutations of indices for the list of AIs
						aiListPermutations = ListUtils.samplePermutations(
								TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()), 120);
					}

					if (numGamesToPlay % aiListPermutations.size() != 0)
					{
						if (roundToNextPermutationsDivisor)
						{
							numGamesToPlay += (numGamesToPlay % aiListPermutations.size());
						}
						else if (!suppressDivisorWarning)
						{
							System.out.println
							(
								String.format
								(
									"Warning: number of games to play (%d) is "
									+ "not divisible by the number of "
									+ "permutations of list of AIs (%d)",
									Integer.valueOf(numGamesToPlay), Integer.valueOf(aiListPermutations.size())
								)
							);
						}
					}
				}
				else
				{
					// only need a single permutation; order in which AIs were given to us
					aiListPermutations.add(TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()));
				}
				
				// start with a warming up
				if (printOut)
					System.out.println("Warming up...");
				
				long stopAt = 0L;
				final long start = System.nanoTime();
				final double abortAt = start + warmingUpSecs * 1000000000.0;
				while (stopAt < abortAt)
				{
					game.start(context);
					game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
					stopAt = System.nanoTime();
				}
				System.gc();
				
				if (printOut)
					System.out.println("Finished warming up!");
				
				// prepare results writing
				final List<String> agentStrings = new ArrayList<String>();
				for (final AI ai : agents)
				{
					agentStrings.add(ai.friendlyName());
				}
				resultsSummary = new ResultsSummary(game, agentStrings);
				
				for (int gameCounter = 0; gameCounter < numGamesToPlay; ++gameCounter)
				{
					checkWallTime(0.05);
					
					if (interrupted)
					{
						// Time to abort the experiment due to wall time
						break;
					}
					
					// Compute list of AIs to use for this game
					// (we rotate every game)
					final List<AI> currentAIList = new ArrayList<AI>(numPlayers);
					final int currentAIsPermutation = gameCounter % aiListPermutations.size();
					
					final TIntArrayList currentPlayersPermutation = aiListPermutations.get(currentAIsPermutation);
					currentAIList.add(null); // 0 index not used

					for (int i = 0; i < currentPlayersPermutation.size(); ++i)
					{
						currentAIList.add
						(
							agents.get(currentPlayersPermutation.getQuick(i) % agents.size())
						);
					}

					// Play a game
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
							// Time to abort the experiment due to wall time						
							break;
						}
						
						model.startNewStep(context, currentAIList, maxSeconds, maxIterations, maxSearchDepth, 0.0);
					}

					// Record results
					if (context.trial().over())
					{
						final double[] utilities = RankUtils.agentUtilities(context);
						final int numMovesPlayed = context.trial().numMoves() - context.trial().numInitialPlacementMoves();
						final int[] agentPermutation = new int[currentPlayersPermutation.size() + 1];
						currentPlayersPermutation.toArray(agentPermutation, 0, 1, currentPlayersPermutation.size());
						
						resultsSummary().recordResults(agentPermutation, utilities, numMovesPlayed);
					}
					
					// Close AIs
					for (int p = 1; p < currentAIList.size(); ++p)
					{
						currentAIList.get(p).closeAI();
					}
					
					if (printOut && (gameCounter < 5 || gameCounter % 10 == 9))
					{
						System.out.print(resultsSummary().generateIntermediateSummary());
					}
				}
				
				if (outDir != null)
				{
					if (outputSummary)
					{
						final File outFile = new File(outDir + "/results.txt");
						outFile.getParentFile().mkdirs();
						try (final PrintWriter writer = new PrintWriter(outFile, "UTF-8"))
						{
							writer.write(resultsSummary().generateIntermediateSummary());
						}
						catch (final FileNotFoundException | UnsupportedEncodingException e)
						{
							e.printStackTrace();
						}
					}
					
					if (outputAlphaRankData)
					{
						final File outFile = new File(outDir + "/alpha_rank_data.csv");
						outFile.getParentFile().mkdirs();
						resultsSummary().writeAlphaRankData(outFile);
					}
				}
			}		
		};
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set the name of the game to be played
	 * @param gameName
	 * @return This, modified.
	 */
	public EvalGamesSet setGameName(final String gameName)
	{
		this.gameName = gameName;
		return this;
	}
	
	/**
	 * Set the game options to use when compiling game
	 * @param gameOptions
	 * @return This, modified.
	 */
	public EvalGamesSet setGameOptions(final List<String> gameOptions)
	{
		this.gameOptions = gameOptions;
		return this;
	}
	
	/**
	 * Set the ruleset to use when compiling game
	 * @param ruleset
	 * @return This, modified.
	 */
	public EvalGamesSet setRuleset(final String ruleset)
	{
		this.ruleset = ruleset;
		return this;
	}
	
	/**
	 * Set list of AIs / agents
	 * @param agents
	 * @return This, modified.
	 */
	public EvalGamesSet setAgents(final List<AI> agents)
	{
		this.agents = agents;
		return this;
	}
	
	/**
	 * Set number of games to play
	 * @param numGames
	 * @return This, modified.
	 */
	public EvalGamesSet setNumGames(final int numGames)
	{
		this.numGames = numGames;
		return this;
	}
	
	/**
	 * Set cap on game length (num turns)
	 * @param gameLengthCap
	 * @return This, modified.
	 */
	public EvalGamesSet setGameLengthCap(final int gameLengthCap)
	{
		this.gameLengthCap = gameLengthCap;
		return this;
	}
	
	/**
	 * Set max seconds per move for AI
	 * @param maxSeconds
	 * @return This, modified.
	 */
	public EvalGamesSet setMaxSeconds(final double maxSeconds)
	{
		for (int i = 0; i < this.maxSeconds.length; i++)
			this.maxSeconds[i] = maxSeconds;
		return this;
	}
	
	public EvalGamesSet setMaxSeconds(final double[] maxSeconds)
	{
		this.maxSeconds = maxSeconds;
		return this;
	}
	
	/**
	 * Set max iterations per move for AI
	 * @param maxIterations
	 * @return This, modified.
	 */
	public EvalGamesSet setMaxIterations(final int maxIterations)
	{
		this.maxIterations = maxIterations;
		return this;
	}
	
	/**
	 * Set max search depth per move for AI
	 * @param maxSearchDepth
	 * @return This, modified.
	 */
	public EvalGamesSet setMaxSearchDepth(final int maxSearchDepth)
	{
		this.maxSearchDepth = maxSearchDepth;
		return this;
	}
	
	/**
	 * Set whether to rotate agents
	 * @param rotateAgents
	 * @return This, modified.
	 */
	public EvalGamesSet setRotateAgents(final boolean rotateAgents)
	{
		this.rotateAgents = rotateAgents;
		return this;
	}
	
	/**
	 * Set number of seconds for warming up
	 * @param warmingUpSecs
	 * @return This, modified.
	 */
	public EvalGamesSet setWarmingUpSecs(final int warmingUpSecs)
	{
		this.warmingUpSecs = warmingUpSecs;
		return this;
	}
	
	/**
	 * Set whether to round to next divisor of number of permutations
	 * @param roundToNextPermutationsDivisor
	 * @return This, modified.
	 */
	public EvalGamesSet setRoundToNextPermutationsDivisor(final boolean roundToNextPermutationsDivisor)
	{
		this.roundToNextPermutationsDivisor = roundToNextPermutationsDivisor;
		return this;
	}
	
	/**
	 * Set output directory
	 * @param outDir
	 * @return This, modified.
	 */
	public EvalGamesSet setOutDir(final File outDir)
	{
		this.outDir = outDir;
		return this;
	}
	
	/**
	 * Set whether to output data for alpha-rank processing.
	 * @param outputAlphaRankData
	 * @return This, modified
	 */
	public EvalGamesSet setOutputAlphaRankData(final boolean outputAlphaRankData)
	{
		this.outputAlphaRankData = outputAlphaRankData;
		return this;
	}
	
	/**
	 * Set whether to output summary of results.
	 * @param outputSummary
	 * @return This, modified
	 */
	public EvalGamesSet setOutputSummary(final boolean outputSummary)
	{
		this.outputSummary = outputSummary;
		return this;
	}
	
	/**
	 * Set whether we want to print intermediate results to standard output
	 * @param printOut
	 * @return This, modified.
	 */
	public EvalGamesSet setPrintOut(final boolean printOut)
	{
		this.printOut = printOut;
		return this;
	}
	
	/**
	 * Set whether we want to suppress warnings about number of trials not being
	 * divisible by number of permutations of agents.
	 * @param suppress
	 * @return This, modified.
	 */
	public EvalGamesSet setSuppressDivisorWarning(final boolean suppress)
	{
		this.suppressDivisorWarning = suppress;
		return this;
	}

	public ResultsSummary resultsSummary() 
	{
		return resultsSummary;
	}
	
	//-------------------------------------------------------------------------

}
