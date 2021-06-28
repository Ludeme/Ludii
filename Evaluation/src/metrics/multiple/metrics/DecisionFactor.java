package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Number of possible moves, when the number of possible moves is greater than 1.
 * 
 * @author matthew.stephenson
 */
public class DecisionFactor extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DecisionFactor(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Decision Factor " + multiMetricValue.name(), 
			"Number of possible moves, when the number of possible moves is greater than 1.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			concept,
			multiMetricValue
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double[] getMetricValueList(final Trial trial, final Context context)
	{
		final ArrayList<Double> valueList = new ArrayList<>();
		for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
		{
			if (context.game().moves(context).moves().size() > 1)
				valueList.add(Double.valueOf(context.game().moves(context).moves().size()));
			
			context.game().apply(context, trial.getMove(i));
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------

}
