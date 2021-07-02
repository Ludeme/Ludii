package metadata.graphics.no;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;
import metadata.graphics.no.Boolean.NoAnimation;
import metadata.graphics.no.Boolean.NoBoard;
import metadata.graphics.no.Boolean.NoCurves;
import metadata.graphics.no.Boolean.NoDicePips;
import metadata.graphics.no.Boolean.NoSunken;

/**
 * Hides a graphic element.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public class No implements GraphicsItem
{
	/**
	 * @param boardType The type of data.
	 * @param value     True if the graphic data has to be hidden [True].
	 * 
	 * @example (no Board)
	 * @example (no Animation)
	 * @example (no Curves)
	 */
	public static GraphicsItem construct
	(
		     final NoBooleanType boardType, 
		@Opt final Boolean value
	)
	{
		switch (boardType)
		{
		case Board:
			return new NoBoard(value);
		case Animation:
			return new NoAnimation(value);
		case Sunken:
			return new NoSunken(value);
		case DicePips:
			return new NoDicePips(value);
		case Curves:
			return new NoCurves(value);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("No(): A NoBooleanType is not implemented.");
	}

	// -------------------------------------------------------------------------------

	private No()
	{
		// Ensure that compiler does not pick up default constructor
	}

	// -------------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		throw new UnsupportedOperationException("No.concepts(...): Should never be called directly.");
	}

	@Override
	public long gameFlags(final Game game)
	{
		throw new UnsupportedOperationException("No.gameFlags(...): Should never be called directly.");
	}

	@Override
	public boolean needRedraw()
	{
		throw new UnsupportedOperationException("No.gameFlags(...): Should never be called directly.");
	}
}
