package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Number of turns in a game.
 * 
 * @author matthew.stephenson
 */
public class DurationTurns extends Metric
{
	
	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected double turnTally = 0.0;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DurationTurns()
	{
		super
		(
			"Duration Turns", 
			"Number of turns in a game.", 
			0.0, 
			Constants.INFINITY,
			Concept.DurationTurns
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
		// Count the number of turns.
		double turnTally = 0;
		for (final Trial trial : trials)
			turnTally += trial.numTurns();
		
		return Double.valueOf(turnTally / trials.length);
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
		turnTally += context.trial().numTurns();
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		return turnTally / numTrials;
	}
	
	//-------------------------------------------------------------------------

}
