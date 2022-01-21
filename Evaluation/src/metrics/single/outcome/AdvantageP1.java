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
 * Percentage of games where player 1 won.
 * 
 * @author matthew.stephenson
 */
public class AdvantageP1 extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public AdvantageP1()
	{
		super
		(
			"AdvantageP1", 
			"Percentage of games where player 1 won.", 
			0.0, 
			1.0,
			Concept.AdvantageP1
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
		
		// Count number of wins for P1 (draws count as half a win)
		double p1Wins = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final ReplayTrial trial = trials[i];
			final RandomProviderState rng = randomProviderStates[i];
			final Context context = Utils.setupTrialContext(game, rng, trial);
			
			if (context.state().playerToAgent(trial.trial().status().winner()) == 1)
				p1Wins++;
			else if (context.state().playerToAgent(trial.trial().status().winner()) <= 0)
				p1Wins += 0.5;
		}

		return p1Wins / trials.length;
	}

	//-------------------------------------------------------------------------

}