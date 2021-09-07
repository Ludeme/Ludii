package metrics.single.stateEvaluation.decisiveness;

import java.util.ArrayList;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage number of moves after a winning player has a state evaluation above the decisiveness threshold.
 * 
 * @author matthew.stephenson
 */
public class DecisivenessMoves extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DecisivenessMoves()
	{
		super
		(
			"Decisiveness Moves", 
			"Percentage number of moves after a winning player has a state evaluation above the decisiveness threshold.", 
			0.0, 
			1.0,
			Concept.DecisivenessMoves
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Evaluation evaluation,
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
			
			final double decisivenessThreshold = DecisivenessThreshold.decisivenessThreshold(game, evaluation, trial, rngState);
			
			final Context context = Utils.setupNewContext(game, rngState);
			final ArrayList<Integer> highestRankedPlayers = Utils.highestRankedPlayers(trial, context);
			
			int turnAboveDecisivenessthreshold = trial.numberRealMoves();
			boolean aboveThresholdFound = false;
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				for (final Integer playerIndex : highestRankedPlayers)
				{
					if (Utils.evaluateState(evaluation, context, playerIndex) > decisivenessThreshold)
					{
						aboveThresholdFound = true;
						turnAboveDecisivenessthreshold = i - trial.numInitialPlacementMoves();
						break;
					}
				}
				
				if (aboveThresholdFound)
					break;
				
				context.game().apply(context, trial.getMove(i));
			}
			
			avgDecisivenessThreshold += turnAboveDecisivenessthreshold/trial.numberRealMoves();
		}

		return avgDecisivenessThreshold / trials.length;
	}

	//-------------------------------------------------------------------------

}
