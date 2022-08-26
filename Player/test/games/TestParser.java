package games;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
 * Parses all files in res/lud/test/parser.
 *
 * @author cambolbro
 */
public class TestParser
{
	@Test
	public static void testCompilingLudFromMemory()
	{
		System.out.println("\n======================================================");
		System.out.println("Test: Parsing test .lud from memory:\n");

		// Cause grammar to be initialised
		Grammar.grammar();
		final Report report = new Report();
		
		final long startAt = System.currentTimeMillis();

		// Load from memory
		final String[] choices = FileHandling.listGames();

		for (final String fileName : choices)
		{
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/parser"))
				continue;
			
			// Get game description from resource
			System.out.println("---------------------------------------------------");
			System.out.println("File: " + fileName);

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
			if (!report.isError())
			{
				if (report.isWarning())
					for (final String warning : report.warnings())
						System.out.println("- Warning: " + warning);
				
				System.out.println("Parsed okay.");				
			}
			else
			{
				//System.out.println("\nFailed to parse:");
				System.out.println(description.expanded());
				for (final String error : report.errors())
					System.out.println("* " + error);
			}
		}

		final long stopAt = System.currentTimeMillis();
		final double secs = (stopAt - startAt) / 1000.0;
		System.out.println("\nDone in " + secs + "s.");
	}

}
