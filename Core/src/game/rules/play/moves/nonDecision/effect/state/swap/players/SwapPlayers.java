package game.rules.play.moves.nonDecision.effect.state.swap.players;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.play.RoleType;
import other.action.others.ActionSwap;
import other.action.state.ActionSetNextPlayer;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Swap two players.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SwapPlayers extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The first player to swap. */
	private final IntFunction player1;

	/** The second player to swap. */
	private final IntFunction player2;

	/**
	 * @param player1 The index of the first player.
	 * @param role1   The role of the first player.
	 * @param player2 The index of the second player.
	 * @param role2   The role of the second player.
	 * @param then    The moves applied after that move is applied.
	 */
	public SwapPlayers
	(
		     @Or  final IntFunction player1,
		     @Or  final RoleType    role1,
		     @Or2 final IntFunction player2,
		     @Or2 final RoleType    role2,
		@Opt      final Then        then
	)
	{
		super(then);
		
		this.player1 = (player1 == null) ? RoleType.toIntFunction(role1) : player1;
		this.player2 = (player2 == null) ? RoleType.toIntFunction(role2) : player2;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final int pid1 = player1.eval(context);
		final int pid2 = player2.eval(context);
		final Moves moves = new BaseMoves(super.then());
		final ActionSwap actionSwap = new ActionSwap(pid1, pid2);
		actionSwap.setDecision(true);
		final Move swapMove = new Move(actionSwap);
		swapMove.actions().add(new ActionSetNextPlayer(context.state().mover()));
		swapMove.setMover(context.state().mover());
		moves.moves().add(swapMove);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = player1.gameFlags(game) | player2.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (isDecision())
			concepts.set(Concept.SwapPlayersDecision.id(), true);
		else
			concepts.set(Concept.SwapPlayersEffect.id(), true);

		concepts.or(super.concepts(game));
		concepts.or(player1.concepts(game));
		concepts.or(player2.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(player1.writesEvalContextRecursive());
		writeEvalContext.or(player2.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(player1.readsEvalContextRecursive());
		readEvalContext.or(player2.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= player1.missingRequirement(game);
		missingRequirement |= player2.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= player1.willCrash(game);
		willCrash |= player2.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		player1.preprocess(game);
		player2.preprocess(game);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "swap the players" + thenString;
	}
}
