package metrics.viability;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.trial.Trial;

/**
 * Metric that measures Average number or moves in a game.
 * 
 * @author matthew.stephenson
 */
public class DurationMoves extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DurationMoves()
	{
		super
		(
			"Duration Moves", 
			"Average number or moves in a game.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			-1.0,
			0.0
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
		
		double moveTally = 0;
		for (final Trial trial : trials)
			moveTally += trial.numMoves();
		
		return moveTally / trials.length;
	}

	//-------------------------------------------------------------------------

}
