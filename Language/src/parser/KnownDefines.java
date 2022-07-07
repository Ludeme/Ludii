package parser; 

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import main.FileHandling;
import main.grammar.Report;

//-----------------------------------------------------------------------------

/**
 * Record of known defines from Common/res/def and below.
 * 
 * @author cambolbro
 */
public class KnownDefines
{	
  	// Map of known defines from bin/def/ and below
  	private final Map<String, Define> knownDefines = new HashMap<String, Define>();
  	
	//-------------------------------------------------------------------------

  	/**
  	 * Singleton class for known defines.
  	 * @author cambolbro
  	 */
	private static class KnownDefinesProvider 
	{
		public static final KnownDefines KNOWN_DEFINES = new KnownDefines();
	}

	//-------------------------------------------------------------------------

	/**
	 * Private constructor: access class as singleton through getKnownDefines().
	 */
	KnownDefines()
	{
		final Report report = new Report();
		loadKnownDefines(report);
		if (report.isError())
			System.out.println(report);
	}

	//-------------------------------------------------------------------------

	/**
	 * Access point for getting known defines.
	 * @return Map of known defines.
	 */
	public static KnownDefines getKnownDefines() 
	{
		return KnownDefinesProvider.KNOWN_DEFINES;
	}
	
	/**
	 * @return The known defines.
	 */
	public Map<String, Define> knownDefines()
	{
		return knownDefines;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load known defines from file.
	 */
	void loadKnownDefines(final Report report)
	{
		knownDefines.clear();
		
//       System.out.println("File.separator is '" + File.separator + "'.");

		// Try loading from JAR file
		final String[] defs = FileHandling.getResourceListing(KnownDefines.class, "def/", ".def");
		
        if (defs == null)
        {
        	// Not a JAR
        	try 
        	{ 		
        		// Start with known .def file
				final URL url = KnownDefines.class.getResource("def/rules/play/moves/StepToEmpty.def");
        		String path = new File(url.toURI()).getPath();
				path = path.substring(0, path.length() - "rules/play/moves/StepToEmpty.def".length());

        		// Get the list of .def files in this directory and subdirectories
        		try
				{
					recurseKnownDefines(path, report);
					if (report.isError())
						return;
				} 
        		catch (final Exception e)
				{
					e.printStackTrace();
				}
         	}
        	catch (final URISyntaxException exception)
        	{
        		exception.printStackTrace();
        	}
        }
        else
        {
        	// JAR file
        	for (final String def : defs)
        	{
        		final Define define = processDefFile(def.replaceAll(Pattern.quote("\\"), "/"), "/def/", report);
        		if (report.isError())
        			return;
        		
				knownDefines.put(define.tag(), define);
        	}
        }
        
//        System.out.println(knownDefines.size() + " known defines loaded:");
//        for (final Define define : knownDefines.values())
//        	System.out.println("+ " + define.tag());
 	}

	void recurseKnownDefines
	(
		final String path,
		final Report report
	) throws Exception
	{
        final File root = new File(path);
        final File[] list = root.listFiles();
        
        if (list == null) 
        	return;

        for (final File file : list)
        {
            if (file.isDirectory())
            {
            	// Recurse to subdirectory
            	recurseKnownDefines(path + file.getName() + File.separator, report);
            	if (report.isError())
            		return;
            }
            else
            {
				if (!file.getName().contains(".def"))
					continue;  // not a define file
				
				final String filePath = path + file.getName();
				final Define define = processDefFile(filePath.replaceAll(Pattern.quote("\\"), "/"), "/def/", report);
				if (report.isError())
					return;
				
				knownDefines.put(define.tag(), define);
            }
        }
    }

	//-------------------------------------------------------------------------

	/**
	 * Processes a potential define file.
	 * 
	 * @param defRoot
	 * @param defFilePath
	 * @param report
	 * 
	 * @return The define.
	 */
	public static Define processDefFile
	(
		final String defFilePath, 
		final String defRoot, 
		final Report report
	)
	{
		final InputStream in = KnownDefines.class.getResourceAsStream(defFilePath.substring(defFilePath.indexOf(defRoot)));
		
		// Extract the definition text from file 
		final StringBuilder sb = new StringBuilder();
		try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8")))
		{
			String line;
			while ((line = rdr.readLine()) != null)
				sb.append(line + "\n");
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		final Define define = Expander.interpretDefine(sb.toString(), null, report, true);
		return define;
	}
		
	//-------------------------------------------------------------------------
			
}
