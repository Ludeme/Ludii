package metrics.viability;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Status;
import metrics.Metric;
import other.trial.Trial;

/**
 * Metric that measures tendency for games to end in a draw.
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
			"Tendency for games to end by draw.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			1.0
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final String args, 
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		if (trials.length == 0)
			return 0;
		
		// Count number of draws
		double naturalDraws = 0.0;
		for (final Trial trial : trials)
		{
			final Status result = trial.status();
			if (result.winner() == 0 && trial.numTurns() <= game.getMaxTurnLimit())
				naturalDraws++;
		}

		final double naturalDrawsPercentage = naturalDraws / trials.length;
		return naturalDrawsPercentage;
	}

	//-------------------------------------------------------------------------

}
