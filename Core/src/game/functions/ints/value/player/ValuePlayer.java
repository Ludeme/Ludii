package game.functions.ints.value.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import other.concept.Concept;
import other.context.Context;

/**
 * To get the value of a specific player.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ValuePlayer extends BaseIntFunction
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
	public ValuePlayer
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
			this.playerId = indexPlayer;
		else
			this.playerId = RoleType.toIntFunction(role);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int pid = playerId.eval(context);
		return context.state().getValue(pid);
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
		concepts.set(Concept.PlayerValue.id(), true);
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

	//-------------------------------------------------------------------------

	/**
	 * @return The role of the player.
	 */
	public IntFunction role()
	{
		return playerId;
	}
}