package grammar; 

// From: https://github.com/ddopson/java-class-enumerator
// Reference: https://stackoverflow.com/questions/10119956/getting-class-by-its-name

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//-----------------------------------------------------------------------------

/**
 *
 */
public class ClassEnumeratorFindAll 
{
//	private static void log(String msg) 
//	{
//		System.out.println("ClassEnumeratorFindAll: " + msg);
//	}

	private static Class<?> loadClass(String className) {
		try {
			return Class.forName(className);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
		}
	}

	/**
	 * Given a directory returns all classes within that directory
	 * 
	 * @param directory
	 * @return Classes within Directory
	 */
	public static List<Class<?>> processDirectory(File directory) {

		final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

//		log("Reading Directory '" + directory + "'");
		try {
			// Get the list of the files contained in the package
			final String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				final String fileName = files[i];
				String className = null;

				// we are only interested in .class files
				if (fileName.endsWith(".class")) {
					// removes the .class extension
					className = fileName.substring(0, fileName.length() - 6);
				}

//				log("FileName '" + fileName + "'  =>  class '" + className + "'");

				if (className != null) {
					classes.add(loadClass(className));
				}

				// If the file is a directory recursively find class.
				final File subdir = new File(directory, fileName);
				if (subdir.isDirectory()) {
					classes.addAll(processDirectory(subdir));
				}

				// if file is a jar file
				if (fileName.endsWith(".jar")) {
					
					classes.addAll(processJarfile(directory.getAbsolutePath() + "/" + fileName));

				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return classes;
	}

	/**
	 * Given a jar file's URL returns all classes within jar file.
	 * 
	 * @param jarPath The path of the Jar.
	 * @return The list of classes.
	 */
	public static List<Class<?>> processJarfile(String jarPath) 
	{
		final List<Class<?>> classes = new ArrayList<Class<?>>();

		try (JarFile jarFile = new JarFile(jarPath))
		{
			// Get contents of jar file and iterate through them
			final Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();

				// Get content name from jar file
				final String entryName = entry.getName();
				String className = null;

				// If content is a class save class name.
				if (entryName.endsWith(".class")) 
				{
					className = entryName.replace('/', '.').replace('\\', '.');
					className = className.substring(0, className.length() - ".class".length());
				}

//				log("JarEntry '" + entryName + "'  =>  class '" + className + "'");

				// If content is a class add class to List
				if (className != null) {
					classes.add(loadClass(className));
					continue;
				}

				// If jar contains another jar then iterate through it
				if (entryName.endsWith(".jar")) {
					classes.addAll(processJarfile(entryName));
					continue;
				}
				// If content is a directory
				final File subdir = new File(entryName);
				if (subdir.isDirectory()) {
					classes.addAll(processDirectory(subdir));
				}

			}
			jarFile.close();
		} 
		catch (final IOException e) 
		{
			throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
		}

		return classes;
	}
}
