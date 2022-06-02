package metadata.graphics.piece;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.piece.colour.PieceColour;
import metadata.graphics.piece.families.PieceFamilies;
import metadata.graphics.piece.ground.PieceBackground;
import metadata.graphics.piece.ground.PieceForeground;
import metadata.graphics.piece.name.PieceAddStateToName;
import metadata.graphics.piece.name.PieceExtendName;
import metadata.graphics.piece.name.PieceRename;
import metadata.graphics.piece.rotate.PieceRotate;
import metadata.graphics.piece.scale.PieceScale;
import metadata.graphics.piece.style.PieceStyle;
import metadata.graphics.util.ComponentStyleType;
import metadata.graphics.util.colour.Colour;

/**
 * Sets a graphic data to the pieces.
 * 
 * @author Eric.Piette
 */
public class Piece implements GraphicsItem
{
	/**
	 * For setting the style of a piece.
	 * 
	 * @param pieceType          The type of data to apply to the pieces.
	 * @param roleType           Player whose index is to be matched.
	 * @param pieceName          Base piece name to match.
	 * @param componentStyleType Component style wanted for this piece.
	 *
	 * @return GraphicsItem object.
	 * 
	 * @example (piece Style ExtendedShogi)
	 */
	public static GraphicsItem construct
	(
		     final PieceStyleType pieceType, 
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		     final ComponentStyleType componentStyleType
	)
	{
		switch (pieceType)
		{
		case Style:
			return new PieceStyle(roleType, pieceName, componentStyleType);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceStyleType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the name of a piece.
	 * 
	 * @param pieceType The type of data to apply to the pieces.
	 * @param roleType  Player whose index is to be matched.
	 * @param piece     Base piece name to match.
	 * @param container Container index to match.
	 * @param state     State to match.
	 * @param value     Value to match.
	 * @param name      Text to use.
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (piece Rename piece:"Die" "Triangle")
	 * @example (piece ExtendName P2 "2")
	 * @example (piece AddStateToName)
	 */
	public static GraphicsItem construct
	(
		     		final PieceNameType pieceType, 
		@Opt 		final RoleType roleType,
		@Opt @Name  final String piece,
		@Opt @Name  final Integer container,
		@Opt @Name  final Integer state,
		@Opt @Name  final Integer value,
		@Opt 		final String name
	)
	{
		switch (pieceType)
		{
		case ExtendName:
			return new PieceExtendName(roleType, piece, container, state, value, (name == null) ? "" : name);
		case Rename:
			return new PieceRename(roleType, piece, container, state, value, (name == null) ? "" : name);
		case AddStateToName:
			return new PieceAddStateToName(roleType, piece, container, state, value);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceNameType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the families of the pieces.
	 * 
	 * @param pieceType     The type of data to apply to the pieces.
	 * @param pieceFamilies Set of family names for the pieces used in the game.
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (piece Families {"Defined" "Microsoft" "Pragmata" "Symbola"})
	 */
	public static GraphicsItem construct
	(
		final PieceFamiliesType pieceType, 
	    final String[] pieceFamilies
	)
	{
		switch (pieceType)
		{
		case Families:
			return new PieceFamilies(pieceFamilies);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceFamiliesType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the background or foreground image of a piece.
	 * 
	 * @param pieceType  The type of data to apply to the pieces.
	 * @param roleType   Player whose index is to be matched.
	 * @param pieceName  Base piece name to match.
	 * @param container Container index to match.
	 * @param image      Name of the image to draw.
	 * @param state  	 State to match.
	 * @param value  	 Value to match.
	 * @param fillColour Colour for the inner sections of the image. Default value
	 *                   is the fill colour of the component.
	 * @param edgeColour Colour for the edges of the image. Default value is the
	 *                   edge colour of the component.
	 * @param scale      Scale for the drawn image relative to the cell size of the
	 *                   container [1.0].
	 * @param scaleX	 Scale for the drawn image, relative to the cell size of the container, along x-axis [1.0].
	 * @param scaleY	 Scale for the drawn image, relative to the cell size of the container, along y-axis [1.0].
	 * @param rotation   Amount of rotation for drawn image.
	 * @param offsetX       Offset distance percentage to push the image to the right [0].
	 * @param offsetY       Offset distance percentage to push the image down [0].
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (piece Foreground "Pawn" image:"Pawn" fillColour:(colour White)
	 *          scale:0.9)
	 * 
	 * @example (piece Background "Han" image:"octagon" fillColour:(colour White)
	 *          edgeColour:(colour White))
	 */
	public static GraphicsItem construct 
	(
			       final PieceGroundType pieceType, 
		@Opt       final RoleType roleType,
		@Opt       final String pieceName,
		@Opt @Name final Integer container,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
			 @Name final String image,
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
		switch (pieceType)
		{
		case Background:
			return new PieceBackground(roleType, pieceName, container, state, value, image, fillColour, edgeColour, scale, scaleX, scaleY, rotation, offsetX, offsetY);
		case Foreground:
			return new PieceForeground(roleType, pieceName, container, state, value, image, fillColour, edgeColour, scale, scaleX, scaleY, rotation, offsetX, offsetY);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceGroundType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For setting the colour of a piece.
	 * 
	 * @param pieceType    		The type of data to apply to the pieces.
	 * @param roleType     		Player whose index is to be matched.
	 * @param pieceName    		Base piece name to match.
	 * @param container 		Container index to match.
	 * @param state     		State to match.
	 * @param value     		Value to match.
	 * @param fillColour   		Fill colour for this piece.
	 * @param strokeColour 		Stroke colour for this piece.
	 * @param secondaryColour 	Secondary colour for this piece.
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (piece Colour P2 "CounterStar" fillColour:(colour Red))
	 */
	public static GraphicsItem construct
	(
			       final PieceColourType pieceType, 
		@Opt       final RoleType roleType,
		@Opt       final String pieceName,
		@Opt @Name final Integer container,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Opt @Name final Colour fillColour,
		@Opt @Name final Colour strokeColour,
		@Opt @Name final Colour secondaryColour
	)
	{
		switch (pieceType)
		{
		case Colour:
			return new PieceColour(roleType, pieceName, container, state, value, fillColour, strokeColour, secondaryColour);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceColourType is not implemented.");
	}
	
	//-------------------------------------------------------------------------------
	
	/**
	 * For rotating the piece.
	 * 
	 * @param pieceType  The type of data to apply to the pieces.
	 * @param roleType   Player whose index is to be matched.
	 * @param pieceName  Base piece name to match.
	 * @param container Container index to match.
	 * @param state      State to match.
	 * @param value      Value to match.
	 * @param degrees  	 Degrees to rotate clockwise.
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (piece Rotate P2 degrees:90)
	 */
	public static GraphicsItem construct
	(
			       final PieceRotateType pieceType, 
		@Opt       final RoleType roleType,
		@Opt       final String pieceName,
		@Opt @Name final Integer container,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Name 	   final Integer degrees
	)
	{
		switch (pieceType)
		{
		case Rotate:
			return new PieceRotate(roleType, pieceName, container, state, value, degrees);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceRotateType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For scaling a piece.
	 * 
	 * @param pieceType The type of data to apply to the pieces.
	 * @param roleType  Player whose index is to be matched.
	 * @param pieceName Base piece name to match.
	 * @param container container index to match.
	 * @param state      State to match.
	 * @param value      Value to match.
	 * @param scale    Scaling factor in both x and y direction.
	 * @param scaleX   The scale of the image along x-axis.
	 * @param scaleY   The scale of the image along y-axis.
	 * 
	 * @return GraphicsItem object.
	 * 
	 * @example (piece Scale "Pawn" .5)
	 * @example (piece Scale "Disc" .5)
	 */
	public static GraphicsItem construct
	(
			 	   final PieceScaleType pieceType, 
		@Opt 	   final RoleType roleType,
		@Opt 	   final String pieceName,
		@Opt @Name final Integer container,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Opt 	   final Float scale,
		@Opt @Name final Float scaleX,
		@Opt @Name final Float scaleY
	)
	{
		switch (pieceType)
		{
		case Scale:
			return new PieceScale(roleType, pieceName, container, state, value, scale, scaleX, scaleY);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Piece(): A PieceScaleType is not implemented.");
	}

	//-------------------------------------------------------------------------------

	private Piece()
	{
		// Ensure that compiler does not pick up default constructor
	}

	//-------------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		throw new UnsupportedOperationException("Piece.concepts(...): Should never be called directly.");
	}

	@Override
	public long gameFlags(final Game game)
	{
		throw new UnsupportedOperationException("Piece.gameFlags(...): Should never be called directly.");
	}

	@Override
	public boolean needRedraw()
	{
		throw new UnsupportedOperationException("Piece.gameFlags(...): Should never be called directly.");
	}
}
