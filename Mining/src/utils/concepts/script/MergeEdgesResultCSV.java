package utils.concepts.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import main.UnixPrintWriter;

/**
 * Script to merge all the csv results of the edges results generated for all the rulesets of the museum game.
 * 
 * @author Eric.Piette
 */
public class MergeEdgesResultCSV
{
	/** The path of the csv folder. */
	private static final String FolderCSV = "C:\\Users\\eric.piette\\Ludii\\Ludii\\Mining\\res\\concepts\\input\\ToMerge";
	
	/** The names of the board. */
	private static final String boardOne = "Both Extension Joined Diagonal";
	private static final String boardTwo = "Both Extension No Joined Diagonal";
	private static final String boardThree = "No Extension Joined Diagonal";
	private static final String boardFour = "No Extension No Joined Diagonal";
	private static final String boardFive = "Top Extension Joined Diagonal ";
	private static final String boardSix = "Top Extension No Joined Diagonal";
	
	public static void main(final String[] args) throws IOException
	{
		createMergeFile(boardOne);
		createMergeFile(boardTwo);
		createMergeFile(boardThree);
		createMergeFile(boardFour);
		createMergeFile(boardFive);
		createMergeFile(boardSix);
	}
	
	/**
	 * To create a merged csv for the edges results.
	 * @param boardName
	 */
	public static void createMergeFile(final String boardName)
	{
		final File folder = new File(FolderCSV.replaceAll(Pattern.quote("\\"), "/"));

		final String edgeResults = "EdgesResults" + boardName + ".csv";
		
		try (final PrintWriter mainWriter = new UnixPrintWriter(new File(edgeResults), "UTF-8"))
		{
			for (final File agentFolder : folder.listFiles())
			{
				final String agentName = agentFolder.getName();
				for (final File file : agentFolder.listFiles())
				{
					if(file.getName().contains(boardName))
					{
						String rulesetName = file.getName().substring(file.getName().indexOf('-') + 1);
						rulesetName = rulesetName.substring(0,rulesetName.length()-4);
						try 
						(
							final BufferedReader reader = 
								new BufferedReader
								(
									new FileReader(file.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/"))
								)
						)
						{
							mainWriter.print(agentName + ",");
							mainWriter.print(rulesetName + ",");
							String line = reader.readLine();
							while (line != null)
							{
								final double frequency = Double.parseDouble(line.substring(line.lastIndexOf(',') + 1 ));
								mainWriter.print(frequency);
								mainWriter.print(",");
								line = reader.readLine();
							}
							reader.close();
							mainWriter.println("");
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
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
