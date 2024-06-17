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
		currValueList.add(Double.valueOf(context.game().moves(context).moves().size()));
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		if (!context.trial().over())
			currValueList.add(Double.valueOf(context.game().moves(context).moves().size()));
	}
	
	//-------------------------------------------------------------------------

}
