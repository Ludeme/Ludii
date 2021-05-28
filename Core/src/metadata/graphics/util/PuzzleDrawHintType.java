package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines different ways of visualising stacks of pieces.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum PuzzleDrawHintType implements GraphicsItem 
{
	/** Hints drawn in the middle. */
	Default,
	
	/** Hints drawn in the top left. */
	TopLeft,
	
	/** Draw the hint next to the region. */
	NextTo,
	
	/** No hints. */
	None,

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
