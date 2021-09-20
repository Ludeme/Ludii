package game.functions.ints.state;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the score of one specific player.
 * 
 * @author  Eric.Piette
 */
public final class Score extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the player. */
	private final IntFunction playerFn;

	//-------------------------------------------------------------------------

	/**
	 * Score of one specific player.
	 * 
	 * @param player The index of the player.
	 * @param role   The roleType of the player.
	 * @example      (score Mover)
	 */
	public Score
	(
		@Or final game.util.moves.Player player,
		@Or final RoleType               role
	)
	{
		int numNonNull = 0;
		if (player != null)
			numNonNull++;
		if (role != null)
			numNonNull++;
		
		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");
			
		if (player != null)
			playerFn = player.index();
		else
			playerFn = RoleType.toIntFunction(role);

	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.score(playerFn.eval(context));
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = GameType.Score;
		return gameFlags | playerFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(playerFn.concepts(game));
		concepts.set(Concept.Scoring.id(), true);
		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(playerFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(playerFn.readsEvalContextRecursive());
		return readEvalContext;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		playerFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= playerFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= playerFn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the score of Player " + playerFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
	
}
