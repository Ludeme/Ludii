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
 * Similarity between player win-rates. Draws and multi-player results calculated as partial wins.
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Balance extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Balance()
	{
		super
		(
			"Balance", 
			"Similarity between player win-rates. Draws and multi-player results calculated as partial wins.", 
			0.0, 
			1.0,
			Concept.Balance
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
		
		// Count number of wins per player
		final int[] wins = new int[numPlayers + 1];		
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = trials[i];
			final RandomProviderState rng = randomProviderStates[i];
			final Context context = Utils.setupTrialContext(game, rng, trial);
			
			for (int p = 1; p <= numPlayers; p++) 
				wins[p] += (RankUtils.agentUtilities(context)[p] + 1.0) / 2.0;
		}
		
		// Get mean win rate over all players
		final double[] rate = new double[numPlayers + 1];
		for (int p = 1; p <= numPlayers; p++)
			rate[p] = wins[p] / (double)trials.length;

		// Find maximum discrepancy
		double maxDisc = 0.0;
		for (int pa = 1; pa <= numPlayers; pa++)
		{
			for (int pb = pa+1; pb <= numPlayers; pb++)
			{
				final double disc = Math.abs(rate[pa] - rate[pb]);
				if (disc > maxDisc)
					maxDisc = disc;
			}
		}
		
		return Double.valueOf(1.0 - maxDisc);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------

}