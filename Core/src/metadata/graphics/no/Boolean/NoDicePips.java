package metadata.graphics.no.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether pips on the dice should be always drawn as a single number.
 * 
 * @author Matthew.Stephenson
 * 
 */
@Hide
public class NoDicePips implements GraphicsItem
{
	/** If pips on the dice should be always drawn as a single number. */
	private final boolean noDicePips;
		
	//-------------------------------------------------------------------------

	/**
	 * @param noDicePips  Whether pips on the dice should be always drawn as a single number. [True].
	 */
	public NoDicePips
	(
		@Opt final Boolean noDicePips
	)
	{
		this.noDicePips = (noDicePips == null) ? true : noDicePips.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If pips on the dice should be always drawn as a single number.
	 */
	public boolean noDicePips()
	{
		return noDicePips;
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
