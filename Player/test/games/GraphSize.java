package games;

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

//-----------------------------------------------------------------------------

/**
 * Print the graph size of each game.
 *
 * @author Eric.Piette
 */
public class GraphSize
{
	@Test
	public static void testCompilingLudFromMemory()
	{
		final List<String> list = new ArrayList<String>();
		final List<String> failedGames = new ArrayList<String>();

		boolean failure = false;

		// Load from memory
		final String[] choices = FileHandling.listGames();

		for (final String fileName : choices)
		{
			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
				continue;

			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/"))
				continue;

			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
				continue;

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
					// System.out.println("line: " + line);
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
				
//				if (
//						!game.isStacking() && 
//						!game.isStochasticGame() && 
//						!game.hiddenInformation() && 
//						!game.board().isMancalaBoard() && 
//						!game.hasCard() &&
//						!game.hasDominoes() &&
//						!game.isDeductionPuzzle() &&
//						game.players().size() == 3)
//				{
//					System.out.print(game.name() + "_" + game.board().topology().vertices().size()
//						+ "_"
//						+ game.board().topology().edges().size() + "_" + game.board().topology().faces().size()
//						+ "_");
//					System.out.println();
//				}
				
				System.out.print(game.name() + " - Graph size = (" + game.board().topology().cells().size()
						+ " vertices, "
						+ game.board().topology().edges().size() + " edges, " + game.board().topology().vertices().size()
						+ " faces) ");
				if (game.equipment().containers().length < 2)
					System.out.println();
				else
				{
					for (int i = 1; i < game.equipment().containers().length - 1; i++)
						System.out.print(
								"Hand " + i + " size = " + game.equipment().containers()[i].numSites() + " / ");
					System.out.println("Hand " + (game.equipment().containers().length - 1) + " size = "
							+ game.equipment().containers()[game.equipment().containers().length - 1].numSites());
				}
			}
			catch (final Exception e)
			{
				failure = true;
				e.printStackTrace();
			}

			if (game != null)
				list.add(game.name());
			else
			{
				failure = true;
				failedGames.add(fileName);
				System.err.println("** FAILED TO COMPILE: " + fileName + ".");
			}
		}
		if (failure)
			fail();
	}

}
