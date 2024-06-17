package metrics.single.boardCoverage;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Percentage of default board sites which a piece was placed on at some point.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverageDefault extends Metric
{
	
	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected double numSitesCovered = 0.0;
	
	/** For incremental computation */
	protected Set<TopologyElement> sitesCovered = null;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverageDefault()
	{
		super
		(
			"Board Coverage Default", 
			"Percentage of default board sites which a piece was placed on at some point.", 
			0.0, 
			1.0,
			Concept.BoardCoverageDefault
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
			
			sitesCovered.addAll(Utils.boardDefaultSitesCovered(context));
			for (final Move m : trial.generateRealMovesList())
			{
				context.game().apply(context, m);
				sitesCovered.addAll(Utils.boardDefaultSitesCovered(context));
			}
			
			numSitesCovered += ((double) sitesCovered.size()) / context.board().topology().getGraphElements(context.board().defaultSite()).size();
		}

		return Double.valueOf(numSitesCovered / trials.length);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		sitesCovered = new HashSet<TopologyElement>();
		sitesCovered.addAll(Utils.boardDefaultSitesCovered(context));
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		sitesCovered.addAll(Utils.boardDefaultSitesCovered(context));
	}
	
	@Override
	public void observeFinalState(final Context context)
	{
		numSitesCovered += ((double) sitesCovered.size()) / context.board().topology().getGraphElements(context.board().defaultSite()).size();
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		return numSitesCovered / numTrials;
	}
	
	//-------------------------------------------------------------------------

}
