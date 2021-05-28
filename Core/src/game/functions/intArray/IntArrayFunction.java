package game.functions.intArray;

import java.util.BitSet;

import game.Game;
import game.types.state.GameType;
import other.context.Context;

/**
 * Returns an int array function.
 * 
 * @author cambolbro
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public interface IntArrayFunction extends GameType
{
	/**
	 * @param context
	 * @return The result of applying this function to this trial.
	 */
	public int[] eval(final Context context);

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

}
