
package gameDistance.metrics.sequence;

import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.DistanceUtils;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;

//-----------------------------------------------------------------------------

/**
 * Uses Smith Waterman Alignment, which is a local alignment of ludemes 
 * https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm
 * 
 * @author Matthew.Stephenson, Markus
 */
public class LocalAlignment implements DistanceMetric
{	
	
	
	//-----------------------------------------------------------------------------
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final List<String> gameAString = dataset.getSequence(gameA);
		final List<String> gameBString = dataset.getSequence(gameB);
		
		final String[] wordsA = gameAString.toArray(new String[0]);
		final String[] wordsB = gameBString.toArray(new String[0]);
	
		final double d = smithWatermanAlignment(wordsA, wordsB);
		final double maxCost = Math.max(wordsA.length, wordsB.length) * DistanceUtils.HIT_VALUE;
		final double finalScore = 1 - d / maxCost;
		
		return finalScore;
	}

	//-----------------------------------------------------------------------------
	
	// Smith Waterman Alignment
	// https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm
	private static int smithWatermanAlignment(final String[] wordsA, final String[] wordsB)
	{
		int maximumValue = 0;
		final int[][] distances = new int[wordsA.length + 1][wordsB.length + 1];
		for (int i = 1; i < distances.length; i++)
		{
			for (int j = 1; j < distances[0].length; j++)
			{
				final int valueFromLeft = DistanceUtils.GAP_PENALTY + distances[i - 1][j];
				final int valueFromTop = DistanceUtils.GAP_PENALTY + distances[i][j - 1];

				int valueFromTopLeft;
				if (wordsA[i - 1].equals(wordsB[j - 1]))
					valueFromTopLeft = DistanceUtils.HIT_VALUE + distances[i - 1][j - 1];
				else
					valueFromTopLeft = DistanceUtils.MISS_VALUE + distances[i - 1][j - 1];

				final int finalVal = Math.max(0, Math.max(valueFromTopLeft, Math.max(valueFromTop, valueFromLeft)));
				distances[i][j] = finalVal;

				if (finalVal > maximumValue)
					maximumValue = finalVal;
			}
		}
		
		return maximumValue;
	}
	
	//-----------------------------------------------------------------------------
	
}
