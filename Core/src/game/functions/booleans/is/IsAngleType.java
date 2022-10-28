package game.functions.booleans.is;

/**
 * Defines the types of Is for a connected or blocked test.
 * 
 * @author Eric.Piette
 */
public enum IsAngleType
{
	/** To check if a site and two other sites checking conditions form an acute angle (< 90 degrees). */
	Acute,

	/** To check if a site and two other sites checking conditions form a right angle (= 90 degrees). */
	Right,

	/** To check if a site and two other sites checking conditions form an obtuse angle (> 90 degrees). */
	Obtuse,

	/** To check if a site and two other sites checking conditions form a reflex angle (> 180 degrees). */
	Reflex
}
