package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.junit.Test;

import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import other.GameLoader;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Unit test that checks for games with custom playouts
 * that all the moves they pick are indeed legal.
 * 
 * @author Dennis Soemers
 */
public class TestCustomPlayouts
{
	/** 
	 * We populate this with game names for which we know for sure that we want them 
	 * to be using custom (AddToEmpty) playouts
	 */
	public static Set<String> ADD_TO_EMPTY_GAMES;
	
	static {
		ADD_TO_EMPTY_GAMES = new HashSet<String>();
		ADD_TO_EMPTY_GAMES.add("Cross");
		ADD_TO_EMPTY_GAMES.add("Havannah");
		ADD_TO_EMPTY_GAMES.add("Hex");
		ADD_TO_EMPTY_GAMES.add("Y (Hex)");
		ADD_TO_EMPTY_GAMES.add("Squava");
		ADD_TO_EMPTY_GAMES.add("Tic-Tac-Four");
		ADD_TO_EMPTY_GAMES.add("Tic-Tac-Mo");
		ADD_TO_EMPTY_GAMES.add("Tic-Tac-Toe");
		ADD_TO_EMPTY_GAMES.add("Yavalade");
		ADD_TO_EMPTY_GAMES.add("Yavalath");
		//ADD_TO_EMPTY_GAMES.add("Four-Player Hex");
		ADD_TO_EMPTY_GAMES.add("Three-Player Hex");
		ADD_TO_EMPTY_GAMES.add("Gomoku");
		ADD_TO_EMPTY_GAMES.add("Sim");
	}
	
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
				final String fileName = fileEntry.getPath();

				final Game game = GameLoader.loadGameFromFile(fileEntry);
				
				if (!game.hasCustomPlayouts())
					continue;
				
				// Remember that we've hit this one
				ADD_TO_EMPTY_GAMES.remove(game.name());
				
				System.out.println("Testing game with custom playouts: " + fileName);
				
				testCustomPlayout(game);
			}
		}
		
		// Make sure that we found all the games we expected to find
		if (!ADD_TO_EMPTY_GAMES.isEmpty())
		{
			System.err.println("Expected the following games to have AddToEmpty playouts:");
			for (final String game : ADD_TO_EMPTY_GAMES)
			{
				System.err.println(game);
			}
			fail();
		}
	}
	
	/**
	 * Tests custom playout implementation for given game
	 * @param game
	 */
	public static void testCustomPlayout(final Game game)
	{
		// Play our trial (with custom playouts)
		final Context playedContext = new Context(game, new Trial(game));
		final RandomProviderDefaultState gameStartRngState = (RandomProviderDefaultState) playedContext.rng().saveState();
		game.start(playedContext);
		final Trial playedTrial = game.playout(playedContext, null, 1.0, null, 0, -1, ThreadLocalRandom.current());

		// Ensure that all played moves were legal and outcome is correct
		final List<Move> loadedMoves = playedTrial.generateCompleteMovesList();

		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		context.rng().restoreState(gameStartRngState);

		game.start(context);

		int moveIdx = 0;

		while (moveIdx < trial.numInitialPlacementMoves())
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

			if (game.mode().mode() == ModeType.Alternating)
			{
				final Move loadedMove = loadedMoves.get(moveIdx);
				final List<Action> loadedAllActions = loadedMove.getActionsWithConsequences(context);

				Move matchingMove = null;
				for (final Move move : legalMoves.moves())
				{
					if (move.from() == loadedMove.from() && move.to() == loadedMove.to())
					{
						if (move.getActionsWithConsequences(context).equals(loadedAllActions))
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
					System.out.println("No matching move found for: " + loadedAllActions);

					for (final Move move : legalMoves.moves())
					{
						System.out.println("legal move: " + move.getActionsWithConsequences(context));
					}
					
//					try 
//					{
//						System.out.println("Saving trial at time of crash to: " + new File("./Trial.trl").getAbsolutePath());
//						trial.saveTrialToTextFile(new File("./Trial.trl"), game.name(), new ArrayList<String>(), gameStartRngState);
//					} 
//					catch (final IOException e) {
//						e.printStackTrace();
//					}

					fail();
				}

				game.apply(context, matchingMove);
			}
			else
			{
				// simultaneous-move game
				// we expect each of the actions of the loaded move to be contained
				// in at least one of the legal moves
				boolean foundNonMatch = false;

				for (final Action subAction : loadedMoves.get(moveIdx).actions())
				{
					boolean foundMatch = false;

					for (final Move move : legalMoves.moves())
					{
						if (move.getActionsWithConsequences(context).contains(subAction))
						{
							foundMatch = true;
							break;
						}
					}

					if (!foundMatch)
					{
						foundNonMatch = true;
						break;
					}
				}

				assert (!foundNonMatch);
				game.apply(context, loadedMoves.get(moveIdx));
			}

			++moveIdx;
		}

		if (trial.status() == null)
			assert(playedTrial.status() == null);
		else
			assert(trial.status().winner() == playedTrial.status().winner());

		assert(Arrays.equals(context.trial().ranking(), playedContext.trial().ranking()));
	}

}
