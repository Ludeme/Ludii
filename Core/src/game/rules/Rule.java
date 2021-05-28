package game.rules;

import game.types.state.GameType;
import other.Ludeme;
import other.context.Context;

/**
 * Defines a rule of the game.
 * 
 * @author cambolbro
 */
public interface Rule extends GameType, Ludeme
{
	/**
	 * @param context
	 */
	public void eval(final Context context);
}
