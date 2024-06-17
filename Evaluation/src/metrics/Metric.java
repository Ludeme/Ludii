package metrics;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.multiple.MultiMetricFramework.MultiMetricValue;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * Base class for game metrics.
 * @author cambolbro and matthew.stephenson
 */
public abstract class Metric
{
	
	//-----------------------------------------
	
	/** Unique name for this metric. */
	private final String name;
	
	/** Brief description of what this metric measures. */ 
	private final String notes;  
	
	/** Range of possible values.*/
	private final Range<Double, Double> range;
	
	/** Concept associated with this Metric. */
	private final Concept concept;
	
	/** Process for calculating the metric value, if a multi-metric. Otherwise null. */
	private final MultiMetricValue multiMetricValue;

	//-------------------------------------------------------------------------
	
	public Metric
	(
		final String name, final String notes, final double min, final double max, 
		final Concept concept
	)
	{
		this(name, notes, min, max, concept, null);
	}

	public Metric
	(
		final String name, final String notes, final double min, final double max, 
		final Concept concept, final MultiMetricValue multiMetricValue
	)
	{
		this.name   = new String(name);
		this.notes  = new String(notes);
		range  = new Range<Double, Double>(Double.valueOf(min), Double.valueOf(max));
		this.concept = concept;
		this.multiMetricValue = multiMetricValue;
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

	public double min()
	{
		return range.min().intValue();
	}

	public double max()
	{
		return range.max().intValue();
	}
	
	public Concept concept() 
	{
		return concept;
	}
	
	public MultiMetricValue multiMetricValue() 
	{
		return multiMetricValue;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Apply this metric.
	 * @param game The game to run.
	 * @param trials At least one trial to be measured, may be multiple trials.
	 * @return Evaluation of the specified trial(s) according to this metric.
	 */
	public abstract Double apply
	(
		final Game game,
		final Evaluation evaluation,
		final Trial[] trials,
		final RandomProviderState[] randomProviderStates
	);
	
	/**
	 * Start processing a new trial.
	 * @param context Initial state.
	 * @param fullTrial The complete trial (not just the stage we're at with stepping through it).
	 */
	public abstract void startNewTrial(final Context context, final Trial fullTrial);
	
	/**
	 * Observe the next state for incrementally computing metrics.
	 * @param context
	 */
	public abstract void observeNextState(final Context context);
	
	/**
	 * Observe the final state for incrementally computing metrics
	 * (NOTE: we will have also already observed this same state via observeNextState()).
	 * @param context
	 */
	public abstract void observeFinalState(final Context context);
	
	/**
	 * Finalise incremental computation of metric (i.e., signal that we have seen
	 * the last of the final trial).
	 * @param game
	 * @param numTrials
	 * @return The metric value.
	 */
	public abstract double finaliseMetric(final Game game, final int numTrials);
	
	//-------------------------------------------------------------------------

}

