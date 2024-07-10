package metrics.multiple.metrics;

import java.util.ArrayList;

import game.types.board.RelationType;
import game.types.board.SiteType;
import main.Constants;
import metrics.Evaluation;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
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

	/** For incremental computation */
	protected Topology boardTopology = null;
	
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
			Constants.INFINITY,
			concept,
			multiMetricValue
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double[] getMetricValueList(final Evaluation evaluation, final Trial trial, final Context context)
	{
		final Topology boardTopology = context.board().topology();
		if (context.game().booleanConcepts().get(Concept.Cell.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Cell, RelationType.Adjacent);
		if (context.game().booleanConcepts().get(Concept.Edge.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Edge, RelationType.Adjacent);
		if (context.game().booleanConcepts().get(Concept.Vertex.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Vertex, RelationType.Adjacent);
		
		final ArrayList<Double> valueList = new ArrayList<>();

		for (final Move m : trial.generateRealMovesList())
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
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		boardTopology = context.board().topology();
		if (context.game().booleanConcepts().get(Concept.Cell.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Cell, RelationType.Adjacent);
		if (context.game().booleanConcepts().get(Concept.Edge.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Edge, RelationType.Adjacent);
		if (context.game().booleanConcepts().get(Concept.Vertex.id()))
			boardTopology.preGenerateDistanceToEachElementToEachOther(SiteType.Vertex, RelationType.Adjacent);
		
		currValueList = new ArrayList<Double>();
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		final Move m = context.trial().lastMove();
		
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
			currValueList.add(Double.valueOf(boardTopology.distancesToOtherSite(moveType)[m.from()][m.to()]));
		}
	}
	
	//-------------------------------------------------------------------------

}
