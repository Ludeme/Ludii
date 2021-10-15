package metadata.graphics.hand;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.hand.placement.HandPlacement;

/**
 * Sets a graphic data to the hand.
 * 
 * @author Matthew.Stephenson
 */
public class Hand implements GraphicsItem
{
	/**
	 * For setting the placement of the hand.
	 * 
	 * @param HandPlacementType The type of data.
	 * @param player 		Roletype owner of the hand.
	 * @param scale			Scale for the board.
	 * @param offsetX       Offset distance percentage to push the board to the right.
	 * @param offsetY       Offset distance percentage to push the board down.
	 * @param vertical 		If the hand should be drawn vertically.
	 * 
	 *  @example (hand Placement P1 scale:1.0 offsetX:0.5 offsetY:0.5 vertical:True)
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
					final HandPlacementType handType, 
					final RoleType player,
		@Opt @Name  final Float scale,
		@Opt @Name  final Float offsetX,
		@Opt @Name  final Float offsetY,
		@Opt @Name  final Boolean vertical
	)
	{
		switch (handType)
		{
		case Placement:
			return new HandPlacement(player, scale, offsetX, offsetY, vertical);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("hand(): A HandPlacementType is not implemented.");
	}
	
	//-------------------------------------------------------------------------------

	private Hand()
	{
		// Ensure that compiler does not pick up default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		throw new UnsupportedOperationException("Hand.concepts(...): Should never be called directly.");
	}

	@Override
	public long gameFlags(final Game game)
	{
		throw new UnsupportedOperationException("Hand.gameFlags(...): Should never be called directly.");
	}

	@Override
	public boolean needRedraw()
	{
		throw new UnsupportedOperationException("Hand.gameFlags(...): Should never be called directly.");
	}
}
