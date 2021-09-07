package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.Evaluation;
import metrics.Utils;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Evaluation values for each move.
 * 
 * @author matthew.stephenson
 */
public class MoveEvaluation extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public MoveEvaluation(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Move Evaluation " + multiMetricValue.name(), 
			"Evaluation values for each move.",
			0.0, 
			1.0,
			concept,
			multiMetricValue
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double[] getMetricValueList(final Evaluation evaluation, final Trial trial, final Context context)
	{
		final ArrayList<Double> valueList = new ArrayList<>();
		for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
		{
			valueList.add(Utils.evaluateMove(evaluation, context, trial.getMove(i)));
			context.game().apply(context, trial.getMove(i));
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------

}