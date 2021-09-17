package game.rules.play.moves.nonDecision.effect.set.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.play.RoleType;
import main.Constants;
import other.action.Action;
import other.action.others.ActionSetValueOfPlayer;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Is used to set the value associated with a player.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetValuePlayer extends Effect
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The player. */
	private final IntFunction playerId;

	/** The value. */
	private final IntFunction valueFn;

	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player.
	 * @param role   The role of the player.
	 * @param value  The value of the player.
	 * @param then   The moves applied after that move is applied.
	 */
	public SetValuePlayer
	(
		     @Or final game.util.moves.Player player,
		     @Or final RoleType               role,
			     final IntFunction            value,
		@Opt     final Then                   then
	)
	{
		super(then);

		if (player != null)
			playerId = player.index();
		else
			playerId = RoleType.toIntFunction(role);

		valueFn = value;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		final int pid = playerId.eval(context);
		final int value = valueFn.eval(context);

		if(pid < 0 || pid > context.game().players().count())
			return moves;
		
		final Action action = new ActionSetValueOfPlayer(pid, value);
		final Move move = new Move(action);
		move.setFromNonDecision(Constants.OFF);
		move.setToNonDecision(Constants.OFF);
		move.setMover(context.state().mover());
		moves.moves().add(move);

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
	public long gameFlags(final Game game)
	{
		long gameFlags = playerId.gameFlags(game) | valueFn.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(playerId.concepts(game));
		concepts.or(valueFn.concepts(game));
		concepts.set(Concept.PlayerValue.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(playerId.writesEvalContextRecursive());
		writeEvalContext.or(valueFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(playerId.readsEvalContextRecursive());
		readEvalContext.or(valueFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= playerId.missingRequirement(game);
		missingRequirement |= valueFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= playerId.willCrash(game);
		willCrash |= valueFn.willCrash(game);

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
		playerId.preprocess(game);
		valueFn.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "set the value of Player " + playerId.toEnglish(game) + " to " + valueFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------

}
