package metrics.single;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage of moves where there was more than 1 possible move.
 * 
 * @author matthew.stephenson
 */
public class DecisionMoves extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DecisionMoves()
	{
		super
		(
			"Decision Moves", 
			"Percentage of moves where there was more than 1 possible move.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			Concept.DecisionMoves
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
		double avgNumDecisionMoves = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the number of possible options for each move.
			double numDecisionMoves = 0;

			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				if (context.game().moves(context).moves().size() > 1)
					numDecisionMoves++;
				
				context.game().apply(context, trial.getMove(i));
			}
			
			avgNumDecisionMoves += numDecisionMoves / trial.numberRealMoves();
		}

		return avgNumDecisionMoves / trials.length;
	}

	//-------------------------------------------------------------------------

}
