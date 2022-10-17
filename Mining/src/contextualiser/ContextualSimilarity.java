package contextualiser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import game.Game;
import utils.DBGameInfo;

public class ContextualSimilarity 
{
	private static final String rulesetIdsFilePath = "../Mining/res/concepts/input/GameRulesets.csv";
	// private static final String rulesetLudemesOutputFilePath = "../../LudiiPrivate/DataMiningScripts/Geacron/GeacronDistance/res/output/contextualiser/similarity_";
	private static final String rulesetLudemesOutputFilePath = "../Mining/res/recons/input/contextualiser/similarity_";

	/**
	 * @param game Game to compare similarity against.
	 * @param conceptSimilarity true if using concept similarity, otherwise using cultural similarity.
	 * @return Map of game/ruleset names to similarity values.
	 */
	public static final Map<String, Double> getRulesetSimilarities(final Game game, final boolean conceptSimilarity)
	{
		// Get all ruleset ids from DB
		final String name = DBGameInfo.getUniqueName(game);
		final Map<String, Integer> rulesetIds = DBGameInfo.getRulesetIds(rulesetIdsFilePath);
		final int rulesetId = rulesetIds.get(name).intValue();
		
		final Map<Integer, Double> rulesetSimilaritiesIds = new HashMap<>();			// Map of ruleset ids to similarity
		final Map<String, Double> rulesetSimilaritiesNames = new HashMap<>();			// Map of game/ruleset names to similarity
		final String fileName = rulesetLudemesOutputFilePath + rulesetId + ".csv";
				
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) 
		{
			br.readLine();		// Skip first line of column headers.
		    String line;
		    while ((line = br.readLine()) != null) 
		    {
		        final String[] values = line.split(",");
		        
		        double similarity = -1.0;
		        if (conceptSimilarity)
		        	similarity = Double.valueOf(values[2]).doubleValue();
		        else
		        	similarity = Double.valueOf(values[1]).doubleValue();
		        
		        rulesetSimilaritiesIds.put(Integer.valueOf(values[0]), Double.valueOf(similarity));
		        
//		        if (!rulesetIds.containsValue(Integer.valueOf(values[0])))
//		        	System.out.println("ERROR, two rulesets with the same name. ruleset id: " + Integer.valueOf(values[0]));

		        // Convert ruleset ids to corresponding names.
		        for (final Map.Entry<String, Integer> entry : rulesetIds.entrySet()) 
		        	if (entry.getValue().equals(Integer.valueOf(values[0])))
		        		rulesetSimilaritiesNames.put(entry.getKey(), Double.valueOf(similarity));
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
