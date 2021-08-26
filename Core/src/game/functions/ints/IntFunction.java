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
	public int eval(final Context context);
	
	/**
	 * @param context
	 * @param other
	 * @return True if this function would return a value that exceeds the given other
	 */
	public boolean exceeds(final Context context, final IntFunction other);

	/**
	 * @return if the IntFunction is a hint.
	 */
	public boolean isHint();

	/**
	 * @param game The game.
	 * @return Accumulated flags corresponding to the game concepts.
	 */
	public BitSet concepts(final Game game);

	/**
	 * @return Accumulated flags corresponding to read data in EvalContext.
	 */
	public BitSet readsEvalContextRecursive();

	/**
	 * @return Accumulated flags corresponding to write data in EvalContext.
	 */
	public BitSet writesEvalContextRecursive();

	/**
	 * @param game The game.
	 * @return True if a required ludeme is missing.
	 */
	public boolean missingRequirement(final Game game);

	/**
	 * @param game The game.
	 * @return True if the ludeme can crash the game during its play.
	 */
	public boolean willCrash(final Game game);
	
	public String toEnglish(final Game game);
}
