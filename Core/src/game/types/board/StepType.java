package game.types.board;

/**
 * Defines possible ``turtle steps'' for describing walks through adjacent sites.
 * 
 * @author cambolbro and Eric.Piette
 * 
 * @remarks For example, the movement of a Chess knight may be described as {\tt (walkToSites \{ \{F F R F\} \{F F L F\} \})}. Please note that a walk cannot leave the playing area and return.
 */
public enum StepType
{
	/** Forward a step. */
	F,
	
	/** Turn left a step. */
	L,
	
	/** Turn right a step. */
	R,
}
