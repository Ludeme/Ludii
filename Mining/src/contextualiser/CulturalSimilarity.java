package contextualiser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import game.Game;
import utils.DBGameInfo;

public class CulturalSimilarity 
{
	private static final String rulesetIdsFilePath = "../Mining/res/concepts/input/GameRulesets.csv";
	private static final String rulesetLudemesOutputFilePath = "../../LudiiPrivate/DataMiningScripts/Geacron/GeacronDistance/res/output/contextualiser/similarity_";

	public static final Map<String, Double> getRulesetSimilarities(final Game game)
	{
		// Get all ruleset ids from DB
		final String name = DBGameInfo.getUniqueName(game);
		final Map<String, Integer> rulesetIds = DBGameInfo.getRulesetIds(rulesetIdsFilePath);
		final int rulesetId = rulesetIds.get(name);
		
		final Map<Integer, Double> rulesetSimilaritiesIds = new HashMap<>();			// Map of ruleset ids to CSN similarity
		final Map<String, Double> rulesetSimilaritiesNames = new HashMap<>();			// Map of game/ruleset names to CSN similarity
		final String fileName = rulesetLudemesOutputFilePath + rulesetId + ".csv";
				
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) 
		{
			br.readLine();		// Skip first line of column headers.
		    String line;
		    while ((line = br.readLine()) != null) 
		    {
		        final String[] values = line.split(",");
		        rulesetSimilaritiesIds.put(Integer.valueOf(values[0]), Double.valueOf(values[1]));
		        
//		        if (!rulesetIds.containsValue(Integer.valueOf(values[0])))
//		        	System.out.println("ERROR, two rulesets with the same name. ruleset id: " + Integer.valueOf(values[0]));

		        // Convert ruleset ids to corresponding names.
		        for (final Map.Entry<String, Integer> entry : rulesetIds.entrySet()) 
		        	if (entry.getValue().equals(Integer.valueOf(values[0])))
		        		rulesetSimilaritiesNames.put(entry.getKey(), Double.valueOf(values[1]));
		    }
		}
		catch (final Exception e)
		{
			System.out.println("Could not find similarity file, ruleset probably has no evidence.");
			e.printStackTrace();
		}

		return rulesetSimilaritiesNames;
	}
	
}
