package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.junit.Test;

import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import game.types.state.GameType;
import main.collections.FastArrayList;
import main.collections.ListUtils;
import other.GameLoader;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import utils.AIUtils;

/**
 * Unit test that, for every game, tests whether we can
 * correctly run playouts in parallel.
 * 
 * @author Dennis Soemers
 */
public class TestParallelPlayouts
{
	
	/** Number of parallel playouts we run */
	private static final int NUM_PARALLEL = 4;
	
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
				System.out.println("Trying game file: " + fileName);

				final Game game = GameLoader.loadGameFromFile(fileEntry);
				
				if (game.isSimulationMoveGame())
					continue;
				
				if (game.hasSubgames())
					continue;

				// run parallel trials
				final Context[] contexts = new Context[NUM_PARALLEL];
				final RandomProviderDefaultState[] gameStartRngStates = new RandomProviderDefaultState[NUM_PARALLEL];
				
				for (int i = 0; i < NUM_PARALLEL; ++i)
				{
					contexts[i] = new Context(game, new Trial(game));
					gameStartRngStates[i] = (RandomProviderDefaultState) contexts[i].rng().saveState();
				}
				
				final ExecutorService executorService = Executors.newFixedThreadPool(NUM_PARALLEL);
				
				final List<Future<Context>> playedContexts = new ArrayList<Future<Context>>(NUM_PARALLEL);
				for (int i = 0; i < NUM_PARALLEL; ++i)
				{
					final Context context = contexts[i];
					playedContexts.add(executorService.submit(() -> {
						game.start(context);
						game.playout(context, null, 1.0, null, 0, 30, ThreadLocalRandom.current());
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
					e.printStackTrace();
					fail();
				}
				
				executorService.shutdown();
					
				// check if we can still execute them all the same way in serial mode
				for (int parallelPlayout = 0; parallelPlayout < NUM_PARALLEL; ++parallelPlayout)
				{
					final Context parallelContext = endContexts[parallelPlayout];
					final Trial parallelTrial = parallelContext.trial();
					final List<Move> loadedMoves = parallelTrial.generateCompleteMovesList();
					
					final Trial trial = new Trial(game);
					final Context context = new Context(game, trial);
					context.rng().restoreState(gameStartRngStates[parallelPlayout]);
					
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
						
						if (trial.over())
						{
							System.out.println("Serial trial already over after moves:");
							for (final Move move : trial.generateCompleteMovesList())
							{
								System.out.println(move);
							}
							System.out.println("When run in parallel, trial only ended after moves:");
							for (final Move move : parallelTrial.generateCompleteMovesList())
							{
								System.out.println(move);
							}
							fail();
						}
						
						final Moves legalMoves = game.moves(context);
						final List<Action> loadedMoveAllActions = loadedMoves.get(moveIdx).getActionsWithConsequences(context);
						
						if (game.mode().mode() == ModeType.Alternating)
						{
							Move matchingMove = null;
							for (final Move move : legalMoves.moves())
							{
								if (move.getActionsWithConsequences(context).equals(loadedMoveAllActions))
								{
									matchingMove = move;
									break;
								}
							}
							
							if (matchingMove == null)
							{
								if (loadedMoves.get(moveIdx).isPass() && legalMoves.moves().isEmpty())
									matchingMove = loadedMoves.get(moveIdx);
							}
							
							if (matchingMove == null)
							{
								System.out.println("No matching move found for: " + loadedMoveAllActions);
								
								for (final Move move : legalMoves.moves())
								{
									System.out.println("legal move: " + move.getActionsWithConsequences(context));
								}
								
								fail();
							}
							
							//assert(matchingMove != null);
							assert
							(
								matchingMove.fromNonDecision() == matchingMove.toNonDecision() ||
								(game.gameFlags() & GameType.UsesFromPositions) != 0L
							);
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
						assert(parallelTrial.status() == null);
					else
						assert(trial.status().winner() == parallelTrial.status().winner());
					
					if (!Arrays.equals(context.trial().ranking(), parallelContext.trial().ranking()))
					{
						System.out.println("Ranking when run in parallel: " + Arrays.toString(parallelContext.trial().ranking()));
						System.out.println("Ranking when run serially: " + Arrays.toString(context.trial().ranking()));
						fail();
					}
					
					// we're done with this one, let's allow memory to be cleared
					endContexts[parallelPlayout] = null;
				}
			}
		}
	}

}
