package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines different ways of visualising stacks of pieces.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum PuzzleHintLocationType implements GraphicsItem 
{
	/** Hints placed on top-left site of region. */
	Default,

	/** Draw hint on edge between vertex. */
	BetweenVertices,

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
