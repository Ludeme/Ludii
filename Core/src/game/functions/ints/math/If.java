package game.functions.ints.math;

import java.util.BitSet;

import game.Game;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns a value according to a condition.
 * 
 * @author Eric Piette
 * @remarks This ludeme is used to get a different int depending on a condition in a value of a
 *        ludeme.
 */
public final class If extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which condition. */
	private final BooleanFunction cond;

	/** Value A. */
	private final IntFunction valueA;

	/** Value B. */
	private final IntFunction valueB;

	//-------------------------------------------------------------------------

	/**
	 * Return a value according to a condition.
	 * 
	 * @param cond   The condition.
	 * @param valueA The integer returned if the condition is true.
	 * @param valueB The integer returned if the condition is false.
	 * @example (if (is Mover P1) 1 2)
	 */
	public If
	(
		final BooleanFunction cond,
		final IntFunction     valueA,
		final IntFunction     valueB
	)
	{
		this.cond = cond;
		this.valueA = valueA;
		this.valueB = valueB;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (cond.eval(context))
			return valueA.eval(context);
	
		return valueB.eval(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return valueA.isStatic() && valueB.isStatic() && cond.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return cond.gameFlags(game) | valueA.gameFlags(game) | valueB.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(cond.concepts(game));
		concepts.or(valueA.concepts(game));
		concepts.or(valueB.concepts(game));
		concepts.set(Concept.ConditionalStatement.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(cond.writesEvalContextRecursive());
		writeEvalContext.or(valueA.writesEvalContextRecursive());
		writeEvalContext.or(valueB.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(cond.readsEvalContextRecursive());
		readEvalContext.or(valueA.readsEvalContextRecursive());
		readEvalContext.or(valueB.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		cond.preprocess(game);
		valueA.preprocess(game);
		valueB.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (cond instanceof FalseConstant)
		{
			game.addRequirementToReport("One of the condition of a (if ...) ludeme is \"false\" which is wrong.");
			missingRequirement = true;
		}

		missingRequirement |= cond.missingRequirement(game);
		missingRequirement |= valueA.missingRequirement(game);
		missingRequirement |= valueB.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= cond.willCrash(game);
		willCrash |= valueA.willCrash(game);
		willCrash |= valueB.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "if " + cond.toEnglish(game) + " then " + valueA.toEnglish(game) + " otherwise " + valueB.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}
