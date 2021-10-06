
package metrics.individual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.CountMap;
import common.LudRul;
import common.Score;
import common.StringCleaner;
import game.Game;
import metrics.DistanceMetric;

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
	Map<String,Map<String, Integer>> allFrequencies = new HashMap<>();
	
	@Override
	public String getName()
	{
		return "JensenShannonDivergence";
	}
	
	@Override
	public Score
			distance(final LudRul candidate, final LudRul gameToCompareWith)
	{

		return distance(candidate.getDescriptionExpanded(),
				gameToCompareWith.getDescriptionExpanded());
	}

	@Override
	public Score distance(
			final String gameADescriptionExpanded,
			final String gameBDescriptionExpanded
	)
	{
		Map<String, Integer> frequencyA = allFrequencies.get(gameADescriptionExpanded);
		if (frequencyA==null) {
			frequencyA = JensenShannonDivergence.getFrequencies(gameADescriptionExpanded);
			allFrequencies.put(gameADescriptionExpanded, frequencyA);
		}
			
		Map<String, Integer> frequencyB = allFrequencies.get(gameBDescriptionExpanded);
		if (frequencyB==null) {
			frequencyB = JensenShannonDivergence.getFrequencies(gameBDescriptionExpanded);
			allFrequencies.put(gameBDescriptionExpanded, frequencyB);
		}
		
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
	 * Cleans the string of special signs and then creates a HashMap of
	 * frequencies <word, count>
	 * 
	 * @param contentData ???
	 * @return A map of word frequency.
	 */
	public static Map<String, Integer> getFrequencies(final String contentData)
	{
		final StringCleaner sc = StringCleaner.cleanAll;
		final String[] words = sc.cleanAndSplit(contentData);
		final CountMap<String> cm = new CountMap<>();
		cm.countUnique(words);

		return cm.getHashMap();
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
	public Score distance(
			final Game gameA, final List<Game> gameB, final int numberTrials,
			final int maxTurns, final double thinkTime, final String AIName
	)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new JensenShannonDivergence();
	}

}
