package game.types.board;

//import game.functions.trackStep.TrackStep;

/**
 * Defines special steps for describing tracks on the board.
 * 
 * @author cambolbro
 * 
 * @remarks For example, a track may be defined as { 0 N Repeat E End }.
 */
public enum TrackStepType
{
	/** Off the track. */
	Off,

	/** End of the track. */
	End,
	
	/** Repeat stepping in the current direction. */
	Repeat;	
	
//	public TrackStep eval()
//	{
//		return null;
//	}
}
