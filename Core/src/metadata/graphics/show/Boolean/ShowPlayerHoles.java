package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the player's holes on the board should be marked with their owner.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only used by a specific number of board styles when creating the board's design (e.g. Mancala).
 */
@Hide
public class ShowPlayerHoles implements GraphicsItem
{
	
	/** If the player's holes should be marked. */
	private final boolean showPlayerHoles;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showPlayerHoles  Whether the player's holes should be marked or not [True].
	 */
	public ShowPlayerHoles
	(
		@Opt final Boolean showPlayerHoles
	)
	{
		this.showPlayerHoles = (showPlayerHoles == null) ? true : showPlayerHoles.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the player's holes should be marked.
	 */
	public boolean showPlayerHoles()
	{
		return showPlayerHoles;
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
