package metrics.multiple.pieceNumber;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Difference in the number of pieces at the start vs. the end of the game.
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
			"Difference in the number of pieces at the start vs. the end of the game.", 
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
		double avgPieceDifference = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			final int numStartPieces = Utils.boardSitesCovered(context).size();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
				context.game().apply(context, trial.getMove(i));
			
			final int numEndPieces = Utils.boardSitesCovered(context).size();
			
			avgPieceDifference += numEndPieces - numStartPieces;
		}

		return avgPieceDifference / trials.length;
	}

	//-------------------------------------------------------------------------


}
