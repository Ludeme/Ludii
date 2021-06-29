package metrics.multiple.metrics;

import java.util.ArrayList;

import metrics.Utils;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * The number of pieces on the board.
 * 
 * @author matthew.stephenson
 */
public class PieceNumber extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public PieceNumber(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Piece Number " + multiMetricValue.name(), 
			"The number of pieces on the board.", 
			-1, 
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
		valueList.add(Double.valueOf(Utils.numPieces(context)));
		for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
		{
			context.game().apply(context, trial.getMove(i));
			valueList.add(Double.valueOf(Utils.numPieces(context)));
		}
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------


}
