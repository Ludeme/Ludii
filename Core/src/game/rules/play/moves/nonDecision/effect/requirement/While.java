package game.rules.play.moves.nonDecision.effect.requirement;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant.TrueConstant;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Applies a move until the condition becomes false.
 * 
 * @author Eric.Piette
 */
public final class While extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves to apply until the condition is false. */
	final Moves moves;
	
	/** The conditions to check. */
	final BooleanFunction condition;

	//-------------------------------------------------------------------------

	/**
	 * @param condition    Conditions to make false thanks to the move to apply.
	 * @param moves        Moves to apply until the condition is false.
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (while (!= 100 (score P1)) (addScore P1 1))
	 */
	public While
	(
				final BooleanFunction condition, 
				final Moves           moves, 
		@Opt 	final Then            then
	)
	{
		super(then);
		this.condition = condition;
		this.moves = moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return intersected list of moves
		final Moves result = new BaseMoves(super.then());

		final Context newContext = new TempContext(context);
		int numIteration = 0;
		while (condition.eval(newContext))
		{
			for (final Move m : moves.eval(newContext).moves())
			{
				m.apply(newContext, false);
				result.moves().add(m);
			}
			numIteration++;
			if (numIteration > Constants.MAX_NUM_ITERATION)
			{
				throw new IllegalArgumentException(
						"Infinite While(), the condition can not be reached.");
			}
		}

		// Add the consequences.
		if (then() != null)
			for (int j = 0; j < result.moves().size(); j++)
				result.moves().get(j).then().add(then().moves());

		return result;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = moves.gameFlags(game) | super.gameFlags(game) | condition.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(condition.concepts(game));
		concepts.or(moves.concepts(game));
		concepts.set(Concept.CopyContext.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(moves.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(moves.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (condition instanceof TrueConstant)
		{
			game.addRequirementToReport("The ludeme (while ...) has an infinite condition which is \"true\".");
			missingRequirement = true;
		}

		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= condition.missingRequirement(game);
		missingRequirement |= moves.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= condition.willCrash(game);
		willCrash |= moves.willCrash(game);

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
		condition.preprocess(game);
		moves.preprocess(game);
	}
}
