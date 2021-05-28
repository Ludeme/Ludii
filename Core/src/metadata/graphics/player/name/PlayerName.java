package metadata.graphics.player.name;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Sets the name of a player.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class PlayerName implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Player name to apply. */
	private final String name;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  Player whose index is to be matched.
	 * @param name	Name wanted for this player.
	 * 
	 * @example (player Name P1 "Player 1")
	 */
	public PlayerName
	(
		final RoleType roleType,
		final String   name
	)
	{
		this.roleType = roleType;
		this.name   = name;
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
	 * @return String to apply onto player.
	 */
	public String name()
	{
		return name;
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
