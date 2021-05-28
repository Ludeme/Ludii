package utils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Utility class to incrementally keep track of exponential
 * moving averages.
 * 
 * @author Dennis Soemers
 */
public class ExponentialMovingAverage implements Serializable
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** Weight assigned to most recent point of data. 0 = all data points weighed equally */
	protected final double alpha;
	
	/** Our running mean */
	protected double runningMean = 0.0;
	
	/** Our denominator in running mean */
	protected double denominator = 0.0;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (default alpha of 0.05)
	 */
	public ExponentialMovingAverage()
	{
		this(0.05);
	}
	
	/**
	 * Constructor
	 * @param alpha
	 */
	public ExponentialMovingAverage(final double alpha)
	{
		this.alpha = alpha;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our (exponential) moving average
	 */
	public double movingAvg()
	{
		return runningMean;
	}
	
	/**
	 * Observe a new data point
	 * @param data
	 */
	public void observe(final double data)
	{
		denominator = (1 - alpha) * denominator + 1;
		runningMean += (1.0 / denominator) * (data - runningMean);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Writes this tracker to a binary file
	 * @param filepath
	 */
	public void writeToFile(final String filepath)
	{
		try 
		(
			final ObjectOutputStream out = 
				new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filepath)))
		)
		{
			out.writeObject(this);
			out.flush();
			out.close();
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
