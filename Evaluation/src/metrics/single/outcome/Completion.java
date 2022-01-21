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
 * Percentage of games which have a winner (not draw or timeout).
 * 
 * @author cambolbro and matthew.stephenson
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
			"Percentage of games which have a winner (not draw or timeout).", 
			0.0, 
			1.0,
			Concept.Completion
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
		// Count number of completed games
		double completedGames = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final ReplayTrial trial = trials[i];
			final RandomProviderState rng = randomProviderStates[i];
			final Context context = Utils.setupTrialContext(game, rng, trial);
			
			if (context.state().playerToAgent(trial.trial().status().winner()) != 0)
				completedGames++;
		}

		return completedGames / trials.length;
	}

	//-------------------------------------------------------------------------

}
