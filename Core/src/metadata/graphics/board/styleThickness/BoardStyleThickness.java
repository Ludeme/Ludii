package metadata.graphics.board.styleThickness;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.BoardGraphicsType;

/**
 * Sets the preferred scale for the thickness of a specific aspect of the board.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Different aspects of the board that can be specified are defined in BoardGraphicsType (must be InnerEdge, OuterEdge or Vertex).
 */
@Hide
public class BoardStyleThickness implements GraphicsItem
{
	/** boardGraphicsType to set the thickness of (must be InnerEdge or OuterEdge). */
	private final BoardGraphicsType boardGraphicsType;
	
	/** Thickness scale. */
	private final float thickness;
		
	//-------------------------------------------------------------------------

	/**
	 * @param boardGraphicsType The board graphics type to which the colour is to be applied (must be InnerEdge or OuterEdge).
	 * @param thickness			The assigned thickness scale for the specified boardGraphicsType.
	 * 
	 * @example (board StyleThickness OuterEdges 2.0)
	 */
	public BoardStyleThickness
	(
		final BoardGraphicsType boardGraphicsType,
		final Float             thickness
	)
	{
		this.boardGraphicsType = boardGraphicsType;
		this.thickness         = thickness.floatValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return BoardGraphicsType that the scale is applied to.
	 */
	public BoardGraphicsType boardGraphicsType()
	{
		return boardGraphicsType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Thickness scale to apply onto the specified boardGraphicsType.
	 */
	public float thickness()
	{
		return thickness;
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
