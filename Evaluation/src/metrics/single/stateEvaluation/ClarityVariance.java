package metrics.single.stateEvaluation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
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
			final ArrayList<ArrayList<Double>> completeMoveEvaluations = new ArrayList<>();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				ArrayList<Double> moveEvaluations = new ArrayList<>();
				for (Move m : context.game().moves(context).moves())
					moveEvaluations.add(Utils.HeuristicEvaluateMove(context, m));
				
				completeMoveEvaluations.add(moveEvaluations);
				context.game().apply(context, trial.getMove(i));
			}
			
			// clarity += ((double) completeMoveEvaluations.size()) / game.board().topology().getGraphElements(game.board().defaultSite()).size();
		}

		return clarity / trials.length;
	}

}
