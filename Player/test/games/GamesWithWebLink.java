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

/**
 * To print all the link in each game.
 * 
 * @author Eric.Piette
 */
public class GamesWithWebLink
{
	@Test
	public static void testLinkInGame()
	{
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
			}
			catch (final Exception e)
			{
				failure = true;
				e.printStackTrace();
			}

			if (game != null)
			{
				for(final String source :game.metadata().info().getSource())
				{
					if(source.contains("http") || source.contains("www"))
						System.out.println("Game: " + game.name() + " has a source with a link");
				}
				
				for(final String rules :game.metadata().info().getRules())
				{
					if(rules.contains("http") || rules.contains("www"))
						System.out.println("Game: " + game.name() + " has a rule with a link");
				}
				
				for(final String publisher : game.metadata().info().getPublisher())
				{
					if(publisher.contains("http")|| publisher.contains("www"))
						System.out.println("Game: " + game.name() + " has a publisher with a link");
				}
				
				for(final String description : game.metadata().info().getDescription())
				{
					if(description.contains("http")|| description.contains("www"))
						System.out.println("Game: " + game.name() + " has a description with a link");
				}

			}
			else
			{
				failure = true;
				failedGames.add(fileName);
				System.err.println("** FAILED TO COMPILE: " + fileName + ".");
			}
		}
		if (failure)
			fail();

//		try (OutputStream os = new FileOutputStream(new File("rulesLudii.txt")))
//		{
//			os.write(sb.toString().getBytes(), 0, sb.length());
//		}
//		catch (final IOException e)
//		{
//			e.printStackTrace();
//		}
//		finally{
//            try {
//                os.close();
//            } catch (final IOException e) {
//                e.printStackTrace();
//            }
//		}
	}

}
