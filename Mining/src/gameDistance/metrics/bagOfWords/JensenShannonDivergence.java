
package gameDistance.metrics.bagOfWords;

import java.util.Map;

import game.Game;
import gameDistance.DistanceUtils;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;

//-----------------------------------------------------------------------------

/**
 * https://en.wikipedia.org/wiki/Jensen%E2%80%93Shannon_divergence
 * 
 * @author Matthew.Stephenson, Sofia, Markus
 */
public class JensenShannonDivergence implements DistanceMetric
{

	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final Map<String, Double> datasetA = DistanceUtils.getGameDataset(dataset, gameA);
		final Map<String, Double> datasetB = DistanceUtils.getGameDataset(dataset, gameB);

		double klDiv1 = 0.0;
		double klDiv2 = 0.0;

		for (final String word : vocabulary.keySet())
		{
			double valA = 0.0;
			double valB = 0.0;
			
			if (datasetA.containsKey(word))
				valA = datasetA.get(word).doubleValue() * vocabulary.get(word).doubleValue();
			if (datasetB.containsKey(word))
				valB = datasetB.get(word).doubleValue() * vocabulary.get(word).doubleValue();

			final double avg = (valA + valB) / 2.0;
			assert (avg != 0.0);

			if (valA != 0.0)
				klDiv1 += valA * Math.log(valA / avg);

			if (valB != 0.0)
				klDiv2 += valB * Math.log(valB / avg);
		}
		
		final double jensonsShannonDivergence = (klDiv1 + klDiv2) / 2.0 / Math.log(2);
		
		return jensonsShannonDivergence;
	}

}
