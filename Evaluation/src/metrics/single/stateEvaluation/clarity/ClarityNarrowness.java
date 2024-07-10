package metrics.single.stateEvaluation.clarity;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.math.statistics.Stats;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * The percentage of legal moves that have an evaluation value at least 75% above the difference between the max move evaluation value and average move evaluation value.
 * 
 * @author matthew.stephenson
 */
public class ClarityNarrowness extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public ClarityNarrowness()
	{
		super
		(
			"Clarity Narrowness", 
			"The percentage of legal moves that have an evaluation value at least 75% above the difference between the max move evaluation value and average move evaluation value.", 
			0.0, 
			1.0,
			Concept.Narrowness
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
		if (game.hasSubgames() || game.isSimultaneousMoveGame())
			return null;
		
		double clarity = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record all sites covered in this trial.
			final Stats moveNarrowness = new Stats();
			
			for (final Move m : trial.generateRealMovesList())
			{
				final Stats moveEvaluations = new Stats();
				for (final Move legalMoves : context.game().moves(context).moves())
					moveEvaluations.addSample(Utils.evaluateMove(evaluation, context, legalMoves).doubleValue());
				
				moveEvaluations.measure();
				
				final double maxEvaluation = moveEvaluations.max();
				final double averageEvaluation = moveEvaluations.mean();
				final double threshold = averageEvaluation + 0.75 * (maxEvaluation - averageEvaluation);
				
				int numberAboveThreshold = 0;
				for (int j = 0; j < moveEvaluations.n(); j++)
					if (moveEvaluations.get(j) > threshold)
						numberAboveThreshold++;

				moveNarrowness.addSample(moveEvaluations.n() == 0 ? 0 : numberAboveThreshold/moveEvaluations.n());
				
				context.game().apply(context, m);
			}
			
			moveNarrowness.measure();
			clarity += moveNarrowness.mean();
		}

		return Double.valueOf(trials.length == 0 ? 0 : clarity / trials.length);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityNarrowness.");
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityNarrowness.");
	}
	
	@Override
	public void observeFinalState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityNarrowness.");
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityNarrowness.");
		return Double.NaN;
	}
	
	//-------------------------------------------------------------------------

}
