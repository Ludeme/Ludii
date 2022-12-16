package gameDistance.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import contextualiser.ContextualSimilarity;
import game.Game;
import gameDistance.datasets.Dataset;
import other.GameLoader;

/**
 * Game distance utility functions.
 * 
 * @author matthew.stephenson
 */
public class DistanceUtils 
{

	// Alignment edit costs
	public final static int HIT_VALUE = 5;			// Should be positive
	public final static int MISS_VALUE = -5;		// Should be negative
	public final static int GAP_PENALTY = -1;		// Should be negative
	
	public final static int nGramLength = 4;
	
	// vocabulary store paths.
	private final static String vocabularyStorePath = "res/gameDistance/vocabulary/";
	
	//-----------------------------------------------------------------------------
	
	public static Map<String, Double> fullVocabulary(final Dataset dataset, final String datasetName, final boolean overrideStoredVocabularies)
	{
		final File vocabularyFile = new File(vocabularyStorePath + datasetName + ".txt");
		
		// Recover vocabulary from previously stored txt file if available.
		if (vocabularyFile.exists() && !overrideStoredVocabularies)
		{
			try (final FileInputStream fileInput = new FileInputStream(vocabularyFile))
			{
				try (final ObjectInputStream objectInput = new ObjectInputStream(fileInput))
				{
					@SuppressWarnings("unchecked")
					final Map<String, Double> vocabulary = (HashMap<String, Double>)objectInput.readObject();
			        objectInput.close();
			        fileInput.close();
			        return vocabulary;
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}	
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}	
		}

		// Calculate full Ludii game vocabulary.
		double numGames = 0.0;
		final Map<String, Double> vocabulary = new HashMap<>();
		for (final String[] gameRulesetName : GameLoader.allAnalysisGameRulesetNames())
		{
			final Game game = GameLoader.loadGameFromName(gameRulesetName[0], gameRulesetName[1]);
			System.out.println(game.name());
			numGames++;
			for (final String s : dataset.getBagOfWords(game).keySet())
			{
				if (vocabulary.containsKey(s))
					vocabulary.put(s, Double.valueOf(vocabulary.get(s).doubleValue() + 1.0));
				else
					vocabulary.put(s, Double.valueOf(1.0));
			}
		}
		for (final Map.Entry<String, Double> entry : vocabulary.entrySet())
			entry.setValue(Double.valueOf(Math.log(numGames / entry.getValue().doubleValue())));
		
		// Store vocabulary to txt file.
		if (!vocabularyFile.exists())
		{
			try
			{
				vocabularyFile.createNewFile();
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
			try (final FileOutputStream myFileOutStream = new FileOutputStream(vocabularyFile))
			{
				try (final ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream))
				{
			        myObjectOutStream.writeObject(vocabulary);
			        myObjectOutStream.close();
			        myFileOutStream.close();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}	
		}
		
		return vocabulary;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @return CSN distance between two rulesetIds
	 */
	public static double getRulesetCSNDistance(final int rulesetId1, final int rulesetId2)
	{
		return getAllRulesetCSNDistances(rulesetId1).get(Integer.valueOf(rulesetId2)).doubleValue();
	}
	
	/**
	 * @return Map of rulesetId (key) to CSN distance (value) pairs, based on distance to specified rulesetId.
	 */
	public static Map<Integer, Double> getAllRulesetCSNDistances(final int rulesetId)
	{
		// Load ruleset distances from specific directory.
		final String distancesFilePath = ContextualSimilarity.rulesetContextualiserFilePath + rulesetId + ".csv";
		
		// Map of rulesetId (key) to CSN distance (value) pairs.
		final Map<Integer, Double> rulesetCSNDistances = new HashMap<>();	
		
		try (BufferedReader br = new BufferedReader(new FileReader(distancesFilePath))) 
		{
		    String line = br.readLine();	// column names
		    
		    while ((line = br.readLine()) != null) 
		    {
		        final String[] values = line.split(",");
		        rulesetCSNDistances.put(Integer.valueOf(Integer.parseInt(values[0])), Double.valueOf(Double.parseDouble(values[1])));
		    }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return rulesetCSNDistances;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @return Geo distance between two rulesetIds
	 */
	public static double getRulesetGeoDistance(final int rulesetId1, final int rulesetId2)
	{
		final Map<Integer, Double> geoSimilarities = getAllRulesetGeoDistances(rulesetId1);
		final Double geoSimilarity = geoSimilarities.get(Integer.valueOf(rulesetId2));
		return geoSimilarity != null ? geoSimilarity.doubleValue() : 0.0;
	}
	
	/**
	 * @return Map of rulesetId (key) to Geo distance (value) pairs, based on distance to specified rulesetId.
	 */
	public static Map<Integer, Double> getAllRulesetGeoDistances(final int rulesetId)
	{
		// Load ruleset distances from specific directory.
		final String distancesFilePath = "../Mining/res/recons/input/rulesetGeographicalDistances.csv";

		final Map<Integer, Double> rulesetGeoDistanceIds = new HashMap<>();	
		
		try (BufferedReader br = new BufferedReader(new FileReader(distancesFilePath))) 
		{
			br.readLine();		// Skip first line of column headers.
		    String line;
		    while ((line = br.readLine()) != null) 
		    {
		        final String[] values = line.split(",");
		        
		        if (Integer.valueOf(values[0]).intValue() != rulesetId)
		        	continue;
		        
		        final double similarity = Math.max((20000 - Double.valueOf(values[2]).doubleValue()) / 20000, 0);	// 20000km is the maximum possible distance
		        rulesetGeoDistanceIds.put(Integer.valueOf(values[1]), Double.valueOf(similarity));
		    }
		}
		catch (final Exception e)
		{
			System.out.println("Could not find similarity file, ruleset probably has no evidence.");
			e.printStackTrace();
		}
		
		return rulesetGeoDistanceIds;
	}
	
	//-----------------------------------------------------------------------------
	
	public static Map<String, Double> getGameDataset(final Dataset dataset, final Game game)
	{
		final Map<String, Double> datasetGame = dataset.getBagOfWords(game);
		
		// Convert the raw frequency counts for datasetA into probability distributions.
		double valueSum = 0.0;
		for (final Map.Entry<String, Double> entry : datasetGame.entrySet())
			valueSum += entry.getValue().doubleValue();
		for (final Map.Entry<String, Double> entry : datasetGame.entrySet())
			entry.setValue(Double.valueOf(entry.getValue().doubleValue()/valueSum));

		return datasetGame;
	}
	
	//-----------------------------------------------------------------------------
	
	public static final Map<String, Double> defaultVocabulary(final Dataset dataset, final Game gameA, final Game gameB)
	{
		final Map<String, Double> vocabulary = new HashMap<>();
		
		final Map<String, Double> datasetA = getGameDataset(dataset, gameA);
		final Map<String, Double> datasetB = getGameDataset(dataset, gameB);
		
		for (final String s : datasetA.keySet())
			vocabulary.put(s, Double.valueOf(1.0));
		for (final String s : datasetB.keySet())
			vocabulary.put(s, Double.valueOf(1.0));

		return vocabulary;
	}

}
