package game.functions.booleans.is.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;

/**
 * Is used to check if a player is a friend.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsFriend extends BaseBooleanFunction
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
	public IsFriend
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

		this.playerId = (role != null) ? RoleType.toIntFunction(role) : indexPlayer;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (context.game().requiresTeams())
		{
			final TIntArrayList teamMembers = new TIntArrayList();
			final int tid = context.state().getTeam(context.state().mover());
			for (int i = 1; i < context.game().players().size(); i++)
				if (context.state().getTeam(i) == tid)
					teamMembers.add(i);
			return teamMembers.contains(playerId.eval(context));
		}

		return (playerId.eval(context) == context.state().mover()
				|| context.state().mover() == context.game().players().size());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return this.playerId.isStatic();
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
		concepts.set(Concept.IsFriend.id(), true);
		concepts.or(playerId.concepts(game));
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
