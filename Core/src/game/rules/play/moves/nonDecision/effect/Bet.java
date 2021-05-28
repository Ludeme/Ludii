package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.range.RangeFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.moves.Player;
import main.Constants;
import other.action.state.ActionBet;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Is used to bet an amount.
 * 
 * @author Eric.Piette
 */
public final class Bet extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The player making the bet. */
	private final IntFunction playerFn;

	/** The role making the bet to check in the required warning. */
	private final RoleType role;

	/** The range of the bet. */
	private final RangeFunction range;

	/**
	 * @param who   The data about the player to bet.
	 * @param role  The roleType of the player to bet.
	 * @param range The range of the bet.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (bet P1 (range 0 5))
	 */
	public Bet
	(
		@Or      final Player        who,
		@Or      final RoleType      role,
		         final RangeFunction range,
		    @Opt final Then          then
	)
	{
		super(then);
		
		int numNonNull = 0;
		if (who != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Piece(): One who or role parameter must be non-null.");

		this.range = range;
		this.playerFn = (role != null) ? RoleType.toIntFunction(role) : who.index();
		this.role = role;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final int player = playerFn.eval(context);
		final int min = range.minFn().eval(context);
		final int max = range.maxFn().eval(context);

		for (int i = min; i <= max; i++)
		{
			final ActionBet actionBet = new ActionBet(player, i);
			if (isDecision())
				actionBet.setDecision(true);
			final Move move = new Move(actionBet);
			move.setDecision(true);
			move.setFromNonDecision(Constants.OFF);
			move.setToNonDecision(Constants.OFF);
			if (context.game().mode().mode() == ModeType.Simultaneous)
				move.setMover(player);
			else
				move.setMover(context.state().mover());
			moves.moves().add(move);
		}

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
		long gameFlags = super.gameFlags(game) | range.gameFlags(game) | playerFn.gameFlags(game) | GameType.Bet;

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(range.concepts(game));
		concepts.or(playerFn.concepts(game));
		if (isDecision())
			concepts.set(Concept.BetDecision.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(range.writesEvalContextRecursive());
		writeEvalContext.or(playerFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(range.readsEvalContextRecursive());
		readEvalContext.or(playerFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// We check if the role is correct.
		if (role != null)
		{
			final int indexOwnerPhase = role.owner();
			if ((indexOwnerPhase < 1 && !role.equals(RoleType.Mover) && !role.equals(RoleType.Prev)
					&& !role.equals(RoleType.Next)
					&& !role.equals(RoleType.All)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"The ludeme (bet ...) or (move Bet ...) is used with a wrong RoleType: " + role + ".");
				missingRequirement = true;
			}
		}

		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= range.missingRequirement(game);
		missingRequirement |= playerFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= range.willCrash(game);
		willCrash |= playerFn.willCrash(game);

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
		range.preprocess(game);
		playerFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "Bet";
	}
}
