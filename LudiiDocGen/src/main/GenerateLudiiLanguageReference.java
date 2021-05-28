package main;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import tex.GenerateLudiiDocTex;
import xml.GenerateLudiiDocXML;

/**
 * Main method to go through full process for generating Ludii language reference
 *
 * @author Dennis Soemers
 */
public class GenerateLudiiLanguageReference
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
		GenerateLudiiDocTex.generateTex();
	}

}
