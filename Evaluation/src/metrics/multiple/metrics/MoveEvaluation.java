package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.Utils;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Evaluation scores for all moves made.
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
			"Move Evaluation", 
			"Evaluation scores for all moves made.",
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
			valueList.add(Utils.UCTEvaluateMove(context, trial.getMove(i)));
			context.game().apply(context, trial.getMove(i));
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------

}