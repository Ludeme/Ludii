package game.functions.dim;

import game.types.state.GameType;

/**
 * Returns an integer corresponding to a dimension.
 * 
 * @author Eric.Piette and cambolbro
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public interface DimFunction extends GameType
{
	/**
	 * @return The result of the function.
	 */
	public int eval();
}
