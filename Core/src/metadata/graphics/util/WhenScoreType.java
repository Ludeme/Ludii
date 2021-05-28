package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Specifies when to show player scores to the user.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum WhenScoreType implements GraphicsItem  
{
	/** Always show player scores. */
	Always,
	
	/** Never show player scores. */
	Never,
	
	/** Only show player scores at end of game. */
	AtEnd,
	;

	// -------------------------------------------------------------------------------

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
