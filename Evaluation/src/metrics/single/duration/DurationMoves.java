package metrics.single.duration;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Number of moves in a game.
 * 
 * @author matthew.stephenson
 */
public class DurationMoves extends Metric
{
	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	double moveTally = 0.0;

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
			Constants.INFINITY,
			Concept.DurationMoves
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
		// Count the number of moves.
		double moveTally = 0;
		for (final Trial trial : trials)
			moveTally += trial.numberRealMoves();
		
		return Double.valueOf(moveTally / trials.length);
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
		moveTally += context.trial().numberRealMoves();
	}
	
	//-------------------------------------------------------------------------

}
