package game.util.moves;

import game.functions.ints.IntFunction;
import game.functions.ints.state.Mover;
import other.BaseLudeme;

/**
 * Specifies operations based on the ``who'' data.
 * 
 * @author Eric.Piette
 */
public class Player extends BaseLudeme
{
	/** The index of the player. */
	private final IntFunction index;

	//-------------------------------------------------------------------------

	/** The index function returned by this class. */
	private final IntFunction indexReturned;

	//-------------------------------------------------------------------------

	/**
	 * @param index The index of the player [(mover)].
	 * 
	 * @example (player (mover))
	 * @example (player 2)
	 */
	public Player
	(
		final IntFunction index
	)
	{
		this.index = index;
		this.indexReturned = (index == null) ? new Mover()
				: index;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The player index.
	 */
	public IntFunction originalIndex()
	{
		return index;
	}

	/**
	 * @return The player index returned by the index and the role.
	 */
	public IntFunction index()
	{
		return indexReturned;
	}
}
