package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import main.Status.EndType;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Number of turns in a game (excluding timeouts).
 * 
 * @author matthew.stephenson
 */
public class DurationTurnsNotTimeouts extends Metric
{

	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected double turnTally = 0.0;

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
			if (game.players().count() <= 1)
				return Double.valueOf(1);
			else
				return Double.valueOf(game.getMaxTurnLimit() * game.players().count());
		}
			
					
		return Double.valueOf(turnTally / numTrials);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		// Do nothing
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		// Do nothing
	}
	
	@Override
	public void observeFinalState(final Context context)
	{
		if (context.trial().status() != null) 
		{
			final boolean trialTimedOut = context.trial().status().endType() == EndType.MoveLimit || context.trial().status().endType() == EndType.TurnLimit;
			if (!trialTimedOut)
			{
				turnTally += context.trial().numTurns();
			}
		}
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		return turnTally / numTrials;
	}
	
	//-------------------------------------------------------------------------

}
