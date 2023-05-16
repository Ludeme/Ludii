package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Specified where to draw state of an item in the interface, relative to its position.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum ValueLocationType implements GraphicsItem 
{
	/** No location. */
	None,
	
	/** At the top left corner of the item's location. */
	CornerLeft,
	
	/** At the top left corner of the item's location. */
	CornerRight,
	
	/** At the top of the item's location. */
	Top,
	
	/** Centred on the item's location. */
	Middle,
	;

	//-------------------------------------------------------------------------------

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
