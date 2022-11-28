package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import main.Status.EndType;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Number of turns in a game (excluding timeouts).
 * 
 * @author matthew.stephenson
 */
public class DurationTurnsNotTimeouts extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DurationTurnsNotTimeouts()
	{
		super
		(
			"Duration Turns Not Timeouts", 
			"Number of turns in a game (excluding timeouts).", 
			0.0, 
			Constants.INFINITY,
			Concept.DurationTurnsNotTimeouts
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
		// Count the number of turns.
		double turnTally = 0;
		double numTrials = 0;
		for (final Trial trial : trials)
		{
			final boolean trialTimedOut = trial.status().endType() == EndType.MoveLimit || trial.status().endType() == EndType.TurnLimit;
			if (!trialTimedOut)
			{
				turnTally += trial.numTurns();
				numTrials++;
			}
		}
		
		// Check if all trials timed out
		if (numTrials == 0)
		{
			if (game.players().count() == 1)
				return Double.valueOf(1);
			else
				return Double.valueOf(game.getMaxTurnLimit() * game.players().count());
		}
			
					
		return Double.valueOf(turnTally / numTrials);
	}

	//-------------------------------------------------------------------------

}
