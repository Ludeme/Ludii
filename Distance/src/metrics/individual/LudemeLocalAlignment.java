
package metrics.individual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;
import metrics.support.EditCost;

//-----------------------------------------------------------------------------

/**
 * Uses Smith Waterman Alignment, which is a local alignment of ludemes 
 * 
 * @author Markus
 */
public class LudemeLocalAlignment implements DistanceMetric
{
	public static final double LOG_2 = Math.log(2);
	final EditCost ec;
	
	public LudemeLocalAlignment(final EditCost thisec)
	{
		ec = thisec;
	}
	
	public LudemeLocalAlignment()
	{
		this(new EditCost());
	}

	// -------------------------------------------------------------------------

	@Override
	public String getName()
	{
		return "LudemeLocalAlignment" + "_" + ec.hit() +"_" + ec.miss() + "_"+ ec.gapPenalty();
	}
	
	@Override
	public Score distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		return distance(candidate.getDescriptionExpanded(), gameToCompareWith.getDescriptionExpanded());
	}

	@Override
	public Score distance(final String candidate, final String gameToCompareWith)
	{
		final String dataCleanA = cleanString(candidate);
		final String[] wordsA = dataCleanA.split("\\s+");
		final String dataCleanB = cleanString(gameToCompareWith);
		final String[] wordsB = dataCleanB.split("\\s+");

		
		final double d = smithWatermanAlignment(wordsA, wordsB, ec);
		final double maxCost = Math.max(wordsA.length, wordsB.length) * ec.hit();
		final double finalScore = 1 - d / maxCost;

		return new Score(finalScore);
	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{

		return distance(gameA.description().expanded(), gameB.description().expanded());
	}

	// -------------------------------------------------------------------------

	/**
	 * Determines the matching score, when align the actions of two trials using the Smith
	 * Waterman Algorithm
	 * {@link "https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm"}
	 * 
	 * @param wordsA
	 * @param wordsB
	 * @param editCost
	 * @return
	 */
	private static int smithWatermanAlignment(final String[] wordsA, final String[] wordsB, final EditCost editCost)
	{

		
		int maximumValue = 0;
		final int[][] distances = new int[wordsA.length + 1][wordsB.length + 1];
		for (int i = 1; i < distances.length; i++)
		{
			for (int j = 1; j < distances[0].length; j++)
			{
				final int valueFromLeft = editCost.gapPenalty() + distances[i - 1][j];
				final int valueFromTop = editCost.gapPenalty() + distances[i][j - 1];

				int valueFromTopLeft;
				if (wordsA[i - 1].equals(wordsB[j - 1]))
					valueFromTopLeft = editCost.hit() + distances[i - 1][j - 1];
				else
					valueFromTopLeft = editCost.miss() + distances[i - 1][j - 1];

				final int finalVal = Math.max(0, Math.max(valueFromTopLeft, Math.max(valueFromTop, valueFromLeft)));
				distances[i][j] = finalVal;

				if (finalVal > maximumValue)
					maximumValue = finalVal;
			}
		}
		// DistanceMatrix.printOutAllignmentMatrix(wordsA,wordsB,distances,"allignment.csv");

		return maximumValue;

	}

	/**
	 * @param contentData ...
	 * @return String with just single words and no double spaces.
	 */
	public String cleanString(final String contentData)
	{
		final String data = contentData;
		final String dataAlphabetic = data.replaceAll("[^A-Za-z0-9 ]", " ");

		// Maybe keep numbers, to ???
		// dataAlphabetic = data.replaceAll("[^A-Za-z ]"," ");

		final String dataSingleSpace = dataAlphabetic.trim().replaceAll(" +", " ");
		final String dataClean = dataSingleSpace.toLowerCase();

		return dataClean;
	}

	// -------------------------------------------------------------------------

	/**
	 * Cleans the string of special signs and then creates a HashMap of frequencies
	 * <word, count>
	 * 
	 * @param contentData ???
	 * @return A map of word frequency.
	 */
	public Map<String, Integer> getFrequencies(final String contentData)
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
				n = Integer.valueOf(n.intValue() + 1); // if word does not exists, set to one otherwise increment
														// counter
			
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
	public static TreeMap<String, Double> frequencyToDistribution(final Map<String, Integer> map)
	{
		final TreeMap<String, Double> distributionNormalised = new TreeMap<String, Double>();

		double sum = 0.0;
		for (final Map.Entry<String, Integer> entry : map.entrySet())
		{
			final Integer value = entry.getValue();
			distributionNormalised.put(entry.getKey(), Double.valueOf(value.doubleValue()));
			sum += value.doubleValue();
		}

		for (final Map.Entry<String, Double> entry : distributionNormalised.entrySet())
			distributionNormalised.put(entry.getKey(), Double.valueOf(entry.getValue().doubleValue() / sum));

		return distributionNormalised;
	}

	// -------------------------------------------------------------------------

	/**
	 * Code inspired from:
	 * https://github.com/mimno/Mallet/blob/master/src/cc/mallet/util/Maths.java
	 * 
	 * Calculates the shannon divergence between both normalised word distributions
	 * Note: Taking the root would lead to shannonDistance
	 * 
	 * Pre: valA + valB will never add to 0. Markus guarantees this.
	 * 
	 * @param distributionA First distribution of words.
	 * @param distributionB Second distribution of words.
	 * 
	 * @return The distance between these distributions.
	 */
	public static double jensenShannonDivergence(
			final TreeMap<String, Double> distributionA, final TreeMap<String, Double> distributionB
	)
	{
		final Set<String> vocabulary = new HashSet<String>(distributionA.keySet());
		vocabulary.addAll(distributionB.keySet());

		double klDiv1 = 0.0;
		double klDiv2 = 0.0;

		for (final String word : vocabulary)
		{
			final Double valARet = distributionA.get(word);
			final Double valBRet = distributionB.get(word);
			double valA = 0.0;
			double valB = 0.0;
			
			if (valARet != null)
				valA = valARet.doubleValue();

			if (valBRet != null)
				valB = valBRet.doubleValue();

			final double avg = (valA + valB) / 2.0;
			assert (avg != 0.0);

			if (valA != 0.0)
				klDiv1 += valA * Math.log(valA / avg);

			if (valB != 0.0)
				klDiv2 += valB * Math.log(valB / avg);
		}
		final double jensonsShannonDivergence = (klDiv1 + klDiv2) / 2.0 / LOG_2;
		return jensonsShannonDivergence;
	}

	@Override
	public Score distance(final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns, final double thinkTime, final String AIName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new LudemeLocalAlignment();
	}
	
	@Override
	public boolean hasUserSelectionDialog() {
		return true;
	}
	
	@Override
	public DistanceMetric showUserSelectionDialog() {
		final EditCost thisec = EditCost.showUserSelectionDialog();
		return new LudemeLocalAlignment(thisec);
		
	}

}
