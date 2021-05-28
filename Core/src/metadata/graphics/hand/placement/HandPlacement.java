package metadata.graphics.hand.placement;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Changes the placement of the hands
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class HandPlacement implements GraphicsItem
{

	/** The owner of the hand. */
	private final RoleType player;
	
	/** Scale of hand. */
	private final float scale;
	
	/** Offset right for hand. */
	private final float offsetX;
	
	/** Offset down for hand. */
	private final float offsetY;
	
	/** If the hand should be drawn vertically. */
	private final boolean vertical;
		
	//-------------------------------------------------------------------------

	/**
	 * @param player 		Roletype owner of the hand.
	 * @param scale			Scale for the board [1.0].
	 * @param offsetX       Offset distance percentage to push the board to the right [0].
	 * @param offsetY       Offset distance percentage to push the board down [0].
	 * @param vertical 		If the hand should be drawn vertically [False].
	 */
	public HandPlacement
	(
		final RoleType player,
		@Opt @Name final Float scale,
		@Opt @Name final Float offsetX,
		@Opt @Name final Float offsetY,
		@Opt @Name final Boolean vertical
	)
	{
		this.player = player;
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.offsetX = (offsetX == null) ? 0 : offsetX.floatValue();
		this.offsetY = (offsetY == null) ? 0 : offsetY.floatValue();
		this.vertical = (vertical == null) ? false : vertical.booleanValue();
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

	/**
	 * @return If the hand should be drawn vertically.
	 */
	public boolean isVertical() 
	{
		return vertical;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The owner of the hand.
	 */
	public RoleType getPlayer() 
	{
		return player;
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
