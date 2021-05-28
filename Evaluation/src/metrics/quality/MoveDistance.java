package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import game.types.board.RelationType;
import metrics.Metric;
import metrics.Utils;
import other.context.Context;
import other.trial.Trial;

/**
 * Metric that measures average distance of all moves
 * 
 * @author matthew.stephenson
 */
public class MoveDistance extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public MoveDistance()
	{
		super
		(
			"Move Distance", 
			"Average move distance over all trials.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1
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
		if (trials.length == 0)
			return 0;
		
		double avgMoveDistance = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the distance travelled for each move.
			double moveDistance = 0;
			
			context.game().board().topology().preGenerateDistanceToEachElementToEachOther(context.board().defaultSite(), RelationType.Adjacent);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				
				if 
				(
					trial.getMove(i).fromType() == context.board().defaultSite() 
					&& 
					trial.getMove(i).toType() == context.board().defaultSite()
					&&
					trial.getMove(i).from() < context.game().board().numSites()
					&&
					trial.getMove(i).to() < context.game().board().numSites()
					&&
					trial.getMove(i).from() != trial.getMove(i).to()
				)	
					moveDistance += context.board().topology().distancesToOtherSite(context.board().defaultSite())[trial.getMove(i).from()][trial.getMove(i).to()];
			}
			
			final int numMoves = trial.numMoves() - trial.numInitialPlacementMoves();
			avgMoveDistance += moveDistance / numMoves;
		}

		return avgMoveDistance / trials.length;
	}

	//-------------------------------------------------------------------------

}
