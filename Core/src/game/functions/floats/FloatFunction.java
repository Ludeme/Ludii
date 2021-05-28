package game.functions.floats;

import java.util.BitSet;

import game.Game;
import game.types.state.GameType;
import other.context.Context;

/**
 * Returns a float.
 * 
 * @author cambolbro
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public interface FloatFunction extends GameType
{
	/**
	 * @param context
	 * @return The result of applying this function to this trial.
	 */
	public float eval(final Context context);

	/**
	 * @param game The game.
	 * @return Accumulated flags corresponding to the game concepts.
	 */
	public BitSet concepts(final Game game);

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
