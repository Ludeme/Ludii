package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the pits on the board should be marked with their owner.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only used by a specific number of board styles when creating the board's design (e.g. Mancala).
 */
@Hide
public class ShowPits implements GraphicsItem
{
	/** If the pits should be marked. */
	private final boolean showPits;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showPits  Whether the pits should be marked or not [True].
	 */
	public ShowPits
	(
		@Opt final Boolean showPits
	)
	{
		this.showPits = (showPits == null) ? true : showPits.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the pits should be marked.
	 */
	public boolean showPits()
	{
		return showPits;
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
