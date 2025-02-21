package utils.concepts.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import main.UnixPrintWriter;

/**
 * Script to merge all the CSV results of many jobs in the cluster for the db and compute the ids of each line in the resulting csv.
 * Because PhPMyAdmin accepts only files up to 2048 Ko, It is necessary to generate multiple files.
 * 
 * @author Eric.Piette
 */
public class MergeDBResultCSV
{
	/** The path of the csv folder. */
	private static final String FolderCSV = "C:\\Users\\ericp\\Ludii\\Ludii\\Mining\\res\\concepts\\input\\ToMerge";

	public static void main(final String[] args) throws IOException
	{
		final File folder = new File(FolderCSV.replaceAll(Pattern.quote("\\"), "/"));
		//final int linelimit = 50000;
		
		final List<File> csvFiles = new ArrayList<File>();
		for (final File file : folder.listFiles())
			csvFiles.add(file);
		
		final String fileName = "RulesetConcepts";
		int fileNumber = 0;
		final String extensionName = ".csv";
		int id = 1;
		
		List<String> linesToWrite = new ArrayList<String>();
		try 
		{
		PrintWriter mainWriter = new UnixPrintWriter(new File(fileName + fileNumber + extensionName), "UTF-8");
		for (final File csv : csvFiles)
			{
				try 
				(
					final BufferedReader reader = 
						new BufferedReader
						(
							new FileReader(csv.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/"))
						)
				)
				{
					String line = reader.readLine();
					while (line != null)
					{
						final String lineFromComa = line.substring(line.indexOf(','));
						final String idRuleset = lineFromComa.substring(1, 3);
						if (!idRuleset.equals("-1"))
						{
							linesToWrite.add(id + lineFromComa);
							id++;
//							if(id % linelimit == 1)
//							{
//								fileNumber++;
//								mainWriter = new UnixPrintWriter(new File(fileName + fileNumber + extensionName), "UTF-8");
//							}
						}
						line = reader.readLine();
					}
					reader.close();
					
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				} 
			}

		for(String conceptLine: linesToWrite)
			mainWriter.println(conceptLine);
		mainWriter.close();
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (final UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
}
