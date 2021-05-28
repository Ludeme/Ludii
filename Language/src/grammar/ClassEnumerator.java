package grammar; 

// From: https://github.com/ddopson/java-class-enumerator
// Reference: https://stackoverflow.com/questions/10119956/getting-class-by-its-name

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//-----------------------------------------------------------------------------

/**
 * Routines for enumerating classes.
 * @author Dennis and cambolbro
 */
public class ClassEnumerator 
{
//	private static void log(String msg) 
//	{
//		System.out.println("ClassDiscovery: " + msg);	
//	}

	private static Class<?> loadClass(String className) 
	{
		try 
		{
			return Class.forName(className);
		} 
		catch (final ClassNotFoundException e) {
			throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
		}
	}
	
	/**
	 * Given a package name and a directory returns all classes within that directory
	 * @param directory
	 * @param pkgname
	 * @return Classes within Directory with package name
	 */
	public static List<Class<?>> processDirectory(File directory, String pkgname) {
		
		final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		 
//		log("Reading Directory '" + directory + "'");
		
		// Get the list of the files contained in the package

		final String[] files = directory.list();
		for (int i = 0; i < files.length; i++) {
			final String fileName = files[i];
			String className = null;
			
			// we are only interested in .class files
			if (fileName.endsWith(".class")) {
				// removes the .class extension
				className = pkgname + '.' + fileName.substring(0, fileName.length() - 6);
			}
			
//			log("FileName '" + fileName + "'  =>  class '" + className + "'");
			
			if (className != null) {
				classes.add(loadClass(className));
			}
			
			//If the file is a directory recursively class this method.
			final File subdir = new File(directory, fileName);
			if (subdir.isDirectory()) {
				classes.addAll(processDirectory(subdir, pkgname + '.' + fileName));
			}
		}
		return classes;
	}

	/**
	 * Given a jar file and a package name returns all classes within jar file.
	 * 
	 * @param jarFile
	 * @param pkgname
	 * @return The list of the classes.
	 */
	public static List<Class<?>> processJarfile(final JarFile jarFile, final String pkgname) 
	{
		final List<Class<?>> classes = new ArrayList<Class<?>>();
		
		//Turn package name to relative path to jar file
		final String relPath = pkgname.replace('.', '/');
		
		try   
		{
			// Get contents of jar file and iterate through them
			final Enumeration<JarEntry> entries = jarFile.entries();
			while(entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				
				//Get content name from jar file
				final String entryName = entry.getName();
				String className = null;
				
				//If content is a class save class name.
				if(entryName.endsWith(".class") && entryName.startsWith(relPath) 
						&& entryName.length() > (relPath.length() + "/".length())) {
					className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
				}
				
	//			log("JarEntry '" + entryName + "'  =>  class '" + className + "'");
				
				//If content is a class add class to List
				if (className != null) {
					classes.add(loadClass(className));
				}
			}
			jarFile.close();
		}
		catch (final IOException e) 
		{
			throw new RuntimeException("Unexpected IOException reading JAR File '" + jarFile.getName() + "'", e);
		}

		return classes;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Return all classes contained in a given package.
	 * @param pkg
	 * @return The list of the classes.
	 */
	public static List<Class<?>> getClassesForPackage(final Package pkg) 
	{
		final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		//Get name of package and turn it to a relative path
		final String pkgname = pkg.getName();
		final String relPath = pkgname.replace('.', '/');

		// **
		// ** The following code fails when looking for path in web archive.
		// **
		//final URL url = ClassLoader.getSystemClassLoader().getResource(relPath);
		
		final URL url = ClassEnumerator.class.getClassLoader().getResource(relPath);

		//System.out.println("pkg=" + pkg + ", relPath=" + relPath + ", url=" + url);

		// If the resource is a jar get all classes from jar
		if (url.getPath().contains(".jar")) 
		{
			try 
			(
				final JarFile jarFile = new JarFile
				(
					new File(ClassEnumerator.class.getProtectionDomain().getCodeSource().getLocation().toURI()))
				)
			{
				classes.addAll(processJarfile(jarFile, pkgname));
			}
			catch (final IOException | URISyntaxException e)
			{
				throw new RuntimeException("Unexpected problem with JAR file for: " + relPath);
			}
		} 
		else 
		{
			try 
			{
				classes.addAll(processDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pkgname));
			} 
			catch (final UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
		}

		return classes;
	}
	
}
