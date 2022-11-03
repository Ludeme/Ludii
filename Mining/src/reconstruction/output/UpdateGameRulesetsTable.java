package reconstruction.output;

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

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.StringRoutines;
import main.UnixPrintWriter;

/**
 * Update the GameRulesets Table with the outcome rulesets from the reconstruction process.
 * @author Eric.Piette
 */
public class UpdateGameRulesetsTable
{
	// Load ruleset avg common true concepts from specific directory.
	final static String gameRulesetsFilePath = "./res/recons/input/GameRulesets.csv";
	
	// rulesets reconstructed.
	final static String pathReconstructed    = "./res/recons/output/";
	
	// game name.
	final static String gameName        = "Samantsy";
	
	public static void main(final String[] args)
	{
		final int nextId = 1 + getMaxId();
		updateGameRulesets(nextId);
	}
	
	/**
	 * Generate the new GameRulesets.csv with the new rulesets.
	 * @param nextId The next id to use.
	 */
	private static void updateGameRulesets(int nextId)
	{
		final String pathReportReconstrution = pathReconstructed + gameName+".csv";
		
		final List<String> rulesetNameList = new ArrayList<String>();
		final TIntArrayList idReconsList = new TIntArrayList();
		final TDoubleArrayList scoreList = new TDoubleArrayList();
		final TDoubleArrayList similaryScoreList = new TDoubleArrayList();
		final TDoubleArrayList conceptualScoreList = new TDoubleArrayList();
		final List<String> idsUsedList = new ArrayList<String>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(pathReportReconstrution))) 
		{
			String line = br.readLine();
			while (line != null)
			{
				String lineNoQuote = line.replaceAll(Pattern.quote("\""), "");

				int separatorIndex = lineNoQuote.indexOf(',');
				final String rulesetName = lineNoQuote.substring(0, separatorIndex);
				lineNoQuote = lineNoQuote.substring(rulesetName.length() + 1);
				rulesetNameList.add(rulesetName);
				
				separatorIndex = lineNoQuote.indexOf(',');
				final String idReconsStr = lineNoQuote.substring(0, separatorIndex);
				idReconsList.add(Integer.parseInt(idReconsStr));

				lineNoQuote = lineNoQuote.substring(idReconsStr.length() + 1);
				separatorIndex = lineNoQuote.indexOf(',');
				final String scoreStr = lineNoQuote.substring(0, separatorIndex);
				scoreList.add(Double.parseDouble(scoreStr));
				
				lineNoQuote = lineNoQuote.substring(scoreStr.length() + 1);
				separatorIndex = lineNoQuote.indexOf(',');
				final String similarityScoreStr = lineNoQuote.substring(0, separatorIndex);
				similaryScoreList.add(Double.parseDouble(similarityScoreStr));
				
				lineNoQuote = lineNoQuote.substring(similarityScoreStr.length() + 1);
				separatorIndex = lineNoQuote.indexOf(',');
				final String culturalScoreStr = lineNoQuote.substring(0, separatorIndex);
				conceptualScoreList.add(Double.parseDouble(culturalScoreStr));
				
				lineNoQuote = lineNoQuote.substring(culturalScoreStr.length() + 1);
				final String ids = lineNoQuote;
				idsUsedList.add(ids);
				
				line = br.readLine();
			}
			br.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		final String output = "GameRulesets.csv";
		
		// Write the new CSV.
		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			// Copy the previous CSV.
			try (BufferedReader br = new BufferedReader(new FileReader(gameRulesetsFilePath))) 
			{	
				String line;	// column names
			    while ((line = br.readLine()) != null) 
			    {
					writer.println(line);
			    }
			    for(int i = 0; i < rulesetNameList.size(); i++)
			    {
					final List<String> lineToWrite = new ArrayList<String>();
					lineToWrite.add("\"" + (nextId + i) + "\"");
					lineToWrite.add("\"" + idReconsList.get(i) + "\"");
					lineToWrite.add("\"" + rulesetNameList.get(i) + "\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"2\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"0\"");
					lineToWrite.add("\"NULL\"");
					lineToWrite.add("\"0\"");
					lineToWrite.add("\"0\"");
					lineToWrite.add("\"" + scoreList.get(i) + "\"");
					lineToWrite.add("\"" + similaryScoreList.get(i) + "\"");
					lineToWrite.add("\"" + conceptualScoreList.get(i) + "\"");
					lineToWrite.add("\"" + idsUsedList.get(i) + "\"");
					writer.println(StringRoutines.join(",", lineToWrite));
			    }
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}
		
		System.out.println("GameRulesets CSV Updated");
	}

	/**
	 * @return the max id of the rulesets
	 */
	private static int getMaxId()
	{
		// ids of the rulesets
		final TIntArrayList ids = new TIntArrayList();	
		
		try (BufferedReader br = new BufferedReader(new FileReader(gameRulesetsFilePath))) 
		{
		    String line;	// column names
		    while ((line = br.readLine()) != null) 
		    {
		    	if(line.length() > 2 &&  line.charAt(0) == '"' && Character.isDigit(line.charAt(1)))
		    	{
		    		final String subLine = line.substring(1);
		    		int i = 0;
		    		char c = subLine.charAt(i);
		    		while(c != '"')
		    		{
		    			i++;
		    			c = subLine.charAt(i);
		    		}
		    		ids.add(Integer.parseInt(subLine.substring(0,i)));
		    	}
		    }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		return ids.max();
	}
}
