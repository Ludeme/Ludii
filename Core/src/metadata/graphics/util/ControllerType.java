package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines supported controller types for handling user interactions for particular topologies.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum ControllerType implements GraphicsItem
{
	/** Basic user interaction controller. */ 
	BasicController,
	
	/** User interaction controller for games played on pyramidal topologies. */
	PyramidalController,
	;
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @param value The name.
	 * @return The controller type from its name.
	 */
	public static ControllerType fromName(final String value)
	{
		try
		{
			return valueOf(value);
		}
		catch (final Exception e)
		{
			return BasicController;
		}
	}

	// -----------------------------------------------------------------------------
	
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
