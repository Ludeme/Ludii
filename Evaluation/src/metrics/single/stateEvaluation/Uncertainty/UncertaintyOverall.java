package metrics.single.stateEvaluation.Uncertainty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.math.Stats;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Based on the variance in the expected scores for the moves in a turn.
 * 
 * @author matthew.stephenson
 */
public class UncertaintyOverall extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public UncertaintyOverall()
	{
		super
		(
			"Clarity Variance", 
			"Based on the variance in the expected scores for the moves in a turn.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			1.0,
			0.5,
			Concept.BoardCoverage
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
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				Stats moveEvaluations = new Stats();
				for (Move m : context.game().moves(context).moves())
					moveEvaluations.addSample(Utils.HeuristicEvaluateMove(context, m));

				moveEvaluationVariance.addSample(moveEvaluations.varn());
				context.game().apply(context, trial.getMove(i));
			}
			
			clarity += moveEvaluationVariance.sum() / moveEvaluationVariance.n();
		}

		return clarity / trials.length;
	}

}
