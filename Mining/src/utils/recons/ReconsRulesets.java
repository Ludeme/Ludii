package utils.recons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import compiler.Compiler;
import completer.Completion;
import game.Game;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Description;
import other.GameLoader;
import other.concept.Concept;

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
		int numReconsNoWarning = args.length < 2 ?  1 : Integer.parseInt(args[2]);
		int numReconsNoWarningExpectedConcepts = args.length < 3 ?  1 : Integer.parseInt(args[3]);
		int maxNumberAttempts = args.length < 4 ?  10000 : Integer.parseInt(args[4]);
		
		System.out.println("\n=========================================\nTest: Start reconstruction all of rulesets:\n");

		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();
		CompleterWithPrepro completer = new CompleterWithPrepro();

		for (final String fileName : choices)
		{
			//if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/"))
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/board/hunt/Fortresse"))
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
			
			final String metadata = desc.contains("metadata") ? desc.substring(desc.indexOf("(metadata")) : "";

			int numAttempts = 0;
			List<String> compilingCompletions = new ArrayList<String>();
			List<String> compilingNoWarningCompletions = new ArrayList<String>();
			List<String> compilingNoWarningExpectedConceptsCompletions = new ArrayList<String>();
			
			// Run the recons process until enough attempts is executed or reconstruction are generated.
			while(numAttempts < maxNumberAttempts && 
					(compilingCompletions.size() < numRecons ||
							compilingNoWarningCompletions.size() < numReconsNoWarning ||
							compilingNoWarningExpectedConceptsCompletions.size() < numReconsNoWarningExpectedConcepts)
			)
			{
				List<Completion> completions = null;
				try
				{
					completions = completer.completeSampled(desc, 1, null);
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
						// Test if the completion compiles.
						Game game = null;
						try{game = (Game) Compiler.compileTest(new Description(completionRaw), false);}
						catch(final Exception e)
						{
//							System.out.println("Impossible to compile number "+ n);
//							System.out.println("DESC IS");
//							System.out.println(completionRaw);
//							e.printStackTrace();
						}
						if(game != null)
						{
							final String rawDescMetadata = completionRaw + "\n" + metadata;
							compilingCompletions.add(rawDescMetadata);
							if(!game.hasMissingRequirement() && !game.willCrash())
								compilingNoWarningCompletions.add(rawDescMetadata);
							game = (Game) Compiler.compileTest(new Description(rawDescMetadata), false);
							
							// Check if the concepts expected are present.
							boolean expectedConcepts = Concept.isExpectedConcepts(rawDescMetadata);
							if(expectedConcepts)
								compilingNoWarningExpectedConceptsCompletions.add(rawDescMetadata);
						}
					}
				}
				numAttempts++;
			}

			for (int n = 0; n < compilingCompletions.size(); n++) 
			{
				if(compilingNoWarningExpectedConceptsCompletions.contains(compilingCompletions.get(n)))
					CompleterWithPrepro.saveCompletion(outputPath + gameName + "/" + "noWarning/"+ "expectedConcepts/", gameName+n, compilingCompletions.get(n));
				else if(compilingNoWarningCompletions.contains(compilingCompletions.get(n)))
					CompleterWithPrepro.saveCompletion(outputPath + gameName + "/" + "noWarning/", gameName+n, compilingCompletions.get(n));
				else
					CompleterWithPrepro.saveCompletion(outputPath + gameName + "/", gameName+n, compilingCompletions.get(n));
			}

			System.out.println("Num Attempts = " + numAttempts);
			
			System.out.println(compilingCompletions.size() + " recons generated, " + compilingNoWarningCompletions.size() + " recons without warning generated.");
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
