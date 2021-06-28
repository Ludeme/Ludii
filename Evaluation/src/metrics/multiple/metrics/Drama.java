package metrics.multiple.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import metrics.Utils;
import metrics.multiple.MultiMetricFramework;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Number of moves/severity where the eventual winner was behind.
 * 
 * @author matthew.stephenson
 */
public class Drama extends MultiMetricFramework
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Drama(final MultiMetricValue multiMetricValue, final Concept concept)
	{
		super
		(
			"Drama", 
			"Number of moves/severity where the eventual winner was behind.", 
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
		
		final ArrayList<Integer> highestRankedPlayers = new ArrayList<>();
		final double highestRanking = Arrays.stream(trial.ranking()).max().getAsDouble();
		for (int i = 1; i <= context.game().players().count(); i++)
			if (trial.ranking()[i] == highestRanking)
				highestRankedPlayers.add(i);
		
		if (highestRankedPlayers.size() > 0)
		{
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				// Get the highest state evaluation for any player.
				final ArrayList<Double> allPlayerStateEvaulations = Utils.UCTAllPlayerStateEvaulations(context);
				final double highestStateEvaluation = Collections.max(allPlayerStateEvaulations);
				
				// Get the average difference between the winning player(s) and the highest state evaluation.
				double differenceBetweenWinnersAndMax = 0.0;
				for (final int highestRankedPlayer : highestRankedPlayers)
				{
					final double playerStateEvaluation = allPlayerStateEvaulations.get(highestRankedPlayer);
					differenceBetweenWinnersAndMax += (highestStateEvaluation-playerStateEvaluation)/highestRankedPlayers.size();
				}
				
				valueList.add(Double.valueOf(differenceBetweenWinnersAndMax));
				context.game().apply(context, trial.getMove(i));
			}
		}
		else
		{
			System.out.println("ERROR, highestRankedPlayers list is empty");
		}
		
		return valueList.toArray(new Double[0]);
	}

	//-------------------------------------------------------------------------

}
