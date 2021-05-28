package metadata.graphics.show.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the holes with a local state of zero on the board should be marked with their owner.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only used by a specific number of board styles when creating the board's design (e.g. Mancala).
 */
@Hide
public class ShowLocalStateHoles implements GraphicsItem
{
	
	/** If the holes with a local state of zero should be marked. */
	private final boolean useLocalState;
		
	//-------------------------------------------------------------------------

	/**
	 * @param useLocalState If the holes with a local state of zero should be marked [True].
	 */
	public ShowLocalStateHoles
	(
		@Opt final Boolean useLocalState
	)
	{
		this.useLocalState = (useLocalState == null) ? true : useLocalState.booleanValue();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return If the holes with a local state of zero should be marked.
	 */
	public boolean useLocalState() 
	{
		return useLocalState;
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
