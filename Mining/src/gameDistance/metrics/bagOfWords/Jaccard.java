
package gameDistance.metrics.bagOfWords;

import java.util.Map;

import game.Game;
import gameDistance.DistanceUtils;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;

//-----------------------------------------------------------------------------

/**
 * https://en.wikipedia.org/wiki/Jaccard_index
 * 
 * @author Matthew.Stephenson, Markus
 */
public class Jaccard implements DistanceMetric
{	
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final Map<String, Double> datasetA = DistanceUtils.getGameDataset(dataset, gameA);
		final Map<String, Double> datasetB = DistanceUtils.getGameDataset(dataset, gameB);

		double nominator = 0.0;
		double denominator = 0.0;
		for (final String word : vocabulary.keySet())
		{
			double frqA = 0.0;
			double frqB = 0.0;
			
			if (datasetA.containsKey(word))
				frqA = datasetA.get(word).doubleValue() * vocabulary.get(word).doubleValue();
			if (datasetB.containsKey(word))
				frqB = datasetB.get(word).doubleValue() * vocabulary.get(word).doubleValue();
			
			nominator += Math.min(frqA,frqB);
			denominator += Math.max(frqA, frqB);
		}
		final double finalVal = 1-(nominator/denominator);

		return finalVal;
	}
	
}
