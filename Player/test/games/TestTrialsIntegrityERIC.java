package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import compiler.Compiler;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import main.FileHandling;
import main.collections.FastArrayList;
import main.collections.ListUtils;
import main.grammar.Description;
import manager.utils.game_logs.MatchRecord;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import utils.AIUtils;

/**
 * A Unit Test to load Trials from the TravisTrials repository, and check if they
 * all still play out the same way in the current Ludii codebase.
 * 
 * @author Dennis Soemers and Eric.Piette
 */
@SuppressWarnings("static-method")
public class TestTrialsIntegrityERIC
{
	/**
	 * The test to run
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void test() throws FileNotFoundException, IOException
	{
		final File startFolder = new File("../Common/res/lud");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();

		//final String moreSpecificFolder = "../Common/res/lud/board/war/leaping/diagonal";
		final String moreSpecificFolder = "";
		
		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

					if (fileEntryPath.equals("../Common/res/lud/plex"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wip"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wishlist"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/reconstruction"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/test"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip deduction puzzles

					if (fileEntryPath.equals("../Common/res/lud/bad"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/bad_playout"))
						continue;

//					// We exclude that game from the tests because the legal
//					// moves are too slow to test.
//					if (fileEntryPath.contains("Residuelllllllll"))
//						continue;

						gameDirs.add(fileEntry);
				}
				else
				{
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					if (moreSpecificFolder.equals("") || fileEntryPath.contains(moreSpecificFolder))
						entries.add(fileEntry);
				}
			}
		}
		
		boolean gameReached = false;
		final String gameToReached = "";
		final String gameToSkip = "";

		final long startTime = System.currentTimeMillis();

		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains("Oust"))
			{
				if (fileEntry.getName().contains(gameToReached) || gameToReached.length() == 0)
					gameReached = true;

				if (!gameReached)
					continue;

				if (!gameToSkip.equals("") && fileEntry.getName().contains(gameToSkip))
					continue;

				final String ludPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
				final String trialDirPath = ludPath
						.replaceFirst(Pattern.quote("/Common/res/"), Matcher.quoteReplacement("/../TravisTrials/"))
						.replaceFirst(Pattern.quote("/lud/"), Matcher.quoteReplacement("/random_trials/"))
						.replace(".lud", "");

				final File trialsDir = new File(trialDirPath);

				if (!trialsDir.exists())
				{
					System.err.println("WARNING: No directory of trials exists at: " + trialsDir.getAbsolutePath());
					continue;
				}

				final File[] trialFiles = trialsDir.listFiles();

				if (trialFiles.length == 0)
				{
					System.err.println("WARNING: No trial files exist in directory: " + trialsDir.getAbsolutePath());
					continue;
				}

				// Load the string from lud file
				String desc = "";
				try
				{
					desc = FileHandling.loadTextContentsFromFile(ludPath);
				}
				catch (final FileNotFoundException ex)
				{
					fail("Unable to open file '" + ludPath + "'");
				}
				catch (final IOException ex)
				{
					fail("Error reading file '" + ludPath + "'");
				}

				// Parse and compile the game
				final Game game = (Game)Compiler.compileTest(new Description(desc), false);
				if (game == null)
					fail("COMPILATION FAILED for the file : " + ludPath);

				if (game.hasSubgames() || game.isSimulationMoveGame())
					continue;

				for (final File trialFile : trialFiles)
				{
					System.out.println("Testing re-play of trial: " + trialFile);
					final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
					final Trial loadedTrial = loadedRecord.trial();
					final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();

					final Context context = new Context(game, new Trial(game));
					context.rng().restoreState(loadedRecord.rngState());

					final Trial trial = context.trial();
					game.start(context);
					
					int moveIdx = 0;
					
					while (moveIdx < trial.numInitialPlacementMoves())
					{
//						System.out.println("init moveIdx: " + moveIdx);
//						System.out.println("Move on the trial is = " + trial.getMove(moveIdx));
//						System.out.println("loadedMoves.get(moveIdx) = " + loadedMoves.get(moveIdx));
						assert (loadedMoves.get(moveIdx).equals(trial.getMove(moveIdx)));
						++moveIdx;
					}

					while (moveIdx < loadedMoves.size())
					{
						// System.out.println("moveIdx after init: " + moveIdx);

						while (moveIdx < trial.numMoves())
						{
							// looks like some actions were auto-applied (e.g.
							// in ByScore End condition)
							// so we just check if they're equal, without
							// applying them again from loaded file
							final List<Action> loadedAllActions = loadedMoves.get(moveIdx).getActionsWithConsequences(context);
							final List<Action> trialMoveAllActions = trial.getMove(moveIdx).getActionsWithConsequences(context);
							
							if(!loadedAllActions.equals(trialMoveAllActions))
							{
								System.out.println("moveIdx = " + (moveIdx - trial.numInitialPlacementMoves()));
								System.out.println("loadedAllActions = " + loadedAllActions);
								System.out.println("trialMoveAllActions = " + trialMoveAllActions);
							}
							
							assert (loadedAllActions.equals(trialMoveAllActions)) : 
								("Loaded Move Actions = "
										+ loadedAllActions + ", trial actions = "
										+ trialMoveAllActions);
							++moveIdx;
						}

						if (moveIdx == loadedMoves.size())
							break;

						if (trial.over())
						{
							System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
							System.out.println("Failed at trial file: " + trialFile);
							System.out.println("corrected moveIdx = " + (moveIdx - context.currentInstanceContext().trial().numInitialPlacementMoves()));
							System.out.println("moveIdx = " + moveIdx);
							System.out.println("Trial was not supposed to be over, but it is!");
							fail();
						}

						final Moves legalMoves = game.moves(context);

						// make sure that the number of legal moves is the same
						// as stored in file
						if (loadedTrial.auxilTrialData() != null)
						{
							if (legalMoves.moves().size() != loadedTrial.auxilTrialData().legalMovesHistorySizes()
									.getQuick(moveIdx - trial.numInitialPlacementMoves()))
							{
								System.out.println("moveIdx = " + (moveIdx - trial.numInitialPlacementMoves()));
								System.out.println("legalMoves.moves().size() = " + legalMoves.moves().size());
								System.out.println(
										"loadedTrial.legalMovesHistorySizes().getQuick(moveIdx - trial.numInitPlace()) = "
												+ loadedTrial.auxilTrialData().legalMovesHistorySizes()
														.getQuick(moveIdx - trial.numInitialPlacementMoves()));
							}

							assert (legalMoves.moves().size() == loadedTrial.auxilTrialData().legalMovesHistorySizes()
									.getQuick(moveIdx - trial.numInitialPlacementMoves()));
						}


						final Move loadedMove = loadedMoves.get(moveIdx);
						final List<Action> loadedMoveAllActions = loadedMove.getActionsWithConsequences(context);

						if (game.mode().mode() == ModeType.Alternating)
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
								System.out.println("moveIdx = " + (moveIdx - trial.numInitialPlacementMoves()));
								System.out.println("Loaded move = " + loadedMove.getActionsWithConsequences(context)
										+ " from is " + loadedMove.fromType() + " to is "
										+ loadedMove.toType());

								for (final Move move : legalMoves.moves())
								{
									System.out.println("legal move = " + move.getActionsWithConsequences(context) + " move from is "
											+ move.fromType() + " to " + move.toType());
								}
							}

							assert (matchingMove != null);
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
					{
						if (loadedTrial.status() != null)
							System.out
									.println("Game not over but should be in moveIdx = "
											+ (moveIdx - trial.numInitialPlacementMoves()));
						assert (loadedTrial.status() == null);
					}
					else
						assert (trial.status().winner() == loadedTrial.status().winner());

					assert (Arrays.equals(trial.ranking(), loadedTrial.ranking()));
				}
			}
		}

		System.out.println("Finished TestTrialsIntegrity!");

		final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		System.out.println("Done in " + minutes + " minutes " + seconds + " seconds");
	}

}
