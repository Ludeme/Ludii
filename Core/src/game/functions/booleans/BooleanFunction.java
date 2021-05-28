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
	public boolean eval(final Context context);
	
	/**
	 * @return True if the function is sure to return false if eval() were
	 * called now, regardless of context.
	 */
	public boolean autoFails();
	
	/**
	 * @return True if the function is sure to return true if eval() were
	 * called now, regardless of context.
	 */
	public boolean autoSucceeds();

	/**
	 * @return The regions constraints by this function.
	 */
	public RegionFunction regionConstraint();

	/**
	 * @return The locations constraints by this function.
	 */
	public IntFunction[] locsConstraint();

	/**
	 * @return The type of the static region.
	 */
	public RegionTypeStatic staticRegion();

	/**
	 * @param context The context.
	 * @return The sites satisfying the condition when sites are the cause.
	 */
	public List<Location> satisfyingSites(final Context context);

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
	 * @param context The context.
	 * @return The concepts returned only if the booleanFunction is true.
	 */
	public abstract BitSet stateConcepts(final Context context);
}
