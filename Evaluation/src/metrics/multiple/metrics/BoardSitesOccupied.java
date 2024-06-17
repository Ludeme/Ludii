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
 * Percentage of board sites which have a piece on it.
 * Note. Only looks at the default site type.
 * 
 * @author matthew.stephenson
 */
public class BoardSitesOccupied extends MultiMetricFramework
{

	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected int numberDefaultBoardSites = 0;
	
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
	public Double[] getMetricValueList(final Evaluation evaluation, final Trial trial, final Context context)
	{
		final ArrayList<Double> valueList = new ArrayList<>();
		final int numberDefaultBoardSites = context.board().topology().getGraphElements(context.board().defaultSite()).size();
		valueList.add(Double.valueOf(Double.valueOf(Utils.boardDefaultSitesCovered(context).size()).doubleValue() / numberDefaultBoardSites));
		for (final Move m : trial.generateRealMovesList())
		{
			context.game().apply(context, m);
			valueList.add(Double.valueOf(Double.valueOf(Utils.boardDefaultSitesCovered(context).size()).doubleValue() / numberDefaultBoardSites));
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		currValueList = new ArrayList<Double>();
		numberDefaultBoardSites = context.board().topology().getGraphElements(context.board().defaultSite()).size();
		currValueList.add(Double.valueOf(Double.valueOf(Utils.boardDefaultSitesCovered(context).size()).doubleValue() / numberDefaultBoardSites));
	}
	
	//-------------------------------------------------------------------------


}
