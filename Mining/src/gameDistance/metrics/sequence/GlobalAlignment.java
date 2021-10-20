
package gameDistance.metrics.sequence;

import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.DistanceUtils;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;

//-----------------------------------------------------------------------------

/**
 * Uses the NeedlemanWunsch algorithm to align the ludemes.
 * https://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithm
 * 
 * @author Matthew.Stephenson, Markus
 */
public class GlobalAlignment implements DistanceMetric
{	
	
	//-----------------------------------------------------------------------------
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final List<String> gameAString = dataset.getSequence(gameA);
		final List<String> gameBString = dataset.getSequence(gameB);
		
		final String[] wordsA = gameAString.toArray(new String[0]);
		final String[] wordsB = gameBString.toArray(new String[0]);
	
		final double d = needlemanWunshAllignment(wordsA, wordsB);
		final double finalScore = 1.0-(d / Math.max(wordsA.length, wordsB.length)/ maxValue());
		return finalScore;
	}
	
	//-----------------------------------------------------------------------------
	
	private static double needlemanWunshAllignment(final String[] wordsA, final String[] wordsB)
	{
		int maximumValue = 0;
		final int[][] distances = new int[wordsA.length + 1][wordsB.length + 1];
		for (int i = 0; i < distances.length; i++)
			distances[i][0] = i * DistanceUtils.GAP_PENALTY;
		for (int i = 0; i < distances[0].length; i++)
			distances[0][i] = i * DistanceUtils.GAP_PENALTY;

		for (int i = 1; i < distances.length; i++)
		{
			for (int j = 1; j < distances[0].length; j++)
			{
				final int valueFromLeft = DistanceUtils.GAP_PENALTY + distances[i-1][j];
				final int valueFromTop = DistanceUtils.GAP_PENALTY + distances[i][j-1];

				int valueFromTopLeft;
				if (wordsA[i-1].equals(wordsB[j-1]))
					valueFromTopLeft = DistanceUtils.HIT_VALUE + distances[i-1][j-1];
				else
					valueFromTopLeft = DistanceUtils.MISS_VALUE + distances[i-1][j-1];

				final int finalVal = Math.max(valueFromTopLeft, Math.max(valueFromTop, valueFromLeft));
				distances[i][j] = finalVal;

				if (finalVal > maximumValue)
					maximumValue = finalVal;
			}
		}
		
		return distances[distances.length - 1][distances[0].length - 1];
	}
	
	//-----------------------------------------------------------------------------
	
	private static int maxValue()
	{
		final int first = Math.max(Math.abs(DistanceUtils.HIT_VALUE), Math.abs(DistanceUtils.GAP_PENALTY));
		final int second = Math.max(first, Math.abs(DistanceUtils.MISS_VALUE));
		return second;
	}
	
	//-----------------------------------------------------------------------------

}
