package metrics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import other.context.Context;
import other.trial.Trial;

/**
 * Lets us incrementally track and update many different metrics at the same time as
 * we're walking through multiple trials.
 * 
 * @author Dennis Soemers
 */
public class MetricsTracker 
{
	
	//-------------------------------------------------------------------------
	
	/** The metrics we want to track */
	private final List<Metric> metrics;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param metrics
	 */
	public MetricsTracker(final List<Metric> metrics)
	{
		this.metrics = metrics;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Inform all the metrics that we're now starting to walk through a new trial.
	 * @param context The initial state.
	 * @param fullTrial The complete trial (not just the stage we're at with stepping through it).
	 */
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		for (final Metric metric : metrics)
		{
			metric.startNewTrial(context, fullTrial);
		}
	}
	
	/**
	 * Let all the metrics observe a new state
	 * @param context
	 */
	public void observeNextState(final Context context)
	{
		for (final Metric metric : metrics)
		{
			metric.observeNextState(context);
		}
	}
	
	/**
	 * Let all the metrics observe the final state of a trial
	 * @param context
	 */
	public void observeFinalState(final Context context)
	{
		// TODO
	}
	
	/**
	 * Finalise computation of all metrics.
	 * @return Mapping from concept names to metric values.
	 */
	public Map<String, Double> finaliseMetrics()
	{
		final Map<String, Double> metricsMap = new HashMap<String, Double>();
		
		// TODO
		
		return metricsMap;
	}
	
	//-------------------------------------------------------------------------

}
