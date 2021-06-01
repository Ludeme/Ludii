package metadata.graphics.piece.name;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether the local state value of a piece should be added to its name.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks This ludeme is used for finding and displaying the correct piece image 
 *          for components (e.g. for the game Chopsticks).
 */
@Hide
public class PieceAddStateToName implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String piece;
	
	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType 	Player whose index is to be matched.
	 * @param piece 	Base piece name to match.
	 * @param state		State to match.
	 * @param value   	   	Value to match.
	 */
	public PieceAddStateToName
	(
		@Opt 		final RoleType roleType,
		@Opt @Name  final String piece,
		@Opt @Name  final Integer state,
		@Opt @Name final Integer value
	)
	{
		this.roleType = roleType;
		this.piece = piece;
		this.state = state;
		this.value = value;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return State condition to check.
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
	public String piece()
	{
		return piece;
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
