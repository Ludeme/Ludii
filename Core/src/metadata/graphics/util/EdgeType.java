package metadata.graphics.util;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines edge type for drawing board elements, e.g. for graph games.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum EdgeType implements GraphicsItem 
{
	/** All board edges. */
	All,
		
	/** Inner board edges. */
	Inner,
	
	/** Outer board edges. */
	Outer,
	
	/** Interlayer board edges. */
	Interlayer;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns true if this is equal to or a subset of eA
	 * 
	 * @param eA
	 * @return True if this super set of the edgeType in entry.
	 */
	public boolean supersetOf(final EdgeType eA)
	{
		if (this.equals(eA))
			return true;
		if (this.equals(All))
			return true;
		
		return false;
	}

	// -------------------------------------------------------------------------

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
