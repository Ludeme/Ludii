package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Percentage of games which end in a draw.
 * 
 * @author matthew.stephenson
 */
public class Drawishness extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Drawishness()
	{
		super
		(
			"Drawishness", 
			"Percentage of games which end in a draw.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			1.0,
			0.0,
			Concept.Drawishness
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
		// Count number of draws
		double naturalDraws = 0.0;
		for (final Trial trial : trials)
			if (trial.status().winner() == 0 && trial.numTurns() <= game.getMaxTurnLimit())
				naturalDraws++;

		return naturalDraws / trials.length;
	}

	//-------------------------------------------------------------------------

}
