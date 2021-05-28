package metadata.graphics.show;

/**
 * Defines the types of Show metadata depending only of a boolean.
 * 
 * @author Eric.Piette
 */
public enum ShowBooleanType
{
	/** To indicates whether the pits on the board should be marked with their owner. */
	Pits,
	
	/** To indicates whether the player's holes on the board should be marked with their owner. */
	PlayerHoles,

	/** To indicates whether the holes with a local state of zero should be marked. */
	LocalStateHoles,
	
	/** To indicates whether the owner of each region should be shown. */
	RegionOwner,

	/** To indicates whether the cost of the graph element has to be shown. */
	Cost,
	
	/** To indicates whether the hints of the puzzle has to be shown. */
	Hints,
	
	/** To indicates whether the edge directions should be shown. */
	EdgeDirections,
	
	/** To indicates whether the possible moves are always shown. */
	PossibleMoves,
	
	/** To indicates whether curved edges should be shown. */
	CurvedEdges,
	
	/** To indicates whether straight edges should be shown. */
	StraightEdges,
}