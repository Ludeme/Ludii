package metadata.graphics.board.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the board should be drawn in a checkered pattern.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Colouring is done based on the board's phases.
 */
@Hide
public class BoardCheckered implements GraphicsItem
{
	/** If the board should be checkered. */
	private final boolean checkeredBoard;
		
	//-------------------------------------------------------------------------

	/**
	 * @param checkeredBoard Whether the board should be checkered or not [True].
	 */
	public BoardCheckered
	(
		@Opt final Boolean checkeredBoard
	)
	{
		this.checkeredBoard = (checkeredBoard == null) ? true : checkeredBoard.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the board should be checkered.
	 */
	public boolean checkeredBoard()
	{
		return checkeredBoard;
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
