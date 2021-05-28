package metadata.graphics.puzzle;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

//-----------------------------------------------------------------------------

/**
 * Indicates whether the game is an adversarial puzzle.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Used in games which are expressed as a N-player game, 
 *          but are actually puzzles, e.g. Chess puzzle.
 */
public class AdversarialPuzzle implements GraphicsItem
{
	/** If the games is an adversarial puzzle. */
	private final boolean adversarialPuzzle;
		
	//-------------------------------------------------------------------------

	/**
	 * @param adversarialPuzzle  Whether the game is an adversarial puzzle or not [True].
	 * 
	 * @example (adversarialPuzzle)
	 */
	public AdversarialPuzzle
	(
		@Opt final Boolean adversarialPuzzle
	)
	{
		this.adversarialPuzzle = (adversarialPuzzle == null) ? true : adversarialPuzzle.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the games is an adversarial puzzle.
	 */
	public boolean adversarialPuzzle()
	{
		return adversarialPuzzle;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}
}
