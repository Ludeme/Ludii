package game.functions.intArray.math;

import java.util.BitSet;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.IntArrayConstant;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns an array when the condition is satisfied and another when it is not.
 * 
 * @author Eric.Piette
 */
public final class If extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Condition to check. */
	private final BooleanFunction condition;

	/** Value returned if the condition is true. */
	private final IntArrayFunction ok;

	/** Value returned if the condition is false. */
	private final IntArrayFunction notOk;

	//-------------------------------------------------------------------------

	/**
	 * @param cond  The condition to satisfy.
	 * @param ok    The array returned when the condition is satisfied.
	 * @param notOk The array returned when the condition is not satisfied.
	 * 
	 * @example (if (is Mover P1) (values Remembered "RememberedP1") (values Remembered "RememberedP2"))
	 */
	public If
	(
			 final BooleanFunction cond,
			 final IntArrayFunction  ok,
		@Opt final IntArrayFunction  notOk
	)
	{
		this.condition = cond;
		this.ok = ok;
		this.notOk = (notOk == null) ? new IntArrayConstant(new IntFunction[0]) : notOk;
	}

	//-------------------------------------------------------------------------

	@Override
	public final int[] eval(final Context context)
	{
		if (condition.eval(context))
			return this.ok.eval(context);
		else
			return this.notOk.eval(context);
	}

	@Override
	public boolean isStatic()
	{
		return condition.isStatic() && ok.isStatic() && notOk.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return condition.gameFlags(game) | ok.gameFlags(game) | notOk.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(condition.concepts(game));
		concepts.or(ok.concepts(game));
		concepts.or(notOk.concepts(game));

		concepts.set(Concept.ConditionalStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(ok.writesEvalContextRecursive());
		writeEvalContext.or(notOk.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(ok.readsEvalContextRecursive());
		readEvalContext.or(notOk.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (condition instanceof FalseConstant)
		{
			game.addRequirementToReport("One of the condition of a (if ...) ludeme is \"false\" which is wrong.");
			missingRequirement = true;
		}

		missingRequirement |= condition.missingRequirement(game);
		missingRequirement |= ok.missingRequirement(game);
		missingRequirement |= notOk.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= condition.willCrash(game);
		willCrash |= ok.willCrash(game);
		willCrash |= notOk.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		condition.preprocess(game);
		ok.preprocess(game);
		notOk.preprocess(game);
	}
}
