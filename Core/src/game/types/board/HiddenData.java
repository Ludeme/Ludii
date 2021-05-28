package game.types.board;

/**
 * Defines possible data to be hidden.
 * 
 * @author Eric.Piette
 */
public enum HiddenData
{
	/** The id of the component on the location is hidden. */
	What,

	/** The owner of the component of the location is hidden. */
	Who,

	/** The local state of the location is hidden. */
	State,

	/** The number of components on the location is hidden. */
	Count,

	/** The rotation of the component on the location is hidden. */
	Rotation,

	/** The piece value of the component on the location is hidden. */
	Value,
}
