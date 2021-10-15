package metadata.graphics.piece.style;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.ComponentStyleType;

/**
 * Sets the style of a piece.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class PieceStyle implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String pieceName;
	
	/** Component style to apply. */
	private final ComponentStyleType componentStyleType;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType           Player whose index is to be matched.
	 * @param pieceName          Base piece name to match.
	 * @param componentStyleType Component style wanted for this piece.
	 */
	public PieceStyle
	(
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		     final ComponentStyleType componentStyleType
	)
	{
		this.roleType = roleType;
		this.pieceName = pieceName;
		this.componentStyleType = componentStyleType;
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
	 * @return ComponentStyleType to apply onto component.
	 */
	public ComponentStyleType componentStyleType()
	{
		return componentStyleType;
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
