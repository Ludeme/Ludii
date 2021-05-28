package metadata.graphics.others;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Indicates if the game should auto-pass when this is the only legal move.
 * 
 * @author Matthew.Stephenson
 */
public class AutoPass implements GraphicsItem
{
	/** If the game should auto-pass when this is the only legal move. */
	private final boolean autoPass;
		
	//-------------------------------------------------------------------------

	/**
	 * @param autoPass If the game should auto-pass when this is the only legal move [True].
	 * 
	 * @example (autoPass False)
	 */
	public AutoPass
	(
		final Boolean autoPass
	)
	{
		this.autoPass = (autoPass == null) ? true : autoPass.booleanValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return If the game should auto-pass when this is the only legal move.
	 */
	public boolean autoPass()
	{
		return autoPass;
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
