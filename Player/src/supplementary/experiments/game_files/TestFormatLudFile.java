package supplementary.experiments.game_files;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import main.FileHandling;
import main.grammar.Report;

/**
 * Script to write a formatted version of a .lud file's contents to
 * "Ludii/Player/FormattedLud.lud"   (this file is ignored by git!)
 *
 * @author Dennis Soemers
 */
public class TestFormatLudFile
{
	/** Path of the .lud file we want to test formatting for */
	private static final String LUD_PATH = "/lud/board/space/blocking/Mu Torere.lud";
	
	/** Path we write to */
	private static final String WRITE_PATH = "FormattedLud.lud";
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private TestFormatLudFile()
	{
		// Should not construct
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(final String[] args) throws FileNotFoundException, IOException
	{
		final String fileContents = FileHandling.loadTextContentsFromFile("../Common/res" + LUD_PATH);
		final String formatted = new main.grammar.Token(fileContents, new Report()).toString();
		
		try (final PrintWriter writer = new PrintWriter(new FileWriter(WRITE_PATH)))
		{
			writer.print(formatted);
		}
	}
	
	//-------------------------------------------------------------------------

}
