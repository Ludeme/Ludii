package travis.integrity;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.source64.SplitMix64;
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
 * @author Dennis Soemers and cambolbro
 */
public class TestTrialsIntegrity
{
	/**
	 * The test to run
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	@SuppressWarnings("static-method")
	public void test() throws FileNotFoundException, IOException
	{
		System.out.println(
				"\n=========================================\nIntegrity Test\n=========================================\n");

		final long startAt = System.nanoTime();
		final File startFolder = new File("../Common/res/lud");
		final SplitMix64 rng = new SplitMix64();
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
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					
					if (fileEntryPath.equals("../Common/res/lud/plex"))
						continue;
					
					if (fileEntryPath.equals("../Common/res/lud/wip"))
						continue;
					
					if (fileEntryPath.equals("../Common/res/lud/wishlist"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/test"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/reconstruction"))
						continue;
					
					if (fileEntryPath.equals("../Common/res/lud/puzzle/deduction"))
						continue;	// skip puzzles for now
					
					if (fileEntryPath.equals("../Common/res/lud/bad"))
						continue;
					
					if (fileEntryPath.equals("../Common/res/lud/bad_playout"))
						continue;
					
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}
		
		int iterations = 0;
		for (final File fileEntry : entries)
		{			
			if (fileEntry.getName().contains(".lud"))
			{
				final String ludPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
				final String trialDirPath = 
						ludPath
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
				Game game = null;
				try
				{
					game = (Game)Compiler.compileTest(new Description(desc), false);
				}
				catch (final Exception e)
				{
					System.out.println("Fail(): Testing re-play of trial: " + ludPath);
					e.printStackTrace();
					fail("COMPILATION FAILED for the file : " + ludPath);
				}

				if (game == null)
				{
					System.out.println("Fail(): Testing re-play of trial: " + ludPath);
					fail("COMPILATION FAILED for the file : " + ludPath);
				}

				if (game.isSimulationMoveGame())
					continue;
				
				System.out.print(".");
				if (++iterations % 80 == 0)
					System.out.println();

				final File trialFile = trialFiles[rng.nextInt(trialFiles.length)];
				final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
				final Trial loadedTrial = loadedRecord.trial();
				final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();

				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);
				context.rng().restoreState(loadedRecord.rngState());

				game.start(context);

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
							System.out.println(
									"Loaded Move Actions = " + loadedMoves.get(moveIdx).getActionsWithConsequences(context));
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
						System.out.println("corrected moveIdx = " + (moveIdx - context.currentInstanceContext().trial().numInitialPlacementMoves()));
						System.out.println("moveIdx = " + moveIdx);
						System.out.println("Trial was not supposed to be over, but it is!");
						fail();
					}

					final Moves legalMoves = game.moves(context);

					// make sure that the number of legal moves is the same
					// as stored in file
					final int numInitPlacementMoves = context.currentInstanceContext().trial()
							.numInitialPlacementMoves();

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

						game.apply(context, matchingMove);
					}
					else
					{
						// simultaneous-move game
						// the full loaded move should be equal to one of the
						// possible large combined moves
						final FastArrayList<Move> legal = legalMoves.moves();

						final int numPlayers = game.players().count();
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
							System.out.println("Found no combination of submoves that generate loaded move: "
									+ loadedMoveAllActions);
							fail();
						}

						game.apply(context, loadedMoves.get(moveIdx));
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

		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");
	}

}
