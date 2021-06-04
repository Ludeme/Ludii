package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Difference in player scores at the end of the game.
 * 
 * @author matthew.stephenson
 */
public class ScoreDifference extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public ScoreDifference()
	{
		super
		(
			"Score Difference", 
			"ifference in player scores at the end of the game.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			-1,
			0.0,
			Concept.ScoreDifference
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
		if (!game.requiresScore())
			return 0.0;
		
		final int numPlayers = game.players().count();
		
		// Count number of wins per player
		final int[] score = new int[numPlayers + 1];		
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
				context.game().apply(context, trial.getMove(i));

			for (int playerId = 1; playerId <= numPlayers; playerId++)
				score[playerId] += context.score(playerId);
		}
		
		// Get mean score over all players
		final double[] rate = new double[numPlayers + 1];
		for (int p = 1; p <= numPlayers; p++)
			rate[p] = score[p] / (double)trials.length;

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
		return maxDisc;
	}

	//-------------------------------------------------------------------------

}