package game.rules.phase;

import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.types.play.RoleType;
import main.Constants;
import other.context.Context;

/**
 * Enables a player or all the players to proceed to another phase of the game.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks If no phase is specified, moves to the next phase in the list, 
 *          wrapping back to the first phase if needed.
 *          The ludeme returns Undefined (-1) if the condition is false or if
 *          the named phase does not exist.
 */
public final class NextPhase extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Index of the player to go to another phase. N+1 if this is all. */
	private final IntFunction who;

	/** Condition to reach a phase. */
	private final BooleanFunction cond;

	/** Name of the phase to reach. */
	private final String phaseName;

	//-------------------------------------------------------------------------

	/**
	 * @param role        The roleType of the player [Shared].
	 * @param indexPlayer The index of the player.
	 * @param cond        The condition to satisfy to go to another phase [True].
	 * @param phaseName   The name of the phase.
	 * 
	 * @example (nextPhase Mover (= (count Moves) 10) "Movement")
	 */
	public NextPhase
	(
		@Opt @Or final RoleType               role,
		@Opt @Or final game.util.moves.Player indexPlayer,
		@Opt     final BooleanFunction        cond,
		@Opt     final String                 phaseName
	)
	{
		int numNonNull = 0;
		if (role != null)
			numNonNull++;
		if (indexPlayer != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter must be non-null.");

		this.cond = (cond == null) ? new BooleanConstant(true) : cond;
		this.phaseName = phaseName;
		
		if (indexPlayer != null)
			this.who = indexPlayer.index();
		else if (role != null)
			this.who = RoleType.toIntFunction(role);
		else
			this.who = new Id(null, RoleType.Shared);
	}

	//-------------------------------------------------------------------------

	@Override
	public final int eval(final Context context)
	{
		if (context.game().rules().phases() == null)
			return Constants.UNDEFINED;

		final Phase[] phases = context.game().rules().phases();
		if (cond.eval(context))
		{
			// Return the next phase of the player if no specific name defined
			if (phaseName == null)
			{
				final int pid = who.eval(context);
				final int currentPhase = (pid == context.game().players().size())
						? context.state().currentPhase(context.state().mover())
						: context.state().currentPhase(pid);
				return (currentPhase + 1) % phases.length;
			}
			else
			{
				for (int phaseId = 0; phaseId < phases.length; phaseId++)
				{
					final Phase phase = phases[phaseId];
					if (phase.name().equals(phaseName))
						return phaseId;
				}
			}
			throw new IllegalArgumentException("BUG: Phase " + phaseName + " unfounded.");
		}

		return Constants.UNDEFINED;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The player concerned by that nextPhase ludeme.
	 */
	public IntFunction who()
	{
		return who;
	}

	/**
	 * @return The name of the next phase.
	 */
	public String phaseName()
	{
		return phaseName;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return cond.isStatic() && who.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return cond.gameFlags(game) | who.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(cond.concepts(game));
		concepts.or(who.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(cond.writesEvalContextRecursive());
		writeEvalContext.or(who.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(cond.readsEvalContextRecursive());
		readEvalContext.or(who.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= cond.missingRequirement(game);
		missingRequirement |= who.missingRequirement(game);

		if (who instanceof IntConstant)
		{
			final int whoValue = ((IntConstant) who).eval(null);
			if (whoValue < 1 || whoValue > game.players().size())
			{
				game.addRequirementToReport("A wrong player index is used in (nextPhase ...).");
				missingRequirement = true;
			}
		}

		if (cond instanceof FalseConstant)
		{
			game.addRequirementToReport("The condition of a (nextPhase ...) ludeme is \"false\" which is wrong.");
			missingRequirement = true;
		}

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= cond.willCrash(game);
		willCrash |= who.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		cond.preprocess(game);
		who.preprocess(game);
	}

	//-------------------------------------------------------------------------

}
