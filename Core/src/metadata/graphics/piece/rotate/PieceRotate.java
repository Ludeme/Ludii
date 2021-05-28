package metadata.graphics.piece.rotate;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether to apply any vertical or horizontal image reflections to a piece.
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
	
	/** Degrees to rotate the image clockwise. */
	private final int degrees;
	
	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType 		Player whose index is to be matched.
	 * @param pieceName 	Base piece name to match.
	 * @param state   	   	State to match.
	 * @param value   	   	Value to match.
	 * @param degrees 		Degrees to rotate the image clockwise.
	 */
	public PieceRotate
	(
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Name final Integer degrees
	)
	{
		this.roleType = roleType;
		this.pieceName = pieceName;
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
	 * @return Reflect image horizontally.
	 */
	public int degrees()
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
