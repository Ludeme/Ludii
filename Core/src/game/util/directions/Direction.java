package game.util.directions;

import game.functions.directions.DirectionsFunction;

/**
 * The different direction which can be used by some moves.
 * 
 * @author Eric.Piette
 */
public interface Direction
{
	/**
	 * @return The corresponding direction functions.
	 */
	public DirectionsFunction directionsFunctions();
}
