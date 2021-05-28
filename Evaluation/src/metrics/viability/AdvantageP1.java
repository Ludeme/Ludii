package metrics.viability;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.trial.Trial;

/**
 * Metric that measures Tendency for Player 1 to win.
 * 
 * @author matthew.stephenson
 */
public class AdvantageP1 extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public AdvantageP1()
	{
		super
		(
			"AdvantageP1", 
			"Tendency for Player 1 to win.", 
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
		if (game.players().count() < 1)
			return 0.0;
		
		// Count number of wins for P1
		double p1Wins = 0.0;
		for (final Trial trial : trials)
			if (trial.status().winner() == 1)
				p1Wins++;

		return p1Wins / trials.length;
	}

	//-------------------------------------------------------------------------

}