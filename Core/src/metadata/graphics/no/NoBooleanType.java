package metadata.graphics.no;

/**
 * Defines the types of Hide metadata depending only of a boolean.
 * 
 * @author Eric.Piette
 */
public enum NoBooleanType
{
	/** To indicate whether the board should be hidden. */
	Board,

	/** To indicate whether the animations should be hidden. */
	Animation,
	
	/** To indicate whether the sunken outline should be drawn. */
	Sunken,

	/** To indicate whether pieces drawn in the hand should be scaled or not. */
	HandScale,
	
	/** To indicate if the lines that make up the board's rings should be drawn as straight lines. */
	Curves,
	
	/** To indicate if the colour of the masked players should not be the colour of the player. */
	MaskedColour,
	
	/** To indicate if pips on the dice should be always drawn as a single number. */
	DicePips,
}
