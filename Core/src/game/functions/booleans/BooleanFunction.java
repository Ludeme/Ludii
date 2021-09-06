package game.functions.booleans;

import java.util.BitSet;
import java.util.List;

import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.state.GameType;
import other.context.Context;
import other.location.Location;

/**
 * Returns a boolean.
 * 
 * @author cambolbro and Eric.Piette
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public interface BooleanFunction extends GameType
{
	/**
	 * @param context The context.
	 * @return The result of applying this function to this trial.
	 */
	boolean eval(final Context context);
	
	/**
	 * @return True if the function is sure to return false if eval() were
	 * called now, regardless of context.
	 */
	boolean autoFails();
	
	/**
	 * @return True if the function is sure to return true if eval() were
	 * called now, regardless of context.
	 */
	boolean autoSucceeds();

	/**
	 * @return The regions constraints by this function.
	 */
	RegionFunction regionConstraint();

	/**
	 * @return The locations constraints by this function.
	 */
	IntFunction[] locsConstraint();

	/**
	 * @return The type of the static region.
	 */
	RegionTypeStatic staticRegion();

	/**
	 * @param context The context.
	 * @return The sites satisfying the condition when sites are the cause.
	 */
	List<Location> satisfyingSites(final Context context);

	/**
	 * @param game The game.
	 * @return Accumulated flags corresponding to the game concepts.
	 */
	BitSet concepts(final Game game);

	/**
	 * @return Accumulated flags corresponding to read data in EvalContext.
	 */
	BitSet readsEvalContextRecursive();

	/**
	 * @return Accumulated flags corresponding to write data in EvalContext.
	 */
	BitSet writesEvalContextRecursive();

	/**
	 * @param game The game.
	 * @return True if a required ludeme is missing.
	 */
	boolean missingRequirement(final Game game);

	/**
	 * @param game The game.
	 * @return True if the ludeme can crash the game during its play.
	 */
	boolean willCrash(final Game game);

	/**
	 * @param context The context.
	 * @return The concepts returned only if the booleanFunction is true.
	 */
	BitSet stateConcepts(final Context context);
	
	/**
	 * @param game
	 * @return This boolean Function in English.
	 */
	String toEnglish(Game game);
}
