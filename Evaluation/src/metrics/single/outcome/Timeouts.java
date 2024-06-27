package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Status.EndType;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage of games which end via timeout.
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Timeouts extends Metric
{

	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected double timeouts = 0.0;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Timeouts()
	{
		super
		(
			"Timeouts", 
			"Percentage of games which end via timeout.", 
			0.0, 
			1.0,
			Concept.Timeouts
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		// Count number of timeouts.
		double timeouts = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = trials[i];
			
			// Trial ended by timeout.
			final boolean trialTimedOut = trial.status().endType() == EndType.MoveLimit || trial.status().endType() == EndType.TurnLimit;
			
			if (trialTimedOut)
				timeouts++;
		}

		return Double.valueOf(timeouts / trials.length);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		// Do nothing
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		// Do nothing
	}
	
	@Override
	public void observeFinalState(final Context context)
	{
		final Trial trial = context.trial();
		final boolean trialTimedOut = trial.status() == null || trial.status().endType() == EndType.MoveLimit || trial.status().endType() == EndType.TurnLimit;
		
		if (trialTimedOut)
			timeouts++;
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		return timeouts / numTrials;
	}
	
	//-------------------------------------------------------------------------

}
