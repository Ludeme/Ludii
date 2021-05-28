package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the possible moves are always shown.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class ShowPossibleMoves implements GraphicsItem
{
	/** If the possible moves are always shown. */
	private final boolean showPossibleMoves;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showPossibleMoves  Whether the possible moves are always shown. [True].
	 */
	public ShowPossibleMoves
	(
		@Opt final Boolean showPossibleMoves
	)
	{
		this.showPossibleMoves = (showPossibleMoves == null) ? true : showPossibleMoves.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the possible moves are always shown.
	 */
	public boolean showPossibleMoves()
	{
		return showPossibleMoves;
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
