package metadata.graphics.board.placement;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Modifies the central placement of the game board.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class BoardPlacement implements GraphicsItem
{
	/** Scale of board. */
	private final float scale;
	
	/** Offset right for board. */
	private final float offsetX;
	
	/** Offset down for board. */
	private final float offsetY;
		
	//-------------------------------------------------------------------------

	/**
	 * @param scale			Scale for the board [1.0].
	 * @param offsetX       Offset distance percentage to push the board to the right [0].
	 * @param offsetY       Offset distance percentage to push the board down [0].
	 */
	public BoardPlacement
	(
		@Opt @Name final Float scale,
		@Opt @Name final Float offsetX,
		@Opt @Name final Float offsetY
	)
	{
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.offsetX = (offsetX == null) ? 0 : offsetX.floatValue();
		this.offsetY = (offsetY == null) ? 0 : offsetY.floatValue();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Scale of board.
	 */
	public float scale()
	{
		return scale;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Offset right for board.
	 */
	public float offsetX()
	{
		return offsetX;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Offset down for board.
	 */
	public float offsetY()
	{
		return offsetY;
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
