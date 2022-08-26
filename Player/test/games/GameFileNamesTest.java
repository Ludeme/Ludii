package games;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;

//-----------------------------------------------------------------------------

/**
 * Tests if all game names are equal to their filenames
 *
 * @author Dennis Soemers
 */
public class GameFileNamesTest
{
	@Test
	public static void testGameFileNames() throws IOException
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		// We compute the .lud files (and not the ludemeplex).
		final List<File> entries = new ArrayList<>();

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

					if (path.equals("../Common/res/lud/wishlist"))
						continue;

					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/bad"))
						continue;

					if (path.equals("../Common/res/lud/bad_playout"))
						continue;

					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}

		// Test of compilation for each of them.
		for (final File fileEntry : entries)
		{
			final String filepath = fileEntry.getPath();
			final String filename = fileEntry.getName();

			System.out.println("File: " + filepath);

			// Load the string from file
			final String desc = FileHandling.loadTextContentsFromFile(filepath);

			// Parse and compile the game
//			final UserSelections userSelections = new UserSelections();
			final Game game = (Game)Compiler.compileTest(new Description(desc), false);
			assert (game != null);
			assert (filename.substring(0, filename.length() - ".lud".length()).equals(game.name()));
		}
	}

}
