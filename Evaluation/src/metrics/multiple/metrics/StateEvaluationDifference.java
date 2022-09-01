package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.Evaluation;
import metrics.Utils;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Difference in player state evaluations.
 * 
 * @author matthew.stephenson
 */
public class StateEvaluationDifference extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public StateEvaluationDifference(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"State Evaluation Difference " + multiMetricValue.name(), 
			"Difference in player state evaluations.",
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
		valueList.add(Double.valueOf(getStateEvaluationDiscrepancy(evaluation, context)));
		for (final Move m : trial.generateRealMovesList())
		{
			context.game().apply(context, m);
			valueList.add(Double.valueOf(getStateEvaluationDiscrepancy(evaluation, context)));
		}
		return valueList.toArray(new Double[0]);
	}
	
	//-------------------------------------------------------------------------
	
	private static double getStateEvaluationDiscrepancy(final Evaluation evaluation, final Context context)
	{
		final int numPlayers = context.game().players().count();
		final ArrayList<Double> allPlayerStateEvaluations = Utils.allPlayerStateEvaluations(evaluation, context);

		// Find maximum discrepancy
		double maxDisc = 0.0;
		for (int pa = 1; pa <= numPlayers; pa++)
		{
			for (int pb = pa+1; pb <= numPlayers; pb++)
			{
				final double disc = Math.abs(allPlayerStateEvaluations.get(pa).doubleValue() - allPlayerStateEvaluations.get(pb).doubleValue());
				if (disc > maxDisc)
					maxDisc = disc;
			}
		}
		
		return maxDisc;
	}

	//-------------------------------------------------------------------------

}