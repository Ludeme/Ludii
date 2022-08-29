package game.rules.start;

import game.Game;
import game.rules.Rule;
import other.BaseLudeme;

/**
 * Sets the initial setup rule for the start of each trial (i.e. game).
 * 
 * @author cambolbro
 */
public abstract class StartRule extends BaseLudeme implements Rule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * @param game The game.
	 * @return The state of the starting rule.
	 */
	@SuppressWarnings("static-method")
	public int state(final Game game)
	{
		return 0;
	}

	/**
	 * @param game The game.
	 * @return The count of the starting rule.
	 */
	@SuppressWarnings("static-method")
	public int count(final Game game)
	{
		return 0;
	}

	/**
	 * @param game The game.
	 * @return The number of component to place.
	 */
	@SuppressWarnings("static-method")
	public int howManyPlace(final Game game)
	{
		return 0;
	}

	/**
	 * @return True if the starting rule is a set rule.
	 */
	@SuppressWarnings("static-method")
	public boolean isSet()
	{
		return false;
	}
}
