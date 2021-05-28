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
 * Checks if a player is the next player.
 * 
 * @author  Eric.Piette
 */
@Hide
public final class IsNext extends BaseBooleanFunction
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
	public IsNext
	(
		@Or final IntFunction who,
		@Or final RoleType    role
	)
	{
		int numNonNull = 0;
		if (who != null)
			numNonNull++;
		if (role != null)
			numNonNull++;
		
		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");
		
		this.who = (role != null) ? RoleType.toIntFunction(role) : who;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return (who.eval(context) == context.state().next());
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
}
