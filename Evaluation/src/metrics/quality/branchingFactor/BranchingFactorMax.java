package metrics.quality.branchingFactor;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.context.Context;
import other.trial.Trial;

/**
 * Maximum number of possible moves.
 * 
 * @author matthew.stephenson
 */
public class BranchingFactorMax extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BranchingFactorMax()
	{
		super
		(
			"Branching Factor Maximum", 
			"Maximum number of possible moves.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			null
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final String args, 
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		double maxBranchingFactor = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the number of possible options for each move.
			double maxLegalMovesSizes = context.game().moves(context).moves().size();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves()-1; i++)
			{
				context.game().apply(context, trial.getMove(i));
				maxLegalMovesSizes = Math.max(maxLegalMovesSizes, context.game().moves(context).moves().size());
			}
			
			maxBranchingFactor += maxLegalMovesSizes;
		}

		return maxBranchingFactor / trials.length;
	}

	//-------------------------------------------------------------------------

}
