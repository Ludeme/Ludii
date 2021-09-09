package game.util.moves;

import game.Game;
import other.BaseLudeme;

/**
 * Sets the flips state of a piece.
 * 
 * @author Eric.Piette
 */
public class Flips extends BaseLudeme
{
	/** The first flip value. */
	final private int flipA;

	/** The second flip value. */
	final private int flipB;

	//-------------------------------------------------------------------------

	/**
	 * @param flipA The first state of the flip.
	 * @param flipB The second state of the flip.
	 * @example (flips 1 2)
	 */
	public Flips
	(
		final Integer flipA, 
		final Integer flipB
	)
	{
		this.flipA = flipA.intValue();
		this.flipB = flipB.intValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @param currentState
	 * 
	 * @return The other state.
	 */
	public int flipState(final int currentState)
	{
		if (currentState == flipA)
			return flipB;
		else if (currentState == flipB)
			return flipA;
		else
			return currentState; // no flip state, just return current state
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The flipA state.
	 */
	public int flipA()
	{
		return flipA;
	}

	/**
	 * @return The flipB state.
	 */
	public int flipB()
	{
		return flipB;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "with a " + flipA + " on one side and a " + flipB + " on the other side";
	}

}
