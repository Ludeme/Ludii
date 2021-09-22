package main.math;

import main.Constants;

/**
 * Object that can compute some statistics of data incrementally (online). In
 * contrast to our Stats class, this one does not have to retain all observations
 * in memory (and can more quickly return aggregates because it no longer requires
 * any loops through all observations).
 * 
 * Uses algorithm found by Welford, as described here: 
 * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
 * 
 * @author Dennis Soemers
 *
 */
public class IncrementalStats 
{
	
	//-------------------------------------------------------------------------
	
	/** Number of observations */
	private int n;
	
	/** Mean of observations */
	private double mean;
	
	/** Sum of squared differences from mean */
	private double sumSquaredDifferences;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public IncrementalStats()
	{
		n = 0;
		mean = 0.0;
		sumSquaredDifferences = 0.0;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other
	 */
	public IncrementalStats(final IncrementalStats other)
	{
		n = other.getNumObservations();
		mean = other.getMean();
		sumSquaredDifferences = other.getSumSquaredDifferences();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the mean of all observations so far
	 * 
	 * @return
	 */
	public double getMean()
	{
		return mean;
	}
	
	/**
	 * Returns the number of observations processed so far
	 * 
	 * @return
	 */
	public int getNumObservations()
	{
		return n;
	}
	
	/**
	 * @return Sample standard deviation (biased if n = 1)
	 */
	public double getStd()
	{
		return Math.sqrt(getVariance());
	}
	
	/**
	 * @return Sum of squared differences
	 */
	public double getSumSquaredDifferences()
	{
		return sumSquaredDifferences;
	}
	
	/**
	 * Returns the sample variance, except for the case where that would be exactly 0.0.
	 * 
	 * In that case, it instead returns a value that decreases as we get more observations
	 * 
	 * @return
	 */
	public double getNonZeroVariance()
	{
		if (sumSquaredDifferences == 0.0)
		{
			// We probably don't really want a variance of 0, so instead return something
			// that decreases as we get more observations
			//System.out.println("returning 1 / (n + epsilon) = " + (1.0 / (n + Globals.EPSILON)));
			return 1.0 / (n + Constants.EPSILON);
		}
		
		return getVariance();	
	}
	
	/**
	 * Returns the sample variance of all observations so far (biased if n = 1)
	 * 
	 * @return
	 */
	public double getVariance()
	{
		if (n > 1)
			return (sumSquaredDifferences / (n - 1));
		
		return (sumSquaredDifferences / (n));		
	}
	
	/**
	 * Initialises the data with some initial values (useful for simulating 
	 * prior distribution / knowledge)
	 * 
	 * @param n
	 * @param mean
	 * @param sumSquaredDifferences
	 */
	public void init(final int n, final double mean, final double sumSquaredDifferences)
	{
		this.n = n;
		this.mean = mean;
		this.sumSquaredDifferences = sumSquaredDifferences;
	}
	
	/**
	 * Initialise from another object
	 * @param other
	 */
	public void initFrom(final IncrementalStats other)
	{
		this.n = other.getNumObservations();
		this.mean = other.getMean();
		this.sumSquaredDifferences = other.getSumSquaredDifferences();
	}
	
	/**
	 * Add a new observation
	 * 
	 * @param observation
	 */
	public void observe(final double observation)
	{
		++n;
		double delta = observation - mean;
		mean += delta / n;
		sumSquaredDifferences += delta * (observation - mean);
	}
	
	/**
	 * Opposite of observing, we'll forget about a previously made observation
	 * 
	 * @param observation
	 */
	public void unobserve(final double observation)
	{
		final int wrongN = n;
		final double wrongMean = mean;
		final double wrongSsd = sumSquaredDifferences;
		
		--n;
		mean = (wrongN * wrongMean - observation) / n;
		final double delta = observation - mean;
		sumSquaredDifferences = wrongSsd - delta * (observation - wrongMean);
	}
	
	/**
	 * Merges two sets of incrementally collected stats, using Chan et al.'s method as described on
	 * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static IncrementalStats merge(final IncrementalStats a, final IncrementalStats b)
	{
		final double meanA = a.getMean();
		final double meanB = b.getMean();
		final int nA = a.getNumObservations();
		final int nB = b.getNumObservations();
		
		final double delta = meanA - meanB;
		final int sumN = nA + nB;
		
		// Two different methods of computing mean, not sure which is safer / more stable
		double newMean = meanA + delta * ((double)nB / sumN);
		//double newMean = (nA * meanA + nB * meanB) / (sumN);
		
		final double newSumSquaredDifferences = (sumN == 0) ? 0.0 :
				a.getSumSquaredDifferences() + b.getSumSquaredDifferences() +
				delta * delta * ((nA * nB) / sumN);
		
		final IncrementalStats mergedStats = new IncrementalStats();
		mergedStats.init(sumN, newMean, newSumSquaredDifferences);
		return mergedStats;
	}
	
	@Override
	public String toString()
	{
		return "[n = " + n + ", mean = " + mean + ", std = " + getStd() + "]";
	}
	
	//-------------------------------------------------------------------------
	
}