
package gameDistance.metrics.sequence;

import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;
import gameDistance.utils.DistanceUtils;

//-----------------------------------------------------------------------------

/**
 * Uses repeated Smith Waterman Alignment, which is a local alignment of ludemes 
 * https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm
 * 
 * @author Matthew.Stephenson, Markus
 */
public class RepeatedLocalAlignment implements DistanceMetric
{	
	
	//-----------------------------------------------------------------------------
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final List<String> gameAString = dataset.getSequence(gameA);
		final List<String> gameBString = dataset.getSequence(gameB);
		
		final String[] wordsA = gameAString.toArray(new String[0]);
		final String[] wordsB = gameBString.toArray(new String[0]);
		
		final double d = repeatedSmithWatermanAlignment(wordsA, wordsB, 0);
		final double maxCost = Math.max(wordsA.length, wordsB.length) * DistanceUtils.HIT_VALUE;
		final double finalScore = 1 - d / maxCost;

		return finalScore;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Find the maximum value and then backtrack along the biggest numbers to 0.
	 */
	private int repeatedSmithWatermanAlignment(final String[] wordsA, final String[] wordsB, final int score)
	{
		if (wordsA.length==0 || wordsB.length==0) 
			return score;
		
		int maximumValue = -1;
		int maximumI = 0;
		int maximumJ = 0;
		
		int[][] scoreMat = new int[wordsA.length + 1][wordsB.length + 1];
		for (int i = 1; i < scoreMat.length; i++)
		{
			for (int j = 1; j < scoreMat[0].length; j++)
			{
				final int valueFromLeft = DistanceUtils.GAP_PENALTY + scoreMat[i-1][j];
				final int valueFromTop = DistanceUtils.GAP_PENALTY + scoreMat[i][j-1];

				int valueFromTopLeft;
				if (wordsA[i - 1].equals(wordsB[j - 1]))
					valueFromTopLeft = DistanceUtils.HIT_VALUE + scoreMat[i-1][j-1];
				else
					valueFromTopLeft = DistanceUtils.MISS_VALUE + scoreMat[i-1][j-1];

				final int finalVal = Math.max(0, Math.max(valueFromTopLeft, Math.max(valueFromTop, valueFromLeft)));
				scoreMat[i][j] = finalVal;

				if (finalVal > maximumValue) 
				{
					maximumValue = finalVal;
					maximumI = i;
					maximumJ = j;
				}		
			}
		}
		final int[] ij = findStartIJfromAllignmentMatrix(scoreMat,maximumI,maximumJ);
		
		if(maximumValue < DistanceUtils.HIT_VALUE*3)
			return score + maximumValue;
		
		if (ij[0] == maximumI || ij[1] == maximumJ)
			return score;

		scoreMat = null;
		final String[] wordsACut = cutAwayAlligned(wordsA,ij[0]-1, maximumI-1);
		final String[] wordsBCut = cutAwayAlligned(wordsB,ij[1]-1, maximumJ-1);
		
		return repeatedSmithWatermanAlignment(wordsACut, wordsBCut, score+maximumValue);
	}

	//-----------------------------------------------------------------------------
	
	/**
	 * Travel along the maximum values starting from this cell.
	 */
	private int[] findStartIJfromAllignmentMatrix(final int[][] scoreMat, final int i, final int j)
	{
		int nextI;
		int nextJ;
		int maxProgenitor;
		
		final int leftUp = scoreMat[i-1][j-1];
		maxProgenitor = leftUp;
		nextI = i-1;
		nextJ = j-1;
		
		final int up = scoreMat[i][j-1];
		if (up > maxProgenitor) 
		{
			maxProgenitor = up;
			nextI = i;
			nextJ = j-1;
		}
		
		final int left = scoreMat[i-1][j];
		if (left > maxProgenitor) 
		{
			maxProgenitor = left;
			nextI = i-1;
			nextJ = j;
		}
			
		if (maxProgenitor == 0)
			return new int[] {i,j};
		else
			return findStartIJfromAllignmentMatrix(scoreMat, nextI,nextJ);	
	}
	
	//-----------------------------------------------------------------------------
	
	private static String[] cutAwayAlligned(final String[] wordsA, final int minI,  final int maximumI)
	{
		final int firstLenght = minI;
		final int tailLength= wordsA.length-maximumI-1;
		final int newLength = minI+tailLength;
		final String[] cutted = new String[newLength];
		
		System.arraycopy(wordsA, 0, cutted, 0, firstLenght);
	    System.arraycopy(wordsA, maximumI+1,cutted, firstLenght, tailLength);
		return cutted;
	}
	
	//-----------------------------------------------------------------------------
	
}
