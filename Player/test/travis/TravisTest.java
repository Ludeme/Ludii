package travis;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.junit.Test;

import ai.TestDefaultAIs;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import game.types.state.GameType;
import games.TestCustomPlayouts;
import games.TestTrialSerialization;
import main.FileHandling;
import main.StringRoutines;
import main.collections.FastArrayList;
import main.collections.ListUtils;
import main.grammar.Description;
import main.options.Option;
import manager.utils.game_logs.MatchRecord;
import other.GameLoader;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import tensor.TestStateMoveTensors;
import utils.AIUtils;

/**
 * Unit Test for Travis uniting many other JUnit tests:
 * 
 * - CompilationTest
 * - GameFileNameTest
 * - OnePlayoutPerGameTestWithOptions
 * - TestCustomPlayouts
 * - TestParallelPlayouts,
 * - TestStateMoveTensors
 * - TestTrialSerialization.
 * - TestTrialsIntegrity.
 *
 * @author Eric.Piette
 */
public class TravisTest
{
	/** True if we want to run only some tests between the minimum day and the maximum hour of the day. */
	private final static boolean USE_TIME = false;
	
	/** The minimum hour in the day to stop to run all the tests.*/
	private final static int MIN_HOUR = 6;
	
	/** The maximum hour in the day to stop to run all the tests.*/
	private final static int MAX_HOUR = 18;

	/** Number of parallel playouts we run. */
	private static final int NUM_PARALLEL = 4;
	
	/** The current game compiled. */
	private Game gameCompiled = null;

	/** The current path of the game compiled. */
	private String pathGameCompiled = "";

	//-------------------------------------------------------------------------------

	@Test
	public void runTests()
	{
		// Get the current hour in our time zone.
		final Date date = new Date();
		final DateFormat df = new SimpleDateFormat("HH");
		df.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		final int hour = Integer.parseInt(df.format(date));

		// Load from memory
		final String[] choices = FileHandling.listGames();

		for (final String filePath : choices)
		{
			final long startGameAt = System.nanoTime();
			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
				continue;

			// We exclude that game from the tests because the legal moves are
			// too slow to test.
//			if (!filePath.replaceAll(Pattern.quote("\\"), "/").contains("Tavli"))
//				continue;

			// Get game description from resource
			// System.out.println("Game: " + filePath);

			String path = filePath.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));

			String desc = "";
			String line;
			try 
			(
				final InputStream in = GameLoader.class.getResourceAsStream(path);
				final BufferedReader rdr = new BufferedReader(new InputStreamReader(in));	
			)
			{
				while ((line = rdr.readLine()) != null)
				{
					desc += line + "\n";
					// System.out.println("line: " + line);
				}
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
				fail();
			}

			// Parse and compile the game
			Game game = null;
			try
			{
				game = (Game)compiler.Compiler.compileTest(new Description(desc), false);
				pathGameCompiled = filePath;
			}
			catch (final Exception e)
			{
				System.err.println("** FAILED TO COMPILE: " + filePath + ".");
				e.printStackTrace();
				fail();
			}

			if (game != null)
			{
				System.out.println("Compiled " + game.name() + ".");
				this.gameCompiled = game;

				final int indexLastSlash = filePath.lastIndexOf('/');
				final String fileName = filePath.substring(indexLastSlash + 1, filePath.length() - ".lud".length());

				if (!fileName.equals(game.name()))
				{
					System.err.println("The fileName of " + fileName
							+ ".lud is not equals to the name of the game which is " + game.name());
					fail();
				}
			}
			else
			{
				System.err.println("** FAILED TO COMPILE: " + filePath + ".");
				fail();
			}

			// TO REMOVE WHEN MATCH WILL BE FIXED
//			if (game.hasSubgames())
//				continue;

			if (game.hasMissingRequirement())
			{
				System.err.println(game.name() + " has missing requirements.");
				fail();
			}

			if (game.willCrash())
			{
				System.err.println(game.name() + " is going to crash.");
				fail();
			}

			try
			{
				testIntegrity();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}

			final List<String> excludedCustomPlayouts = new ArrayList<String>();
			excludedCustomPlayouts.add("Kriegsspiel");
			excludedCustomPlayouts.add("Throngs ");
			excludedCustomPlayouts.add("Omny");
			excludedCustomPlayouts.add("Lifeline");
			excludedCustomPlayouts.add("Shisen-Sho");
			excludedCustomPlayouts.add("Allemande");
			excludedCustomPlayouts.add("Chains of Thought");

			if (!containsPartOf(excludedCustomPlayouts, game.name()))
				testCustomPlayouts();

			//-------------------------------------------------------------------------

			final List<String> excludedTensors = new ArrayList<String>();
			excludedTensors.add("Kriegsspiel");
			excludedTensors.add("Throngs");
			excludedTensors.add("Omny");
			excludedTensors.add("Lifeline");
			excludedTensors.add("Shisen-Sho");
			excludedTensors.add("Allemande");
			excludedTensors.add("Chains of Thought");

			if (!containsPartOf(excludedTensors, game.name()))
				testStateMoveTensors();

			//-------------------------------------------------------------------------

			final List<String> excludedPlayoutPerOption = new ArrayList<String>();
			excludedPlayoutPerOption.add("Kriegsspiel");
			excludedPlayoutPerOption.add("Throngs");
			excludedPlayoutPerOption.add("Mini Wars");
			excludedPlayoutPerOption.add("Omny");
			excludedPlayoutPerOption.add("Lifeline");
			excludedPlayoutPerOption.add("Shisen-Sho");
			excludedPlayoutPerOption.add("Allemande");
			excludedPlayoutPerOption.add("Chains of Thought");

			if (!containsPartOf(excludedPlayoutPerOption, game.name()))
				testPlayoutPerOption((USE_TIME) ? (hour < MIN_HOUR || hour > MAX_HOUR) : true);

			//-------------------------------------------------------------------------

			// testParallelPlayouts((USE_TIME) ? (hour < MIN_HOUR || hour >
			// MAX_HOUR) : true);

			final List<String> excludedParallelPlayouts = new ArrayList<String>();
			excludedParallelPlayouts.add("Kriegsspiel");
			excludedParallelPlayouts.add("Throngs");
			excludedParallelPlayouts.add("Omny");
			excludedParallelPlayouts.add("Lifeline");
			excludedParallelPlayouts.add("Shisen-Sho");
			excludedParallelPlayouts.add("Allemande");
			excludedParallelPlayouts.add("Chains of Thought");
			excludedParallelPlayouts.add("Nodal Chess");

			if (!containsPartOf(excludedParallelPlayouts, game.name()))
				testParallelPlayouts(true);

			//-------------------------------------------------------------------------

			// testDefaultAIs((USE_TIME) ? (hour < MIN_HOUR || hour >
			// MAX_HOUR)
			// : true);

			final List<String> excludedDefaultAI = new ArrayList<String>();

			if (!containsPartOf(excludedDefaultAI, game.name()))
				testDefaultAIs(true);

			//-------------------------------------------------------------------------

			/**
			 * WARNING: the Trial Serialisation test must always be the LAST
			 * test! It modifies the Game objects, which makes any tests that
			 * run afterwards and re-use the same Game object invalid!
			 */

			final List<String> excludedSerialisation = new ArrayList<String>();
			excludedSerialisation.add("Kriegsspiel");
			excludedSerialisation.add("Throngs");
			excludedSerialisation.add("Omny");
			excludedSerialisation.add("Lifeline");
			excludedSerialisation.add("Shisen-Sho");
			excludedSerialisation.add("Allemande");
			excludedSerialisation.add("Chains of Thought");

			if (!containsPartOf(excludedSerialisation, game.name()))
				testTrialSerialisation();

			final long stopGameAt = System.nanoTime();
			final double Gamesecs = (stopGameAt - startGameAt) / 1000000000.0;
			System.out.println("All tests on this game done in " + Gamesecs + "s.\n");
		}

		// Check if all the games using ADD_TO_EMPTY have been checked.
		// Make sure that we found all the games we expected to find
		if (!TestCustomPlayouts.ADD_TO_EMPTY_GAMES.isEmpty())
		{
			System.err.println("Expected the following games to have AddToEmpty playouts:");
			for (final String gameCustom : TestCustomPlayouts.ADD_TO_EMPTY_GAMES)
			{
				System.err.println(gameCustom);
			}
			fail();
		}
	}

	//-------------------------------------------------------------------------------------------

	/**
	 * The test of the tensors.
	 */
	public void testStateMoveTensors()
	{
		TestStateMoveTensors.testTensors(gameCompiled);
	}
	
	//-------------------------------------------------------------------------------------------

	/**
	 * The test of the serialisation.
	 */
	public void testTrialSerialisation()
	{
		if (gameCompiled.isSimulationMoveGame())
			return;

		TestTrialSerialization.testTrialSerialization(gameCompiled);

		// delete temp file if we succeeded unit test
		TestTrialSerialization.TEMP_TRIAL_FILE.delete();
	}

	//-------------------------------------------------------------------------------------------

	/**
	 * The test of the parallels playouts.
	 */
	public void testParallelPlayouts(final boolean toTest)
	{
		// To do that test only if we are on the right timeline.
		if (!toTest)
			return;

		if (gameCompiled.isDeductionPuzzle())
			return;

		if (gameCompiled.isSimulationMoveGame())
			return;

		// run parallel trials
		final Context[] contexts = new Context[NUM_PARALLEL];
		final RandomProviderDefaultState[] gameStartRngStates = new RandomProviderDefaultState[NUM_PARALLEL];

		for (int i = 0; i < NUM_PARALLEL; ++i)
		{
			contexts[i] = new Context(gameCompiled, new Trial(gameCompiled));
			gameStartRngStates[i] = (RandomProviderDefaultState) contexts[i].rng().saveState();
		}

		final ExecutorService executorService = Executors.newFixedThreadPool(NUM_PARALLEL);

		final List<Future<Context>> playedContexts = new ArrayList<Future<Context>>(NUM_PARALLEL);
		for (int i = 0; i < NUM_PARALLEL; ++i)
		{
			final Context context = contexts[i];
			playedContexts.add(executorService.submit(() ->
			{
				gameCompiled.start(context);
				gameCompiled.playout(context, null, 1.0, null, 0, 30, ThreadLocalRandom.current());
				return context;
			}));
		}

		// store outcomes of all parallel trials
		final Context[] endContexts = new Context[NUM_PARALLEL];

		try
		{
			for (int i = 0; i < NUM_PARALLEL; ++i)
			{
				endContexts[i] = playedContexts.get(i).get();
			}
		}
		catch (final InterruptedException | ExecutionException e)
		{
			System.err.println("Failing in game: " + gameCompiled.name());
			e.printStackTrace();
			fail();
		}
		
		executorService.shutdown();

		// check if we can still execute them all the same way in serial
		// mode
		for (int parallelPlayout = 0; parallelPlayout < NUM_PARALLEL; ++parallelPlayout)
		{
			final Context parallelContext = endContexts[parallelPlayout];
			final Trial parallelTrial = parallelContext.trial();
			final List<Move> loadedMoves = parallelTrial.generateCompleteMovesList();

			final Trial trial = new Trial(gameCompiled);
			final Context context = new Context(gameCompiled, trial);
			context.rng().restoreState(gameStartRngStates[parallelPlayout]);

			gameCompiled.start(context);

			int moveIdx = 0;

			while (moveIdx < trial.numInitialPlacementMoves())
			{
				assert (loadedMoves.get(moveIdx).equals(trial.getMove(moveIdx)));
				++moveIdx;
			}

			while (moveIdx < loadedMoves.size())
			{
				while (moveIdx < trial.numMoves())
				{
					// looks like some actions were auto-applied (e.g.
					// in ByScore End condition)
					// so we just check if they're equal, without
					// applying them again from loaded file
					assert (loadedMoves.get(moveIdx).getActionsWithConsequences(context)
							.equals(trial.getMove(moveIdx).getActionsWithConsequences(context))) : ("Loaded Move Actions = "
									+ loadedMoves.get(moveIdx).getActionsWithConsequences(context) + ", trial actions = "
									+ trial.getMove(moveIdx).getActionsWithConsequences(context));
					++moveIdx;
				}

				if (moveIdx == loadedMoves.size())
					break;
				
				if(!loadedMoves.get(moveIdx).isDecision())
				{
					System.err.println("A move is not a decision in the game: " + gameCompiled.name());
					System.err.println("The move is " + loadedMoves.get(moveIdx));
					fail();
				}

				if (trial.over())
				{
					System.err.println("Failing in game: " + gameCompiled.name());
					System.err.println("Serial trial already over after moves:");
					for (final Move move : trial.generateCompleteMovesList())
					{
						System.err.println(move);
					}
					System.err.println("When run in parallel, trial only ended after moves:");
					for (final Move move : parallelTrial.generateCompleteMovesList())
					{
						System.err.println(move);
					}
					fail();
				}

				final Moves legalMoves = gameCompiled.moves(context);
				final Move loadedMove = loadedMoves.get(moveIdx);
				final List<Action> loadedMoveAllActions = loadedMove.getActionsWithConsequences(context);

				if (gameCompiled.mode().mode() == ModeType.Alternating)
				{
					Move matchingMove = null;
					for (final Move move : legalMoves.moves())
					{
						if (move.from() == loadedMove.from() && move.to() == loadedMove.to())
						{
							if (move.getActionsWithConsequences(context).equals(loadedMoveAllActions))
							{
								matchingMove = move;
								break;
							}
						}
					}

					if (matchingMove == null)
					{
						if (loadedMove.isPass() && legalMoves.moves().isEmpty())
							matchingMove = loadedMove;
					}

					if (matchingMove == null)
					{
						System.err.println("Failing in game: " + gameCompiled.name());
						System.err.println("No matching move found for: " + loadedMoveAllActions);

						for (final Move move : legalMoves.moves())
						{
							System.err.println("legal move: " + move.getActionsWithConsequences(context));
						}

						fail();
					}

					// assert(matchingMove != null);
					assert
					(
						matchingMove.fromNonDecision() == matchingMove.toNonDecision() ||
						(context.currentInstanceContext().game().gameFlags() & GameType.UsesFromPositions) != 0L
					);
					gameCompiled.apply(context, matchingMove);
				}
				else
				{
					// simultaneous-move game
					// the full loaded move should be equal to one of
					// the possible large combined moves
					final FastArrayList<Move> legal = legalMoves.moves();

					final int numPlayers = gameCompiled.players().count();
					@SuppressWarnings("unchecked")
					final FastArrayList<Move>[] legalPerPlayer = new FastArrayList[numPlayers + 1];
					final List<List<Integer>> legalMoveIndicesPerPlayer = new ArrayList<List<Integer>>(
							numPlayers + 1);

					for (int p = 1; p <= numPlayers; ++p)
					{
						legalPerPlayer[p] = AIUtils.extractMovesForMover(legal, p);

						final List<Integer> legalMoveIndices = new ArrayList<Integer>(legalPerPlayer[p].size());
						for (int i = 0; i < legalPerPlayer[p].size(); ++i)
						{
							legalMoveIndices.add(Integer.valueOf(i));
						}
						legalMoveIndicesPerPlayer.add(legalMoveIndices);
					}

					final List<List<Integer>> combinedMoveIndices = ListUtils
							.generateTuples(legalMoveIndicesPerPlayer);

					boolean foundMatch = false;
					for (final List<Integer> submoveIndicesCombination : combinedMoveIndices)
					{
						// Combined all the per-player moves for this
						// combination of indices
						final List<Action> actions = new ArrayList<>();
						final List<Moves> topLevelCons = new ArrayList<Moves>();

						for (int p = 1; p <= numPlayers; ++p)
						{
							final Move move = legalPerPlayer[p]
									.get(submoveIndicesCombination.get(p - 1).intValue());
							if (move != null)
							{
								final Move moveToAdd = new Move(move.actions());
								actions.add(moveToAdd);

								if (move.then() != null)
								{
									for (int i = 0; i < move.then().size(); ++i)
									{
										if (move.then().get(i).applyAfterAllMoves())
											topLevelCons.add(move.then().get(i));
										else
											moveToAdd.then().add(move.then().get(i));
									}
								}
							}
						}

						final Move combinedMove = new Move(actions);
						combinedMove.setMover(numPlayers + 1);
						combinedMove.then().addAll(topLevelCons);

						final List<Action> combinedMoveAllActions = combinedMove.getActionsWithConsequences(context);
						if (loadedMoveAllActions.equals(combinedMoveAllActions))
						{
							foundMatch = true;
							break;
						}
					}

					if (!foundMatch)
					{
						System.err.println("Failing in game: " + gameCompiled.name());
						System.err.println("Found no combination of submoves that generate loaded move: "
								+ loadedMoveAllActions);
						fail();
					}
					gameCompiled.apply(context, loadedMoves.get(moveIdx));
				}

				++moveIdx;
			}

			if (trial.status() == null)
				assert (parallelTrial.status() == null);
			else
				assert (trial.status().winner() == parallelTrial.status().winner());

			if (!Arrays.equals(context.trial().ranking(), parallelContext.trial().ranking()))
			{
				System.err.println("Failing in game: " + gameCompiled.name());
				System.err.println(
						"Ranking when run in parallel: " + Arrays.toString(parallelContext.trial().ranking()));
				System.err.println("Ranking when run serially: " + Arrays.toString(context.trial().ranking()));
				fail();
			}

			// we're done with this one, let's allow memory to be
			// cleared
			endContexts[parallelPlayout] = null;
		}
	}


	//-------------------------------------------------------------------------------------------
		
	/**
	 * The test to run for the custom Playouts.
	 */
	public void testCustomPlayouts()
	{
		if (gameCompiled.hasSubgames())
			return;

		if (gameCompiled.isDeductionPuzzle())
			return;

		if (gameCompiled.isSimulationMoveGame())
			return;

		if (!gameCompiled.hasCustomPlayouts())
			return;

		// System.out.println(gameCompiled.name());

		// Remember that we've hit this one
		TestCustomPlayouts.ADD_TO_EMPTY_GAMES.remove(gameCompiled.name());

		TestCustomPlayouts.testCustomPlayout(gameCompiled);
	}
	
	//------------------------------------------------------------------------------------
	
	/**
	 * The test to run for the default AIs.
	 */
	public void testDefaultAIs(final boolean toTest)
	{
		// To do that test only if we are on the right timeline.
		if (!toTest)
			return;

		if (gameCompiled.isDeductionPuzzle())
			return;

		if (gameCompiled.isSimulationMoveGame())
			return;

		TestDefaultAIs.testDefaultAI(gameCompiled);
	}

	//------------------------------------------------------------------------------------

	/**
	 * To test each combination of options.
	 */
	public void testPlayoutPerOption(final boolean toTest)
	{
		// To do that test only if we are on the right timeline.
		if (!toTest)
			return;

		if (gameCompiled.isDeductionPuzzle())
			return;

		if (gameCompiled.isSimulationMoveGame())
			return;

		assert (gameCompiled != null);

		final List<List<String>> optionCategories = new ArrayList<List<String>>();

		for (int o = 0; o < gameCompiled.description().gameOptions().numCategories(); o++)
		{
			final List<Option> options = gameCompiled.description().gameOptions().categories().get(o).options();
			final List<String> optionCategory = new ArrayList<String>();

			for (int j = 0; j < options.size(); j++)
			{
				final Option option = options.get(j);
				optionCategory.add(StringRoutines.join("/", option.menuHeadings().toArray(new String[0])));
			}

			if (optionCategory.size() > 0)
				optionCategories.add(optionCategory);
		}

		List<List<String>> optionCombinations = ListUtils.generateTuples(optionCategories);

		// If no option (just the default game) we do not run the test.
		if (optionCombinations.size() <= 1)
			return;

		// System.out.println(game.name());

		// We keep only the combinations of options with only one of these
		// option categories.
		optionCombinations = combinationsKept("Board Size/", optionCombinations);
		optionCombinations = combinationsKept("Rows/", optionCombinations);
		optionCombinations = combinationsKept("Columns/", optionCombinations);
		optionCombinations = combinationsKept("Safe Teleportations/", optionCombinations);
		optionCombinations = combinationsKept("Robots/", optionCombinations);
		optionCombinations = combinationsKept("Board/", optionCombinations);
		optionCombinations = combinationsKept("Dual/", optionCombinations);
		optionCombinations = combinationsKept("Players/", optionCombinations);
		optionCombinations = combinationsKept("Start Rules/", optionCombinations);
		optionCombinations = combinationsKept("Play Rules/", optionCombinations);
		optionCombinations = combinationsKept("End Rules/", optionCombinations);
		optionCombinations = combinationsKept("Version/", optionCombinations);
		optionCombinations = combinationsKept("Slide/", optionCombinations);
		optionCombinations = combinationsKept("Tiling/", optionCombinations);
		optionCombinations = combinationsKept("Track/", optionCombinations);
		optionCombinations = combinationsKept("Throw/", optionCombinations);
		optionCombinations = combinationsKept("Ruleset/", optionCombinations);
		optionCombinations = combinationsKept("Dice/", optionCombinations);
		optionCombinations = combinationsKept("Start Rules Tiger/", optionCombinations);
		optionCombinations = combinationsKept("Start Rules Goat/", optionCombinations);
		optionCombinations = combinationsKept("Capture/", optionCombinations);
		optionCombinations = combinationsKept("Is/", optionCombinations);
		optionCombinations = combinationsKept("Multi-", optionCombinations);
		optionCombinations = combinationsKept("Equi", optionCombinations);
		optionCombinations = combinationsKept("Discs", optionCombinations);
		optionCombinations = combinationsKept("Value", optionCombinations);
		optionCombinations = combinationsKept("Balance Rule", optionCombinations);
		optionCombinations = combinationsKept("Star Cells", optionCombinations);
		optionCombinations = combinationsKept("Board Shape", optionCombinations);

		for (final List<String> optionCombination : optionCombinations)
		{
			// System.out.println("Compiling and running playout with options: "
			// + optionCombination);

			try
			{
				final Game gameWithOptions = GameLoader.loadGameFromName(gameCompiled.name() + ".lud",
						optionCombination);

				if (gameWithOptions.hasMissingRequirement())
				{
					System.err.println(gameWithOptions.name() + "with the option combination = " + optionCombination
							+ " has missing requirements.");
					fail();
				}

				if (gameWithOptions.willCrash())
				{
					System.err.println(gameWithOptions.name() + "with the option combination = " + optionCombination
							+ " is going to crash.");
					fail();
				}

				final Trial trial = new Trial(gameWithOptions);
				final Context context = new Context(gameWithOptions, trial);
				gameWithOptions.start(context);
				gameWithOptions.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
			}
			catch (final Exception e)
			{
				System.out.println("On the game " + gameCompiled.name());
				System.out.println("The playout with these options: " + optionCombination + "failed.");
				e.printStackTrace();
				fail();
			}
		}
	}

	//------------------------------------------------------------------------------------

	public void testIntegrity() throws FileNotFoundException, IOException
	{
		final SplitMix64 rng = new SplitMix64();

		final File folder = new File("../Common/res" + pathGameCompiled);
		if (folder.getName().contains(".lud"))
		{
			if (gameCompiled.isDeductionPuzzle())
				return;

			final String ludPath = folder.getPath().replaceAll(Pattern.quote("\\"), "/");
			final String trialDirPath = ludPath
					.replaceFirst(Pattern.quote("/Common/res/"), Matcher.quoteReplacement("/Player/res/"))
					.replaceFirst(Pattern.quote("/lud/"), Matcher.quoteReplacement("/random_trials/"))
					.replace(".lud", "");

			final File trialsDir = new File(trialDirPath);

			if (!trialsDir.exists())
			{
				System.err.println("WARNING: No directory of trials exists at: " + trialsDir.getAbsolutePath());
				return;
			}

			final File[] trialFiles = trialsDir.listFiles();

			if (trialFiles.length == 0)
			{
				System.err.println("WARNING: No trial files exist in directory: " + trialsDir.getAbsolutePath());
				return;
			}

			if (gameCompiled.isSimulationMoveGame())
				return;

			final File trialFile = trialFiles[rng.nextInt(trialFiles.length)];
			final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, gameCompiled);
			final Trial loadedTrial = loadedRecord.trial();
			final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();

			final Trial trial = new Trial(gameCompiled);
			final Context context = new Context(gameCompiled, trial);
			context.rng().restoreState(loadedRecord.rngState());

			gameCompiled.start(context);

			int moveIdx = 0;

			while (moveIdx < context.currentInstanceContext().trial().numInitialPlacementMoves())
			{
//						System.out.println("init moveIdx: " + moveIdx);
//						System.out.println("Move on the trial is = " + trial.moves().get(moveIdx));
//						System.out.println("loadedMoves.get(moveIdx) = " + loadedMoves.get(moveIdx));

				if (!loadedMoves.get(moveIdx).equals(trial.getMove(moveIdx)))
				{
					System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
					System.out.println("Failed at trial file: " + trialFile);
					System.out.println("Moves not equal.");
					System.out.println("init moveIdx: " + moveIdx);
					System.out.println("Move on the trial is = " + trial.getMove(moveIdx));
					System.out.println("loadedMoves.get(moveIdx) = " + loadedMoves.get(moveIdx));
					System.out.println("All moves in trial = " + trial.generateCompleteMovesList());
					fail("One of the init moves was different in stored trial!");
				}

				++moveIdx;
			}

			while (moveIdx < loadedMoves.size())
			{
				// System.out.println("moveIdx after init: " + moveIdx);

				while (moveIdx < trial.numMoves())
				{
					// looks like some actions were auto-applied (e.g. in
					// ByScore End condition)
					// so we just check if they're equal, without applying
					// them again from loaded file
					if (!loadedMoves.get(moveIdx).getActionsWithConsequences(context)
							.equals(trial.getMove(moveIdx).getActionsWithConsequences(context)))
					{
						System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
						System.out.println("Mismatch in actions.");
						System.out.println("Loaded Move Actions = " + loadedMoves.get(moveIdx).getActionsWithConsequences(context));
						System.out.println("trial actions = " + trial.getMove(moveIdx).getActionsWithConsequences(context));
						fail("Mismatch in auto-applied actions.");
					}

					++moveIdx;
				}

				if (moveIdx == loadedMoves.size())
					break;

				if (trial.over())
				{
					System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
					System.out.println("Failed at trial file: " + trialFile);
					System.out.println("corrected moveIdx = "
							+ (moveIdx - context.currentInstanceContext().trial().numInitialPlacementMoves()));
					System.out.println("moveIdx = " + moveIdx);
					System.out.println("Trial was not supposed to be over, but it is!");
					fail();
				}

				final Moves legalMoves = gameCompiled.moves(context);

				// make sure that the number of legal moves is the same
				// as stored in file
				final int numInitPlacementMoves = context.currentInstanceContext().trial().numInitialPlacementMoves();

				if (loadedTrial.auxilTrialData() != null)
				{
					if (legalMoves.moves().size() != loadedTrial.auxilTrialData().legalMovesHistorySizes()
							.getQuick(moveIdx - numInitPlacementMoves))
					{
						System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
						System.out.println("Failed at trial file: " + trialFile);
						System.out.println("corrected moveIdx = " + (moveIdx - numInitPlacementMoves));
						System.out.println("moveIdx = " + moveIdx);
						System.out.println("trial.numInitialPlacementMoves() = " + numInitPlacementMoves);
						System.out.println("legalMoves.moves().size() = " + legalMoves.moves().size());
						System.out.println(
								"loadedTrial.legalMovesHistorySizes().getQuick(moveIdx - trial.numInitPlace()) = "
										+ loadedTrial.auxilTrialData().legalMovesHistorySizes()
												.getQuick(moveIdx - numInitPlacementMoves));
						System.out.println("legalMoves.moves() = " + legalMoves.moves());
						fail("Incorrect number of legal moves");
					}
				}

				final Move loadedMove = loadedMoves.get(moveIdx);
				final List<Action> loadedMoveAllActions = loadedMove.getActionsWithConsequences(context);

				if (gameCompiled.mode().mode() == ModeType.Alternating)
				{
					Move matchingMove = null;
					for (final Move move : legalMoves.moves())
					{
						if (move.from() == loadedMove.from() && move.to() == loadedMove.to())
						{
							if (move.getActionsWithConsequences(context).equals(loadedMoveAllActions))
							{
								matchingMove = move;
								break;
							}
						}
					}

					if (matchingMove == null)
					{
						if (loadedMove.isPass() && legalMoves.moves().isEmpty())
							matchingMove = loadedMove;
					}

					if (matchingMove == null)
					{
						System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
						System.out.println("Failed at trial file: " + trialFile);
						System.out.println("moveIdx = " + (moveIdx - trial.numInitialPlacementMoves()));
						System.out.println("Loaded move = " + loadedMoves.get(moveIdx).getActionsWithConsequences(context)
								+ " from is " + loadedMoves.get(moveIdx).fromType() + " to is "
								+ loadedMoves.get(moveIdx).toType());

						for (final Move move : legalMoves.moves())
						{
							System.out.println("legal move = " + move.getActionsWithConsequences(context) + " move from is "
									+ move.fromType() + " to " + move.toType());
						}

						fail("Found no matching move");
					}

					gameCompiled.apply(context, matchingMove);
				}
				else
				{
					// simultaneous-move game
					// the full loaded move should be equal to one of the
					// possible large combined moves
					final FastArrayList<Move> legal = legalMoves.moves();

					final int numPlayers = gameCompiled.players().count();
					@SuppressWarnings("unchecked")
					final FastArrayList<Move>[] legalPerPlayer = new FastArrayList[numPlayers + 1];
					final List<List<Integer>> legalMoveIndicesPerPlayer = new ArrayList<List<Integer>>(numPlayers + 1);

					for (int p = 1; p <= numPlayers; ++p)
					{
						legalPerPlayer[p] = AIUtils.extractMovesForMover(legal, p);

						final List<Integer> legalMoveIndices = new ArrayList<Integer>(legalPerPlayer[p].size());
						for (int i = 0; i < legalPerPlayer[p].size(); ++i)
						{
							legalMoveIndices.add(Integer.valueOf(i));
						}
						legalMoveIndicesPerPlayer.add(legalMoveIndices);
					}

					final List<List<Integer>> combinedMoveIndices = ListUtils.generateTuples(legalMoveIndicesPerPlayer);

					boolean foundMatch = false;
					for (final List<Integer> submoveIndicesCombination : combinedMoveIndices)
					{
						// Combined all the per-player moves for this
						// combination of indices
						final List<Action> actions = new ArrayList<>();
						final List<Moves> topLevelCons = new ArrayList<Moves>();

						for (int p = 1; p <= numPlayers; ++p)
						{
							final Move move = legalPerPlayer[p].get(submoveIndicesCombination.get(p - 1).intValue());
							if (move != null)
							{
								final Move moveToAdd = new Move(move.actions());
								actions.add(moveToAdd);

								if (move.then() != null)
								{
									for (int i = 0; i < move.then().size(); ++i)
									{
										if (move.then().get(i).applyAfterAllMoves())
											topLevelCons.add(move.then().get(i));
										else
											moveToAdd.then().add(move.then().get(i));
									}
								}
							}
						}

						final Move combinedMove = new Move(actions);
						combinedMove.setMover(numPlayers + 1);
						combinedMove.then().addAll(topLevelCons);

						final List<Action> combinedMoveAllActions = combinedMove.getActionsWithConsequences(context);
						if (loadedMoveAllActions.equals(combinedMoveAllActions))
						{
							foundMatch = true;
							break;
						}
					}

					if (!foundMatch)
					{
						System.out.println(
								"Found no combination of submoves that generate loaded move: " + loadedMoveAllActions);
						fail();
					}

					gameCompiled.apply(context, loadedMoves.get(moveIdx));
				}

				++moveIdx;
			}

			if (trial.status() == null)
			{
				if (loadedTrial.status() != null)
				{
					System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
					System.out.println("Failed at trial file: " + trialFile);
					System.out.println("Status doesn't match.");
					System.out.println("trial      : " + trial.status());
					System.out.println("loadedTrial: " + loadedTrial.status());
				}

				assert (loadedTrial.status() == null);
			}
			else
			{
				if (trial.status().winner() != loadedTrial.status().winner())
				{
					System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
					System.out.println("Failed at trial file: " + trialFile);
					System.out.println("Winners don't match.");
					System.out.println("trial      : " + trial.status().winner());
					System.out.println("loadedTrial: " + loadedTrial.status().winner());
				}

				assert (trial.status().winner() == loadedTrial.status().winner());
			}

			if (!Arrays.equals(trial.ranking(), loadedTrial.ranking()))
			{
				System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
				System.out.println("Rankings not equal.");
				System.out.println("trial       : " + trial.ranking());
				System.out.println("loadedTrial : " + loadedTrial.ranking());
			}

			assert (Arrays.equals(trial.ranking(), loadedTrial.ranking()));
		}
	}

	//------------------------------------------------------------------------------------

	/**
	 * @param optionToCheck The option to check.
	 * @param optionCombinations The original combinations of options.
	 * 
	 * @return The list of combinations of option with only one option between
	 * the specific option to check.
	 */
	public static List<List<String>> combinationsKept(final String optionToCheck, final List<List<String>> optionCombinations)
	{
		final SplitMix64 rng = new SplitMix64();
		final List<List<String>> optionCombinationsKept = new ArrayList<List<String>>();

		if (!optionCombinations.isEmpty())
		{
			// We check if the option to check exist.
			final List<String> firstOptionCombination = optionCombinations.get(0);
			boolean optionExists = false;
			String optionToKeep = "";
			for (final String option : firstOptionCombination)
				if (option.contains(optionToCheck))
				{
					optionExists = true;
					break;
				}


			// We get a random option between the different possibilities of
			// that option.
			if (optionExists)
			{
				final int indexSizeSelected = rng.nextInt(optionCombinations.size());
				final List<String> optionsSelected = optionCombinations.get(indexSizeSelected);
				for (final String option : optionsSelected)
					if (option.contains(optionToCheck))
					{
						optionToKeep = option;
						// System.out.println("Only the combinations options
						// with " + option + " will be tested");
						break;
					}
			}
			else // If that option is not found we return the original
					// combinations of options.
				return optionCombinations;

			// We keep only the option selected.
			if (optionExists)
				for (final List<String> optionCombination : optionCombinations)
					for (final String option : optionCombination)
						if (option.equals(optionToKeep))
						{
							optionCombinationsKept.add(optionCombination);
							break;
						}
		}

		return optionCombinationsKept;
	}

	//------------------------------------------------------------------------------------
	/**
	 * @param list The list of string.
	 * @param test The string to check.
	 * @return True if the string to check is contains in any string in the
	 * list.
	 */
	public static boolean containsPartOf(final List<String> list, final String test)
	{
		for (final String string : list)
			if (test.contains(string))
				return true;

		return false;
	}
}
