package metrics.single.stateEvaluation.decisiveness;

import java.util.ArrayList;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
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
			0.0, 
			1.0,
			Concept.DecisivenessThreshold
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
		if (game.hasSubgames() || game.isSimultaneousMoveGame())
			return null;
		
		double avgDecisivenessThreshold = 0.0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			final double decisivenessThreshold = decisivenessThreshold(game, evaluation, trial, rngState);
			avgDecisivenessThreshold += decisivenessThreshold;
		}

		return Double.valueOf(avgDecisivenessThreshold / trials.length);
	}

	//-------------------------------------------------------------------------
	
	public static double decisivenessThreshold(final Game game, final Evaluation evaluation, final Trial trial, final RandomProviderState rngState)
	{
		// Setup a new instance of the game
		final Context context = Utils.setupNewContext(game, rngState);
		
		double decisivenessThreshold = -1.0;
		
		final ArrayList<Integer> highestRankedPlayers = Utils.highestRankedPlayers(trial, context);
		
		for (final Move m : trial.generateRealMovesList())
		{
			final ArrayList<Double> allPlayerStateEvaluations = Utils.allPlayerStateEvaluations(evaluation, context);
			for (int j = 1; j < allPlayerStateEvaluations.size(); j++)
				if (allPlayerStateEvaluations.get(j).doubleValue() > decisivenessThreshold && !highestRankedPlayers.contains(Integer.valueOf(j)))
					decisivenessThreshold = allPlayerStateEvaluations.get(j).doubleValue();

			context.game().apply(context, m);
		}
		
		return decisivenessThreshold;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		System.err.println("Incrementally computing metric not yet implemented for DecisivenessThreshold.");
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for DecisivenessThreshold.");
	}

	//-------------------------------------------------------------------------

}
