package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Common file handling routines.
 * @author cambolbro and Dennis Soemers and Matthew.Stephenson
 */
public class FileHandling
{
	
	//-------------------------------------------------------------------------
	
	/** Only need to compute this once, afterwards we can just return it immediately */
	private static String[] gamesList = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return List of all the games that we can automatically find (i.e. the built-in games).
	 */
	public static String[] listGames()
	{
		if (gamesList == null)
		{
			// Try loading from JAR file
	        String[] choices = FileHandling.getResourceListing(FileHandling.class, "lud/", ".lud");

    		final List<String> names = new ArrayList<>();
	        if (choices == null)
	        {
	        	try 
	        	{
	        		// Try loading from memory in IDE
	        		// Start with known .lud file
					final URL url = FileHandling.class.getResource("/lud/board/space/line/Tic-Tac-Toe.lud");
	        		String path = new File(url.toURI()).getPath();
					path = path.substring(0, path.length() - "board/space/line/Tic-Tac-Toe.lud".length());
	
					//System.out.println(path);
	        		// Get the list of .lud files in this directory and subdirectories
	        		visit(path, names);
		    		Collections.sort(names);
		    		choices = names.toArray(new String[names.size()]);
	
	        	}
	        	catch (final URISyntaxException exception)
	        	{
	        		exception.printStackTrace();
	        	}

	        }
	        
        	// We check the proprietary games.
//    		final List<String> proprieatryNames = new ArrayList<>();
//    		final String pathProprietaryGames = "../../LudiiPrivate/ProprietaryGames/res";
//    		final File file = new File(pathProprietaryGames.replaceAll(Pattern.quote("\\"), "/"));
//    		if (file.exists())
//    	    	visit(file.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/") + File.separator, proprieatryNames);
	        
    		// We add them to the list if some are found.
//    		if(!proprieatryNames.isEmpty())
//    		{
//    			for(int i = 0 ; i < proprieatryNames.size();i++)
//    			{
//    				final String name = proprieatryNames.get(i).substring(proprieatryNames.get(i).indexOf("lud")-1);
//    				proprieatryNames.set(i, name);
//    			}
    			
//    			for(String name: choices)
//    				names.add(name);
//    			
//    			names.addAll(proprieatryNames);
//	    		Collections.sort(names);
//	    		choices = names.toArray(new String[names.size()]);
//    		}
    		
	        gamesList = choices;
		}
		
		// To protect against users accidentally modifying this array, we return a copy
		return Arrays.stream(gamesList).filter(s -> !shouldIgnoreLud(s)).toArray(String[]::new);
	}
		
	//-------------------------------------------------------------------------
	
	/**
	 * @param lud
	 * @return True if we wish to ignore the given lud
	 */
	public static boolean shouldIgnoreLud(final String lud)
	{
		return 
				(
					lud.contains("lud/bad/") ||
					lud.contains("lud/bad_playout/") ||
					lud.contains("lud/wishlist/")
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lud
	 * @return True if we wish to ignore the given lud (release version)
	 */
	public static boolean shouldIgnoreLudRelease(final String lud)
	{
		return 
				(
					shouldIgnoreLudAnalysis(lud) ||
					lud.contains("/proprietary/")
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lud
	 * @return True if we wish to ignore the given lud (release version)
	 */
	public static boolean shouldIgnoreLudThumbnails(final String lud)
	{
		return 
				(
					shouldIgnoreLud(lud) ||
					lud.contains("/proprietary/") ||
					lud.contains("/subgame/") ||
					lud.contains("/test/") ||
					lud.contains("/wip/")
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lud
	 * @return True if we wish to ignore the given lud (remote version)
	 */
	public static boolean shouldIgnoreLudRemote(final String lud)
	{
		return 
				(
					shouldIgnoreLudRelease(lud) ||
					lud.contains("/puzzle/") ||
					lud.contains("/simulation/")
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lud
	 * @return True if we wish to ignore the given lud (evaluation version)
	 */
	public static boolean shouldIgnoreLudEvaluation(final String lud)
	{
		return 
				(
					shouldIgnoreLudRelease(lud) ||
					lud.contains("/puzzle/") ||
					lud.contains("/simulation/")
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lud
	 * @return True if we wish to ignore the given lud (analysis version)
	 */
	public static boolean shouldIgnoreLudAnalysis(final String lud)
	{
		return 
				(
					shouldIgnoreLud(lud) ||
					lud.contains("/subgame/") ||
					lud.contains("/test/") ||
					lud.contains("/wip/") ||
					lud.contains("/WishlistDLP/") ||
					lud.contains("/simulation/") ||
					lud.contains("/reconstruction/pending/") ||
					lud.contains("/reconstruction/validation/")
				);
	}
	
	//-------------------------------------------------------------------------

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
				if (file.getName().contains(".lud") && !shouldIgnoreLud(path))
				{
					// Add this game name to the list of choices
					final String name = new String(file.getName());

					if(path.contains("LudiiPrivate"))
						names.add(file.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/"));
					else if (containsGame(path + File.separator + file.getName()))
						names.add(path.substring(path.indexOf(File.separator + "lud" + File.separator)) + name);
				}
            }
        }
    }
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param filePath
	 * @return Whether this file contains a game description (not tested).
	 */
	public static boolean containsGame(final String filePath)
	{
		final File file = new File(filePath);
		if (file != null)
		{
			InputStream in = null;
			
			String path = file.getPath().replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));
			final URL url = FileHandling.class.getResource(path);
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
					if (line.contains("(game"))
						return true;
			}
			catch (final Exception e)
			{
				System.out.println("FileHandling.containsGame(): Failed to load " + filePath + ".");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param filePath
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

	/**
	 * Based on: http://www.uofr.net/~greg/java/get-resource-listing.html
	 * 
	 * Changed a lot after copying from that source! Made recursive.
	 *
	 * Recursively lists directory contents for a resource folder.
	 * Works for regular files and also JARs.
	 *
	 * @author Greg Briggs
	 * @param cls
	 * @param path Should end with "/", but not start with one.
	 * @param filter
	 * @return Just the name of each member item, not the full paths (or actually maybe full paths).
	 */
	public static String[] getResourceListing
	(
		final Class<?> cls, final String path, final String filter
	)
	{
		URL dirURL = cls.getClassLoader().getResource(path);
		if (dirURL == null)
		{
			// If a jar file, can't find directory, assume the same jar as cls
			final String me = cls.getName().replace(".", "/") + ".class";
			dirURL = cls.getClassLoader().getResource(me);
		}

		if (dirURL != null && dirURL.getProtocol().equals("file"))
		{			
			// File path: return its contents
			try
			{
				final String dirPath = dirURL.toURI().toString().substring("file:/".length()).replaceAll(Pattern.quote("%20"), " ");
				final String[] toReturn = listFilesOfType(dirPath, filter).toArray(new String[0]);
				
				for (int i = 0; i < toReturn.length; ++i)
				{
					toReturn[i] = toReturn[i].replaceAll(Pattern.quote("\\"), "/");
					toReturn[i] = "/" + toReturn[i].substring(toReturn[i].indexOf(path));
				}
				
				return toReturn;
			}
			catch (final URISyntaxException e)
			{
				e.printStackTrace();
			}
		}

		if (dirURL.getProtocol().equals("jar"))
		{
			// JAR path
			final String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
			JarFile jar = null;
			try
			{
				jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			}
			catch (final UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}

			final Enumeration<JarEntry> entries = jar.entries();  //gives all entries in jar
			final List<String> result = new ArrayList<>();

			while (entries.hasMoreElements())
			{
				final String filePath = entries.nextElement().getName();
				
				if (filePath.endsWith(filter) && filePath.startsWith(path))
				{
					result.add(File.separator + filePath);
				}
			}
			try
			{
				jar.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}

			Collections.sort(result);
			return result.toArray(new String[result.size()]);
		}
		return null;
	}
	
	/**
	 * Optimised version of getResourceListing() for cases where we expect to find only
	 * a single entry in the returned array (but it will fall back to return the full
	 * listing if we can't immediately access just the single file).
	 * 
	 * @param cls
	 * @param path
	 * @param filter
	 * @return Listing of resources (containing just a single element if we could find it immediately)
	 */
	public static String[] getResourceListingSingle(final Class<?> cls, final String path, final String filter)
	{
		URL dirURL = cls.getClassLoader().getResource(path);
		if (dirURL == null)
		{
			// If a jar file, can't find directory, assume the same jar as cls
			final String me = cls.getName().replace(".", "/") + ".class";
			dirURL = cls.getClassLoader().getResource(me);
		}

		if (dirURL != null && dirURL.getProtocol().equals("file"))
		{			
			// File path: return its contents
			try
			{
				final String dirPath = dirURL.toURI().toString().substring("file:/".length()).replaceAll(Pattern.quote("%20"), " ");
				
				if (new File(dirPath + filter).exists())
				{
					// We can just return this one file immediately
					return new String[]{ "/" + path + filter };
				}
				
				final String[] toReturn = listFilesOfType(dirPath, filter).toArray(new String[0]);
				
				for (int i = 0; i < toReturn.length; ++i)
				{
					toReturn[i] = toReturn[i].replaceAll(Pattern.quote("\\"), "/");
					toReturn[i] = "/" + toReturn[i].substring(toReturn[i].indexOf(path));
				}
								
				return toReturn;
			}
			catch (final URISyntaxException e)
			{
				e.printStackTrace();
			}
		}

		if (dirURL.getProtocol().equals("jar"))
		{
			// JAR path
			final String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
			JarFile jar = null;
			try
			{
				jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			}
			catch (final UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			
			final ZipEntry entry = jar.getEntry(path + filter);
			if (entry != null)
			{
				// We'll just immediately return this one
				return new String[] { File.separator + path + filter };
			}
			
			// Search through JAR
			final Enumeration<JarEntry> entries = jar.entries();  //gives all entries in jar
			final List<String> result = new ArrayList<>();

			while (entries.hasMoreElements())
			{
				final String filePath = entries.nextElement().getName();

				if (filePath.endsWith(filter) && filePath.startsWith(path))
				{
					System.out.println("filePath = " + filePath);
					result.add(File.separator + filePath);
				}
			}
			try
			{
				jar.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}

			Collections.sort(result);
			return result.toArray(new String[result.size()]);
		}
		
		return null;
	}

	//-------------------------------------------------------------------------
	
	public static List<String> listFilesOfType(final String path, final String extension)
	{
		final List<String> files = new ArrayList<String>();
		walk(path, files, extension);		
		return files;
	}

	static void walk(final String path, final List<String> files, final String extension)
	{
		final File root = new File("/" + path);
        final File[] list = root.listFiles();

        if (list == null) 
        	return;

        for (final File file : list) 
        {
            if (file.isDirectory()) 
            {
            	//System.out.println("Dir:" + file.getAbsoluteFile());
                walk(file.getAbsolutePath(), files, extension);
            }
            else 
            {
            	//System.out.println("File:" + file.getAbsoluteFile());
            	if (file.getName().contains(extension))
            		files.add(new String(file.getAbsolutePath()));
            }
        }
	}

	//-------------------------------------------------------------------------
	
	public static void findMissingConstructors()
	{
		final List<String> files = listFilesOfType("/Users/cambolbro/Ludii/dev/Core/src/game", ".java");
		System.out.println(files.size() + " .java files found.");
		
		for (final String path : files)
		{
			int c = path.length()-1;
			while (c >= 0 && path.charAt(c) != '/')
				c--;
			if (c < 0)
				continue;
			final String className = path.substring(c+1, path.length()-5);
			final String constructorName = "public " + className;
			//System.out.println("Path: " + path + ", className: " + className + ", constructorName: " + constructorName);
			
			boolean abstractClass    = false;
			boolean constructorFound = false;
			try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(path), "UTF-8")))
			{
				String line;			
				while (true)
				{
					line = reader.readLine();
					if (line == null)
						break;
					
					if (line.contains("abstract class"))
					{
						abstractClass = true; 
						break;
					}
					if (line.contains(constructorName) || line.contains(" construct()"))
					{
						constructorFound = true; 
						break;
					}
					//System.out.println(line);
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			
			if (!abstractClass && !constructorFound)
				System.out.println("Missing " + path);  //className + ".");
		}		
	}
	
	//-------------------------------------------------------------------------
	
	public static void findEmptyRulesets()
	{
		final List<String> files = listFilesOfType("/Users/cambolbro/Ludii/dev/Common/res/lud", ".lud");
		System.out.println(files.size() + " .lud files found.");
		
		for (final String path : files)
		{
			int c = path.length()-1;
			while (c >= 0 && path.charAt(c) != '/')
				c--;
			if (c < 0)
				continue;

			final StringBuilder sb = new StringBuilder();
			
			try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(path), "UTF-8")))
			{
				String line;			
				do
				{
					line = reader.readLine();
					sb.append(line);
				} while (line != null);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}

			final String str = sb.toString();
			
			int r = str.indexOf("(ruleset");
			if (r >= 0)
			{
				// File contains ruleset
				while (r < str.length() && str.charAt(r) != '{')
					r++;
				
				if (r < str.length())
				{
					// We have a curly brace
					final int rr = StringRoutines.matchingBracketAt(str, r);
					if (rr < 0)
						throw new RuntimeException("No closing '}' in ruleset: " + str);

					final String sub = str.substring(r+1, rr);
					boolean isChar = false;
					for (int s = 0; s < sub.length(); s++)
					{
						final char ch = sub.charAt(s);
						if 
						(
							StringRoutines.isTokenChar(ch) 
							|| 
							StringRoutines.isNameChar(ch) 
							|| 
							StringRoutines.isNumeric(ch) 
							|| 
							StringRoutines.isBracket(ch)
						)
							isChar = true;
					}
					if (!isChar)
						System.out.println(path + " has an empty ruleset.");
				}
			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
		}		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Print game options per .lud to file.
	 * @param fileName
	 * @throws IOException 
	 */
	public static void printOptionsToFile(final String fileName) throws IOException
	{
		final String[] list = listGames();
		
		// Prepare the output file
		final File file = new File(fileName);
		if (!file.exists())
			file.createNewFile();

		try (final FileWriter fw = new FileWriter(file.getName(), false))
		{
			try (final BufferedWriter writer = new BufferedWriter(fw))
			{
				for (final String name : list)
				{
					final String str = gameAsString(name);
					System.out.println(name + "(" + str.length() + " chars).");
				
					final String[] subs = str.split("\n");
		
					writer.write("\n" + name + "(" + str.length() + " chars):\n");
					for (int n = 0; n < subs.length; n++)
						if (subs[n].contains("(option "))
							writer.write(subs[n] + "\n");
				}
//				writer.close();
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Print game options per .lud to file.
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
	
	/**
	 * @param name Path of game file (.lud) with name.
	 * @return Contents of specified .lud file as string.
	 */
	public static String gameAsString(final String name)
	{
		// Not opening - just locating resource at the moment
		InputStream in = FileHandling.class.getResourceAsStream(name.startsWith("/lud/") ? name : "/lud/" + name);
		
		if (in == null)
		{
			// exact match with full filepath under /lud/ not found; let's try
			// to see if we can figure out which game the user intended
			final String[] allGameNames = FileHandling.listGames();
			int shortestNonMatchLength = Integer.MAX_VALUE;
			String bestMatchFilepath = null;
			final String givenName = name.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
			
			for (final String gameName : allGameNames)
			{
				final String str = gameName.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
				
				if (str.endsWith(givenName))
				{
					final int nonMatchLength = str.length() - givenName.length();
					if (nonMatchLength < shortestNonMatchLength)
					{
						shortestNonMatchLength = nonMatchLength;
						bestMatchFilepath = "..\\Common\\res\\" + gameName;
					}
				}
			}
			
			String resourceStr = bestMatchFilepath.replaceAll(Pattern.quote("\\"), "/");
			resourceStr = resourceStr.substring(resourceStr.indexOf("/lud/"));			
			in = FileHandling.class.getResourceAsStream(resourceStr);
		}
	
		// Open the resource and use it.
		final StringBuilder sb = new StringBuilder();
		try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in)))
		{
			String line;
			while ((line = rdr.readLine()) != null)
				sb.append(line + "\n");
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	/**
	 * @param fileContents 	The contents of the file.
	 * @param filePath		The path of the file.
	 * @param fileName		The name of the file.
	 */
	public static void saveStringToFile
	(
		final String fileContents, final String filePath, final String fileName
	)
	{
		try
		{
			final File file = new File(filePath + fileName);
			try (final PrintWriter out = new PrintWriter(file.getAbsolutePath())) 
			{
			    out.println(fileContents);
			}
			catch (final FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether cambolbro is compiling this project.
	 */
	public static boolean isCambolbro()
	{
		return isUser("/Users/cambolbro/eclipse/Ludii/dev/Player");
	}

	/**
	 * @return Whether the specified person is compiling this project.
	 */
	public static boolean isUser(final String userName)
	{
		final Path path = Paths.get(System.getProperty("user.dir"));
		//System.out.println("user.dir: " + path);
		return path.toString().contains(userName);
	}
	
	//-------------------------------------------------------------------------

}
