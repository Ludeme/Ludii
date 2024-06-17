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
 * Percentage of games where player 1 won. Draws and multi-player results calculated as partial wins.
 * 
 * @author matthew.stephenson
 */
public class AdvantageP1 extends Metric
{

	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected double p1Wins = 0.0;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public AdvantageP1()
	{
		super
		(
			"AdvantageP1", 
			"Percentage of games where player 1 won. Draws and multi-player results calculated as partial wins.", 
			0.0, 
			1.0,
			Concept.AdvantageP1
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
		if (game.players().count() <= 1)
			return null;
		
		double p1Wins = 0.0;
		
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = trials[i];
			final RandomProviderState rng = randomProviderStates[i];
			final Context context = Utils.setupTrialContext(game, rng, trial);
			p1Wins += (RankUtils.agentUtilities(context)[1] + 1.0) / 2.0;
		}

		return Double.valueOf(p1Wins / trials.length);
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
		p1Wins += (RankUtils.agentUtilities(context)[1] + 1.0) / 2.0;
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		return p1Wins / numTrials;
	}
	
	//-------------------------------------------------------------------------

}