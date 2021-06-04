package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * Metric that measures Change in the number of pieces at the start vs. the end of the game.
 * 
 * @author matthew.stephenson
 */
public class PieceNumberChange extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public PieceNumberChange()
	{
		super
		(
			"Piece Number Change", 
			"Change in the number of pieces at the start vs. the end of the game.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			-1, 
			-1,
			0.0,
			Concept.PieceNumberChange
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
		
		double avgPieceDifference = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			final int numStartPieces = boardSitesCovered(context).size();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
				context.game().apply(context, trial.getMove(i));
			
			final int numEndPieces = boardSitesCovered(context).size();
			
			avgPieceDifference += numEndPieces - numStartPieces;
		}

		return avgPieceDifference / trials.length;
	}

	//-------------------------------------------------------------------------
	
	private static TIntArrayList boardSitesCovered(final Context context)
	{
		final TIntArrayList boardSitesCovered = new TIntArrayList();
		final ContainerState cs = context.containerState(0);
		
		for (int i = 0; i < context.game().board().numSites(); i++)
			if (cs.what(i, context.board().defaultSite()) != 0)			// TODO look at all sites.
				boardSitesCovered.add(i);
		
		return boardSitesCovered;
	}

}
