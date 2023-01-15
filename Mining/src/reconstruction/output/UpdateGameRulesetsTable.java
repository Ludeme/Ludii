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

import compiler.Compiler;
import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.grammar.Description;

/**
 * To generate the new lines to add to GameRulesets Table with the outcome rulesets from the reconstruction process.
 * @author Eric.Piette
 */
public class UpdateGameRulesetsTable
{
	// Load ruleset avg common true concepts from specific directory.
	final static String gameRulesetsFilePath = "./res/recons/input/GameRulesets.csv";
	
	// The rulesets reconstructed.
	final static String pathReconstructed    = "./res/recons/output/";
	
	// The game name.
	final static String gameName        = "Dame";
	
	// The precision of the double to use.
	final static int DOUBLE_PRECISION = 5;

	//-------------------------------------------------------------------------
	
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final int nextId = 1 + getMaxId();
		updateGameRulesets(nextId);
	}
	
	/**
	 * Generate the new lines to add to GameRulesets.csv with the new rulesets.
	 * @param nextId The next id to use.
	 */
	private static void updateGameRulesets(int nextId)
	{
		final String pathReportReconstrution = pathReconstructed + gameName + ".csv";
		final String pathFolderReconstrutions = pathReconstructed + gameName + "/";
		
		final List<String> rulesetNameList = new ArrayList<String>();
		final TIntArrayList idReconsList = new TIntArrayList();
		final TDoubleArrayList scoreList = new TDoubleArrayList();
		final TDoubleArrayList similaryScoreList = new TDoubleArrayList();
		final TDoubleArrayList conceptualScoreList = new TDoubleArrayList();
		final TDoubleArrayList geographicalScoreList = new TDoubleArrayList();
		final List<String> idsUsedList = new ArrayList<String>();
		final List<String> otherIdsList = new ArrayList<String>();
		final List<String> toEnglishList = new ArrayList<String>();
		
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
				String scoreStr = lineNoQuote.substring(0, separatorIndex);
				scoreList.add(Double.parseDouble(scoreStr.length() > DOUBLE_PRECISION ? scoreStr.substring(0, DOUBLE_PRECISION) : scoreStr));
				
				lineNoQuote = lineNoQuote.substring(scoreStr.length() + 1);
				separatorIndex = lineNoQuote.indexOf(',');
				String similarityScoreStr = lineNoQuote.substring(0, separatorIndex);
				similaryScoreList.add(Double.parseDouble(similarityScoreStr.length() > DOUBLE_PRECISION ? similarityScoreStr.substring(0, DOUBLE_PRECISION) : similarityScoreStr));
				
				lineNoQuote = lineNoQuote.substring(similarityScoreStr.length() + 1);
				separatorIndex = lineNoQuote.indexOf(',');
				String culturalScoreStr = lineNoQuote.substring(0, separatorIndex);
				conceptualScoreList.add(Double.parseDouble(culturalScoreStr.length() > DOUBLE_PRECISION ? culturalScoreStr.substring(0, DOUBLE_PRECISION) : culturalScoreStr));
				
				lineNoQuote = lineNoQuote.substring(culturalScoreStr.length() + 1);
				separatorIndex = lineNoQuote.indexOf(',');
				String geographicalScoreStr = lineNoQuote.substring(0, separatorIndex);
				geographicalScoreList.add(Double.parseDouble(geographicalScoreStr.length() > DOUBLE_PRECISION ? geographicalScoreStr.substring(0, DOUBLE_PRECISION) : geographicalScoreStr));
				
				lineNoQuote = lineNoQuote.substring(geographicalScoreStr.length() + 1);
				separatorIndex = lineNoQuote.indexOf('}') + 1 ;
				String ids = lineNoQuote.substring(1, separatorIndex - 1);
				idsUsedList.add(ids);
				
				lineNoQuote = lineNoQuote.substring(ids.length() + 3);
				final String otherIds = lineNoQuote.substring(0, lineNoQuote.length());
				otherIdsList.add(otherIds);
				
				final String pathReconstruction = pathFolderReconstrutions + rulesetName + ".lud"; 
				String desc = FileHandling.loadTextContentsFromFile(pathReconstruction);
				final Game game = (Game) Compiler.compileTest(new Description(desc), false);
				toEnglishList.add(game.toEnglish(game));
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
			    for(int i = 0; i < rulesetNameList.size(); i++)
			    {
					final List<String> lineToWrite = new ArrayList<String>();
					lineToWrite.add("\"" + (nextId + i) + "\"");
					lineToWrite.add("\"" + getGameReconsId(idReconsList.get(i)) + "\"");
					lineToWrite.add("\"" + rulesetNameList.get(i) + "\"");
					lineToWrite.add("NULL");
					lineToWrite.add("\"Reconstructed with Ludii\"");
					lineToWrite.add("\"2\"");
					lineToWrite.add("NULL");
					lineToWrite.add("\"" + toEnglishList.get(i) + "\"");
					lineToWrite.add("NULL");
					lineToWrite.add("NULL");
					lineToWrite.add("NULL");
					lineToWrite.add("NULL");
					lineToWrite.add("NULL");
					lineToWrite.add("NULL");
					lineToWrite.add("NULL");
					lineToWrite.add("\"0\"");
					lineToWrite.add("NULL");
					lineToWrite.add("\"0\"");
					lineToWrite.add("\"0\"");
					lineToWrite.add("\"" + scoreList.get(i) + "\"");
					lineToWrite.add("\"" + similaryScoreList.get(i) + "\"");
					lineToWrite.add("\"" + conceptualScoreList.get(i) + "\"");
					lineToWrite.add("\"" + geographicalScoreList.get(i) + "\"");
					lineToWrite.add("\"" + idsUsedList.get(i) + "\"");
					lineToWrite.add("\"" + otherIdsList.get(i) + "\"");
					writer.println(StringRoutines.join(",", lineToWrite));
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
	
	/**
	 * @return the id of the game to recons.
	 */
	private static int getGameReconsId(final int reconsRulesetId)
	{
		try (BufferedReader br = new BufferedReader(new FileReader(gameRulesetsFilePath))) 
		{
		    String line;	// column names
		    while ((line = br.readLine()) != null) 
		    {
		    	if(line.length() > 2 &&  line.charAt(0) == '"' && Character.isDigit(line.charAt(1)))
		    	{
		    		String subLine = line.substring(1);
		    		int i = 0;
		    		char c = subLine.charAt(i);
		    		while(c != '"')
		    		{
		    			i++;
		    			c = subLine.charAt(i);
		    		}
		    		final int rulesetId = Integer.parseInt(subLine.substring(0, i));
		    		if(rulesetId == reconsRulesetId)
		    		{
		    			subLine = subLine.substring(i+3);
			    		i = 0;
			    		c = subLine.charAt(i);
			    		while(c != '"')
			    		{
			    			i++;
			    			c = subLine.charAt(i);
			    		}
			    		return Integer.parseInt(subLine.substring(0, i));
		    		}
		    	}
		    }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		return Constants.UNDEFINED;
	}
}
