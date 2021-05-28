package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.action.Action;
import other.action.die.ActionUpdateDice;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Metric that measures average number of dice rolled per turn.
 * 
 * @author matthew.stephenson
 */
public class DiceRolled extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DiceRolled()
	{
		super
		(
			"Dice rolled", 
			"Average number of dice rolled per turn.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1
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
		
		double avgNumDiceRolled = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the index of all sites covered in this trial.
			double numDiceRolled = 0;
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				numDiceRolled += numDiceRolled(trial.getMove(i));
				context.game().apply(context, trial.getMove(i));
			}
			
			//final int numMoves = trial.numMoves() - trial.numInitialPlacementMoves();
			avgNumDiceRolled += numDiceRolled / trial.numberOfTurnsHalved();					// Not sure if turns or moves..
		}

		return avgNumDiceRolled / trials.length;
	}

	//-------------------------------------------------------------------------
	
	private static int numDiceRolled(final Move m)
	{
		int numDiceRolled = 0;
		for (final Action a : m.actions())
			if (a instanceof ActionUpdateDice)
				numDiceRolled++;
			
		return numDiceRolled;
	}

}
