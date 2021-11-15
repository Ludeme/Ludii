package game.match;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import other.BaseLudeme;

/**
 * Defines an instance game of a match.
 * 
 * @author Eric.Piette
 */
public final class Subgame extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The name of the game. */
	private final String gameName;

	/** The name of the option. */
	private final String optionName;

	/** The game compiled. */
	private Game game = null;

	/** The next instance. */
	private final IntFunction nextInstance;

	/** The result (for the match score) got by the winner. */
	private final IntFunction result;
	
	/** If this is set to true, we should automatically disable memoryless playouts on any games we compile */
	private boolean disableMemorylessPlayouts = false;

	//-------------------------------------------------------------------------

	/**
	 * @param name   The name of the game instance.
	 * @param option The option of the game instance.
	 * @param next   The index of the next instance.
	 * @param result The score result for the match when game instance is over.
	 * 
	 * @example (subgame "Tic-Tac-Toe")
	 */
	public Subgame
	(
				   final String name,
		@Opt	   final String option,
		@Opt @Name final IntFunction next,
		@Opt @Name final IntFunction result
	) 
	{
		gameName = name;
		optionName = option;
		nextInstance = next;
		this.result = result;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * To set the game.
	 * 
	 * @param game
	 */
	public void setGame(final Game game)
	{
		this.game = game;
		
		if (disableMemorylessPlayouts)
			game.disableMemorylessPlayouts();
	}

	/**
	 * @return The name of the game.
	 */
	public String gameName()
	{
		return gameName;
	}

	/**
	 * @return The name of the option.
	 */
	public String optionName()
	{
		return optionName;
	}

	/**
	 * @return the game compiled.
	 */
	public Game getGame()
	{
		return game;
	}
	
	/**
	 * Disables memoryless playouts on the current game (if any), and any future
	 * games that may get set.
	 */
	public void disableMemorylessPlayouts()
	{
		disableMemorylessPlayouts = true;
		
		if (game != null)
			game.disableMemorylessPlayouts();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return next Instance.
	 */
	public IntFunction next()
	{
		return nextInstance;
	}

	/**
	 * @return result for the winner.
	 */
	public IntFunction result()
	{
		return result;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public BitSet concepts(final Game g)
	{
		final BitSet concepts = new BitSet();

		if (game != null)
			concepts.or(game.computeBooleanConcepts());

		if (nextInstance != null)
			concepts.or(nextInstance.concepts(game));

		if (result != null)
			concepts.or(result.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (game != null)
			writeEvalContext.or(game.computeWritingEvalContextFlag());

		if (nextInstance != null)
			writeEvalContext.or(nextInstance.writesEvalContextRecursive());

		if (result != null)
			writeEvalContext.or(result.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (game != null)
			readEvalContext.or(game.computeReadingEvalContextFlag());

		if (nextInstance != null)
			readEvalContext.or(nextInstance.readsEvalContextRecursive());

		if (result != null)
			readEvalContext.or(result.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game g)
	{
		boolean missingRequirement = false;
		if (g != null)
			missingRequirement |= g.missingRequirement(g);

		if (nextInstance != null)
			missingRequirement |= nextInstance.missingRequirement(g);

		if (result != null)
			missingRequirement |= result.missingRequirement(g);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game g)
	{
		boolean willCrash = false;
		if (g != null)
			willCrash |= g.willCrash(g);

		if (nextInstance != null)
			willCrash |= nextInstance.willCrash(g);

		if (result != null)
			willCrash |= result.willCrash(g);
		return willCrash;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("[Subgame: " + gameName);
		
		if (optionName != null)
			sb.append(" (" + optionName + ")");
		
		sb.append("]");
		return sb.toString();
	}
}
