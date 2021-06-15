package metrics.multiple.branchingFactor;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.context.Context;
import other.trial.Trial;

/**
 * Average increase in the number of possible moves.
 * 
 * @author matthew.stephenson
 */
public class BranchingFactorChange extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BranchingFactorChange()
	{
		super
		(
			"Branching Factor Increase", 
			"Average increase in the number of possible moves.",
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			-1, 
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
		double branchingFactorIncrease = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the number of possible options for each move.
			double legalMovesIncreases = 0;
			int lastMovesSize = context.game().moves(context).moves().size();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves()-1; i++)
			{
				context.game().apply(context, trial.getMove(i));
				legalMovesIncreases += context.game().moves(context).moves().size() - lastMovesSize;
				lastMovesSize = context.game().moves(context).moves().size();
			}
			
			branchingFactorIncrease += legalMovesIncreases / (trial.numberRealMoves()-1);
		}

		return branchingFactorIncrease / trials.length;
	}

	//-------------------------------------------------------------------------

}
