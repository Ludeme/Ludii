package game.types.board;

/**
 * Defines regions which can change during play.
 * 
 * @author cambolbro and Eric.Piette
 */
public enum RegionTypeDynamic
{
	/** All the empty sites of the current state. */
	Empty,
	
	/** All the occupied sites of the current state. */
	NotEmpty,
	
	/** All the sites occupied by a piece of the mover. */
	Own,
	
	/** All the sites not occupied by a piece of the mover. */
	NotOwn,
	
	/** All the sites occupied by a piece of an enemy of the mover. */
	Enemy, 
	
	/** All the sites empty or occupied by a {\tt Neutral} piece. */
	NotEnemy,
}
