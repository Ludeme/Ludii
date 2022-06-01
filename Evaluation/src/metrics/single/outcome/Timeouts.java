package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Percentage of games which end via timeout.
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Timeouts extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Timeouts()
	{
		super
		(
			"Timeouts", 
			"Percentage of games which end via timeout.", 
			0.0, 
			1.0,
			Concept.Timeouts
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
		// Count number of timeouts.
		double timeouts = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = trials[i];
			
			// Trial ended by timeout.
			final boolean trialTimedOut = trial.numTurns() > game.getMaxTurnLimit() || trial.numberRealMoves() > game.getMaxMoveLimit();
			
			if (trialTimedOut)
				timeouts++;
		}

		return timeouts / trials.length;
	}

	//-------------------------------------------------------------------------

}
