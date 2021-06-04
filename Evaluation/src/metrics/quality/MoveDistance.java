package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import game.types.board.RelationType;
import game.types.board.SiteType;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Metric that measures average distance of all moves
 * Note. Only for moves between same site type
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
			-1,
			0.0,
			Concept.MoveDistance
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
		
		game.board().topology().preGenerateDistanceToEachElementToEachOther(SiteType.Cell, RelationType.Adjacent);
		game.board().topology().preGenerateDistanceToEachElementToEachOther(SiteType.Edge, RelationType.Adjacent);
		game.board().topology().preGenerateDistanceToEachElementToEachOther(SiteType.Vertex, RelationType.Adjacent);
		
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
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				
				if 
				(
					trial.getMove(i).fromType() == trial.getMove(i).toType() 
					&&
					trial.getMove(i).from() < context.game().board().numSites()
					&&
					trial.getMove(i).to() < context.game().board().numSites()
					&&
					trial.getMove(i).from() != trial.getMove(i).to()
				)	
					moveDistance += game.board().topology().distancesToOtherSite(trial.getMove(i).fromType())[trial.getMove(i).from()][trial.getMove(i).to()];
			}
			
			final int numMoves = trial.numMoves() - trial.numInitialPlacementMoves();
			avgMoveDistance += moveDistance / numMoves;
		}

		return avgMoveDistance / trials.length;
	}

	//-------------------------------------------------------------------------

}
