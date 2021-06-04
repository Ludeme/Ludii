package metrics.quality.boardCoverage;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.context.Context;
import other.trial.Trial;

/**
 * Average percentage of board sites which have a piece on it.
 * Note. Only looks at the default site type.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverageAvg extends Metric
{

	
	//-------------------------------------------------------------------------

		/**
		 * Constructor
		 */
		public BoardCoverageAvg()
		{
			super
			(
				"Board Coverage Average", 
				"Average percentage of board sites which have a piece on it.", 
				"Core Ludii metric.", 
				MetricType.OUTCOMES,
				0.0, 
				1.0,
				0.5,
				null
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
			double avgSitesCovered = 0;
			for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
			{
				// Get trial and RNG information
				final Trial trial = trials[trialIndex];
				final RandomProviderState rngState = randomProviderStates[trialIndex];
				
				// Setup a new instance of the game
				final Context context = Utils.setupNewContext(game, rngState);
				
				// Record the index of all sites covered in this trial.
				double numSitesCovered = boardSitesCovered(context).size();
				
				for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
				{
					context.game().apply(context, trial.getMove(i));
					numSitesCovered += boardSitesCovered(context).size();
				}
				
				avgSitesCovered += (numSitesCovered / game.board().numSites()) / (trial.numberRealMoves()+1);
			}

			return avgSitesCovered / trials.length;
		}

		//-------------------------------------------------------------------------
	

}
