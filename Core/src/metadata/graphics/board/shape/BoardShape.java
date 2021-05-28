package metadata.graphics.board.shape;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.types.board.ShapeType;
import metadata.graphics.GraphicsItem;

/**
 * Sets the shape of the board.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only used by a specific number of board styles when creating the board's design (e.g. Mancala).
 */
@Hide
public class BoardShape implements GraphicsItem
{
	/** Board shape. */
	private final ShapeType shape;
		
	//-------------------------------------------------------------------------

	/**
	 * @param shape	The shape of the board.
	 */
	public BoardShape
	(
		final ShapeType shape
	)
	{
		this.shape = shape;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The board's shape.
	 */
	public ShapeType shape()
	{
		return shape;
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
