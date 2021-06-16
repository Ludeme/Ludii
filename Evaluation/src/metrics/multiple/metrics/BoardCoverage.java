package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.Utils;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage of board sites which have a piece on it in any given turn.
 * Note. Only looks at the default site type.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverage extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverage(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Board Coverage", 
			"Percentage of board sites which have a piece on it in any given turn.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			1.0,
			0.5,
			concept,
			multiMetricValue
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double[] getMetricValueList(final Trial trial, final Context context)
	{
		final ArrayList<Double> valueList = new ArrayList<>();
		valueList.add(Double.valueOf(Utils.boardSitesCovered(context).size()));
		for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
		{
			context.game().apply(context, trial.getMove(i));
			valueList.add(Double.valueOf(Utils.boardSitesCovered(context).size()));
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------


}
