package metadata.graphics.piece.name;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Replaces a piece's name with an alternative.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Used for finding and displaying the correct piece image for components.
 */
@Hide
public class PieceRename implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String piece;

	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
	
	/** String extension to replace Piece name. */
	private final String nameReplacement;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  		Player whose index is to be matched.
	 * @param piece 			Base piece name to match.
	 * @param state				State to match.
	 * @param value   	   	Value to match.
	 * @param nameReplacement	Text to replace piece name.
	 */
	public PieceRename
	(
		@Opt 		final RoleType roleType,
		@Opt @Name  final String piece,
		@Opt @Name  final Integer state,
		@Opt @Name final Integer value,
		     		final String nameReplacement
	)
	{
		this.roleType = roleType;
		this.piece = piece;
		this.state = state;
		this.value = value;
		this.nameReplacement = nameReplacement;
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
	 * @return State condition to check.
	 */
	public Integer state()
	{
		return state;
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
	
	/**
	 * @return Piece value condition to check.
	 */
	public Integer value()
	{
		return value;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return String to replace piece name.
	 */
	public String nameReplacement()
	{
		return nameReplacement;
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
