package other;

import java.util.BitSet;

import game.Game;

/**
 * Ludeme interface.
 * 
 * @author cambolbro and Eric.Piette
 */
public interface Ludeme
{
	/**
	 * @param game The game.
	 * @return English description of this ludeme.
	 */
	public String toEnglish(final Game game);

	/**
	 * @param game The game.
	 * @return Accumulated flags corresponding to the game concepts.
	 */
	public BitSet concepts(final Game game);

	/**
	 * @return Recursively accumulated flags corresponding to read data in EvalContext.
	 */
	public BitSet readsEvalContextRecursive();

	/**
	 * @return Recursively accumulated flags corresponding to write data in EvalContext.
	 */
	public BitSet writesEvalContextRecursive();
	
	/**
	 * @return EvalContext properties read by this ludeme directly (not recursively)
	 */
	public BitSet readsEvalContextFlat();
	
	/**
	 * @return EvalContext properties written by this ludeme directly (not recursively)
	 */
	public BitSet writesEvalContextFlat();

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
}
