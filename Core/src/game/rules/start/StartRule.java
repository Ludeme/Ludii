package game.rules.start;

import game.Game;
import game.rules.Rule;
import other.BaseLudeme;

/**
 * Sets the initial setup rule for the start of each trial (i.e. game).
 * 
 * @author cambolbro
 */
@SuppressWarnings("static-method")
public abstract class StartRule extends BaseLudeme implements Rule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * @param game The game.
	 * @return The state of the starting rule.
	 */
	public int state(final Game game)
	{
		return 0;
	}

	/**
	 * @param game The game.
	 * @return The count of the starting rule.
	 */
	public int count(final Game game)
	{
		return 0;
	}

	/**
	 * @param game The game.
	 * @return The number of component to place.
	 */
	public int howManyPlace(final Game game)
	{
		return 0;
	}

	/**
	 * @return True if the starting rule is a set rule.
	 */
	public boolean isSet()
	{
		return false;
	}
}
