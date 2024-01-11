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

import org.junit.Test;

import compiler.Compiler;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import main.FileHandling;
import main.grammar.Description;
import manager.utils.game_logs.MatchRecord;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * For Puzzle: A Unit Test to load Trials from the TravisTrials repository, and
 * check if they all still play out the same way in the current Ludii codebase.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("static-method")
public class TestTrialsIntegrityPuzzle
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
		System.out.println(
				"\n=========================================\nIntegrity Deduction Puzzle Test\n=========================================\n");

		final long startAt = System.nanoTime();
		final File startFolder = new File("../Common/res/lud/puzzle/deduction");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
					gameDirs.add(fileEntry);
				else
					entries.add(fileEntry);
			}
		}

		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String ludPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
				final String trialDirPath = ludPath
						.replaceFirst(Pattern.quote("/Common/res/"), Matcher.quoteReplacement("/Player/res/"))
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

				if (game.hasSubgames())
					continue;

				for (final File trialFile : trialFiles)
				{
					final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
					final Trial loadedTrial = loadedRecord.trial();
					final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();

					final Trial trial = new Trial(game);
					final Context context = new Context(game, trial);
					context.rng().restoreState(loadedRecord.rngState());

					game.start(context);

					int moveIdx = 0;

					while (moveIdx < trial.numInitialPlacementMoves())
					{
//						System.out.println("init moveIdx: " + moveIdx);
//						System.out.println("Move on the trial is = " + trial.moves().get(moveIdx));
//						System.out.println("loadedMoves.get(moveIdx) = " + loadedMoves.get(moveIdx));

						if (!loadedMoves.get(moveIdx).equals(trial.getMove(moveIdx)))
						{
							System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
							System.out.println("Moves not equal.");
							System.out.println("init moveIdx: " + moveIdx);
							System.out.println("Move on the trial is = " + trial.getMove(moveIdx));
							System.out.println("loadedMoves.get(moveIdx) = " + loadedMoves.get(moveIdx));
							fail("One of the init moves was different in stored trial!");
						}

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

							if (!loadedMoves.get(moveIdx).getActionsWithConsequences(context)
									.equals(trial.getMove(moveIdx).getActionsWithConsequences(context)))
							{
								System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
								System.out.println("Mismatch in actions.");
								System.out.println(
										"Loaded Move Actions = " + loadedMoves.get(moveIdx).getActionsWithConsequences(context));
								System.out.println(
										"trial actions = " + trial.getMove(moveIdx).getActionsWithConsequences(context));
								fail("Mismatch in auto-applied actions.");
							}

							++moveIdx;
						}

						if (moveIdx == loadedMoves.size())
							break;

						if (trial.over())
							fail("Trial over too early for " + ludPath);

						final Moves legalMoves = game.moves(context);

						// make sure that the number of legal moves is the same
						// as stored in file
//						if (legalMoves.moves().size() != loadedTrial.legalMovesHistorySizes()
//								.getQuick(moveIdx - trial.numInitPlace()))
//						{
//							System.out.println("moveIdx = " + (moveIdx - trial.numInitPlace()));
//							System.out.println("legalMoves.moves().size() = " + legalMoves.moves().size());
//							System.out.println(
//									"loadedTrial.legalMovesHistorySizes().getQuick(moveIdx - trial.numInitPlace()) = "
//											+ loadedTrial.legalMovesHistorySizes()
//													.getQuick(moveIdx - trial.numInitPlace()));
//							fail("Incorrect number of legal moves");
//						}

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

						++moveIdx;
					}

					if (trial.status() == null)
					{
						if (loadedTrial.status() != null)
						{
							System.out.println("Fail(): Testing re-play of trial: " + trialFile.getParent());
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
		}

		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");
	}

}
