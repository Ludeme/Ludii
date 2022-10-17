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
import main.StringRoutines;
import main.grammar.Description;
import other.GameLoader;
import compiler.Compiler;

/**
 * To reconstruct rulesets.
 *
 * @author Eric.Piette
 */
public class ReconsRulesets
{
	public static void main(final String[] args)
	{
		String outputPath = args.length == 0 ?  "./res/recons/output/" : args[0];
		int numRecons = args.length < 1 ?  10 : Integer.parseInt(args[1]);
		
		System.out.println("\n=========================================\nTest: Start reconstruction all of rulesets:\n");

		final List<String> failedGames = new ArrayList<String>();

		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();
		CompleterWithPrepro completer = new CompleterWithPrepro();

		for (final String fileName : choices)
		{
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/"))
			//if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/board/hunt/Fortresse"))
			//if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/board/hunt/Bagh Bukree"))
			//if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/eric/recons/Bagh Bukree test"))
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
				completions = completer.completeSampled(desc, numRecons, null);
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
					final String completionRaw = indentNicely(StringRoutines.unformatOneLineDesc(completion.raw()));
					String doNotCompile = "";
					// Test if the completion compiles.
					try{Compiler.compileTest(new Description(completionRaw), false);}
					catch(final Exception e)
					{
//						System.out.println("Impossible to compile number "+ n);
//						System.out.println("DESC IS");
//						System.out.println(completionRaw);
//						e.printStackTrace();
						doNotCompile = "doNotCompile";
					}
					
					CompleterWithPrepro.saveCompletion(outputPath, gameName+n+doNotCompile, completionRaw);

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

	//-----------------------------------------------------------------------------
	
	/**
	 * Nicely indents the description in entry.
	 */
	public static String indentNicely(final String desc)
	{
		final String linesArray[] = desc.split("\\r?\\n");
		final List<String> lines = new ArrayList<String>();
        for (int n = 0; n < linesArray.length; n++)
        	lines.add(linesArray[n]);
		          
        // Left justify all lines
        for (int n = 0; n < lines.size(); n++)
        {
        	String str = lines.get(n);
        	final int c = 0;
        	while (c < str.length() && (str.charAt(c) == ' ' || str.charAt(c) == '\t'))
        		str = str.substring(1);
        	
        	lines.remove(n);
        	lines.add(n, str);
        }

        removeDoubleEmptyLines(lines);
        indentLines(lines);
        
        final StringBuffer outputDesc = new StringBuffer();
        for (final String result : lines)
        	outputDesc.append(result + "\n");
        
        return outputDesc.toString();
    }
	
	/**
	 * Removes double empty lines.
	 */
	final static void removeDoubleEmptyLines(final List<String> lines)
	{
        int n = 1;
        while (n < lines.size())
        {
        	if (lines.get(n).equals("") && lines.get(n-1).equals(""))
        		lines.remove(n);
        	else
        		n++;
        }
 	}
	
	/**
	 * Nicely indents the lines of a desc.
	 */
	final static void indentLines(final List<String> lines)
	{
		final String indentString = "    ";
        int indent = 0;
        for (int n = 0; n < lines.size(); n++)
        {
        	String str = lines.get(n);
        	
        	final int numOpen  = StringRoutines.numChar(str, '(');  // don't count curly braces!
        	final int numClose = StringRoutines.numChar(str, ')');
        	
        	final int difference = numOpen - numClose;
        	
        	if (difference < 0)
        	{
        		// Unindent from this line
        		indent += difference;
        		if (indent < 0)
        			indent = 0;
        	}
        	        	
        	for (int step = 0; step < indent; step++)
        		str = indentString + str; 
 
   	       	lines.remove(n);
        	lines.add(n, str);
        	
        	if (difference > 0)
        		indent += difference;  // indent from next line
        }
	}


}
