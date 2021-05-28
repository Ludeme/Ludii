
package metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.LudRul;
import common.Score;
import game.Game;

//-----------------------------------------------------------------------------

/**
 * Adaption of the bag of words distance matrix as done during the gameDistance
 * project. This method had originaly been implemented by Sofia.
 * 
 * Jensen Shannon Divergence ref: http://...
 * 
 * It evaluates the frequency of different words from .lud files.
 * 
 * Suggestion of expanding and remembering the word distribution in a map with
 * capacity having the expanded string as key, so the words wont have to be
 * counted every time
 * 
 * @author Sofia, Markus
 */
public class JensenShannonDivergence implements DistanceMetric
{
	public static final double LOG_2 = Math.log(2);

	@Override
	public Score
			distance(final LudRul candidate, final LudRul gameToCompareWith)
	{

		return distance(candidate.getDescriptionExpanded(),
				gameToCompareWith.getDescriptionExpanded());
	}

	public Score distance(
			final String gameADescriptionExpanded,
			final String gameBDescriptionExpanded
	)
	{
		final Map<String, Integer> frequencyA = getFrequencies(
				gameADescriptionExpanded);
		final Map<String, Integer> frequencyB = getFrequencies(
				gameBDescriptionExpanded);
		final TreeMap<String, Double> distributionA = frequencyToDistribution(
				frequencyA);
		final TreeMap<String, Double> distributionB = frequencyToDistribution(
				frequencyB);

		final double dist = jensenShannonDivergence(distributionA,
				distributionB);

		return new Score(dist);
	}
	// -------------------------------------------------------------------------

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{

		return distance(gameA.description().expanded(),
				gameB.description().expanded());

	}

	// -------------------------------------------------------------------------

	/**
	 * @param contentData ...
	 * @return String with just single words and no double spaces.
	 */
	public static String cleanString(final String contentData)
	{
		final String data = contentData;
		final String dataAlphabetic = data.replaceAll("[^A-Za-z0-9 ]", " ");

		// Maybe keep numbers, to ???
		// dataAlphabetic = data.replaceAll("[^A-Za-z ]"," ");

		final String dataSingleSpace = dataAlphabetic.trim().replaceAll(" +",
				" ");
		final String dataClean = dataSingleSpace.toLowerCase();

		return dataClean;
	}

	// -------------------------------------------------------------------------

	/**
	 * Cleans the string of special signs and then creates a HashMap of
	 * frequencies <word, count>
	 * 
	 * @param contentData ???
	 * @return A map of word frequency.
	 */
	public static Map<String, Integer> getFrequencies(final String contentData)
	{
		final String dataClean = cleanString(contentData);
		final String[] words = dataClean.split("\\s+");
		final Map<String, Integer> map = new HashMap<>();

		for (final String word : words)
		{
			Integer n = map.get(word);
			if (n == null)
				n = Integer.valueOf(1);
			else
				n = Integer.valueOf(n.intValue() + 1);
			
			// if word does not exists, set to one
			// otherwise increment counter
			map.put(word, n);
		}

		return map;
	}

	// -------------------------------------------------------------------------

	/**
	 * Transform word frequencies to word distributions
	 * 
	 * @param map
	 * @return A TreeMap of distributions of words in map.
	 */
	public static TreeMap<String, Double>
			frequencyToDistribution(final Map<String, Integer> map)
	{
		final TreeMap<String, Double> distributionNormalised = new TreeMap<String, Double>();

		double sum = 0.0;
		for (final Map.Entry<String, Integer> entry : map.entrySet())
		{
			final Integer value = entry.getValue();
			distributionNormalised.put(entry.getKey(), Double.valueOf(value.doubleValue()));
			sum += value.doubleValue();
		}

		for (final Map.Entry<String, Double> entry : distributionNormalised
				.entrySet()) {
			final Double d = Double.valueOf(entry.getValue().doubleValue() / sum);
			distributionNormalised.put(entry.getKey(), d);
		}
		return distributionNormalised;
	}

	// -------------------------------------------------------------------------

	/**
	 * Code inspired from:
	 * https://github.com/mimno/Mallet/blob/master/src/cc/mallet/util/Maths.java
	 * 
	 * Calculates the shannon divergence between both normalised word
	 * distributions Note: Taking the root would lead to shannonDistance
	 * 
	 * Pre: valA + valB will never add to 0. Markus guarantees this.
	 * 
	 * @param distributionA First distribution of words.
	 * @param distributionB Second distribution of words.
	 * 
	 * @return The distance between these distributions.
	 */
	public static double jensenShannonDivergence(
			final TreeMap<String, Double> distributionA,
			final TreeMap<String, Double> distributionB
	)
	{
		final Set<String> vocabulary = new HashSet<>(distributionA.keySet());
		vocabulary.addAll(distributionB.keySet());

		double klDiv1 = 0.0;
		double klDiv2 = 0.0;

		for (final String word : vocabulary)
		{
			Double valA = distributionA.get(word);
			Double valB = distributionB.get(word);

			if (valA == null)
				valA = Double.valueOf(0.0);
			
			if (valB == null)
				valB = Double.valueOf(0.0);

			final double avg = (valA.doubleValue() + valB.doubleValue()) / 2.0;
			assert (avg != 0.0);

			if (valA.doubleValue() != 0.0)
				klDiv1 += valA.doubleValue() * Math.log(valA.doubleValue() / avg);

			if (valB.doubleValue() != 0.0)
				klDiv2 += valB.doubleValue() * Math.log(valB.doubleValue() / avg);
		}
		final double jensonsShannonDivergence = (klDiv1 + klDiv2) / 2.0 / LOG_2;
		return jensonsShannonDivergence;
	}

	@Override
	public Score distance
	(
		final Game gameA, final List<Game> gameB, final int numberTrials,
		final int maxTurns, final double thinkTime, final String AIName
	)
	{
		return null;
	}

}
