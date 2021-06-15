package metrics.quality.boardCoverage;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.context.Context;
import other.trial.Trial;

/**
 * Average increase in percentage of board sites which have a piece on it in any given turn.
 * Note. Only looks at the default site type.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverageChange extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverageChange()
	{
		super
		(
			"Board Coverage Change", 
			"Average increase in percentage of board sites which have a piece on it in any given turn.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			-1, 
			-1,
			0.0,
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
		double avgSitesCoveredIncrease = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the index of all sites covered in this trial.
			double numSitesCoveredIncreases = 0;
			double lastNumSitesCovered = Utils.boardSitesCovered(context).size();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				numSitesCoveredIncreases += context.game().moves(context).moves().size() - lastNumSitesCovered;
				lastNumSitesCovered = Utils.boardSitesCovered(context).size();
			}
			
			avgSitesCoveredIncrease += numSitesCoveredIncreases / (trial.numberRealMoves());
		}

		return avgSitesCoveredIncrease / trials.length;
	}

	//-------------------------------------------------------------------------


}
