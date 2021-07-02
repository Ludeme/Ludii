package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.Utils;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage of board sites which have a piece on it.
 * Note. Only looks at the default site type.
 * 
 * @author matthew.stephenson
 */
public class BoardSitesOccupied extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardSitesOccupied(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Board Sites Occupied " + multiMetricValue.name(), 
			"Percentage of board sites which have a piece on it.", 
			0.0, 
			1.0,
			concept,
			multiMetricValue
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double[] getMetricValueList(final Trial trial, final Context context)
	{
		final ArrayList<Double> valueList = new ArrayList<>();
		final int numberDefaultBoardSites = context.board().topology().getGraphElements(context.board().defaultSite()).size();
		valueList.add(Double.valueOf(Utils.boardDefaultSitesCovered(context).size()) / numberDefaultBoardSites);
		for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
		{
			context.game().apply(context, trial.getMove(i));
			valueList.add(Double.valueOf(Utils.boardDefaultSitesCovered(context).size()) / numberDefaultBoardSites);
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------


}
