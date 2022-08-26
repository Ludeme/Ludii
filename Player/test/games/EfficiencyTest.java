package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import other.context.Context;
import other.trial.Trial;

/**
 * Unit Test to run a time random playouts for all the games
 * 
 * @author Eric.Piette
 */
public class EfficiencyTest
{
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();
		final List<File> badCompEntries = new ArrayList<File>();
		final List<File> badPlayoutEntries = new ArrayList<File>();

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
						continue; // skip deduction puzzles for now

					if (path.equals("../Common/res/lud/bad"))
					{
						// We'll only find intentionally bad lud files here
						for (final File fileEntryInter : fileEntry.listFiles())
						{
							badCompEntries.add(fileEntryInter);
						}
					}
					else if (path.equals("../Common/res/lud/bad_playout"))
					{
						// We'll only find lud files here which should compile,
						// but fail to run
						for (final File fileEntryInter : fileEntry.listFiles())
						{
							badPlayoutEntries.add(fileEntryInter);
						}
					}
					else
					{
						// We'll find files that we should be able to compile
						// and run here
						gameDirs.add(fileEntry);
					}
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}

		// the following entries should correctly compile and run
		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				// Load the string from file
				String desc = "";
				try
				{
					desc = FileHandling.loadTextContentsFromFile(fileName);
				}
				catch (final FileNotFoundException ex)
				{
					System.out.println("Unable to open file '" + fileName + "'");
				}
				catch (final IOException ex)
				{
					System.out.println("Error reading file '" + fileName + "'");
				}

				// Parse and compile the game
				final Game game = (Game)Compiler.compileTest(new Description(desc), false);
				if (game != null)
				{
					System.out.println("Compiled " + game.name() + " successfully.");
				}
				else
				{
					System.out.println("** FAILED TO COMPILE GAME.");
					fail("COMPILATION FAILED for the file : " + fileName);
				}

				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);

				// Warming
				long stopAt = 0;
				long start = System.nanoTime();
				double abortAt = start + 10 * 1000000000.0;

				while (stopAt < abortAt)
				{
					game.start(context);
					game.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
					stopAt = System.nanoTime();
				}

				stopAt = 0;
				System.gc();
				start = System.nanoTime();
				abortAt = start + 30 * 1000000000.0;
				int playouts = 0;
				int moveDone = 0;
				while (stopAt < abortAt)
				{
					game.start(context);
					game.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
					stopAt = System.nanoTime();
					moveDone += context.trial().numMoves();
					playouts++;
				}

				final double secs = (stopAt - start) / 1000000000.0;
				final double rate = (playouts / secs);
				final double rateMove = (moveDone / secs);

				System.out.println(rate + "p/s");
				System.out.println(rateMove + "m/s");
			}
		}

	}

}
