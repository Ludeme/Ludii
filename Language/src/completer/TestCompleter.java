package completer;

//import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

//import org.junit.Test;

//import main.grammar.Report;
//import parser.Completer;

/**
 * Test various reconstruction routines.
 * @author cambolbro
 */
public class TestCompleter
{

	//-------------------------------------------------------------------------

//	@Test
//	public void test()
//	{
//		testSaving();
//		testLoadLuds();
//		testCompletion();		
//	}

	//-------------------------------------------------------------------------
	
	/**
	 * From FileHandling.
	 * @param  filePath
	 * @return Text contents from file.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static String loadTextContentsFromFile(final String filePath) throws FileNotFoundException, IOException
	{
		// Load the string from file
		final StringBuilder sb = new StringBuilder();
		String line = null;
		try 
		(
			final InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8); 
			final BufferedReader bufferedReader = new BufferedReader(isr)
		)
		{
			while ((line = bufferedReader.readLine()) != null)
				sb.append(line + "\n");
		}
		return sb.toString();
	}

	//-------------------------------------------------------------------------

//	void testSaving()
//	{
//		try
//		{
//			Completer.saveReconstruction("Test", "(test ...)");
//		} 
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
		
	//-------------------------------------------------------------------------

	static void testLoadLuds()
	{
		final Map<String, String> luds = Completer.getAllLudContents();
		final Map<String, String> defs = Completer.getAllDefContents();
		
		System.out.println(luds.size() + " luds loaded, " + defs.size() + " defs loaded.");
	}
	
	//-------------------------------------------------------------------------

	static void testCompletion()
	{
		testCompletion(null, "TestReconOneClause.lud");
		testCompletion(null, "TestReconTwoClauses.lud");
		testCompletion(null, "TestReconNested.lud");
		testCompletion(null, "TestReconRange.lud");
		testCompletion(null, "TestReconRanges.lud");
		testCompletion(null, "TestReconRangeSite.lud");
		testCompletion(null, "TestReconInclude.lud");
		testCompletion(null, "TestReconExclude.lud");
		testCompletion(null, "TestReconEnumeration1.lud");
		testCompletion(null, "TestReconEnumeration2.lud");
	}
	
	/**
	 * @param outFilePath Path to save output file (will use default /Common/res/out/recons/ if null).
	 * @param fileName    File name.
	 */
	static void testCompletion(final String outFilePath, final String fileName)
	{
		//final String fileName = "TestReconA.lud";
		final String filePath = "../Common/res/lud/test/recon/" + fileName;
		
		System.out.println("\n####################################################");
		System.out.println("\nTesting completion of " + filePath);
		
		String str = "";
		try
		{
			str = loadTextContentsFromFile(filePath);
			System.out.println("desc:\n" + str);
			System.out.println("File needs completing: " + Completer.needsCompleting(str));
			
			//final Report report = new Report();
			//final List<Completion> completions = Completer.completeExhaustive(str, 3, null);   // save all completions
			final List<Completion> completions = Completer.completeSampled(str, 3, null);  // only save first completion for each file
			for (int n = 0; n < completions.size(); n++) 
			{
				final Completion completion = completions.get(n);
				
				// Don't add ".lud" suffix, that is added by completer
				final int suffixAt = fileName.indexOf(".lud");
				final String outFileName = fileName.substring(0, suffixAt) + "-" + n; 			
				try
				{
					Completer.saveCompletion(outFilePath, outFileName, completion);
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}	
			}
		}
		catch (final FileNotFoundException ex)
		{
			System.out.println("Unable to open file '" + fileName + "'");
		}
		catch (final IOException ex)
		{
			System.out.println("Error reading file '" + fileName + "'");
		}
		
		//final Map<String, String> luds = Completer.getAllLudContents();
		//final Map<String, String> defs = Completer.getAllDefContents();
		
		//System.out.println(luds.size() + " luds loaded, " + defs.size() + " defs loaded.");
	}
	
	//-------------------------------------------------------------------------

	public static void main(String[] args)
	{
		TestCompleter.testLoadLuds();
		TestCompleter.testCompletion();
	}
	
}
