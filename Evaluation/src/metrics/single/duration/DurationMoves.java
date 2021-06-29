package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Number of moves in a game.
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
			"Number of moves in a game.", 
			0.0, 
			-1,
			Concept.DurationMoves
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
		// Count the number of moves.
		double moveTally = 0;
		for (final Trial trial : trials)
			moveTally += trial.numberRealMoves();
		
		return moveTally / trials.length;
	}

	//-------------------------------------------------------------------------

}
