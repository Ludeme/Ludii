package metrics.single.stateEvaluation.clarity;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.math.statistics.Stats;
import metrics.Evaluation;
import metrics.Metric;
import metrics.ReplayTrial;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * The average variance in the evaluation values for the legal move.
 * 
 * @author matthew.stephenson
 */
public class ClarityVariance extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public ClarityVariance()
	{
		super
		(
			"Clarity Variance", 
			"The average variance in the evaluation values for the legal moves.", 
			0.0, 
			1.0,
			Concept.Variance
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Evaluation evaluation,
			final ReplayTrial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		double clarity = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final ReplayTrial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record all sites covered in this trial.
			final Stats moveEvaluationVariance = new Stats();
			
			for (final Move m : trial.fullMoves())
			{
				final Stats moveEvaluations = new Stats();
				for (final Move legalMoves : context.game().moves(context).moves())
					moveEvaluations.addSample(Utils.evaluateMove(evaluation, context, legalMoves));
				
				moveEvaluations.measure();

				moveEvaluationVariance.addSample(moveEvaluations.varn());
				context.game().apply(context, m);
			}
			
			moveEvaluationVariance.measure();
			clarity += moveEvaluationVariance.mean();
		}

		return clarity / trials.length;
	}

}
