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
 * Difference in player scores.
 * 
 * @author matthew.stephenson
 */
public class ScoreDifference extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public ScoreDifference(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Score Difference " + multiMetricValue.name(), 
			"Difference in player scores.", 
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
		valueList.add(Double.valueOf(getScoreDiscrepancy(context)));
		for (final Move m : trial.generateRealMovesList())
		{
			context.game().apply(context, m);
			valueList.add(Double.valueOf(getScoreDiscrepancy(context)));
		}
		return valueList.toArray(new Double[0]);
	}
	
	//-------------------------------------------------------------------------
	
	private static double getScoreDiscrepancy(final Context context)
	{
		if (!context.game().requiresScore())
			return 0.0;
		
		final int numPlayers = context.game().players().count();
		final int[] score = new int[numPlayers + 1];	
		
		for (int p = 1; p <= numPlayers; p++)
			score[p] += context.score(p);

		// Find maximum discrepancy
		double maxDisc = 0.0;
		for (int pa = 1; pa <= numPlayers; pa++)
		{
			for (int pb = pa+1; pb <= numPlayers; pb++)
			{
				final double disc = Math.abs(score[pa] - score[pb]);
				if (disc > maxDisc)
					maxDisc = disc;
			}
		}
		
		return maxDisc;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		currValueList = new ArrayList<Double>();
		currValueList.add(Double.valueOf(getScoreDiscrepancy(context)));
	}
	
	//-------------------------------------------------------------------------

}