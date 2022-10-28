package metadata.graphics.board.curvature;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Sets the preferred curve offset for the board.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class BoardCurvature implements GraphicsItem
{
	
	/** curve offset (used by BoardDesign.curvePath). */
	private final float curveOffset;
		
	//-------------------------------------------------------------------------

	/**
	 * @param curveOffset			curve offset when drawing curves.
	 * 
	 * @example (board Curvature 0.45)
	 */
	public BoardCurvature
	(
		final Float             curveOffset
	)
	{
		this.curveOffset = curveOffset.floatValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return curve offset when drawing curves.
	 */
	public float curveOffset()
	{
		return curveOffset;
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
