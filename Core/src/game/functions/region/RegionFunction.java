package game.functions.region;

import java.util.BitSet;

import game.Game;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns a region (collection of sites) within a container.
 * 
 * @author cambolbro
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public interface RegionFunction extends GameType
{
	/**
	 * @param context
	 * @return The result of applying this function to this trial.
	 */
	public Region eval(final Context context);
	
	/**
	 * @param context
	 * @param location
	 * @return True if and only if the region evaluated in given context would contain location
	 */
	public boolean contains(final Context context, final int location);
	
	/**
	 * @param game The game we're playing
	 * @return The site type of the region returned by this function
	 */
	public SiteType type(final Game game);

	/**
	 * @param game The game.
	 * @return Accumulated flags corresponding to the game concepts.
	 */
	public BitSet concepts(final Game game);

	/**
	 * @return Accumulated flags corresponding to read data in EvalContext.
	 */
	public BitSet readsEvalContextRecursive();

	/**
	 * @return Accumulated flags corresponding to write data in EvalContext.
	 */
	public BitSet writesEvalContextRecursive();

	/**
	 * @param game The game.
	 * @return True if a required ludeme is missing.
	 */
	public boolean missingRequirement(final Game game);

	/**
	 * @param game The game.
	 * @return True if the ludeme can crash the game during its play.
	 */
	public boolean willCrash(final Game game);
	
	/**
	 * @param game
	 * @return RegionFunction described in English.
	 */
	public String toEnglish(final Game game);
}
