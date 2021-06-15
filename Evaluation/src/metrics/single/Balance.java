package metrics.single;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Similarity between player win rates.
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
			"Similarity between player win rates.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			1.0,
			1.0,
			Concept.Balance
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final String args, 
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		final int numPlayers = game.players().count();
		
		// Count number of wins per player
		final int[] wins = new int[numPlayers + 1];		
		for (final Trial trial : trials)
			wins[trial.status().winner()]++;
		
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
		
		return 1.0 - maxDisc;
	}

	//-------------------------------------------------------------------------

}