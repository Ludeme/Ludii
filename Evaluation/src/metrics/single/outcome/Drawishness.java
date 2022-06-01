package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Percentage of games which end in a draw (not including timeouts).
 * 
 * @author matthew.stephenson
 */
public class Drawishness extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Drawishness()
	{
		super
		(
			"Drawishness", 
			"Percentage of games which end in a draw (not including timeouts).", 
			0.0, 
			1.0,
			Concept.Drawishness
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
		final int numPlayers = game.players().count();
		if (numPlayers <= 1)
			return null;
		
		// Count number of draws
		double naturalDraws = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = trials[i];
			
			if (trial.status().winner() == 0 && trial.numTurns() <= game.getMaxTurnLimit() && trial.numberRealMoves() <= game.getMaxMoveLimit())
				naturalDraws++;
		}

		return naturalDraws / trials.length;
	}

	//-------------------------------------------------------------------------

}
