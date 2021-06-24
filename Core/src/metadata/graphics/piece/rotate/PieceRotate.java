package metadata.graphics.piece.rotate;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether to rotate a piece image.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks For games in which each player should see the piece from their own perspective, e.g. Shogi or Chopsticks.
 */
@Hide
public class PieceRotate implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String pieceName;
	
	/** container index condition. */
	private final Integer container;
	
	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
	
	/** Degrees to rotate the image clockwise. */
	private final int degrees;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType 		Player whose index is to be matched.
	 * @param pieceName 	Base piece name to match.
	 * @param container container index to match.
	 * @param state   	   	State to match.
	 * @param value   	   	Value to match.
	 * @param degrees 		Degrees to rotate the image clockwise.
	 */
	public PieceRotate
	(
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		@Opt @Name	final Integer container,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Name final Integer degrees
	)
	{
		this.roleType = roleType;
		this.pieceName = pieceName;
		this.container = container;
		this.state = state;
		this.value = value;
		this.degrees = degrees.intValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return RoleType condition to check.
	 */
	public RoleType roleType()
	{
		return roleType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Piece name condition to check.
	 */
	public String pieceName()
	{
		return pieceName;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return container index condition to check.
	 */
	public Integer container()
	{
		return container;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Rotation for piece image in degrees.
	 */
	public int rotation()
	{
		return degrees;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Piece state condition to check.
	 */
	public Integer state()
	{
		return state;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Piece value condition to check.
	 */
	public Integer value()
	{
		return value;
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
