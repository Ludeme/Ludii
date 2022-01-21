package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.ReplayTrial;
import other.concept.Concept;

/**
 * Number of turns in a game.
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
			"Number or turns in a game.", 
			0.0, 
			-1,
			Concept.DurationTurns
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Evaluation evaluation,
			final ReplayTrial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		// Count the number of turns.
		double turnTally = 0;
		for (final ReplayTrial trial : trials)
			turnTally += trial.trial().numTurns();
		
		return turnTally / trials.length;
	}

	//-------------------------------------------------------------------------

}
