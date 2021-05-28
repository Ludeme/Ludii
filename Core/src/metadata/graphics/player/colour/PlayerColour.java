package metadata.graphics.player.colour;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.colour.Colour;

//-----------------------------------------------------------------------------

/**
 * Sets the colour of a player.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class PlayerColour implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Player colour to apply. */
	private final Colour colour;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  Player whose index is to be matched.
	 * @param colour	Colour wanted for this player.
	 * 
	 * @example (playerColour P1 (colour Black))
	 */
	public PlayerColour
	(
		final RoleType roleType,
		final Colour   colour
	)
	{
		this.roleType = roleType;
		this.colour   = colour;
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
	 * @return Colour to apply onto player.
	 */
	public Colour colour()
	{
		return colour;
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
