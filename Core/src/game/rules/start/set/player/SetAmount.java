package game.rules.start.set.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.start.StartRule;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.action.state.ActionSetAmount;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Initializes the amount of the players.
 * 
 * @author Eric.Piette
 * @remarks This is used mainly for betting games.
 */
@Hide
public final class SetAmount extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The players. */
	protected final IntFunction playersFn;

	/** The amount. */
	protected final IntFunction amountFn;

	//-------------------------------------------------------------------------

	/**
	 * @param role   The roleType of the player.
	 * @param amount The amount to set.
	 */
	public SetAmount
	(
		@Opt final RoleType    role,
			 final IntFunction amount
	)
	{
		if (role != null)
			this.playersFn = RoleType.toIntFunction(role);
		else
			this.playersFn = null;

		this.amountFn = amount;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final int amount = amountFn.eval(context);
		int[] players;

		if (playersFn != null)
		{
			players = new int[]
			{ playersFn.eval(context) };
		}
		else
		{
			players = new int[context.game().players().count()];
			for (int i = 0; i < players.length; i++)
				players[i] = i + 1;
		}

		for (int i = 0; i < players.length; i++)
		{
			final int playerId = players[i];
			final Move move;
			final ActionSetAmount actionAmount = new ActionSetAmount(playerId, amount);
			actionAmount.apply(context, true);
			move = new Move(actionAmount);
			context.trial().addMove(move);
			context.trial().addInitPlacement();
		}
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
		long gameFlags = GameType.Bet | amountFn.gameFlags(game);

		if (playersFn != null)
			gameFlags |= playersFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(amountFn.concepts(game));
		concepts.set(Concept.InitialAmount.id(), true);

		if (playersFn != null)
			concepts.or(playersFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(amountFn.writesEvalContextRecursive());

		if (playersFn != null)
			writeEvalContext.or(playersFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(amountFn.readsEvalContextRecursive());

		if (playersFn != null)
			readEvalContext.or(playersFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (playersFn != null)
			playersFn.preprocess(game);

		amountFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "initAmount ";
		return str;
	}

	@Override
	public boolean isSet()
	{
		return false;
	}
}
