package main.math.statistics;

import gnu.trove.list.array.TDoubleArrayList;

/**
 * Code to compute Kolmogorov-Smirnov statistics for pairs of
 * empirical distributions.
 * 
 * @author Dennis Soemers
 */
public class KolmogorovSmirnov 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private KolmogorovSmirnov()
	{
		// Don't need constructor
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param distA
	 * @param distB
	 * @return Kolmogorov-Smirnov statistic (or "distance") for two given empirical distributions
	 */
	public static double kolmogorovSmirnovStatistic(final TDoubleArrayList distA, final TDoubleArrayList distB)
	{
		// Sort both distributions
		final TDoubleArrayList sortedA = new TDoubleArrayList(distA);
		sortedA.sort();
		final TDoubleArrayList sortedB = new TDoubleArrayList(distB);
		sortedB.sort();
		
		// Loop through both distribution simultaneously and find point of maximum deviation
		final int sampleSizeA = sortedA.size();
		final int sampleSizeB = sortedB.size();
		
		int currIdxA = 0;
		int currIdxB = 0;
		double cumulProbA = 0.0;
		double cumulProbB = 0.0;
		
		double maxDeviation = 0.0;
		
		while (currIdxA < sampleSizeA || currIdxB < sampleSizeB)
		{
			double valA = (currIdxA == sampleSizeA) ? Double.POSITIVE_INFINITY : sortedA.getQuick(currIdxA);
			double valB = (currIdxB == sampleSizeB) ? Double.POSITIVE_INFINITY : sortedB.getQuick(currIdxB);
			
			final double currVal = Math.min(valA, valB);
			if (Double.isInfinite(currVal))
				break;		// Should never happen
			
			while (valA <= currVal)
			{
				cumulProbA += 1.0 / sampleSizeA;
				++currIdxA;
				valA = sortedA.getQuick(currIdxA);
			}
			
			while (valB <= currVal)
			{
				cumulProbB += 1.0 / sampleSizeB;
				++currIdxB;
				valB = sortedB.getQuick(currIdxB);
			}
			
			final double deviation = Math.abs(cumulProbA - cumulProbB);
			if (deviation > maxDeviation)
				maxDeviation = deviation;
		}
		
		return maxDeviation;
	}
	
	//-------------------------------------------------------------------------

}
