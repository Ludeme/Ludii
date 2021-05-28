package metadata.graphics.board.colour;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.BoardGraphicsType;
import metadata.graphics.util.colour.Colour;

/**
 * Sets the colour of a specific aspect of the board.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Different aspects of the board that can be specified are defined in BoardGraphicsType.
 */
@Hide
public class BoardColour implements GraphicsItem
{
	/** boardGraphicsType to colour. */
	private final BoardGraphicsType boardGraphicsType;
	
	/** Colour to apply. */
	private final Colour colour;
		
	//-------------------------------------------------------------------------

	/**
	 * @param boardGraphicsType The board graphics type to which the colour is to be
	 *                          applied.
	 * @param colour            The assigned colour for the specified
	 *                          boardGraphicsType.
	 */
	public BoardColour
	(
		final BoardGraphicsType boardGraphicsType,
		final Colour colour
	)
	{
		this.boardGraphicsType = boardGraphicsType;
		this.colour = colour;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return BoardGraphicsType that the colour is applied to.
	 */
	public BoardGraphicsType boardGraphicsType()
	{
		return boardGraphicsType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Colour to apply onto the specified boardGraphicsType.
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
