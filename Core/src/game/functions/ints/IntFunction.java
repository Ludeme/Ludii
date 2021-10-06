package game.functions.ints;

import java.util.BitSet;

import game.Game;
import game.types.state.GameType;
import other.context.Context;

/**
 * Returns an int.
 * 
 * @author cambolbro and Eric.Piette
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public interface IntFunction extends GameType
{
	/**
	 * @param context The context.
	 * @return The result of applying this function to this trial.
	 */
	int eval(final Context context);
	
	/**
	 * @param context
	 * @param other
	 * @return True if this function would return a value that exceeds the given other
	 */
	boolean exceeds(final Context context, final IntFunction other);

	/**
	 * @return if the IntFunction is a hint.
	 */
	boolean isHint();

	/**
	 * @param game The game.
	 * @return Accumulated flags corresponding to the game concepts.
	 */
	BitSet concepts(final Game game);

	/**
	 * @return Accumulated flags corresponding to read data in EvalContext.
	 */
	BitSet readsEvalContextRecursive();

	/**
	 * @return Accumulated flags corresponding to write data in EvalContext.
	 */
	BitSet writesEvalContextRecursive();

	/**
	 * @param game The game.
	 * @return True if a required ludeme is missing.
	 */
	boolean missingRequirement(final Game game);

	/**
	 * @param game The game.
	 * @return True if the ludeme can crash the game during its play.
	 */
	boolean willCrash(final Game game);
	
	/**
	 * @param game
	 * @return This IntFunction in English.
	 */
	String toEnglish(final Game game);
	
	/**
	 * @return if is in hand.
	 */
	public boolean isHand();
}
