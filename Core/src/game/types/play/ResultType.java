package game.types.play;

/**
 * Defines expected outcomes for each game.
 * 
 * @author cambolbro
 * 
 * @remarks Tie means that everybody wins. Draw means that nobody wins.
 */
public enum ResultType
{
	/** Somebody wins. */
	Win,
	
	/** Somebody loses. */
	Loss,
	
	/** Nobody wins. */
	Draw,
	
	/** Everybody wins. */
	Tie,
	
	/** Game abandoned, typically for being too long. */
	Abandon,
	
	/** Game stopped due to run-time error. */
	Crash,
}
