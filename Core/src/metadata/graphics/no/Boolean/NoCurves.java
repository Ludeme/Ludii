package metadata.graphics.no.Boolean;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates if the lines that make up the board's rings should be drawn as straight lines.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only used by a specific number of board styles when creating the board's design, e.g. Wheel.
 */
@Hide
public class NoCurves implements GraphicsItem
{
	/** If rings on the board should be drawn straight rather than curved. */
	private final boolean straightRingLines;
	
	//-------------------------------------------------------------------------

	/**
	 * @param straightRingLines  Whether rings on the board should be drawn straight or not [True].
	 * 
	 * @example (straightRingLines)
	 */
	public NoCurves
	(
		@Opt final Boolean straightRingLines
	)
	{
		this.straightRingLines = (straightRingLines == null) ? true : straightRingLines.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If rings on the board should be drawn straight rather than curved.
	 */
	public boolean straightRingLines()
	{
		return straightRingLines;
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
