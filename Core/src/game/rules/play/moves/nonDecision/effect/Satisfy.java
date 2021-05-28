package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.puzzle.ActionSet;
import other.action.puzzle.ActionToggle;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.context.TempContext;
import other.move.Move;
import other.move.MoveUtilities;
import other.state.puzzle.ContainerDeductionPuzzleState;

/**
 * Defines constraints applied at run-time for filtering legal puzzle moves.
 * 
 * @author Eric
 * 
 * @remarks This ludeme applies to deduction puzzles.
 */
public class Satisfy extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Constraints to apply. */
	protected final BooleanFunction[] constraints;

	//-------------------------------------------------------------------------

	/**
	 * @param constraint  The constraint of the puzzle.
	 * @param constraints The constraints of the puzzle.
	 * 
	 * @example (satisfy (all Different))
	 */
	public Satisfy
	(
		@Or	final BooleanFunction   constraint,
		@Or	final BooleanFunction[] constraints
	)
	{
		super(null);
		
		int numNonNull = 0;
		if (constraint != null)
			numNonNull++;
		if (constraints != null)
			numNonNull++;
		
		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");
		
		if (constraints != null)
		{
			this.constraints = constraints;
		}
		else
		{
			this.constraints = new BooleanFunction[1];
			this.constraints[0] = constraint;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final SiteType type = context.board().defaultSite();
		final int min = context.board().getRange(type).min(context);
		final int max = context.board().getRange(type).max(context);

		// Eric: improvement possible here in doing the next three steps one time when
		// the game is started.

		// We get all the variables constrained by the puzzle.
		final TIntArrayList varsConstraints = context.game().constraintVariables();

		// We got the variables (locations) already solved.
		final TIntArrayList initLoc = new TIntArrayList();
		if (context.game().rules().start() != null)
			if (context.game().rules().start().rules()[0].isSet())
			{
				final game.rules.start.deductionPuzzle.Set startRule = (game.rules.start.deductionPuzzle.Set) context
						.game().rules().start().rules()[0];
				final Integer[] init = startRule.vars();
				for (final Integer in : init)
					initLoc.add(in.intValue());
			}

		// We keep only the real variables of the problem.
		final TIntArrayList sites = new TIntArrayList();
		for (int i = 0; i < varsConstraints.size(); i++)
		{
			final int var = varsConstraints.getQuick(i);
			if (!initLoc.contains(var))
				sites.add(var);
		}

		// We compute the legal moves
		for (int i = 0; i < sites.size(); i++)
		{
			final int site = sites.getQuick(i);
			for (int index = min; index <= max; index++)
			{
				// SET
				final ActionSet actionSet = new ActionSet(type, site, index);
				actionSet.setDecision(true);
				final Move moveSet = new Move(actionSet);
				moveSet.setFromNonDecision(site);
				moveSet.setToNonDecision(site);

				// Check constraint
				final Context newContext = new TempContext(context);
				newContext.game().apply(newContext, moveSet);
				boolean constraintOK = true;
				if (constraints != null)
					for (final BooleanFunction constraint : constraints)
						if (!constraint.eval(newContext))
						{
							constraintOK = false;
							break;
						}

				if (constraintOK)
				{
					final int saveFrom = context.from();
					final int saveTo = context.to();
					context.setFrom(site);
					context.setTo(Constants.OFF);
					MoveUtilities.chainRuleCrossProduct(context, moves, null, moveSet, false);
					context.setTo(saveTo);
					context.setFrom(saveFrom);
				}

				// TOGGLE
				final ActionToggle actionToggle = new ActionToggle(type, site, index);
				actionToggle.setDecision(true);
				final Move moveToggle = new Move(actionToggle);
				moveToggle.setFromNonDecision(site);
				moveToggle.setToNonDecision(site);

				final ContainerDeductionPuzzleState ps = (ContainerDeductionPuzzleState) context.state().containerStates()[0];

				// Check if all is off
				if (!(ps.isResolved(site, type) && ps.bit(site, index, type)))
				{
					final int saveFrom = context.from();
					final int saveTo = context.to();
					context.setFrom(site);
					context.setTo(Constants.OFF);
					MoveUtilities.chainRuleCrossProduct(context, moves, null, moveToggle, false);
					context.setTo(saveTo);
					context.setFrom(saveFrom);
				}
			}
		}
		
		for (final Move m : moves.moves())
			m.setMover(1);

		return moves;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The constraints to satisfy.
	 */
	public BooleanFunction[] constraints() 
	{
		return constraints;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | GameType.DeductionPuzzle;

		for (final BooleanFunction constraint : constraints)
			gameFlags |= constraint.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.DeductionPuzzle.id(), true);
		concepts.set(Concept.CopyContext.id(), true);

		for (final BooleanFunction constraint : constraints)
			concepts.or(constraint.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());

		for (final BooleanFunction constraint : constraints)
			writeEvalContext.or(constraint.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.From.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		for (final BooleanFunction constraint : constraints)
			readEvalContext.or(constraint.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		for (final BooleanFunction constraint : constraints)
			missingRequirement |= constraint.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (satisfy ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		willCrash |= super.willCrash(game);

		for (final BooleanFunction constraint : constraints)
			willCrash |= constraint.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);

		for (final BooleanFunction constraint : constraints)
			constraint.preprocess(game);
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isConstraintsMoves()
	{
		return true;
	}

}
