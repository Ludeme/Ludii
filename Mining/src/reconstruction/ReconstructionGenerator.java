package reconstruction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import compiler.Compiler;
import completer.Completion;
import game.Game;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.grammar.Description;
import other.GameLoader;
import other.concept.Concept;
import reconstruction.completer.CompleterWithPrepro;
import reconstruction.utils.FormatReconstructionOutputs;

/**
 * Reconstruction Generator.
 *
 * @author Eric.Piette
 */
public class ReconstructionGenerator
{
	final static String defaultOutputPath        = "./res/recons/output/";
	final static int    defaultNumReconsExpected = 10;
	final static int    defaultNumAttempts       = 10000;
	final static String defaultReconsPath        = "/lud/reconstruction/board/war/replacement/checkmate/chaturanga/Samantsy";
	//final static String defaultReconsPath = "/lud/reconstruction/";
	//final static String defaultReconsPath = "/lud/reconstruction/board/hunt/Fortresse";
	//final static String defaultReconsPath = "/lud/reconstruction/board/space/line/Ashanti Alignment Game";
	//final static String defaultReconsPath = "/lud/reconstruction/board/war/other/Macheng";
	//final static String defaultReconsPath = "/lud/reconstruction/board/hunt/Bagh Bukree";
	
	final static double defaultConceptualWeight = 0.5;
	final static double defaultHistoricalWeight = 0.5;
	
	/**
	 * Main method to call the reconstruction with command lines.
	 * @param args
	 */
	public static void main(final String[] args)
	{
		String outputPath = args.length == 0 ?                      defaultOutputPath : args[0];
		int numReconsNoWarningExpectedConcepts = args.length < 1 ?  defaultNumReconsExpected : Integer.parseInt(args[1]);
		int maxNumberAttempts = args.length < 2 ?                   defaultNumAttempts : Integer.parseInt(args[2]);
		double conceptualWeight = args.length < 3 ?                 defaultConceptualWeight : Double.parseDouble(args[3]);
		double historicalWeight = args.length < 4 ?                 defaultHistoricalWeight : Double.parseDouble(args[4]);
		String reconsPath = args.length < 5 ?                       defaultReconsPath : args[5];
	
		reconstruction(outputPath, numReconsNoWarningExpectedConcepts, maxNumberAttempts, conceptualWeight, historicalWeight, reconsPath);
	}
	
	
	/**
	 * @param outputPath         The path of the folder to place the reconstructions.
	 * @param numReconsExpected  The number of reconstruction expected to generate.
	 * @param maxNumberAttempts  The number of attempts.
	 * @param conceptualWeight   The weight of the expected concepts.
	 * @param historicalWeight   The weight of the historical similarity.
	 * @param reconsPath         The path of the file to recons.
	 */
	public static void reconstruction
	(
		String outputPath, 
		int    numReconsExpected, 
		int    maxNumberAttempts,
		double conceptualWeight,
		double historicalWeight,
		String reconsPath
	)
	{
		System.out.println("\n=========================================\nStart reconstruction:\n");
		System.out.println("Output Path = " + outputPath);
		System.out.println("Historical Weight = " + historicalWeight + " Conceptual Weight = " + conceptualWeight);
		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();
		CompleterWithPrepro completer = new CompleterWithPrepro();
		for (final String fileName : choices)
		{
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains(reconsPath))
				continue;
			
			final String gameName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length() - 4);
			
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
			
			// Extract the metadata.
			final String metadata = desc.contains("(metadata") ? desc.substring(desc.indexOf("(metadata")) : "";
			
			// Extract the id of the reconstruction.
			String idStr = metadata.contains("(id") ? metadata.substring(metadata.indexOf("(id") + 5) : "";
			idStr = idStr.substring(0, idStr.indexOf(')') - 1);
			final int idRulesetToRecons = Integer.valueOf(idStr).intValue();

			// To check the expected concepts detected.
//			final List<Concept> expectedConcepts = ComputeCommonExpectedConcepts.computeCommonExpectedConcepts(desc);
//			for(Concept c: expectedConcepts)
//				System.out.println(c.name());
			
			int numAttempts = 0;
			List<Completion> correctCompletions = new ArrayList<Completion>();
			
			// Run the recons process until enough attempts are executed or all reconstructions are generated.
			while(numAttempts < maxNumberAttempts && correctCompletions.size() < numReconsExpected)
			{
				List<Completion> completions = null;
				
				// Run the completer.
				try
				{
					completions = completer.completeSampled(desc, 1, idRulesetToRecons, null);
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}

				// Check the completions.
				if (completions != null)
				{
					for (int n = 0; n < completions.size(); n++) 
					{
						final Completion completion = completions.get(n);
						final String completionRaw = FormatReconstructionOutputs.indentNicely(StringRoutines.unformatOneLineDesc(completion.raw()));
						
						// Test if the completion compiles.
						Game game = null;
						//System.out.println(completionRaw);
						try{game = (Game) Compiler.compileTest(new Description(completionRaw), false);}
						catch(final Exception e)
						{
//							System.out.println("Impossible to compile);
//							System.out.println("DESC IS");
//							System.out.println(completionRaw);
//							e.printStackTrace();
						}
						
						// It compiles.
						if(game != null)
						{
							final String rawDescMetadata = completionRaw + "\n" + metadata;
							completions.get(n).setRaw(rawDescMetadata);
							System.out.print("One Completion found");
							
							// Check if no warning and if no potential crash.
							if(!game.hasMissingRequirement() && !game.willCrash())
							{
								System.out.print( " with no warning");
								
								// Check if the concepts expected are present.
								boolean expectedConcepts = Concept.isExpectedConcepts(rawDescMetadata);
								if(expectedConcepts)
								{
									// All good, add to the list of correct completions.
									correctCompletions.add(completions.get(n));
									System.out.print( " and with the expected concepts");
								}
							}
							System.out.println();
						}
					}
				}
				numAttempts++;
			}

			// We rank the completions.
			Collections.sort(correctCompletions, (c1, c2) -> c1.score() < c2.score() ? 1 : c1.score() == c2.score() ? 0 : -1);
			
			for (int n = 0; n < correctCompletions.size(); n++) 
			{
				System.out.println("Completion " + n + " has a score of " + correctCompletions.get(n).score() + " similarity Score = " + correctCompletions.get(n).similarityScore() + " true concepts score = " + correctCompletions.get(n).commonExpectedConceptsScore() + " IDS used = " + correctCompletions.get(n).idsUsed());
				CompleterWithPrepro.saveCompletion(outputPath + gameName + "/", gameName + n, correctCompletions.get(n).raw());
			}

			System.out.println("Num Attempts = " + numAttempts);
			System.out.println(correctCompletions.size() + " recons generated");

			final String outputReconsData = outputPath + gameName + ".csv";
			try (final PrintWriter writer = new UnixPrintWriter(new File(outputReconsData), "UTF-8"))
			{
				for (int n = 0; n < correctCompletions.size(); n++) 
				{
					final List<String> lineToWrite = new ArrayList<String>();
					lineToWrite.add(gameName + n);
					lineToWrite.add(idRulesetToRecons+"");
					lineToWrite.add(correctCompletions.get(n).score() +"");
					lineToWrite.add(correctCompletions.get(n).similarityScore() +"");
					lineToWrite.add(correctCompletions.get(n).commonExpectedConceptsScore() +"");
					lineToWrite.add(correctCompletions.get(n).idsUsed() +"");
					writer.println(StringRoutines.join(",", lineToWrite));
				}
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");
	}
}
