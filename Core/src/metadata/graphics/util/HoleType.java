package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines hole styles for Mancala board.
 * 
 * @author Eric.Piette
 */
public enum HoleType implements GraphicsItem
{
	/** Hole as Square. */
	Square,

	/** Oval as Square. */
	Oval,

	;

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
