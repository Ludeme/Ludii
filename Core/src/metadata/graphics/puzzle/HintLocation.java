package metadata.graphics.puzzle;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.PuzzleHintLocationType;

/**
 * Indicates how to determine the site for the hint to be drawn.
 * 
 * @author Matthew.Stephenson
 */
public class HintLocation implements GraphicsItem
{
	/** How to determine hint location. */
	private final PuzzleHintLocationType hintLocation;
		
	//-------------------------------------------------------------------------

	/**
	 * @param hintLocation  How to determine hint location.
	 * 
	 * @example (hintLocation BetweenVertices)
	 */
	public HintLocation
	(
		final PuzzleHintLocationType hintLocation
	)
	{
		this.hintLocation = hintLocation;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return How to determine hint location.
	 */
	public PuzzleHintLocationType hintLocation()
	{
		return hintLocation;
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
