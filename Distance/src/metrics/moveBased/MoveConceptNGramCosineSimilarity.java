package metrics.moveBased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;

public class MoveConceptNGramCosineSimilarity implements DistanceMetric
{

	/**
	 * stores move concept n-grams
	 */
//	private HashMap<String, ArrayList<ArrayList<BitSet>>> moveConceptNGramsByGame;
	/**
	 * stores frequencies of move concept n-grams per game each n-gram (n
	 * consecutive BitSets) will be hashed into an Integer
	 */
	private final HashMap<String, HashMap<Integer, Double>> moveConceptNGramFrequenciesByGame;

	private final int n;

	/**
	 * 
	 * @param n length of the n-grams
	 */
	public MoveConceptNGramCosineSimilarity(int n)
	{
		moveConceptNGramFrequenciesByGame = new HashMap<>();
		this.n = n;
	}

	@Override
	public Score distance(Game gameA, Game gameB)
	{
		final HashMap<Integer, Double> nGramFrequenciesA = getNGrams(gameA);
		final HashMap<Integer, Double> nGramFrequenciesB = getNGrams(gameB);

		return new Score(1. - cosineSimilarity(nGramFrequenciesA, nGramFrequenciesB));
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
	 * Computes the cosine similarity between frequencies of move concept n-gram
	 * frequencies.
	 * 
	 * @param fA frequencies of move concept n-grams of the game A
	 * @param fB frequencies of move concept n-grams of the game B
	 * @return the cosine similarity between the concept frequencies, range 0...1
	 */
	private double cosineSimilarity(HashMap<Integer, Double> fA, HashMap<Integer, Double> fB)
	{
		final double nominator = dotProduct(fA, fB);
		final double denominator = calculateNorm(fA) * calculateNorm(fB);

		return nominator / denominator;
	}

	/**
	 * @param fA frequencies of move concept n-grams of the game A
	 * @param fB frequencies of move concept n-grams of the game B
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
	 * @param f frequencies of move concept n-grams of a game
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

	private HashMap<Integer, Double> getNGrams(Game game)
	{
		if (moveConceptNGramFrequenciesByGame.containsKey(game.name()))
		{
			return moveConceptNGramFrequenciesByGame.get(game.name());
		}

		// if ngrams of move concepts are not stored yet, load the trials and compute
		// ngrams
		final HashMap<Integer, Double> frequencies = new HashMap<>();
		try
		{
			final HashMap<Integer, Integer> nGramCounter = new HashMap<>();
			int count = 0;

			final File folder = new File("log/" + game.name() + ".lud" + "/Random/");
			for (final File csv : folder.listFiles(f -> f.isFile() && f.getName().endsWith(".csv")))
			{
				// store n consecutive moves concepts
				final List<BitSet> moveConceptNGram = new LinkedList<>();
				int i = 0;

				// in many games a substantial part of moves have no move concepts assigned
				// this initially led to distance estimates, which were too small
				// hence, we skip n-grams without concepts here
				int consecutivelyEmpty = 0;

				final BufferedReader reader = new BufferedReader(new FileReader(csv));
				String line = "";
				while ((line = reader.readLine()) != null)
				{
					if (line.isEmpty())
					{
						consecutivelyEmpty++;
					} else
					{
						consecutivelyEmpty = 0;
					}
					// skip if n-gram of concepts will be empty
					if (consecutivelyEmpty >= n)
					{
						continue;
					}

					// only keep n BitSets in List
					if (i >= n)
					{
						moveConceptNGram.remove(0);
					}

					final BitSet moveConcepts = new BitSet();
					for (final String s : line.split(", "))
					{
						if (!s.isEmpty())
						{
							moveConcepts.set(Integer.parseInt(s));
						}
					}
					moveConceptNGram.add(moveConcepts);

					i++;

					if (i >= n)
					{
						final int hash = moveConceptNGram.hashCode();
						nGramCounter.put(hash, 1 + nGramCounter.getOrDefault(hash, 0));

						count++;
					}
				}
			}

//			System.out.println("Count: " + count);
			// compute frequencies of n-grams
			for (final Entry<Integer, Integer> entry : nGramCounter.entrySet())
			{
				frequencies.put(entry.getKey(), (double) entry.getValue() / count);
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

		moveConceptNGramFrequenciesByGame.put(game.name(), frequencies);
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
