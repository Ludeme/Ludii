package metrics.multiple.branchingFactor;

import java.util.ArrayList;

import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Average number of possible moves.
 * 
 * @author matthew.stephenson
 */
public class BranchingFactorAvg extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BranchingFactorAvg()
	{
		super
		(
			"Branching Factor Avg", 
			"Average number of possible moves.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			Concept.BranchingFactor
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
