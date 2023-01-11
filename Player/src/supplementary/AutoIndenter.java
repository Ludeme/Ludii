package supplementary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import main.StringRoutines;

/**
 * Convenience class to run auto indenter for .luds and .defs.
 * @author cambolbro
 */
public final class AutoIndenter
{
	private static final String indentString = "    ";

	//-------------------------------------------------------------------------

	/**
	 * Call this to automatically indent all .lud and .def files.
	 */
	public static void main(final String[] args)
	{
		indentFilesNicely();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Nicely indents all .lud and .def files.
	 * Destructively modifies files, but should be safe to use any number of times.
	 */
	public static void indentFilesNicely()
	{
		// 1. Indent all .lud files nicely
		indentFilesNicelyFrom("../Common/res/lud");
		
		// 2. Indent all .def files nicely
		indentFilesNicelyFrom("../Common/res/def");

		// 3. Indent all AI metadata .def files nicely
		indentFilesNicelyFrom("../Common/res/def_ai");
	}

	/**
	 * Nicely indents all files from the specified folder and below. 
	 */
	public static void indentFilesNicelyFrom(final String folderPath)
	{
		final List<File> files = new ArrayList<File>();
		final List<File> dirs  = new ArrayList<File>();
		
		final File folder = new File(folderPath);
		dirs.add(folder);

		for (int i = 0; i < dirs.size(); ++i)
		{
			final File dir = dirs.get(i);
			for (final File file : dir.listFiles())
			{
				if (file.isDirectory())
					dirs.add(file);
				else
					files.add(file);
			}
		}

		for (final File file : files)
		{
			final String absolutePath = file.getAbsolutePath();
			if (absolutePath.contains("/test/dennis/") || absolutePath.contains("\\test\\dennis\\"))
				continue;
			
			indentFileNicely(absolutePath);
		}

		System.out.println(files.size() + " files found from "+ folderPath + ".");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Nicely indents the specified .lud or .def file.
	 */
	public static void indentFileNicely(final String path)
	{
//		if (!path.contains("/Hex.lud"))
//			return;
			
		System.out.println("Indenting " + path + " nicely...");
				
		final File fileToBeModified = new File(path);
		final List<String> lines = new ArrayList<String>();
		          
        // Read lines in
        try (final BufferedReader reader = new BufferedReader(
        		new InputStreamReader(new FileInputStream(fileToBeModified), StandardCharsets.UTF_8)))
        {
            String line = reader.readLine();
            while (line != null) 
            {
                lines.add(new String(line));
                line = reader.readLine();
            }
        }
        catch (final IOException e) { e.printStackTrace(); }
         
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

        //moveDefinesToTop(lines);
        //insertSeparators(lines);
        //extractAIMetadataToDefine(lines);
        
        removeDoubleEmptyLines(lines);
        indentLines(lines);
        
        try (final BufferedWriter writer = new BufferedWriter(
        		new OutputStreamWriter(new FileOutputStream(fileToBeModified), StandardCharsets.UTF_8)))
        {
        	for (final String result : lines)
            	writer.write(result + "\n");
        }
        catch (final IOException e) { e.printStackTrace(); }
    }
	
	//-------------------------------------------------------------------------

	/**
	 * Moves any (define ...) clause to the top of the list.
	 */
	final static void moveDefinesToTop(final List<String> lines)
	{
       final List<String> defines = new ArrayList<String>();
 
        int brackets = 0;
        boolean isDefine = false;
        
        int n = 0;
        while (n < lines.size())
        {
        	final String str = lines.get(n);
        	
        	final int numOpen  = StringRoutines.numChar(str, '(');  // don't count curly braces!
        	final int numClose = StringRoutines.numChar(str, ')');
        	final int difference = numOpen - numClose;
        	
        	final boolean lineHasDefineString = str.contains("(define ");
        	
        	if (lineHasDefineString)
        	{
        		isDefine = true;
        		brackets = 0;
        	}
  
   	      	if (isDefine)
        	{
   	      		// Remove this line and store it
   	      		if (lineHasDefineString && defines.size() > 0)
					defines.add("");  // add an empty line above each define clause

   	      		defines.add(new String(str));
   	       		lines.remove(n);
        	}
   	      	else
   	      	{
   	      		// Move to next line
   	      		n++;
   	      	}
  
   	      	if (lineHasDefineString && difference == 0)
   	      	{
   	      		// Define occurs on single line
   	      		isDefine = false;
   	      		brackets = 0;
   	      	}
   	      	
        	if (difference < 0)
        	{
        		// Unindent from this line
        		brackets += difference;
        	
        		if (brackets < 0)
        			brackets = 0;
        		
        		if (isDefine && brackets == 0)
        		{
        			isDefine = false;
         		}
        	}
        	else if (difference > 0)
        	{
        		brackets += difference;  // indent from next line
        	}
         }
        
        // Prepend defines at start of file
        for (int d = defines.size()-1; d >= 0; d--)
        	lines.add(0, defines.get(d));
	}    
     
	//-------------------------------------------------------------------------

	/**
	 * Extracts the first (ai ...) clause and moves it to a .def file in res/def_ai.
	 */
	final static void extractAIMetadataToDefine(final List<String> lines)
	{
       final List<String> define = new ArrayList<String>();
 
        int brackets = 0;
        boolean isAI = false;
        
        String gameName = "";
        for (final String line : lines)
        	if (line.contains("(game "))
        	{
        		gameName = StringRoutines.gameName(line);
        		break;
        	}
        
        int n = 0;
        while (n < lines.size())
        {
        	final String str = lines.get(n);
        	
        	final int numOpen  = StringRoutines.numChar(str, '(');  // don't count curly braces!
        	final int numClose = StringRoutines.numChar(str, ')');
        	final int difference = numOpen - numClose;
        	
        	final boolean lineHasAIString = str.contains("(ai ");
         	
        	if (lineHasAIString)
        	{
        		isAI = true;
        		brackets = 1;
        		define.add("(define \"" + gameName + "_ai\"");  // open the define
        		lines.add(n+1, "\"" + gameName + "_ai\"");  // add define entry point in .lud
        		n += 2;
        		continue;
        	}
        	
        	if (difference < 0)
        	{
        		// Unindent from this line
        		brackets += difference;
        	
        		if (brackets < 0)
        			brackets = 0;
        		
        		if (isAI && brackets == 0)
        		{
        			define.add(")");  // close define
        			isAI = false;
         		}
        	}
        	else if (difference > 0)
        	{
        		brackets += difference;  // indent from next line
        	}
        	
   	      	if (isAI)
        	{
   	      		// Add this line to the .def and remove it from the .lud
   	      		define.add(new String(str));
   	       		lines.remove(n);
        	}
   	      	else
   	      	{
   	      		// Move to next line
   	      		n++;
   	      	}

         }
        
        // Save define to new file in res/def_ai
        final String outFilePath = "../Common/res/def_ai/" + gameName + "_ai.def";
        try (final FileWriter writer = new FileWriter(outFilePath))
        {
        	for (final String result : define)
            	writer.write(result + "\n");
        }
        catch (final IOException e) { e.printStackTrace(); }
	}    
     
	//-------------------------------------------------------------------------

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
    
	//-------------------------------------------------------------------------

	/**
	 * Inserts separators above key sections.
	 */
	final static void insertSeparators(final List<String> lines)
	{
		boolean optionFound  = false;
		boolean rulesetsFound = false;
		
        for (int n = 2; n < lines.size(); n++)  // skip first couple of lines
        {
        	final String str = lines.get(n);
        	if 
        	(
        		str.contains("(game ") 
        		|| 
        		str.contains("(metadata ") 
        		|| 
        		str.contains("(option ") && !optionFound
        		|| 
        		str.contains("(rulesets ") && !rulesetsFound
        	)
        	{
        		lines.add(n, "");
        		lines.add(n, "//------------------------------------------------------------------------------");
        		lines.add(n, "");
        		
        		if (str.contains("(option "))
        			optionFound = true;

        		if (str.contains("(rulesets "))
        			rulesetsFound = true;
        		
        		n += 3;
        	}
        }
 	}    
    
	//-------------------------------------------------------------------------

	/**
	 * Nicely indents the specified lines of a .lud or .def file.
	 */
	final static void indentLines(final List<String> lines)
	{
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
	
	//-------------------------------------------------------------------------
	
}
