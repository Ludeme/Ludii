package metadata.graphics.piece.name;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Adds additional text to a piece name.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Used for finding and displaying the correct piece image for components.
 */
@Hide
public class PieceExtendName implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String piece;
	
	/** container index condition. */
	private final Integer container;
	
	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
	
	/** String extension to add onto Piece name. */
	private final String nameExtension;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  	Player whose index is to be matched.
	 * @param piece 		Base piece name to match.
	 * @param container container index to match.
	 * @param state			State to match.
	 * @param value   	   	Value to match.
	 * @param nameExtension	Text to add onto piece name.
	 */
	public PieceExtendName
	(
		@Opt 		final RoleType roleType,
		@Opt @Name 	final String piece,
		@Opt @Name	final Integer container,
		@Opt @Name  final Integer state,
		@Opt @Name final Integer value,
		     		final String nameExtension
	)
	{
		this.roleType = roleType;
		this.piece = piece;
		this.container = container;
		this.state = state;
		this.value = value;
		this.nameExtension = nameExtension;
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
	 * @return Piece value condition to check.
	 */
	public Integer value()
	{
		return value;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Piece name condition to check.
	 */
	public String pieceName()
	{
		return piece;
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
	 * @return String to add onto piece name.
	 */
	public String nameExtension()
	{
		return nameExtension;
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
