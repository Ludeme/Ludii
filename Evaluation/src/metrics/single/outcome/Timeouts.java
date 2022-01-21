package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.ReplayTrial;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;

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
	public double apply
	(
			final Game game,
			final Evaluation evaluation,
			final ReplayTrial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		// Count number of timeouts.
		double timeouts = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final ReplayTrial trial = trials[i];
			final RandomProviderState rng = randomProviderStates[i];
			final Context context = Utils.setupTrialContext(game, rng, trial);
			
			if (context.state().playerToAgent(trial.trial().status().winner()) == 0 && (trial.trial().numTurns() > game.getMaxTurnLimit() || trial.trial().numberRealMoves() > game.getMaxMoveLimit()))
				timeouts++;
		}

		return timeouts / trials.length;
	}

	//-------------------------------------------------------------------------

}
