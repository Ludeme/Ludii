package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;

/**
 * Unit Test to check if one of our "official" games has no info
 *
 * @author Eric.Piette
 */
public class TestEmptyInfo
{
	@Test
	public static void testCompilingLudFromFile()
	{
		System.out.println("=========================================\nTest: Compile all .lud from file:\n");

		boolean failure = false;
		final long startAt = System.nanoTime();

		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		final List<String> failedGames = new ArrayList<String>();
		final List<String> noInfoGames = new ArrayList<String>();

		// We compute the .lud files (and not the ludemeplex).
		final List<File> entries = new ArrayList<>();
		final List<File> badEntries = new ArrayList<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

					if (path.contains("lud/plex"))
						continue;

					if (path.contains("lud/wip"))
						continue;

					if (path.contains("wishlist"))
						continue;

					if (path.contains("lud/test"))
						continue;

					if (path.contains("lud/bad"))
					{
						// We'll only find intentionally bad lud files here
						for (final File fileEntryInter : fileEntry.listFiles())
						{
							badEntries.add(fileEntryInter);
						}
					}
					else
					{
						// We'll find files that we should be able to compile
						// here
						gameDirs.add(fileEntry);
					}
				}
				else
				{
					if (!fileEntry.getName().contains(".lud"))
						continue; // skip non-ludeme .DS_Store files

					entries.add(fileEntry);
				}
			}
		}

		// Test of compilation for each of them.
		for (final File fileEntry : entries)
		{
			final String fileName = fileEntry.getPath();

			System.out.println("File: " + fileName);

			// Load the string from file
			String desc = "";
			// String line = null;
			try
			{
				// final FileReader fileReader = new FileReader(fileName);
				// final BufferedReader bufferedReader = new
				// BufferedReader(fileReader);
				// while ((line = bufferedReader.readLine()) != null)
				// desc += line + "\n";
				// bufferedReader.close();
				desc = FileHandling.loadTextContentsFromFile(fileName);
			}
			catch (final FileNotFoundException ex)
			{
				failure = true;
				System.err.println("Unable to open file '" + fileName + "'");
			}
			catch (final IOException ex)
			{
				failure = true;
				System.err.println("Error reading file '" + fileName + "'");
			}

			// Parse and compile the game
			final Game game = (Game)Compiler.compileTest(new Description(desc), false);
			if (game != null && (game.metadata().info().getItem().size() != 0
					|| (game.metadata().info().getItem().size() == 1 && game.metadata().info().getItem().get(0) != null)))
			{
				System.out.println("Compiled and has info " + game.name() + ".\n");
			}
			else if (game == null)
			{
				failure = true;
				failedGames.add(fileName);
				System.err.println("** FAILED TO COMPILE GAME.");
			}
			else if (game.metadata().info().getItem().size() == 0 || (game.metadata().info().getItem().size() == 1
					&& game.metadata().info().getItem().get(0) == null))
			{
				failure = true;
				noInfoGames.add(fileName);
				System.err.println("** HAS NO INFO.");
			}
		}

		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("Compiled " + entries.size() + " games.");
		System.out.println("Time: " + secs + "s.");

		if (!failedGames.isEmpty())
		{
			System.out.println("The uncompiled games are ");
			for (final String name : failedGames)
				System.out.println(name);
		}

		if (!noInfoGames.isEmpty())
		{
			System.out.println("The games without info are ");
			for (final String name : noInfoGames)
				System.out.println(name);
		}

		if (failure)
			fail();
	}

}
