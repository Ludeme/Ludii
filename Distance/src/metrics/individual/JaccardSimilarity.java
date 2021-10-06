package metrics.individual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;

/**
 * Uses Jaccard Similarity to determine the distance
 * @author Markus
 *
 */
public class JaccardSimilarity implements DistanceMetric
{
	Map<String, Map<String, Integer>> allFrequencies = new HashMap<>();

	@Override
	public String getName()
	{
		return "JaccardSimilarity";
	}
	
	@Override
	public void releaseResources()
	{
		allFrequencies.clear();
	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		return distance(gameA.description().expanded(),
				gameB.description().expanded());
	}

	@Override
	public Score distance(final LudRul gameA, final LudRul gameB)
	{
		return distance(gameA.getDescriptionExpanded(),
				gameB.getDescriptionExpanded());
	}

	/**
	 * 
	 * @param gameADescriptionExpanded
	 * @param gameBDescriptionExpanded
	 * @return the distance between both game descriptions
	 */
	@Override
	public Score distance(
			final String gameADescriptionExpanded,
			final String gameBDescriptionExpanded
	)
	{
		Map<String, Integer> frequencyA = allFrequencies
				.get(gameADescriptionExpanded);
		if (frequencyA == null)
		{
			frequencyA = JensenShannonDivergence
					.getFrequencies(gameADescriptionExpanded);
			allFrequencies.put(gameADescriptionExpanded, frequencyA);
		}

		Map<String, Integer> frequencyB = allFrequencies
				.get(gameBDescriptionExpanded);
		if (frequencyB == null)
		{
			frequencyB = JensenShannonDivergence
					.getFrequencies(gameBDescriptionExpanded);
			allFrequencies.put(gameBDescriptionExpanded, frequencyB);
		}
		final Set<String> vocabulary = new HashSet<>(frequencyA.keySet());
		vocabulary.addAll(frequencyB.keySet());
		double nominator = 0.0;
		double denominatorA = 0.0;

		// jaccardcofficent
		for (final String word : vocabulary)
		{
			final Integer frqARet = frequencyA.get(word);
			final Integer frqBRet = frequencyB.get(word);
			int frqA = 0;
			int frqB = 0;
			if (frqARet != null)
				frqA = frqARet.intValue();
			if (frqBRet != null)
				frqB = frqBRet.intValue();
			nominator += Math.min(frqA,frqB);
			denominatorA += Math.max(frqA, frqB);
			
		}
		final double denominator = denominatorA;
		final double finalVal = 1- (nominator / denominator);

		return new Score(finalVal);
	}

	@Override
	public Score distance(
			final Game gameA, final List<Game> gameB, final int numberTrials,
			final int maxTurns,
			final double thinkTime, final String AIName
	)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new JaccardSimilarity();
	}
}