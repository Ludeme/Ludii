package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * Metric that measures percentage of sites on board which a piece touched.
 * Note. Only looks at the default site type.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverage extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverage()
	{
		super
		(
			"Board Coverage", 
			"Percentage of sites on board which a piece touched.", 
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
		if (trials.length == 0)
			return 0;
		
		double avgSitesCovered = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the index of all sites covered in this trial.
			final TIntHashSet sitesCovered = new TIntHashSet();
			
			sitesCovered.addAll(boardSitesCovered(context));
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				sitesCovered.addAll(boardSitesCovered(context));
			}
			
			avgSitesCovered += ((double) sitesCovered.size()) / game.board().numSites();
		}

		return avgSitesCovered / trials.length;
	}

	//-------------------------------------------------------------------------
	
	private static TIntArrayList boardSitesCovered(final Context context)
	{
		final TIntArrayList boardSitesCovered = new TIntArrayList();
		final ContainerState cs = context.containerState(0);
		
		for (int i = 0; i < context.game().board().numSites(); i++)
			if (cs.what(i, context.game().board().defaultSite()) != 0)
				boardSitesCovered.add(i);
		
		return boardSitesCovered;
	}

}
