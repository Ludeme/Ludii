package metadata.graphics.no.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the board should not be drawn sunken.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only applies to graph boards
 */
@Hide
public class NoSunken implements GraphicsItem
{
	/** If the board should not be drawn sunken. */
	private final boolean noSunken;
		
	//-------------------------------------------------------------------------

	/**
	 * @param noSunken  If the board should not be drawn sunken. [True].
	 * 
	 * @example (noSunken)
	 */
	public NoSunken
	(
		@Opt final Boolean noSunken
	)
	{
		this.noSunken = (noSunken == null) ? true : noSunken.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the board should not be drawn sunken.
	 */
	public boolean noSunken()
	{
		return noSunken;
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
