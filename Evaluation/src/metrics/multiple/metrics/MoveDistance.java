package metrics.multiple.metrics;

import java.util.ArrayList;

import game.types.board.RelationType;
import game.types.board.SiteType;
import metrics.Evaluation;
import metrics.ReplayTrial;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.topology.Topology;

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
	public Double[] getMetricValueList(final Evaluation evaluation, final ReplayTrial trial, final Context context)
	{
		final Topology boardTopology = context.game().board().topology();
		if (context.game().booleanConcepts().get(Concept.Cell.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Cell, RelationType.Adjacent);
		if (context.game().booleanConcepts().get(Concept.Edge.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Edge, RelationType.Adjacent);
		if (context.game().booleanConcepts().get(Concept.Vertex.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Vertex, RelationType.Adjacent);
		
		final ArrayList<Double> valueList = new ArrayList<>();

		for (final Move m : trial.fullMoves())
		{
			final SiteType moveType = m.fromType();
			
			if 
			(
				m.fromType() == m.toType() 
				&&
				m.from() < boardTopology.numSites(moveType)
				&&
				m.to() < boardTopology.numSites(moveType)
				&&
				m.from() != m.to()
			)	
			{
				valueList.add(Double.valueOf(boardTopology.distancesToOtherSite(moveType)[m.from()][m.to()]));
			}
			
			context.game().apply(context, m);
		}
		
		
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------

}
