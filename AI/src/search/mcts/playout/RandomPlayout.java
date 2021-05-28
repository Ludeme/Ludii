package search.mcts.playout;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import other.context.Context;
import other.trial.Trial;
import search.mcts.MCTS;

/**
 * A completely random Play-out strategy (selects actions according
 * to a uniform distribution).
 * 
 * @author Dennis Soemers
 */
public final class RandomPlayout implements PlayoutStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Auto-end playouts in a draw if they take more turns than this */
	protected int playoutTurnLimit = -1;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public RandomPlayout()
	{
		playoutTurnLimit = -1;	// no limit
	}
	
	/**
	 * Constructor
	 * @param playoutTurnLimit
	 */
	public RandomPlayout(final int playoutTurnLimit)
	{
		this.playoutTurnLimit = playoutTurnLimit;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Trial runPlayout(final MCTS mcts, final Context context) 
	{
		return context.game().playout(context, null, 1.0, null, 0, playoutTurnLimit, ThreadLocalRandom.current());
	}
	
	@Override
	public int backpropFlags()
	{
		return 0;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean playoutSupportsGame(final Game game)
	{
		if (game.isDeductionPuzzle())
			return (playoutTurnLimit() > 0);
		else
			return true;
	}

	@Override
	public void customise(final String[] inputs)
	{
		for (int i = 1; i < inputs.length; ++i)
		{
			final String input = inputs[i];
			
			if (input.toLowerCase().startsWith("playoutturnlimit="))
			{
				playoutTurnLimit = 
						Integer.parseInt
						(
							input.substring("playoutturnlimit=".length())
						);
			}
		}
	}
	
	/**
	 * @return The turn limit we use in playouts
	 */
	public int playoutTurnLimit()
	{
		return playoutTurnLimit;
	}

	//-------------------------------------------------------------------------

}
