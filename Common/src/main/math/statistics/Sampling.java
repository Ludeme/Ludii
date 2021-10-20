package main.math.statistics;

import java.util.concurrent.ThreadLocalRandom;

import gnu.trove.list.array.TDoubleArrayList;

/**
 * Some utilities for sampling.
 * 
 * @author Dennis Soemers
 */
public class Sampling
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private Sampling()
	{
		// Don't need constructor
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param sampleSize Number of samples to take
	 * @param list List to sample from
	 * @return New list of samples, sampled from given list with replacement
	 */
	public static TDoubleArrayList sampleWithReplacement(final int sampleSize, final TDoubleArrayList list)
	{
		final TDoubleArrayList samples = new TDoubleArrayList(sampleSize);
		
		for (int i = 0; i < sampleSize; ++i)
		{
			samples.add(list.getQuick(ThreadLocalRandom.current().nextInt(list.size())));
		}
		
		return samples;
	}
	
	//-------------------------------------------------------------------------

}
