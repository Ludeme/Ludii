package graphics.svg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import main.FileHandling;

/**
 * Utility class for svg-loading.
 * @author Matthew.Stephenson
 */
public final class SVGLoader
{
	private static String[] choices = null;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	private SVGLoader()
	{
		// should not instantiate
	}

	//-------------------------------------------------------------------------

	/**
	 * @param filePath
	 * @return Whether this file contains a game description (not tested).
	 */
	public static boolean containsSVG(final String filePath)
	{
		final File file = new File(filePath);
		if (file != null)
		{
			InputStream in = null;
			
			String path = file.getPath().replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/svg/"));
			final URL url = 
					SVGLoader.class.getResource
					(
							path
					);
			try
			{
				in = new FileInputStream(new File(url.toURI()));
			}
			catch (final FileNotFoundException | URISyntaxException e)
			{
				e.printStackTrace();
			}

			try (BufferedReader rdr = new BufferedReader(new InputStreamReader(in)))
			{
				String line;
				while ((line = rdr.readLine()) != null)
					if (line.contains("svg"))
						return true;
			}
			catch (final Exception e)
			{
				System.out.println("GameLoader.containsGame(): Failed to load " + filePath + ".");
				System.out.println(e);
			}
		}
		return false;
	}

	//-------------------------------------------------------------------------
	
	public static String[] listSVGs()
	{
		// Try loading from JAR file
        if (choices == null)
        {
        	choices = FileHandling.getResourceListing(SVGLoader.class, "svg/", ".svg");
	    	 if (choices == null)
	         {
	        	try 
	        	{
	        		// Try loading from memory in IDE
	        		// Start with known .svg file
					final URL url = SVGLoader.class.getResource("/svg/misc/dot.svg");
	        		String path = new File(url.toURI()).getPath();
					path = path.substring(0, path.length() - "misc/dot.svg".length());
	
	        		// Get the list of .svg files in this directory and subdirectories
	        		final List<String> names = new ArrayList<>();
	        		visit(path, names);
	
	        		Collections.sort(names);
	        		choices = names.toArray(new String[names.size()]);
	        	}
	        	catch (final URISyntaxException exception)
	        	{
	        		exception.printStackTrace();
	        	}
	         }
        }
        return choices;
	}
	
	static void visit(final String path, final List<String> names)
	{
        final File root = new File( path );
        final File[] list = root.listFiles();

        if (list == null) 
        	return;

        for (final File file : list)
        {
        	if (file.isDirectory())
            {
            	visit(path + file.getName() + File.separator, names);
            }
            else
            {
				if (file.getName().contains(".svg"))
				{
					// Add this game name to the list of choices
					final String name = new String(file.getName());

					if (containsSVG(path + File.separator + file.getName()))
						names.add(path.substring(path.indexOf(File.separator + "svg" + File.separator)) + name);
				}
            }
        }
    }

}
