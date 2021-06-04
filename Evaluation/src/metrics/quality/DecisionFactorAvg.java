package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Average number of possible moves, when the number of legal moves was greater than 1.
 * 
 * @author matthew.stephenson
 */
public class DecisionFactorAvg extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DecisionFactorAvg()
	{
		super
		(
			"Decision Factor Avg", 
			"Average number of possible moves, when the number of legal moves was greater than 1.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			Concept.DecisionFactor
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final String args, 
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		double avgDecisionFactor = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the number of possible options for each move.
			double legalMovesSizes = 0;
			double numDecisionMoves = 0;

			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				if (context.game().moves(context).moves().size() > 1)
				{
					legalMovesSizes += context.game().moves(context).moves().size();
					numDecisionMoves++;
				}
				
				context.game().apply(context, trial.getMove(i));
			}
			
			avgDecisionFactor += legalMovesSizes / numDecisionMoves;
		}

		return avgDecisionFactor / trials.length;
	}

	//-------------------------------------------------------------------------

}
