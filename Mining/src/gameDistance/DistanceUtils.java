package gameDistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import main.FileHandling;
import main.options.Ruleset;
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
	
	// vocabulary store paths.
	private final static String vocabularyStorePath = "res/gameDistance/vocabulary/";
	
	//-----------------------------------------------------------------------------
	
	public static Map<String, Double> fullVocabulary(final Dataset dataset, final String datasetName, final boolean overrideStoredVocabularies)
	{
		final File vocabularyFile = new File(vocabularyStorePath + datasetName + ".txt");
		
		// Recover vocabulary from previously stored txt file.
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
		
		double numGames = 0.0;
		final Map<String, Double> vocabulary = new HashMap<>();
		final String[] choices = FileHandling.listGames();
		
		for (String gameName : choices)
		{
			if (!FileHandling.shouldIgnoreLudAnalysis(gameName))
			{
				gameName = gameName.split("\\/")[gameName.split("\\/").length-1];
				final Game tempGame = GameLoader.loadGameFromName(gameName);
				final List<Ruleset> rulesets = tempGame.description().rulesets();
				
				if (rulesets != null && !rulesets.isEmpty())
				{
					for (int rs = 0; rs < rulesets.size(); rs++)
						if (!rulesets.get(rs).optionSettings().isEmpty())
						{
							final Game rulesetGame = GameLoader.loadGameFromName(gameName, rulesets.get(rs).optionSettings());
							for (final String s : dataset.getBagOfWords(rulesetGame).keySet())
							{
								if (vocabulary.containsKey(s))
									vocabulary.put(s, Double.valueOf(vocabulary.get(s).doubleValue() + 1.0));
								else
									vocabulary.put(s, Double.valueOf(1.0));
							}
							
							numGames++;
						}
				}
				else
				{
					for (final String s : dataset.getBagOfWords(tempGame).keySet())
					{
						if (vocabulary.containsKey(s))
							vocabulary.put(s, Double.valueOf(vocabulary.get(s).doubleValue() + 1.0));
						else
							vocabulary.put(s, Double.valueOf(1.0));
					}
					
					numGames++;
				}
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

	//-----------------------------------------------------------------------------
	
}
