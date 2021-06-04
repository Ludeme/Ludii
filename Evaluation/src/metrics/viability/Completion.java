package metrics.viability;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Status;
import metrics.Metric;
import other.trial.Trial;

/**
 * Metric that measures tendency for games to reach completion.
 * 
 * @author cambolbro
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
			"Tendency for games to not end in a draw or timeout.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			1.0,
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
		int draws = 0;
		for (final Trial trial : trials)
		{
			final Status result = trial.status();
			if (result.winner() == 0)
				draws++;
		}

		final double completion = 1.0 - draws / (double)trials.length;
		return completion;
	}

	//-------------------------------------------------------------------------

}
