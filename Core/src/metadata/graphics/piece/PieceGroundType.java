package metadata.graphics.piece;

/**
 * Defines the types of Piece metadata to set the foreground.
 * 
 * @author Eric.Piette
 */
public enum PieceGroundType
{
	/** To draw a specified image in front of a piece. */
	Background,

	/** To draw a specified image behind a piece. */
	Foreground,
	
	/** To draw a specified image as the hidden symbol. */
	Hidden,
}