package metrics;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import other.AI;
import other.context.Context;
import other.trial.Trial;
import search.mcts.MCTS;

/**
 * Helpful functions for metric analysis.
 * 
 * @author Matthew.Stephenson
 */
public class Utils 
{
	
	//-------------------------------------------------------------------------

	/**
	 * @param game
	 * @param rngState
	 * @return A new context for a given game and RNG.
	 */
	public static Context setupNewContext(final Game game, final RandomProviderState rngState)
	{
		final Context context = new Context(game, new Trial(game));
		context.rng().restoreState(rngState);
		context.reset();
		context.state().initialise(context.currentInstanceContext().game());
		game.start(context);
		context.trial().setStatus(null);
		return context;
	}
	
	//-------------------------------------------------------------------------
	
	public static double UCTEvaluateState(final Context context)
	{
		final AI agent = MCTS.createUCT();
		agent.selectAction(context.game(), context, 0.5, -1, -1);		
		return agent.estimateValue();
	}
	
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	
}
