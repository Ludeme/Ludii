package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import game.types.board.RelationType;
import game.types.board.SiteType;
import metrics.Metric;
import other.concept.Concept;
import other.topology.Topology;
import other.trial.Trial;

/**
 * Average move distance
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
			"Average move distance.", 
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
		final Topology boardTopology = game.board().topology();
		
		boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Cell, RelationType.Adjacent);
		boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Edge, RelationType.Adjacent);
		boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Vertex, RelationType.Adjacent);
		
		double avgMoveDistance = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			
			// Record the distance travelled for each move.
			double moveDistance = 0;
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final SiteType moveType = trial.getMove(i).fromType();
				
				if 
				(
					trial.getMove(i).fromType() == trial.getMove(i).toType() 
					&&
					trial.getMove(i).from() < boardTopology.numSites(moveType)
					&&
					trial.getMove(i).to() < boardTopology.numSites(moveType)
					&&
					trial.getMove(i).from() != trial.getMove(i).to()
				)	
					moveDistance += boardTopology.distancesToOtherSite(moveType)[trial.getMove(i).from()][trial.getMove(i).to()];
			}
			
			avgMoveDistance += moveDistance / trial.numberRealMoves();
		}

		return avgMoveDistance / trials.length;
	}

	//-------------------------------------------------------------------------

}
