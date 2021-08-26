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
 * Checks if a player is the previous player.
 * 
 * @author Eric.Piette
 * @remarks Used to detect if a specific player is the previous player.
 */
@Hide
public final class IsPrev extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The index of the player. */
	private final IntFunction who;

	//-------------------------------------------------------------------------

	/**
	 * @param who  The index of the player.
	 * @param role The roleType of the player.
	 */
	public IsPrev
	(
		@Or final IntFunction who,
		@Or final RoleType    role
	)
	{
		this.who = (role != null) ? RoleType.toIntFunction(role) : who;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return (who.eval(context) == context.state().prev());
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
		return who.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		return who.concepts(game);
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(who.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(who.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		who.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= who.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= who.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return  "in the same turn";
	}
}
