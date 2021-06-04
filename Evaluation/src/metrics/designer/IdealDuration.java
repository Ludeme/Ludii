package metrics.designer;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.trial.Trial;

/**
 * Metric that measures Average number or turns in a game (based on a designer or automatic ideal range).
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
			"Average number or turns in a game.", 
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
		if (trials.length == 0)
			return 0;
		
//		float numberInRange = 0;
//		for (final Trial trial : trials)
//		{
//			
//			if (trial.moves().size() >= minTurn && trial.moves().size() <= maxTurn)
//				numberInRange++;
//		}
//		
//		final double length = numberInRange / (double)trials.length;
//
//		return length;

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
