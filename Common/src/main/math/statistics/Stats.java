package main.math.statistics;
import java.text.DecimalFormat;
import java.util.Locale;

import gnu.trove.list.array.TDoubleArrayList;

//------------------------------------------------------------------------

/**
 * Basic statistics of a list of (double) samples.
 * @author Cameron Browne, Dennis Soemers
 */
public class Stats 
{
	/** Description of what these statistics describe. */
	protected String label = "?";
	
	/** Samples. */
	protected TDoubleArrayList samples = new TDoubleArrayList();
	
	/** CI constant, from: http://mathworld.wolfram.com/StandardDeviation.html */
	//private final double ci = 1.64485;  // for 90%
	protected final double ci = 1.95996;  // for 95%

	/** Minimum value. */
	protected double min;
	
	/** Maximum value. */
	protected double max;

	/** Sum. */
	protected double sum;
	
	/** Mean. */
	protected double mean;
	
	/** Sample variance. */
	protected double varn;
	
	/** Sample standard deviation. */
	protected double stdDevn;

	/** Standard error. */
	protected double stdError;
	
	/** Confidence interval. */
	protected double confInterval;
		
	/** Decimal format for printing. */
	private final static DecimalFormat df3 = new DecimalFormat("#.###");
	private final static DecimalFormat df6 = new DecimalFormat("#.######");

	//---------------------------------------------
	
	/**
	 * Default constructor 
	 */
	public Stats()
	{
		label = "Unnamed";
	}
	
	/**
	* Constructor 
	* @param str Label.
	*/
	public Stats(final String str)
	{
		label = str;
	}

	//---------------------------------------------
	
	/**
	 * @return What these statistics describe.
	 */
	public String label()
	{
		return label;
	}
		
	/**
	 * @param lbl Description of these statistics.
	 */
	public void setLabel(final String lbl)
	{
		label = lbl;
	}

	/**
	 * Add a sample.
	 * @param val Sample to add.
	 */
	public void addSample(final double val)
	{
		samples.add(val);
	}
	
	/**
	 * @param index Sample to get.
	 * @return Specified sample.
	 */
	public double get(final int index)
	{
		return samples.get(index);
	}

	/**
	 * @return Number of samples.
	 */
	public int n()
	{
		return samples.size();
	}
	
	/**
	 * @return Sum.
	 */
	public double sum()
	{
		return sum;
	}

	/**
	 * @return Mean.
	 */
	public double mean()
	{
		return mean;
	}

	/**
	 * @return Sample variance.
	 */
	public double varn()
	{
		return varn;
	}

	/**
	 * @return Standard deviation.
	 */
	public double sd()
	{
		return stdDevn;
	}

	/**
	 * @return Standard error.
	 */
	public double se()
	{
		return stdError;
	}

	/**
	 * @return Confidence interval (95%).
	 */
	public double ci()
	{
		return confInterval;
	}

	/**
	 * @return Minimum value.
	 */
	public double min()
	{
		return min;
	}

	/**
	 * @return Maximum value.
	 */
	public double max()
	{
		return max;
	}
	
	/**
	 * @return Range of values.
	 */
	public double range()
	{
		return max() - min();
	}
	
	/**
	 * @param list Sample list.
	 */
	public void set(final TDoubleArrayList list)
	{
		samples = list;
	}
	
	//---------------------------------------------
	
	/**
	 * Clears this set of statistics.
	 */
	public void clear()
	{
		samples.clear();
		
		sum  = 0.0;
		mean = 0.0;
		varn = 0.0;
		stdDevn   = 0.0;
		stdError   = 0.0;
		confInterval = 0.0;
		min  = 0.0;
		max  = 0.0;
	}
	
	//---------------------------------------------
	
	/**
	 * Measures stats from samples.
	 */
	public void measure()
	{
		sum  = 0.0;
		mean = 0.0;
		varn = 0.0;
		stdDevn   = 0.0;
		stdError   = 0.0;
		confInterval = 0.0;
		min  = 0.0;
		max  = 0.0;
		
		final int n = samples.size();
		
		if (n == 0)
			return;
		
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		
		//	Calculate mean
		for (int i = 0; i < n; ++i)
		{
			final double val = samples.getQuick(i);
			sum += val;
			
			if (val < min)
				min = val;
			else if (val > max)
				max = val;
		}
		
		mean = sum / n;
			
		// We require sample size of at least 2 for sample variance, sample STD, CIs, etc.
		if (n > 1)
		{
			//	Variance 
			for (int i = 0; i < n; ++i)
			{
				final double val = samples.getQuick(i);
				final double diff = val - mean;
				varn += diff * diff;
			}
			
			// N - 1 for sample variance instead of population variance
			varn /= (n - 1);
			
			//	Standard deviation
			stdDevn = Math.sqrt(varn);
					
			// Standard error
			stdError = stdDevn / Math.sqrt(n);

			//	Confidence interval
			//conf = 2 * ci * devn / Math.sqrt(samples.size());
			confInterval = ci * stdDevn / Math.sqrt(n);
		}
	}
	
	/** 
	 * Shows stats.
	 */
	public void show()
	{
		System.out.printf(toString());
	}
	
	/** 
	 * Shows stats.
	 */
	public void showFull()
	{
//		String str = String.format(
//				Locale.ROOT,
//				"%s: N=%d, mean=%.6f (+/-%.6f), sd=%.6f, min=%.6f, max=%.6f.", 
//				label, samples.size(), mean, conf, devn, min, max);
		
		final String str = 
				""      + Locale.ROOT      + ": " +
				"N="    + samples.size()   + ", " +
				"mean=" + df6.format(mean) + " "  +
				"(+/-"  + df6.format(confInterval) + "), " +
				"sd="   + df6.format(stdDevn) + ", " +
				"se="   + df6.format(stdError) + ", " +
				"min="  + df6.format(min)  + ", " +
				"max="  + df6.format(max)  + ".";
				
		System.out.println(str);
	}
	
	@Override
	public String toString()
	{
//		String str = String.format(
//				Locale.ROOT, 
//				"%s: N=%d, mean=%.6f (+/-%.6f).", 
//				label, samples.size(), mean, conf);
		
		final String str = 
				""      + Locale.ROOT      + ": " +
				"N="    + samples.size()   + ", " +
				"mean=" + df6.format(mean) + " "  +
				"(+/-"  + df6.format(confInterval) + ").";

		return str;
	}
	
	public String exportPS()
	{
		return 
			"[ (" + label + ") " + samples.size() + " " + df3.format(mean)
			+ 
			" " + df3.format(min) + " " + df3.format(max) 
			+ 
			" " + df3.format(stdDevn) + " " + df3.format(stdError) + " " + df3.format(ci) 
			+ 
			" ]";
	}
}
