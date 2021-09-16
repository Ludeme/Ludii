package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.play.RoleType;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Sets the 'triggered' value for a player for a specific event.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Trigger extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the player. */
	private final IntFunction playerFunction;

	/** The event to trigger. */
	private final String event;

	//-------------------------------------------------------------------------

	/**
	 * @param event       The event to trigger.
	 * @param indexPlayer The index of the player.
	 * @param role        The roleType of the player.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (trigger "FlagCaptured" Next)
	 */
	public Trigger
	(
			 	 final String      event,
		     @Or final IntFunction indexPlayer,
		     @Or final RoleType    role,
		@Opt     final Then        then
	)
	{
		super(then);

		int numNonNull = 0;
		if (indexPlayer != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (indexPlayer != null)
			playerFunction = indexPlayer;
		else
			playerFunction = RoleType.toIntFunction(role);
		this.event = event;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final int victim = playerFunction.eval(context);
		final Moves moves = new BaseMoves(super.then());

		moves.moves().add(new Move(new other.action.state.ActionTrigger(event, victim)));

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
		long gameFlags = playerFunction.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(playerFunction.concepts(game));
		concepts.set(Concept.Trigger.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(playerFunction.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(playerFunction.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= playerFunction.missingRequirement(game);

		if (playerFunction instanceof IntConstant)
		{
			final int playerValue = ((IntConstant) playerFunction).eval(null);
			if (playerValue < 1 || playerValue > game.players().size())
			{
				game.addRequirementToReport("A wrong player index is used in (trigger ...).");
				missingRequirement = true;
			}

		}

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= playerFunction.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return playerFunction.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		playerFunction.preprocess(game);
	}
	

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "trigger " + event + " for Player " + playerFunction.toEnglish(game);
	}

	//-------------------------------------------------------------------------

}
