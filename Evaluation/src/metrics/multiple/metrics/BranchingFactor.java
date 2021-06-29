package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Number of possible moves.
 * 
 * @author matthew.stephenson
 */
public class BranchingFactor extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BranchingFactor(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Branching Factor " + multiMetricValue.name(), 
			"Number of possible moves.", 
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
		final ArrayList<Double> valueList = new ArrayList<>();
		for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
		{
			valueList.add(Double.valueOf(context.game().moves(context).moves().size()));
			context.game().apply(context, trial.getMove(i));
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------

}
