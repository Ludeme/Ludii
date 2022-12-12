package gameDistance.datasets.bagOfWords;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Dataset containing compilation concepts.
 * - BagOfWords
 * 
 * @author matthew.stephenson
 */
public class ImportConceptDataset implements Dataset
{
	@Override
	public Map<String, Double> getBagOfWords(final Game game) 
	{
		final Map<String, Double> featureMap = new HashMap<>();
		
		// Load files from a specific directory instead.
		final String filePath = "../../LudiiPrivate/DataMiningScripts/Sklearn/res/Input/rulesetConceptsUCT.csv";
		
		List<String> topRow = new ArrayList<>();
		final List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) 
		{
		    String line = br.readLine();
		    topRow = Arrays.asList(line.split(","));
		    
		    while ((line = br.readLine()) != null) 
		    {
		        final String[] values = line.split(",");
		        records.add(Arrays.asList(values));
		    }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		final String gameName = game.name().replaceAll("',()", "").replace(" ", "_");
		String rulesetName = "Default";
		if (game.getRuleset() != null)
			rulesetName = game.getRuleset().heading().replaceAll("',()", "").replace(" ", "_");
		final String formattedRulesetName = gameName + "_" + rulesetName;
		
		// find the record that matches the ruleset name:
		for (final List<String> record : records)
		{
			if (record.get(0).equals(formattedRulesetName))
			{
				for (int i = 1; i < topRow.size(); i++)
				{
					System.out.println(i);
					System.out.println(topRow.get(i));
					System.out.println(Double.valueOf(record.get(i)));
					featureMap.put(topRow.get(i), Double.valueOf(record.get(i)));
				}
				
				break;
			}
		}
		
		System.out.println("Failed to find match for " + formattedRulesetName);
		
		return featureMap;
	}

	/**
	 * Not Supported
	 */
	@Override
	public List<String> getSequence(final Game game) 
	{
		return null;
	}

	/**
	 * Not Supported
	 */
	@Override
	public Tree getTree(final Game game) 
	{
		return null;
	}

}
