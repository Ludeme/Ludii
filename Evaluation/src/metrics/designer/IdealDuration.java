package metrics.designer;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.trial.Trial;

/**
 * Metric that measures Average number or turns in a game, based on an ideal range.
 * 
 * @author matthew.stephenson
 */
public class IdealDuration extends Metric
{
	
	private double minTurn = 0;
	private double maxTurn = 1000;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public IdealDuration()
	{
		super
		(
			"Ideal Duration", 
			"Average number or turns in a game, based on an ideal range.", 
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

		//
		//
		// 1-      +-------+__
		//        /|       |  \__
		//       / |       |     \__      
		//      /  |       |        \__
		//     /   |       |           \__
 		// 0- +----+-------+--------------+---
		//    0   Min     Max           2*Max
		
		double tally = 0;
		for (final Trial trial : trials)
		{
			final int numTurns = trial.numTurns();
			double score = 1;
			
			if (numTurns < minTurn)
				score = numTurns / minTurn;
			else if (numTurns > maxTurn)
				score = 1 - Math.min(1, (numTurns - maxTurn) / maxTurn);
			
			tally += score;
		}
		
		return tally / trials.length;
	}
	
	//-------------------------------------------------------------------------

	public void setMinTurn(final double minTurn) 
	{
		this.minTurn = minTurn;
	}

	public void setMaxTurn(final double maxTurn) 
	{
		this.maxTurn = maxTurn;
	}

	//-------------------------------------------------------------------------

}
