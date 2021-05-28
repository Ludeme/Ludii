package metadata.graphics.no.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;
import other.concept.Concept;

/**
 * Indicates whether the board should be hidden.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Useful in card and hand games which have no physical board.
 */
@Hide
public class NoBoard implements GraphicsItem
{
	/** If the board should be hidden. */
	private final boolean boardHidden;
		
	//-------------------------------------------------------------------------

	/**
	 * @param boardHidden  Whether the board should be hidden or not [True].
	 */
	public NoBoard
	(
		@Opt final Boolean boardHidden
	)
	{
		this.boardHidden = (boardHidden == null) ? true : boardHidden.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the board should be hidden.
	 */
	public boolean boardHidden()
	{
		return boardHidden;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.NoBoard.id(), true);
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
