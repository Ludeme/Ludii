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
	public Double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		// Cannot perform move/state evaluation for matches.
		if (game.hasSubgames() || game.isSimultaneousMoveGame() || game.isSimulationMoveGame())
			return null;
		
		double avgDecisivenessThreshold = 0.0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			final double decisivenessThreshold = DecisivenessThreshold.decisivenessThreshold(game, evaluation, trial, rngState);
			
			final Context context = Utils.setupNewContext(game, rngState);
			final ArrayList<Integer> highestRankedPlayers = Utils.highestRankedPlayers(trial, context);
			
			int turnAboveDecisivenessthreshold = trial.generateRealMovesList().size();
			boolean aboveThresholdFound = false;
			for (int i = 0; i < trial.generateRealMovesList().size(); i++)
			{
				for (final Integer playerIndex : highestRankedPlayers)
				{
					if (Utils.evaluateState(evaluation, context, playerIndex) > decisivenessThreshold)
					{
						aboveThresholdFound = true;
						turnAboveDecisivenessthreshold = i;
						break;
					}
				}
				
				if (aboveThresholdFound)
					break;

				try {
					context.game().apply(context, trial.getMove(i));
				}
				catch(final Exception e) // To avoid a few exceptions in rare cases.
				{
					return null;
				}
			}
			
			avgDecisivenessThreshold += turnAboveDecisivenessthreshold/trial.generateRealMovesList().size();
		}

		return Double.valueOf(avgDecisivenessThreshold / trials.length);
	}

	//-------------------------------------------------------------------------

}
