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
	public double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
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
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final Stats moveEvaluations = new Stats();
				for (final Move m : context.game().moves(context).moves())
					moveEvaluations.addSample(Utils.evaluateMove(evaluation, context, m));
				
				moveEvaluations.measure();
				
				final double maxEvaluation = moveEvaluations.max();
				final double averageEvaluation = moveEvaluations.mean();
				final double threshold = averageEvaluation + 0.75 * (maxEvaluation - averageEvaluation);
				
				int numberAboveThreshold = 0;
				for (int j = 0; j < moveEvaluations.n(); j++)
					if (moveEvaluations.get(j) > threshold)
						numberAboveThreshold++;

				moveNarrowness.addSample(numberAboveThreshold/moveEvaluations.n());
				
				context.game().apply(context, trial.getMove(i));
			}
			
			moveNarrowness.measure();
			clarity += moveNarrowness.mean();
		}

		return clarity / trials.length;
	}

}
