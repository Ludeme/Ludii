package metrics.single;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import gnu.trove.set.hash.TIntHashSet;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Percentage of board sites which a piece touched at some point.
 * Note. Only looks at the default site type.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverageTotal extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverageTotal()
	{
		super
		(
			"Board Coverage", 
			"Percentage of board sites which a piece touched at some point.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			1.0,
			0.5,
			Concept.BoardCoverage
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
		double numSitesCovered = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the index of all sites covered in this trial.
			final TIntHashSet sitesCovered = new TIntHashSet();
			
			sitesCovered.addAll(Utils.boardSitesCovered(context));
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				sitesCovered.addAll(Utils.boardSitesCovered(context));
			}
			
			numSitesCovered += ((double) sitesCovered.size()) / game.board().numSites();
		}

		return numSitesCovered / trials.length;
	}

}
