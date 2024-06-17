package metrics.multiple.metrics;

import java.util.ArrayList;

import main.Constants;
import metrics.Evaluation;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Number of possible moves, when greater than 1.
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
			"Number of possible moves, when greater than 1.", 
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
		final ArrayList<Double> valueList = new ArrayList<>();
		for (final Move m : trial.generateRealMovesList())
		{
			if (context.game().moves(context).moves().size() > 1)
				valueList.add(Double.valueOf(context.game().moves(context).moves().size()));
			
			context.game().apply(context, m);
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		currValueList = new ArrayList<Double>();
	}
	
	//-------------------------------------------------------------------------

}
