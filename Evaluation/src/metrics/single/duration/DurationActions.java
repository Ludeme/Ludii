package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.move.Move;
import other.trial.Trial;

/**
 * Number of actions in a game.
 * 
 * @author matthew.stephenson
 */
public class DurationActions extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DurationActions()
	{
		super
		(
			"Duration Actions", 
			"Number of actions in a game.", 
			0.0, 
			Constants.INFINITY,
			Concept.DurationActions
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
		// Count the number of actions.
		double actionTally = 0;
		for (final Trial trial : trials)
			for (final Move m : trial.generateRealMovesList())
				actionTally += m.actions().size();
		
		return Double.valueOf(actionTally / trials.length);
	}

	//-------------------------------------------------------------------------

}
