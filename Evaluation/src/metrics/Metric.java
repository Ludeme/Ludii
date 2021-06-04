package metrics;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * Base class for game metrics.
 * @author cambolbro
 */
public abstract class Metric
{
	//-----------------------------------------

	public enum MetricType
	{
		OUTCOMES,
	}
	
	//-----------------------------------------
	
	/** Unique name for this metric. */
	private final String name;
	
	/** Brief description of what this metric measures. */ 
	private final String notes;  
	
	/** Who contributed this metric. */ 
	private final String credit;  
	
	/** Type of metric. */
	private final MetricType type;      
	
	/** Range of possible values. */
	private final Range<Double, Double> range;
	
	/** Default value for metric if it cannot be calculated. */
	private final Double defaultValue;
	
	/** Default value for metric if it cannot be calculated. */
	private final Concept concept;

	//-------------------------------------------------------------------------

	public Metric
	(
		final String name, final String notes, final String credit, final MetricType type, 
		final double min, final double max, final double defaultValue, final Concept concept
	)
	{
		this.name   = new String(name);
		this.notes  = new String(notes);
		this.credit = new String(credit);
		this.type   = type;
		range  = new Range<Double, Double>(Double.valueOf(min), Double.valueOf(max));
		this.defaultValue = Double.valueOf(defaultValue);
		this.concept = concept;
	}

	//-------------------------------------------------------------------------

	public String name()
	{
		return name;
	}

	public String notes()
	{
		return notes;
	}

	public String credit()
	{
		return credit;
	}

	public MetricType type()
	{
		return type;
	}

	public double min()
	{
		return range.min().intValue();
	}

	public double max()
	{
		return range.max().intValue();
	}
	
	public Double defaultValue() 
	{
		return defaultValue;
	}
	
	public Concept concept() 
	{
		return concept;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Apply this metric.
	 * @param game The game to run.
	 * @param args Metric-specific arguments.
	 * @param trials At least one trial to be measured, may be multiple trials.
	 * @return Evaluation of the specified trial(s) according to this metric.
	 */
	public abstract double apply
	(
		final Game game,
		final String args, 
		final Trial[] trials,
		final RandomProviderState[] randomProviderStates
	);
	
	//-------------------------------------------------------------------------
	
	protected static TIntArrayList boardSitesCovered(final Context context)
	{
		final TIntArrayList boardSitesCovered = new TIntArrayList();
		final ContainerState cs = context.containerState(0);
		
		for (int i = 0; i < context.game().board().numSites(); i++)
			if (cs.what(i, context.game().board().defaultSite()) != 0)
				boardSitesCovered.add(i);
		
		return boardSitesCovered;
	}
	
	//-------------------------------------------------------------------------

}

