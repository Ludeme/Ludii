package utils.recons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import completer.Completion;
import main.FileHandling;
import other.GameLoader;

/**
 * Unit Test to compile all the reconstruction games on the lud/reconstruction folder
 *
 * @author Eric.Piette
 */
public class ReconsRulesets
{
	public static void main(final String[] args)
	{
		String outputPath = args.length == 0 ?  "./" : args[0];
		
		System.out.println("\n=========================================\nTest: Start reconstruction all of rulesets:\n");

		final List<String> failedGames = new ArrayList<String>();

		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();
		CompleterWithPrepro completer = new CompleterWithPrepro();

		for (final String fileName : choices)
		{
			//if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/"))
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/board/hunt/Fortresse"))
				continue;
			
			final String gameName = fileName.substring(fileName.lastIndexOf("/")+1,fileName.length()-4);
			
			// Get game description from resource
			System.out.println("Game: " + gameName);

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
					desc += line + "\n";
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}

			// Parse and reconstruct one instance of a game which is respected the expected concepts.
			List<Completion> completions = null;
			try
			{
				completions = completer.completeSampled(desc, 10, null);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}

			if (completions != null)
			{
				for (int n = 0; n < completions.size(); n++) 
				{
					final Completion completion = completions.get(n);
					CompleterWithPrepro.saveCompletion(outputPath, gameName+n, completion);
					
					// Check if the concepts expected are present.
					//boolean expectedConcepts = Concept.isExpectedConcepts(completion.raw());
					//System.out.println("RECONS HAS THE EXPECTED CONCEPTS? " + expectedConcepts);
				}
			}
			else
			{
				failedGames.add(fileName);
				System.err.println("** FAILED TO COMPILE: " + fileName);
			}
		}
		
		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");

//		if (!failedGames.isEmpty())
//		{
//			System.out.println("\nUncompiled games:");
//			for (final String name : failedGames)
//				System.out.println(name);
//		}
	}


}
