package xml;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import main.StringRoutines;

/**
 * Code to generate XML representation of Ludii's
 * javadoc documentation.
 *
 * @author Dennis Soemers
 */
public class GenerateLudiiDocXML
{
	
	/** Filepath for our doclet JAR file */
	private static final String DOCLET_FILEPATH = "../LudiiDocGen/lib/jeldoclet.jar";
	
	/** Filepath for directory where we want to write XML Javadoc output */
	private static final String XML_OUT_DIR = "../LudiiDocGen/out/xml";
	
	/** Classpath (with class files) */
	private static final String CLASSPATH = StringRoutines.join
			(
				File.pathSeparator, 
				"../Common/bin",
				"../Common/lib/Trove4j_ApacheCommonsRNG.jar",
				"../Language/bin",
				"../Core/lib/jfreesvg-3.4.jar"
			);
	
	/** Sourcepath (with source files) */
	private static final String SOURCEPATH = "../Core/src";
	
	/**
	 * Main method
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void generateXML() throws InterruptedException, IOException
	{
		final String command = StringRoutines.join(
				" ",
				"javadoc",
				"-doclet",
				"com.jeldoclet.JELDoclet",
				"-docletpath",
				new File(DOCLET_FILEPATH).getCanonicalPath(),
				"-d",
				new File(XML_OUT_DIR).getCanonicalPath(),
				"-classpath",
				CLASSPATH,
				"-sourcepath",
				new File(SOURCEPATH).getCanonicalPath(),
				"-subpackages",
				"game:metadata",
				"-public",
				"-encoding",
				"UTF-8"//,
				//"-verbose"
				);
		System.out.println("Executing command: " + command);
		new ProcessBuilder(command.split(Pattern.quote(" "))).inheritIO().start().waitFor();
	}

}
