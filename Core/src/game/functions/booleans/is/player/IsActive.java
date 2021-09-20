package game.functions.booleans.is.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Checks if a player is active.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsActive extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** Player Index. */
	private final IntFunction playerId;

	//-------------------------------------------------------------------------
 
	/**
	 * @param indexPlayer The index of the player.
	 * @param role        The roleType of the player.
	 */
	public IsActive
	(
		@Or final IntFunction indexPlayer,
		@Or final RoleType    role
	)
	{
		int numNonNull = 0;
		if (indexPlayer != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (indexPlayer != null)
			playerId = indexPlayer;
		else
			playerId = RoleType.toIntFunction(role);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int roleId = playerId.eval(context);

		if (roleId == 0 || roleId > context.game().players().count())
			return false;

		return context.active(roleId);
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
		return playerId.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		return playerId.concepts(game);
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(playerId.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(playerId.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		playerId.preprocess(game);
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= playerId.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= playerId.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Function of which we're checking if the return value is enemy.
	 */
	public IntFunction role()
	{
		return playerId;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "Player " + playerId.toEnglish(game) + " is active";
	}
	
	//-------------------------------------------------------------------------
		
}
