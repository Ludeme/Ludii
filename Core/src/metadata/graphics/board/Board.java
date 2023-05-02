package metadata.graphics.board;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import metadata.graphics.GraphicsItem;
import metadata.graphics.board.Boolean.BoardCheckered;
import metadata.graphics.board.colour.BoardColour;
import metadata.graphics.board.curvature.BoardCurvature;
import metadata.graphics.board.ground.BoardBackground;
import metadata.graphics.board.ground.BoardForeground;
import metadata.graphics.board.placement.BoardPlacement;
import metadata.graphics.board.style.BoardStyle;
import metadata.graphics.board.styleThickness.BoardStyleThickness;
import metadata.graphics.piece.PieceGroundType;
import metadata.graphics.util.BoardGraphicsType;
import metadata.graphics.util.ContainerStyleType;
import metadata.graphics.util.colour.Colour;

/**
 * Sets a graphic data to the board.
 * 
 * @author Eric.Piette
 */
public class Board implements GraphicsItem
{
	/**
	 * For setting the style of a board.
	 * 
	 * @param boardType          					The type of data.
	 * @param containerStyleType 					Container style wanted for the board.
	 * @param replaceComponentsWithFilledCells		True if cells should be filled instead of component drawn [False].
	 * 
	 * @example (board Style Chess)
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
				   final BoardStyleType boardType, 
				   final ContainerStyleType containerStyleType,
		@Opt @Name final Boolean replaceComponentsWithFilledCells
	)
	{
		switch (boardType)
		{
		case Style:
			return new BoardStyle(containerStyleType, replaceComponentsWithFilledCells);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Board(): A BoardStyleType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the thickness style.
	 * 
	 * @param boardType         The type of data.
	 * @param boardGraphicsType The board graphics type to which the colour is to be
	 *                          applied (must be InnerEdge or OuterEdge).
	 * @param thickness         The assigned thickness scale for the specified
	 *                          boardGraphicsType.
	 * 
	 * @example (board StyleThickness OuterEdges 2.0)
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
		 final BoardStyleThicknessType boardType, 
	     final BoardGraphicsType boardGraphicsType,
		 final Float             thickness
	)
	{
		switch (boardType)
		{
		case StyleThickness:
			return new BoardStyleThickness(boardGraphicsType, thickness);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Board(): A BoardStyleThicknessType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the board to be checkered.
	 * 
	 * @param boardType The type of data.
	 * @param value     Whether the graphic data should be applied or not [True].
	 * 
	 * @example (board Checkered)
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
		     final BoardBooleanType boardType, 
		@Opt final Boolean value
	)
	{
		switch (boardType)
		{
		case Checkered:
			return new BoardCheckered(value);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Board(): A BoardBooleanType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the background or the foreground of a board.
	 * 
	 * @param boardType  The type of data to apply to the board.
	 * @param image      Name of the image to draw. Default value is an outline around the board.
	 * @param fillColour Colour for the inner sections of the image. Default value
	 *                   is the phase 0 colour of the board.
	 * @param edgeColour Colour for the edges of the image. Default value is the
	 *                   outer edge colour of the board.
	 * @param scale      Scale for the drawn image relative to the size of the
	 *                   board [1.0].
	 * @param scaleX		Scale for the drawn image, relative to the cell size of the container, along x-axis [1.0].
	 * @param scaleY		Scale for the drawn image, relative to the cell size of the container, along y-axis [1.0].
	 * @param rotation   Rotation of the drawn image (clockwise).
	 * @param offsetX    Offset distance as percentage of board size to push the image to the right [0].
	 * @param offsetY    Offset distance as percentage of board size to push the image to the down [0].
	 * 
	 * @example (board Background image:"octagon" fillColour:(colour White)
	 *          edgeColour:(colour White) scale:1.2)
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
			       final PieceGroundType boardType, 
		@Opt @Name final String image,
		@Opt @Name final Colour fillColour,
		@Opt @Name final Colour edgeColour,
		@Opt @Name final Float scale,
		@Opt @Name final Float scaleX,
		@Opt @Name final Float scaleY,
		@Opt @Name final Integer rotation,
		@Opt @Name final Float offsetX,
		@Opt @Name final Float offsetY
	)
	{
		switch (boardType)
		{
		case Background:
			return new BoardBackground(image, fillColour, edgeColour, scale, scaleX, scaleY, rotation, offsetX, offsetY);
		case Foreground:
			return new BoardForeground(image, fillColour, edgeColour, scale, scaleX, scaleY, rotation, offsetX, offsetY);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceGroundType is not implemented.");
	}
	
	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the colour of the board.
	 * 
	 * @param boardType         The type of data.
	 * @param boardGraphicsType The board graphics type to which the colour is to be
	 *                          applied.
	 * @param colour            The assigned colour for the specified
	 *                          boardGraphicsType.
	 * 
	 * @example (board Colour Phase2 (colour Cyan))
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
		final BoardColourType boardType, 
		final BoardGraphicsType boardGraphicsType,
		final Colour colour
	)
	{
		switch (boardType)
		{
		case Colour:
			return new BoardColour(boardGraphicsType,colour);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Board(): A BoardColourType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the placement of the board.
	 * 
	 * @param BoardPlacementType The type of data.
	 * @param scale scale for the board.
	 * @param offsetX X offset for board center.
	 * @param offsetY Y offset for board center.
	 * 
	 * @example (board Placement scale:0.8)
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
					final BoardPlacementType boardType, 
		@Opt @Name  final Float scale,
		@Opt @Name  final Float offsetX,
		@Opt @Name  final Float offsetY
	)
	{
		switch (boardType)
		{
		case Placement:
			return new BoardPlacement(scale, offsetX, offsetY);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Board(): A BoardShapeType is not implemented.");
	}
	
	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the curvature of the board.
	 * 
	 * @param boardType   The type of data.
	 * @param curveOffset The curve offset.
	 * 
	 * @example (board Curvature 0.45)
	 */
	@SuppressWarnings("javadoc")
	public static GraphicsItem construct
	(
		final BoardCurvatureType boardType, 
		final Float curveOffset
	)
	{
		switch (boardType)
		{
		case Curvature:
			return new BoardCurvature(curveOffset);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Board(): A BoardCurvatureType is not implemented.");
	}

	//-------------------------------------------------------------------------------

	private Board()
	{
		// Ensure that compiler does not pick up default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		throw new UnsupportedOperationException("Board.concepts(...): Should never be called directly.");
	}

	@Override
	public long gameFlags(final Game game)
	{
		throw new UnsupportedOperationException("Board.gameFlags(...): Should never be called directly.");
	}

	@Override
	public boolean needRedraw()
	{
		throw new UnsupportedOperationException("Board.gameFlags(...): Should never be called directly.");
	}
}
