package metadata.graphics.puzzle;

import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.PuzzleDrawHintType;

/**
 * Indicates how the hints for the puzzle should be shown.
 * 
 * @author Matthew.Stephenson
 */
public class DrawHint implements GraphicsItem
{
	/** How hints should be shown. */
	private final PuzzleDrawHintType drawHint;
		
	//-------------------------------------------------------------------------

	/**
	 * @param drawHint How hints should be shown.
	 * 
	 * @example (drawHint TopLeft)
	 */
	public DrawHint
	(
		final PuzzleDrawHintType drawHint
	)
	{
		this.drawHint = drawHint;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return How the hint should be drawn.
	 */
	public PuzzleDrawHintType drawHint()
	{
		return drawHint;
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
