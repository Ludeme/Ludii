package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines different aspects of a stack.
 * 
 * @author matthew.stephenson
 */
public enum StackPropertyType implements GraphicsItem 
{
	/** Stack scale. */
	Scale(1),
	
	/** Stack maximum number of pieces (used by some stack types). */
	Limit(2),
	
	/** Stack design. */
	Type(3),
	;
	
	//-------------------------------------------------------------------------
	
	private final int number;
	
	private StackPropertyType(final int number) 
	{
        this.number = number;
    }
	
	/**
	 * @return The number.
	 */
	public int number() 
	{
		return number;
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
