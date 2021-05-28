package metadata.graphics.piece;

/**
 * Defines the types of Piece metadata to change the name.
 * 
 * @author Eric.Piette
 */
public enum PieceNameType
{
	/** To replace a piece's name with an alternative. */
	Rename,

	/** To add additional text to a piece name. */
	ExtendName,

	/** To add the local state value of a piece to its name. */
	AddStateToName,
	
	/** To set the hidden image for a piece. */
	Hidden,
	
	
}