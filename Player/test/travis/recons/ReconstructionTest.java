package travis.recons;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import other.GameLoader;

/**
 * Unit Test to compile all the reconstruction games on the lud/reconstruction folder
 *
 * @author Eric.Piette
 */
public class ReconstructionTest
{
	@Test
	public void testCompilingLudFromMemory()
	{
		System.out.println("\n=========================================\nTest: Compile all .lud corresponding to reconstruction:\n");

		final List<String> failedGames = new ArrayList<String>();

		boolean failure = false;
		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();

		for (final String fileName : choices)
		{
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/"))
				continue;
			
			// Get game description from resource
			System.out.println("Game: " + fileName);

			String path = fileName.replaceAll(Pattern.quote("\\"), "/");
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
					//						System.out.println("line: " + line);
				}
			}
			catch (final IOException e1)
			{
				failure = true;
				e1.printStackTrace();
			}

			// Parse and compile the game
			Game game = null;
			try
			{
				game = (Game)Compiler.compileTest(new Description(desc), false);
			}
			catch (final Exception e)
			{
				failure = true;
				e.printStackTrace();
			}

			if (game != null)
			{
				System.out.println("Compiled " + game.name() + ".");
			}
			else
			{
				failure = true;
				failedGames.add(fileName);
				System.err.println("** FAILED TO COMPILE: " + fileName + ".");
			}
		}
		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");

		if (!failedGames.isEmpty())
		{
			System.out.println("\nUncompiled games:");
			for (final String name : failedGames)
				System.out.println(name);
		}

		if (failure)
			fail();
	}

}
