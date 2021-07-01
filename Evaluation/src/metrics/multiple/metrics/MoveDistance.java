package metrics.multiple.metrics;

import java.util.ArrayList;

import game.types.board.RelationType;
import game.types.board.SiteType;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.topology.Topology;
import other.trial.Trial;

/**
 * The distance traveled by pieces when they move around the board.
 * Note. Only for moves between same site type
 * 
 * @author matthew.stephenson
 */
public class MoveDistance extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public MoveDistance(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Move Distance " + multiMetricValue.name(), 
			"The distance traveled by pieces when they move around the board.", 
			0.0, 
			-1,
			concept,
			multiMetricValue
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double[] getMetricValueList(final Trial trial, final Context context)
	{
		final Topology boardTopology = context.game().board().topology();
		boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Cell, RelationType.Adjacent);
		if (!context.game().isBoardless())
		{
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Edge, RelationType.Adjacent);
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Vertex, RelationType.Adjacent);
		}
		
		final ArrayList<Double> valueList = new ArrayList<>();

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
			{
				valueList.add(Double.valueOf(boardTopology.distancesToOtherSite(moveType)[trial.getMove(i).from()][trial.getMove(i).to()]));
			}
			
			context.game().apply(context, trial.getMove(i));
		}
		
		
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------

}
