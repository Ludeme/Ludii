package completer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;

public class CompleterTest {
    
    @Test
	public void testLoadLuds()
	{
		final Map<String, String> luds = Completer.getAllLudContents();
		final Map<String, String> defs = Completer.getAllDefContents();
		System.out.println(luds.size() + " luds loaded, " + defs.size() + " defs loaded.");
	}

    @Test
	public void testCompletion() throws IOException
	{
		testCompletion("TestReconOneClause.lud");
		testCompletion("TestReconTwoClauses.lud");
		testCompletion("TestReconNested.lud");
		testCompletion("TestReconRange.lud");
		testCompletion("TestReconRanges.lud");
		testCompletion("TestReconRangeSite.lud");
		testCompletion("TestReconInclude.lud");
		testCompletion("TestReconExclude.lud");
		testCompletion("TestReconEnumeration1.lud");
		testCompletion("TestReconEnumeration2.lud");
	}
    
	private static void testCompletion(final String fileName) throws IOException
	{
		//final String fileName = "TestReconA.lud";
		final String filePath = "../Common/res/lud/test/recon/" + fileName;
		
		System.out.println("\n####################################################");
		System.out.println("\nTesting completion of " + filePath);
		
		String str = loadTextContentsFromFile(filePath);
        System.out.println("desc:\n" + str);
        System.out.println("File needs completing: " + Completer.needsCompleting(str));

        //final Report report = new Report();
        final List<Completion> completions = Completer.complete(str, null);
        for (int n = 0; n < completions.size(); n++) 
        {
            final Completion completion = completions.get(n);
            // Don't add ".lud" suffix, that is added by completer
            final int suffixAt = fileName.indexOf(".lud");
            final String outFileName = fileName.substring(0, suffixAt) + "-" + n; 			
            Completer.saveReconstruction(outFileName, completion);
        }
	}

	private static String loadTextContentsFromFile(final String filePath) throws FileNotFoundException, IOException
	{
		// Load the string from file
		try 
		(
			final InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8); 
			final BufferedReader bufferedReader = new BufferedReader(isr)
		)
		{
            return bufferedReader.lines().collect(Collectors.joining("\n"));
		}
	}
}
