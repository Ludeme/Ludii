
package gameDistance.metrics.bagOfWords;

import java.util.Map;

import game.Game;
import gameDistance.DistanceUtils;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;

//-----------------------------------------------------------------------------

/**
 * @author Matthew.Stephenson, Markus
 * https://en.wikipedia.org/wiki/Cosine_similarity
 */
public class Cosine implements DistanceMetric
{	
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final Map<String, Double> datasetA = DistanceUtils.getGameDataset(dataset, gameA);
		final Map<String, Double> datasetB = DistanceUtils.getGameDataset(dataset, gameB);

		double nominator = 0.0;
		double denominatorA = 0.0;
		double denominatorB = 0.0;
		for (final String word : vocabulary.keySet())
		{
			double frqA = 0.0;
			double frqB = 0.0;
			
			if (datasetA.containsKey(word))
				frqA = datasetA.get(word).doubleValue() * vocabulary.get(word).doubleValue();
			if (datasetB.containsKey(word))
				frqB = datasetB.get(word).doubleValue() * vocabulary.get(word).doubleValue();
			
			nominator+=frqA*frqB;
			denominatorA+=frqA*frqA;
			denominatorB+=frqB*frqB;
		}
		final double denominator = Math.sqrt(denominatorA)*Math.sqrt(denominatorB);
		final double finalVal = 1-(nominator/denominator);

		return finalVal;
	}

}
