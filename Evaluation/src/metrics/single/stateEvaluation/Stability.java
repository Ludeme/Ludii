package metrics.single.stateEvaluation;

import java.util.ArrayList;
import java.util.List;

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
 * Average variance in each player's state evaluation.
 * 
 * @author matthew.stephenson
 */
public class Stability extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Stability()
	{
		super
		(
			"Stability", 
			"Average variance in each player's state evaluation.", 
			0.0, 
			1.0,
			Concept.Stability
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
		
		double avgStability = 0.0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Get the state evaluations for each player across the whole trial.
			final List<List<Double>> allPlayersStateEvaluationsAcrossTrial = new ArrayList<>();
			for (int i = 0; i <= context.game().players().count(); i++)
				allPlayersStateEvaluationsAcrossTrial.add(new ArrayList<>());
			
			final List<Move> realMoves = trial.generateRealMovesList();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final ArrayList<Double> allPlayerStateEvaluations = Utils.allPlayerStateEvaluations(evaluation, context);
				for (int j = 1; j < allPlayerStateEvaluations.size(); j++)
					allPlayersStateEvaluationsAcrossTrial.get(j).add(allPlayerStateEvaluations.get(j));
				
				context.game().apply(context, realMoves.get(i - trial.numInitialPlacementMoves()));
			}
			
			// Record the average variance for each players state evaluations.
			double stateEvaluationVariance = 0.0;
			for (final List<Double> valueList : allPlayersStateEvaluationsAcrossTrial)
			{
				double metricAverage = 0.0;
				for (final Double value : valueList)
					metricAverage += value.doubleValue() / valueList.size();
				
				double metricVariance = 0.0;
				for (final Double value : valueList)
					metricVariance += Math.pow(value.doubleValue() - metricAverage, 2) / valueList.size();

				stateEvaluationVariance += metricVariance;
			}
			
			avgStability += stateEvaluationVariance;
		}

		return Double.valueOf(avgStability / trials.length);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		System.err.println("Incrementally computing metric not yet implemented for Stability.");
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for Stability.");
	}

	//-------------------------------------------------------------------------

}
