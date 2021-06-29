package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
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
			-1,
			null
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
		// Count the number of actions.
		double actionTally = 0;
		for (final Trial trial : trials)
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
				actionTally += trial.getMove(i).actions().size();
		
		return actionTally / trials.length;
	}

	//-------------------------------------------------------------------------

}
