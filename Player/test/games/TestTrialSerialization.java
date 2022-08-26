package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.junit.Test;

import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import main.collections.FastArrayList;
import main.collections.ListUtils;
import manager.utils.game_logs.MatchRecord;
import other.GameLoader;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import utils.AIUtils;

/**
 * Unit Test to test serialization and deserialization of trials.
 * 
 * For every game, we run one playout, serialize it to a temporary file,
 * deserialize it again, and immediately test if we can reproduce the trial.
 * 
 * @author Dennis Soemers
 */
public class TestTrialSerialization
{
	
	/** File for temp trials */
	public static final File TEMP_TRIAL_FILE = new File("./TempLudiiTrialTestFile.txt");
	
	/**
	 * The test to run.
	 */
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();
		
		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);
			
			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					
					if (path.equals("../Common/res/lud/plex"))
						continue;
					
					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/wishlist"))
						continue;
					
					if (path.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/puzzle/deduction"))
						continue;	// skip puzzles for now
					
					if (path.equals("../Common/res/lud/bad"))
						continue;
					
					if (path.equals("../Common/res/lud/bad_playout"))
						continue;
					
					if (path.equals("../Common/res/lud/reconstruction"))
						continue;
					
					if (path.equals("../Common/res/lud/simulation"))
						continue;

					// We'll find files that we should be able to compile and run here
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}
		
		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final Game game = GameLoader.loadGameFromFile(fileEntry);
				
				System.out.println("Attempting to run, save, load and validate trial for game: " + fileEntry.getName());
				testTrialSerialization(game);
			}
		}
		
		// delete temp file if we succeeded unit test
		TEMP_TRIAL_FILE.delete();
	}
	
	/**
	 * Tests trial serialization for given game
	 * @param game
	 */
	public static void testTrialSerialization(final Game game)
	{
		if (game.isDeductionPuzzle())
			return;
		
		// disable custom playouts that cannot properly store history of legal moves per state
		game.disableMemorylessPlayouts();

		Trial trial = new Trial(game);
		Context context = new Context(game, trial);

		final RandomProviderDefaultState gameStartRngState = (RandomProviderDefaultState) context.rng().saveState();

//		trial.storeLegalMovesHistory();
//		if (context.isAMatch())
//			context.currentInstanceContext().trial().storeLegalMovesHistory();

		game.start(context);
		final int maxNumMoves = 10 + (int) (Math.random() * 21);
		game.playout(context, null, 1.0, null, 0, maxNumMoves, ThreadLocalRandom.current());

		try
		{
			// save it to temp file
			trial.saveTrialToTextFile(TEMP_TRIAL_FILE, game.name(), new ArrayList<String>(), gameStartRngState);

			// and immediately load and check it
			final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(TEMP_TRIAL_FILE, game);
			final Trial loadedTrial = loadedRecord.trial();
			final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();

			trial = new Trial(game);
			context = new Context(game, trial);
			context.rng().restoreState(loadedRecord.rngState());

			game.start(context);

			int moveIdx = 0;

			while (moveIdx < context.currentInstanceContext().trial().numInitialPlacementMoves())
			{
				assert(loadedMoves.get(moveIdx).equals(trial.getMove(moveIdx)));
				++moveIdx;
			}

			while (moveIdx < loadedMoves.size())
			{
				while (moveIdx < trial.numMoves())
				{
					// looks like some actions were auto-applied (e.g. in ByScore End condition)
					// so we just check if they're equal, without applying them again from loaded file
					assert
					(loadedMoves.get(moveIdx).getActionsWithConsequences(context).equals(trial.getMove(moveIdx).getActionsWithConsequences(context))) 
					: 
						(
								"Loaded Move Actions = " + loadedMoves.get(moveIdx).getActionsWithConsequences(context) + 
								", trial actions = " + trial.getMove(moveIdx).getActionsWithConsequences(context)
								);
					++moveIdx;
				}

				if (moveIdx == loadedMoves.size())
					break;

				assert(!trial.over());

				final Moves legalMoves = game.moves(context);
				final List<List<Action>> legalMovesAllActions = new ArrayList<List<Action>>();
				for (final Move legalMove : legalMoves.moves())
				{
					legalMovesAllActions.add(legalMove.getActionsWithConsequences(context));
				}

//				final List<List<Action>> loadedLegalMovesAllActions = new ArrayList<List<Action>>();
//				final Trial subtrial = context.currentInstanceContext().trial();
//				for (final Move loadedLegalMove : loadedTrial.auxilTrialData().legalMovesHistory().get(moveIdx - subtrial.numInitialPlacementMoves()))
//				{
//					loadedLegalMovesAllActions.add(loadedLegalMove.getActionsWithConsequences(context));
//				}
//
//				// make sure that all of the stored legal moves are also currently legal moves
//				for (int loadedIdx = 0; loadedIdx < loadedLegalMovesAllActions.size(); ++loadedIdx)
//				{
//					boolean foundMatch = false;
//
//					for (int i = 0; i < legalMovesAllActions.size(); ++i)
//					{
//						if (loadedLegalMovesAllActions.get(loadedIdx).equals(legalMovesAllActions.get(i)))
//						{
//							foundMatch = true;
//							break;
//						}
//					}
//
////					if (!foundMatch)
////					{
////						for (final Move legalMove : legalMoves.moves())
////						{
////							System.out.println(loadedLegalMovesAllActions.get(loadedIdx) + " not equal to " + legalMove.getAllActions(context));
////						}
////					}
//
//					assert (foundMatch) : loadedTrial.auxilTrialData().legalMovesHistory().get(moveIdx - trial.numInitialPlacementMoves()) + " was legal in stored trial, but not found in " + legalMoves.moves();
//				}
//
//				// make sure that all of the currently legal moves are also stored legal moves
//				for (int i = 0; i < legalMovesAllActions.size(); ++i)
//				{
//					boolean foundMatch = false;
//
//					for (int loadedIdx = 0; loadedIdx < loadedLegalMovesAllActions.size(); ++loadedIdx)
//					{
//						if (loadedLegalMovesAllActions.get(loadedIdx).equals(legalMovesAllActions.get(i)))
//						{
//							foundMatch = true;
//							break;
//						}
//					}
//
////					if (!foundMatch)
////					{
////						for (final Move move : loadedTrial.legalMovesHistory().get(moveIdx - trial.numInitPlace()))
////						{
////							System.out.println(legalMove.getAllActions(context) + " not equal to " + move.getAllActions(context));
////						}
////					}
//
//					assert (foundMatch);
//				}

				final List<Action> loadedMoveAllActions = loadedMoves.get(moveIdx).getActionsWithConsequences(context);

				if (game.mode().mode() == ModeType.Alternating)
				{
					Move matchingMove = null;
					for (int i = 0; i < legalMovesAllActions.size(); ++i)
					{
						if (legalMovesAllActions.get(i).equals(loadedMoveAllActions))
						{
							matchingMove = legalMoves.moves().get(i);
							break;
						}
					}

					if (matchingMove == null)
					{
						if (loadedMoves.get(moveIdx).isPass() && legalMoves.moves().isEmpty())
							matchingMove = loadedMoves.get(moveIdx);
					}

//					if (matchingMove == null)
//					{
//						for (int i = 0; i < legalMovesAllActions.size(); ++i)
//						{
//							System.out.println(legalMovesAllActions.get(i) + " does not match " + loadedMoves.get(moveIdx).getAllActions(context));
//						}
//					}

					assert(matchingMove != null);
					game.apply(context, matchingMove);
				}
				else
				{
					// simultaneous-move game
					// the full loaded move should be equal to one of the possible large combined moves				
					final FastArrayList<Move> legal = legalMoves.moves();

					final int numPlayers = game.players().count();
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
						// Combined all the per-player moves for this combination of indices
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
						System.out.println("Found no combination of submoves that generate loaded move: " + loadedMoveAllActions);
						fail();
					}

					game.apply(context, loadedMoves.get(moveIdx));
				}

				++moveIdx;
			}

			if (trial.status() == null)
				assert(loadedTrial.status() == null);
			else
				assert(trial.status().winner() == loadedTrial.status().winner());

			assert(Arrays.equals(trial.ranking(), loadedTrial.ranking()));
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			fail("Crashed when trying to save or load trial.");
		}
	}

}