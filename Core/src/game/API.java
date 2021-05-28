package game;

import java.util.List;
import java.util.Random;

import game.rules.play.moves.Moves;
import other.AI;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;

/**
 * Game API.
 * 
 * @author cambolbro
 */
public interface API
{
	/** Initialise the game graph and other relevant items. */
	public void create();

	//-------------------------------------------------------------------------

	/**
	 * Start new instance of the game.
	 * 
	 * @param context
	 */
	public void start(final Context context);

	//-------------------------------------------------------------------------

	/**
	 * @param context
	 * @return Legal turns from the current state.
	 */
	public Moves moves(final Context context);

	//-------------------------------------------------------------------------

	/**
	 * Apply the specified instructions (i.e. game turn).
	 * 
	 * @param context
	 * @param move
	 * @return Move object as applied, possibly with additional Actions from consequents
	 */
	public Move apply(final Context context, final Move move);

	//-------------------------------------------------------------------------

	/**
	 * Play out game to conclusion from current state.
	 * 
	 * @param context
	 * @param ais     List of AI move planners for each player.
	 * @param thinkingTime The maximum number of seconds that AIs are allowed 
	 * to spend per turn
	 * @param playoutMoveSelector Playout move selector to select moves non-uniformly
	 * @param maxNumBiasedActions Maximum number of actions for which to bias
	 * selection using features (-1 for no limit)
	 * @param maxNumPlayoutActions Maximum number of actions to be applied,
	 * after which we will simply return a null result (-1 for no limit)
	 * @param random RNG for selecting actions
	 * @return A fully played-out Trial object.
	 */
	public Trial playout
	(
		final Context context, 
		final List<AI> ais, 
		final double thinkingTime,
		final PlayoutMoveSelector playoutMoveSelector,
		final int maxNumBiasedActions,
		final int maxNumPlayoutActions,
		final Random random
	);
}
