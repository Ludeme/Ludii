package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import main.options.Ruleset;

/**
 * Gets the unique name representation for a given game object (including ruleset name).
 * 
 * @author Matthew.Stephenson
 */
public class DBGameInfo 
{
	
	// SQL command to update this file located in the same directory
	private static String rulesetIdsInputFilePath = "./res/concepts/input/GameRulesets.csv";
	
	// Cached version of ruleset-Id information.
	private static Map<String, Integer> rulesetIds = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return the unique name representation for a given game object (including ruleset name).
	 */
	public static String getUniqueName(final Game game)
	{
		final String gameName = game.name();
		String rulesetName = "";
		if (game.getRuleset() != null && game.description().rulesets().size() > 1)
		{
			final Ruleset ruleset = game.getRuleset();
			final String startString = "Ruleset/";
			rulesetName = ruleset.heading().substring(startString.length(), ruleset.heading().lastIndexOf('(') - 1);
		}
		
		String gameRulesetName = gameName + "-" + rulesetName;
		gameRulesetName = gameRulesetName.replace(" ", "_").replaceAll("[\"',()]", "");
		
		return gameRulesetName;
	}
	
	//-------------------------------------------------------------------------
	
	public static Map<String, Integer> getRulesetIds()
	{
		return getRulesetIds(rulesetIdsInputFilePath);
	}
	
	/**
	 * @return a Map giving the DB Id for each ruleset in the database (names in same format as getUniqueName)
	 */
	public static Map<String, Integer> getRulesetIds(final String filePath)
	{
		if (rulesetIds == null)
		{
			final Map<String, Integer> rulesetNameIdPairs = new HashMap<>();
			
			final List<String[]> allLines = new ArrayList<>();
			
			try (BufferedReader br = new BufferedReader(new FileReader(filePath))) 
			{
			    String line;
			    while ((line = br.readLine()) != null) 
			    {
			    	final String[] values = line.split(",");
			    	allLines.add(values);
			    }
			} 
			catch (final Exception e1) 
			{
				e1.printStackTrace();
			}
			
			for (final String[] line : allLines)
			{
		        final String gameName = line[0];
		        final String rulesetName = line[1];
		        final Integer Id = Integer.valueOf(line[2].replace("\"", ""));
		        
		        // If game name occurs more than ones, then add ruleset name.
		        int gameCounter = 0;
		        for (final String[] checkLine : allLines)
		        	if (checkLine[0].equals(gameName))
		        		gameCounter++;
		        
		        String gameRulesetName = gameName + "-";
		        if (gameCounter > 1)
		        	gameRulesetName = gameName + "-" + rulesetName;
		        
		        gameRulesetName = gameRulesetName.replace(" ", "_").replaceAll("[\"',()]", "");
		        
		        rulesetNameIdPairs.put(gameRulesetName, Id);
			}
			
			rulesetIds = rulesetNameIdPairs;
		}
		
		return rulesetIds;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The DB Id for a the ruleset in the database corresponding to a given Game object.
	 */
	public static Integer getRulesetId(final Game game)
	{
		return getRulesetIds().get(getUniqueName(game));
	}
	
	//-------------------------------------------------------------------------
}
