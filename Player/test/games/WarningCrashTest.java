package games;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import main.FileHandling;
import main.grammar.Description;
import other.GameLoader;

/**
 * Unit Test for testing that no game has any required ludeme or will crash.
 *
 * @author Eric.Piette
 */
public class WarningCrashTest
{
	@Test
	@SuppressWarnings("static-method")
	public void runTests()
	{
		// Load from memory
		final String[] choices = FileHandling.listGames();

		for (final String filePath : choices)
		{
			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
				continue;

			if (filePath.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
				continue;

			// Get game description from resource
			// System.out.println("Game: " + filePath);

			String path = filePath.replaceAll(Pattern.quote("\\"), "/");
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
					// System.out.println("line: " + line);
				}
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
				fail();
			}

			// Parse and compile the game
			Game game = null;
			try
			{
				game = (Game)compiler.Compiler.compileTest(new Description(desc), false);
			}
			catch (final Exception e)
			{
				System.err.println("** FAILED TO COMPILE: " + filePath + ".");
				e.printStackTrace();
				fail();
			}

			if (game != null)
			{
				System.out.println("Compiled " + game.name() + ".");

				final int indexLastSlash = filePath.lastIndexOf('/');
				final String fileName = filePath.substring(indexLastSlash + 1, filePath.length() - ".lud".length());

				if (!fileName.equals(game.name()))
				{
					System.err.println("The fileName of " + fileName
							+ ".lud is not equals to the name of the game which is " + game.name());
					fail();
				}
			}
			else
			{
				System.err.println("** FAILED TO COMPILE: " + filePath + ".");
				fail();
			}

			if (game.hasMissingRequirement())
			{
				System.err.println(game.name() + " has missing requirements.");
				fail();
			}

			if (game.willCrash())
			{
				System.err.println(game.name() + " is going to crash.");
				fail();
			}

		}
	}
}
