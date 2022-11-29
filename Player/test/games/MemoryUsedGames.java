package games;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import compiler.Compiler;
import game.Game;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.grammar.Description;
import other.GameLoader;

/**
 * To generate a CSV showing the memory used for each game.
 *
 * @author Eric.Piette
 */
public class MemoryUsedGames
{
	public static void main(final String[] args)
	{
		final String output = "MemoryUsageGames.csv";
		
		System.out.println("\n=========================================\nTest: Compile all .lud from memory:\n");

		final List<String> failedGames = new ArrayList<String>();

		boolean failure = false;
		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();

		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
		
			for (final String fileName : choices)
			{
				if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
					continue;
				
				if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
					continue;
				
				if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
					continue;
	
				if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
					continue;
	
				if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/validation/"))
					continue;
	
				if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/pending/"))
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
					Thread.sleep(250);
					System.gc ();
					System.runFinalization ();
					Thread.sleep(250);
					game = (Game)Compiler.compileTest(new Description(desc), false);
					Runtime rt = Runtime.getRuntime();
					long total_mem = rt.totalMemory();
					long free_mem = rt.freeMemory();
					long used_mem = total_mem - free_mem;
					System.out.println("Amount of used memory: " + (used_mem/ 1000000));
					
					final List<String> lineToWrite = new ArrayList<String>();
					lineToWrite.add(game.name());
					lineToWrite.add((used_mem/ 1000000) + " MB");
					writer.println(StringRoutines.join(",", lineToWrite));
					Thread.sleep(250);
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
			
		}
		catch (FileNotFoundException | UnsupportedEncodingException e2)
		{
			e2.printStackTrace();
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
