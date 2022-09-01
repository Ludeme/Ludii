package games;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import grammar.Grammar;
import parser.Parser;
import main.FileHandling;
import main.grammar.Description;
import main.grammar.Report;
import main.options.UserSelections;
import other.GameLoader;

//-----------------------------------------------------------------------------

/**
 * Unit Test to parse all the game descriptions.
 * Basically a speed test.
 *
 * @author cambolbro
 */
public class ParseAllLud
{
	@Test
	public static void testCompilingLudFromMemory()
	{
		System.out.println("\n=========================================");
		System.out.println("Test: Compile all .lud from memory:\n");

		// Cause grammar to be initialised
		Grammar.grammar();
		//final Parser parser = new Parser();
		final Report report = new Report();
		
		final List<String> failed = new ArrayList<String>();

		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();
		for (final String fileName : choices)
		{
			if 
			(
				fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")
				||
				fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")
				||
				fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")
				||
				fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")
			)
				continue;
			
			// Get game description from resource
			System.out.println("Parsing " + fileName + "...");

			String path = fileName.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));

			String desc = "";
				try (final InputStream in = GameLoader.class.getResourceAsStream(path))
				{
					try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in)))
					{
						String line;
						while ((line = rdr.readLine()) != null)
								desc += line + "\n";
					}
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			
			// Parse this game description
			final Description description = new Description(desc);	
			final UserSelections userSelections = new UserSelections(new ArrayList<String>());
			
			Parser.expandAndParse(description, userSelections, report, false);	
			if (report.isError())
			{
				failed.add(fileName);
				System.out.println("X");
				for (final String error : report.errors())
					System.out.println("X: " + error);
			}
		}
		
		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");

		if (!failed.isEmpty())
		{
			System.out.println("\n" + failed.size() + " games did not parse:");
			for (final String name : failed)
				System.out.println(name);
		}
	}

}
