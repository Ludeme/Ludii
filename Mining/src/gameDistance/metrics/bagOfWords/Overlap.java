
package gameDistance.metrics.bagOfWords;

import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;
import main.Constants;

//-----------------------------------------------------------------------------

/**
 * Percentage of overlapping entries
 * 
 * @author Matthew.Stephenson
 */
public class Overlap implements DistanceMetric
{	
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final Map<String, Double> datasetA = dataset.getBagOfWords(gameA);
		final Map<String, Double> datasetB = dataset.getBagOfWords(gameB);

		double nominator = 0.0;
		final double denominator = vocabulary.keySet().size();
		for (final String word : vocabulary.keySet())
		{
			boolean existsA = false;
			boolean existsB = false;
			
			if (datasetA.containsKey(word))
				existsA = datasetA.get(word).doubleValue() > 0.5;
			if (datasetB.containsKey(word))
				existsB = datasetB.get(word).doubleValue() > 0.5;
			
			if (existsA == existsB)
				nominator += 1;	
		}
		final double finalVal = 1-(nominator/denominator);
		
		// Handle floating point imprecision
		if (finalVal < Constants.EPSILON)
			return 0;

		return finalVal;
	}

}
