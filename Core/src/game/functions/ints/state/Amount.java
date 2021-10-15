package game.functions.ints.state;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import game.types.state.GameType;
import main.Constants;
import other.context.Context;

/**
 * Returns the amount of a player.
 * 
 * @author Eric.Piette
 */
public final class Amount extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The owner of the amount.
	 */
	final IntFunction playerFn;

	//-------------------------------------------------------------------------
	
	/**
	 * @param role   The role of the player.
	 * @param player The index of the player.
	 * 
	 * @example (amount Mover)
	 */
	public Amount
	(
		@Or final RoleType               role,
		@Or final game.util.moves.Player player
	)
	{
		int numNonNull = 0;
		if (role != null)
			numNonNull++;
		if (player != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		playerFn = (player == null) ? RoleType.toIntFunction(role) : player.index();
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int player = playerFn.eval(context);

		if (player < context.game().players().size() && player > 0)
			return context.state().amount(player);

		return Constants.UNDEFINED;
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
		return playerFn.gameFlags(game) | GameType.Bet;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		return playerFn.concepts(game);
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
	public String toString()
	{
		return "Amount()";
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the amount owned by " + playerFn.toEnglish(game);
	}
}
