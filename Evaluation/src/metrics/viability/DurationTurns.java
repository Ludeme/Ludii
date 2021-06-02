package metrics.viability;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.trial.Trial;

/**
 * Metric that measures Average number or turns in a game.
 * 
 * @author matthew.stephenson
 */
public class DurationTurns extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DurationTurns()
	{
		super
		(
			"Duration Turns", 
			"Average number or turns in a game.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			-1.0
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
		
		double turnTally = 0;
		for (final Trial trial : trials)
			turnTally += trial.numTurns();
		
		return turnTally / trials.length;
	}

	//-------------------------------------------------------------------------

}
