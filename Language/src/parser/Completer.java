package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.grammar.Description;
import main.grammar.Report;

//-----------------------------------------------------------------------------

/**
 * Completes partial game descriptions ready for expansion.
 * @author cambolbro
 */
public class Completer
{
	
	//-------------------------------------------------------------------------

	public static boolean needsCompleting(final Description description)
	{
		final String rawGame = description.rawGameDescription();
		return rawGame.contains("[") && rawGame.contains("]");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param raw    Partial raw game description.
	 * @param report Report log for warnings and errors.
	 * @return List of completed (raw) game descriptions ready for expansion and parsing.        
	 */
	public static List<String> complete(final String raw, final Report report)
	{
		System.out.println("Completing description...");
		
		// Create list of alternative Descriptions, as each will need to be expanded
		final List<String> completions = new ArrayList<String>();
		
		// ...
		
		return completions;
	}

	//-------------------------------------------------------------------------
	// Ludeme loader code from Matthew
	
	/**
	 * @return Names and contents of all files within the lud/board path.
	 */
	public static Map<String, String> getAllLudContents()
	{
		return getAllDirectoryContents("../Common/res/lud/board/");
	}
	
	/**
	 * @return Names and contents of all files within the define path.
	 */
	public static Map<String, String> getAllDefContents()
	{
		return getAllDirectoryContents("../Common/res/def/");
	}
	
	/**
	 * @return Names and contents of all files within the specific directory path.
	 */
	public static Map<String, String> getAllDirectoryContents(final String dir)
	{
		final File startFolder = new File(dir);
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);
		final Map<String, String> fileContents = new HashMap<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					gameDirs.add(fileEntry);
				}
				else
				{
					try(BufferedReader br = new BufferedReader(new FileReader(fileEntry))) 
					{
					    final StringBuilder sb = new StringBuilder();
					    String line = br.readLine();

					    while (line != null) 
					    {
					        sb.append(line);
					        sb.append(System.lineSeparator());
					        line = br.readLine();
					    }
					    
					    final String everything = sb.toString();
					    fileContents.put(fileEntry.getName(), everything);
					} 
					catch (final FileNotFoundException e) 
					{
						e.printStackTrace();
					} 
					catch (final IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		return fileContents;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Print game options per .lud to file.
	 * @param fileName
	 * @throws IOException 
	 */
	public static void saveReconstruction
	(
		final String name, final String content
	) throws IOException
	{
		final String outFileName = "../Common/res/out/recons/" + name + ".lud";	
		
		// Prepare the output file
		final File file = new File(outFileName);
		if (!file.exists())
			file.createNewFile();

		try 
		(
			final PrintWriter writer = 
				new PrintWriter
				(
					new BufferedWriter(new FileWriter(outFileName, false))
				)
		)
		{
			writer.write(content);
		}
	}
	
	//-------------------------------------------------------------------------

}
