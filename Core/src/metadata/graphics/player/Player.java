package metadata.graphics.player;

import java.util.BitSet;

import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.player.colour.PlayerColour;
import metadata.graphics.player.name.PlayerName;
import metadata.graphics.util.colour.Colour;

/**
 * Sets a graphic element to a player.
 * 
 * @author Eric.Piette
 */
public class Player implements GraphicsItem
{
	// -------------------------------------------------------------------------------

	/**
	 * For setting the colour of a player.
	 * 
	 * @param playerType The type of data.
	 * @param roleType   Player whose index is to be matched.
	 * @param colour     Colour wanted for this player.
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (player Colour P1 (colour Black))
	 */
	public static GraphicsItem construct
	(
		final PlayerColourType playerType, 
		final RoleType roleType, 
		final Colour colour
	)
	{
		switch (playerType)
		{
		case Colour:
			return new PlayerColour(roleType, colour);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Player(): A PlayerColourType is not implemented.");
	}
	
	// -------------------------------------------------------------------------------

	/**
	 * For setting the name of a player.
	 * 
	 * @param playerType The type of data.
	 * @param roleType   Player whose index is to be matched.
	 * @param name     Name wanted for this player.
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (player Name P1 "Player 1")
	 */
	public static GraphicsItem construct
	(
		final PlayerNameType playerType, 
		final RoleType roleType, 
		final String name
	)
	{
		switch (playerType)
		{
		case Name:
			return new PlayerName(roleType, name);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Player(): A PlayerNameType is not implemented.");
	}

	// -------------------------------------------------------------------------------

	private Player()
	{
		// Ensure that compiler does not pick up default constructor
	}

	// -------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		throw new UnsupportedOperationException("Player.concepts(...): Should never be called directly.");
	}

	@Override
	public long gameFlags(final Game game)
	{
		throw new UnsupportedOperationException("Player.gameFlags(...): Should never be called directly.");
	}

	@Override
	public boolean needRedraw()
	{
		throw new UnsupportedOperationException("Player.gameFlags(...): Should never be called directly.");
	}
}
