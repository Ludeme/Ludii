package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines different colours for a piece.
 * 
 * @author matthew.stephenson
 */
public enum PieceColourType implements GraphicsItem 
{
	/** Fill colour. */
	Fill,
	
	/** Edge colour. */
	Edge,
	
	/** Secondary colour. Used for things like the count colour. */
	Secondary,
	;

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
