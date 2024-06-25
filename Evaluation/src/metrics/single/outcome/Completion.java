package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage of games which have a winner (not draw or timeout).
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Completion extends Metric
{

	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected double completedGames = 0.0;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Completion()
	{
		super
		(
			"Completion", 
			"Percentage of games which have a winner (not draw or timeout).", 
			0.0, 
			1.0,
			Concept.Completion
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
		// Count number of completed games
		double completedGames = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = trials[i];
			
			if (trial.status().winner() != 0)
				completedGames++;
		}

		return Double.valueOf(completedGames / trials.length);
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
		if (context.trial().status() != null && context.trial().status().winner() != 0)
			completedGames++;
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		return completedGames / numTrials;
	}
	
	//-------------------------------------------------------------------------

}
