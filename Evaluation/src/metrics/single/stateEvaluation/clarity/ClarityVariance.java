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
		
		double clarity = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record all sites covered in this trial.
			final Stats moveEvaluationVariance = new Stats();
			
			for (final Move m : trial.generateRealMovesList())
			{
				final Stats moveEvaluations = new Stats();
				for (final Move legalMoves : context.game().moves(context).moves())
					moveEvaluations.addSample(Utils.evaluateMove(evaluation, context, legalMoves).doubleValue());
				
				moveEvaluations.measure();

				moveEvaluationVariance.addSample(moveEvaluations.varn());
				context.game().apply(context, m);
			}
			
			moveEvaluationVariance.measure();
			clarity += moveEvaluationVariance.mean();
		}

		return Double.valueOf(clarity / trials.length);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityVariance.");
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityVariance.");
	}
	
	@Override
	public void observeFinalState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityVariance.");
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		System.err.println("Incrementally computing metric not yet implemented for ClarityVariance.");
		return Double.NaN;
	}
	
	//-------------------------------------------------------------------------

}
