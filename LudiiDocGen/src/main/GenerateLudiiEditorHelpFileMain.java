package main;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import help.GenerateLudiiEditorHelpFile;
import xml.GenerateLudiiDocXML;

/**
 * Main method to generate (an updated version of) the help file for
 * tooltips/autocomplete in the Ludii editor.
 *
 * @author Dennis Soemers
 */
public class GenerateLudiiEditorHelpFileMain
{
	
	/**
	 * Main method
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(final String[] args) 
			throws InterruptedException, IOException, ClassNotFoundException, 
			ParserConfigurationException, SAXException
	{
		GenerateLudiiDocXML.generateXML();
		GenerateLudiiEditorHelpFile.generateHelp();
	}

}
