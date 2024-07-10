package metrics.single.outcome;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.RankUtils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage of games which end in a draw (not including timeouts).
 * 
 * @author matthew.stephenson
 */
public class Drawishness extends Metric
{

	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected double naturalDraws = 0.0;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Drawishness()
	{
		super
		(
			"Drawishness", 
			"Percentage of games which end in a draw (not including timeouts).", 
			0.0, 
			1.0,
			Concept.Drawishness
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		final int numPlayers = game.players().count();
		if (numPlayers <= 1)
			return null;
		
		// Count number of draws
		double naturalDraws = 0.0;
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = trials[i];
			final RandomProviderState rng = randomProviderStates[i];
			final Context context = Utils.setupTrialContext(game, rng, trial);
			
			// No players have won/lost.
			boolean allRankingZero = true;
			for (int j = 1; j < RankUtils.agentUtilities(context).length; j++)
			{
				if (RankUtils.agentUtilities(context)[j] != 0.0)
				{
					allRankingZero = false;
					break;
				}
			}
			
			if (allRankingZero)
				naturalDraws++;
		}

		return Double.valueOf(naturalDraws / trials.length);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		// Do nothing
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		// Do nothing
	}
	
	@Override
	public void observeFinalState(final Context context)
	{
		boolean allRankingZero = true;
		for (int j = 1; j < RankUtils.agentUtilities(context).length; j++)
		{
			if (RankUtils.agentUtilities(context)[j] != 0.0)
			{
				allRankingZero = false;
				break;
			}
		}
		
		if (allRankingZero)
			naturalDraws++;
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		return naturalDraws / numTrials;
	}
	
	//-------------------------------------------------------------------------

}
