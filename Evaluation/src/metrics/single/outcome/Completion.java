package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Percentage of games which have a winner (not draw or timeout).
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Completion extends Metric
{

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
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			1.0,
			1.0,
			Concept.Completion
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		// Count number of completed games
		double completedGames = 0.0;
		for (final Trial trial : trials)
			if (trial.status().winner() != 0)
				completedGames++;

		return completedGames / trials.length;
	}

	//-------------------------------------------------------------------------

}
