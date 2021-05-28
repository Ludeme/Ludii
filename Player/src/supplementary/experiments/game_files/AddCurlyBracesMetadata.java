package supplementary.experiments.game_files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import main.FileHandling;
import main.StringRoutines;

/**
 * A small(ish) script to add curly braces to metadata
 *
 * @author Dennis Soemers
 */
public class AddCurlyBracesMetadata
{
	/**
	 * Constructor
	 */
	private AddCurlyBracesMetadata()
	{
		// Should not construct
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to initialise the metadata
	 * 
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// Collect all .lud paths
		final String[] gameNames = FileHandling.listGames();

		// Loop through all our game files
		for (String gameName : gameNames)
		{
			gameName = gameName.replaceAll(Pattern.quote("\\"), "/");
			
			if (gameName.startsWith("/lud/wip/"))
				continue;

//			if (gameName.startsWith("/lud/test/"))
//				continue;
				
			System.out.println("Processing: " + gameName + "...");
			
			// Read full original contents of file
			final File ludFile = new File("../Common/res" + gameName);
			final List<String> originalLines = new ArrayList<String>();
			
			try (final BufferedReader br = new BufferedReader(new FileReader(ludFile)))
			{
				for (String line; (line = br.readLine()) != null; /**/)
				{
					originalLines.add(line);
				}
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			
			// Now we write the same file again, but with extra curly braces
			if (!originalLines.isEmpty())
			{
				try (final PrintWriter writer = new PrintWriter(new FileWriter(ludFile)))
				{
					int metadataStartIdx = -1;
					
					// First all the original non-metadata contents
					for (int i = 0; i < originalLines.size(); ++i)
					{
						final String line = originalLines.get(i);
						
						if (line.contains("(metadata"))		// metadata starts here
						{
							metadataStartIdx = i;
							break;
						}
						
						writer.println(line);
					}
							
					int currLineIdx = metadataStartIdx;
					if (metadataStartIdx != -1)
					{
						// Now we write new metadata with curly braces
						boolean foundClosingBracket = false;
						String fullMetadataString = "";
						String newMetadataString = "";
						
						while (!foundClosingBracket)
						{
							fullMetadataString += originalLines.get(currLineIdx++) + "\n";
							final int openingBracketIdx = fullMetadataString.indexOf("(");
							final int closingBracketIdx = StringRoutines.matchingBracketAt(fullMetadataString, openingBracketIdx);
							
							if (closingBracketIdx >= 0)
							{
								foundClosingBracket = true;
								newMetadataString = fullMetadataString.substring(0, openingBracketIdx) + "(metadata {\n";
								newMetadataString += fullMetadataString.substring(
										openingBracketIdx + 1 + "(metadata".length(), closingBracketIdx);
								newMetadataString += "} )";
								newMetadataString += fullMetadataString.substring(closingBracketIdx + 1);
							}
						}
						
						writer.print(newMetadataString);
						
						// And now any options strings that may have been after metadata
						boolean resumePrinting = false;
						for (int i = currLineIdx; i < originalLines.size(); ++i)
						{
							final String line = originalLines.get(i);
							
							if (resumePrinting)
							{
								writer.println(line);
							}
							else if (line.trim().startsWith("(option"))
							{
								resumePrinting = true;
								writer.println();
								writer.println(line);
							}
						}
					}
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
