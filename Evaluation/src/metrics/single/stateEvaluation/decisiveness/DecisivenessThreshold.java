package metrics.single.stateEvaluation.decisiveness;

import java.util.ArrayList;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Maximum state evaluation value achieved by non-winning player.
 * 
 * @author matthew.stephenson
 */
public class DecisivenessThreshold extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DecisivenessThreshold()
	{
		super
		(
			"Decisiveness Threshold", 
			"Maximum state evaluation value achieved by non-winning player.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			1.0,
			0.0,
			Concept.Timeouts
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
		double avgDecisivenessThreshold = 0.0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Count number of times the expected winner changed.
			double decisivenessThreshold = -1.0;
			
			final ArrayList<Integer> highestRankedPlayers = Utils.highestRankedPlayers(trial, context);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final ArrayList<Double> allPlayerStateEvaluations = Utils.UCTAllPlayerStateEvaulations(context);
				for (int j = 1; j < allPlayerStateEvaluations.size(); j++)
					if (allPlayerStateEvaluations.get(j) > decisivenessThreshold && !highestRankedPlayers.contains(Integer.valueOf(j)))
						decisivenessThreshold = allPlayerStateEvaluations.get(j);

				context.game().apply(context, trial.getMove(i));
			}
			
			avgDecisivenessThreshold += decisivenessThreshold;
		}

		return avgDecisivenessThreshold / trials.length;
	}

	//-------------------------------------------------------------------------

}
