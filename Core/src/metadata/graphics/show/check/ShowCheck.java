package metadata.graphics.show.check;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Indicates whether a "Check" should be displayed when a piece is in threatened.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Should be used only for specific games where this is prudent, e.g.
 *          Chess.
 */
@Hide
public class ShowCheck implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String pieceName;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  Player whose index is to be matched.
	 * @param pieceName Base piece name to match.
	 */
	public ShowCheck
	(
		@Opt final RoleType roleType,
		@Opt final String pieceName
	)
	{
		this.roleType  = roleType;
		this.pieceName = pieceName;
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

	// -------------------------------------------------------------------------

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
