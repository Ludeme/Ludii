package metrics.multiple.branchingFactor;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Average number of possible moves.
 * 
 * @author matthew.stephenson
 */
public class BranchingFactorAvg extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BranchingFactorAvg()
	{
		super
		(
			"Branching Factor Avg", 
			"Average number of possible moves.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			Concept.BranchingFactor
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
		double branchingFactorAvg = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the number of possible options for each move.
			double numLegalMoves = 0;
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				numLegalMoves += context.game().moves(context).moves().size();
				context.game().apply(context, trial.getMove(i));
			}
			
			branchingFactorAvg += numLegalMoves / trial.numberRealMoves();
		}

		return branchingFactorAvg / trials.length;
	}

	//-------------------------------------------------------------------------

}
