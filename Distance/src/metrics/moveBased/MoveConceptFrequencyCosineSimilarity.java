package metrics.moveBased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;

/**
 * Move based distance metric. Computes the frequencies of the move concepts for
 * each game, then returns 1 - cosine similarity of the frequencies.
 * 
 */
public class MoveConceptFrequencyCosineSimilarity implements DistanceMetric
{

	/**
	 * stores all move concept frequencies, s.t. they need to be loaded from the
	 * files only once
	 */
	private final HashMap<String, HashMap<Integer, Double>> moveConceptFrequenciesByGame;

	public MoveConceptFrequencyCosineSimilarity()
	{
		moveConceptFrequenciesByGame = new HashMap<>();
	}

	@Override
	public Score distance(Game gameA, Game gameB)
	{
		final HashMap<Integer, Double> frequenciesA = getMoveConceptFrequencies(gameA);
		final HashMap<Integer, Double> frequenciesB = getMoveConceptFrequencies(gameB);

		return new Score(1. - cosineSimilarity(frequenciesA, frequenciesB));
	}

	@Override
	public Score distance(LudRul gameA, LudRul gameB)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(Game gameA, List<Game> gameB, int numberTrials, int maxTurns, double thinkTime, String AIName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Computes the cosine similarity between frequencies of move concepts.
	 * 
	 * @param fA frequencies of move concepts of the game A
	 * @param fB frequencies of move concepts of the game B
	 * @return the cosine similarity between the concept frequencies, range 0...1
	 */
	private double cosineSimilarity(HashMap<Integer, Double> fA, HashMap<Integer, Double> fB)
	{
		final double nominator = dotProduct(fA, fB);
		final double denominator = calculateNorm(fA) * calculateNorm(fB);

		return nominator / denominator;
	}

	/**
	 * @param fA frequencies of move concepts of the game A
	 * @param fB frequencies of move concepts of the game B
	 * @return the dot product between the concept frequencies
	 */
	private double dotProduct(HashMap<Integer, Double> fA, HashMap<Integer, Double> fB)
	{
		double dotProduct = 0;

		for (final Entry<Integer, Double> entry : fA.entrySet())
		{
			dotProduct += entry.getValue() * fB.getOrDefault(entry.getKey(), 0.);
		}

		return dotProduct;
	}

	/**
	 * @param f frequencies of move concepts of a game
	 * @return the norm of the frequencies
	 */
	private double calculateNorm(HashMap<Integer, Double> f)
	{
		double norm = 0.;
		for (final Double d : f.values())
		{
			norm += Math.pow(d, 2);
		}

		return Math.sqrt(norm);
	}

	/**
	 * Returns the frequencies of all move concepts over all trials of the Game
	 * game. If those are not stored in the HashMap moveConceptFrequenciesByGame
	 * yet, they will be loaded from the .csv files.
	 * 
	 * Moves without any concept assigned to them will not be considered.
	 * 
	 * @param game the game to load the frequencies for
	 * @return HashMap<Integer, Double> containing the frequencies per move concept
	 */
	private HashMap<Integer, Double> getMoveConceptFrequencies(Game game)
	{
		if (moveConceptFrequenciesByGame.containsKey(game.name()))
		{
			return moveConceptFrequenciesByGame.get(game.name());
		}

		// if frequencies are not stored yet, load the move concepts and compute them
		final HashMap<Integer, Double> frequencies = new HashMap<>();
		try
		{
			final HashMap<Integer, Integer> conceptCounter = new HashMap<>();
			int n = 0; // counts overall moves in all trials, maybe needs to be replaced by long?

			final File folder = new File("log/" + game.name() + ".lud" + "/Random/");
			for (final File csv : folder.listFiles(f -> f.isFile() && f.getName().endsWith(".csv")))
			{
				final BufferedReader reader = new BufferedReader(new FileReader(csv));
				String line = "";
				while ((line = reader.readLine()) != null)
				{
					for (final String s : line.split(", "))
					{
						if (!s.isEmpty())
						{
							final int concept = Integer.parseInt(s);
							conceptCounter.put(concept, 1 + conceptCounter.getOrDefault(concept, 0));
						}
					}

					n++;
				}

				reader.close();
			}


			// compute frequencies
			for (final Entry<Integer, Integer> entry : conceptCounter.entrySet())
			{
				frequencies.put(entry.getKey(), (double) entry.getValue() / n);
				// square root of frequencies
//				frequencies.put(entry.getKey(), Math.sqrt((double) entry.getValue() / n));
				// log2 of frequencies
//				frequencies.put(entry.getKey(), Math.log(1. + (double) entry.getValue() / n) / Math.log(2));
			}

		} catch (final FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		moveConceptFrequenciesByGame.put(game.name(), frequencies);
		return frequencies;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(String expandedDescription1, String expandedDescription2)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
