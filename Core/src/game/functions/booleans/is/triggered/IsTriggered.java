package game.functions.booleans.is.triggered;

import java.util.BitSet;

import annotations.Hide;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import other.concept.Concept;
import other.context.Context;

/**
 * Checks if a player was triggered before.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsTriggered extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The player index. */
	private final IntFunction playerId;

	/** The event. */
	private final String event;

	//-------------------------------------------------------------------------

	/**
	 * @param event       The event triggered.
	 * @param indexPlayer The index of the player.
	 * @param role        The roleType of the player.
	 */
	public IsTriggered
	(
			final String      event,
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

		this.event = event;
	}

	//-------------------------------------------------------------------------

	@Override
	public final boolean eval(final Context context)
	{
		final int pid = playerId.eval(context);
		return context.active(pid) && context.state().isTriggered(event, pid);
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
		final BitSet concepts = new BitSet();
		concepts.or(playerId.concepts(game));
		if(event.equals("Connected"))
			concepts.set(Concept.Connection.id(), true);
		return concepts;
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
	
	@Override
	public String toEnglish(final Game game) 
	{
		return playerId.toEnglish(game) +" is "+ event;
	}
}
