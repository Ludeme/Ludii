package metrics.single.boardCoverage;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.context.Context;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Percentage of used board sites which a piece was placed on at some point.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverageUsed extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverageUsed()
	{
		super
		(
			"Board Coverage Used", 
			"Percentage of used board sites which a piece was placed on at some point.", 
			0.0, 
			1.0,
			null
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
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
			
			// Record all sites covered in this trial.
			final Set<TopologyElement> sitesCovered = new HashSet<TopologyElement>();
			
			sitesCovered.addAll(Utils.boardUsedSitesCovered(context));
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				sitesCovered.addAll(Utils.boardUsedSitesCovered(context));
			}
			
			numSitesCovered += ((double) sitesCovered.size()) / game.board().topology().getAllUsedGraphElements(context.game()).size();
		}

		return numSitesCovered / trials.length;
	}

}
