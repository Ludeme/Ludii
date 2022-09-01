package game.functions.directions;

import java.util.List;

import game.Game;
import game.equipment.component.Component;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.directions.DirectionFacing;
import game.util.directions.RelativeDirection;
import other.BaseLudeme;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Provides common functionality for direction functions.
 * 
 * @author Eric.Piette and cambolbro
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public abstract class DirectionsFunction extends BaseLudeme implements Direction
{
	/**
	 * @return The relative directions.
	 */
	@SuppressWarnings("static-method")
	public RelativeDirection[] getRelativeDirections()
	{
		return null;
	}

	/**
	 * @param graphType    The type of the site.
	 * @param element      The graph element.
	 * @param newComponent The component on the site (used in case of temporary
	 *                     modification of the state (forEachDirection)
	 * @param newFacing    The new direction faced by the component on the site
	 *                     (used in case of temporary modification of the state
	 *                     (forEachDirection)
	 * @param newRotation  The new rotation in case of a modification of the
	 *                     rotation by a tile piece (the path of tiles pieces).
	 * @param context      The context.
	 * 
	 * @return the corresponding absolute directions.
	 */
	public abstract List<AbsoluteDirection> convertToAbsolute(final SiteType graphType, final TopologyElement element,
			final Component newComponent, final DirectionFacing newFacing, final Integer newRotation,
			final Context context);

	/**
	 * @param game The game.
	 * @return Accumulated flags for this state type.
	 */
	public abstract long gameFlags(final Game game);

	/**
	 * @return true of the function is immutable, allowing extra optimisations.
	 */
	public abstract boolean isStatic();

	/**
	 * Called once after a game object has been created. Allows for any game-
	 * specific preprocessing (e.g. precomputing and caching of static results).
	 * 
	 * @param game
	 */
	public abstract void preprocess(final Game game);

	@Override
	public DirectionsFunction directionsFunctions()
	{
		return this;
	}
}
