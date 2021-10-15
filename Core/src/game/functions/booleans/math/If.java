package game.functions.booleans.math;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Tests if the condition is true, the function returns the first value, if not
 * it returns the second value.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class If extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Boolean condition. */
	private final BooleanFunction cond;

	/** If condition is true. */
	private final BooleanFunction ok;

	/** If condition is false. */
	private final BooleanFunction notOk;

	/**
	 * @param cond  The condition to check.
	 * @param ok    The boolean returned if the condition is true.
	 * @param notOk The boolean returned if the condition is false.
	 * 
	 * @example (if (is Mover (next)) (is Pending))
	 */
	public If
	(
			 final BooleanFunction cond, 
			 final BooleanFunction ok, 
		@Opt final BooleanFunction notOk
	)
	{
		this.cond  = cond;
		this.ok    = ok;
		this.notOk = notOk;
	} 

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (cond.eval(context))
			return (ok.eval(context));
		
		if (notOk != null)
			return notOk.eval(context);

		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Condition to check
	 */
	public BooleanFunction cond()
	{
		return cond;
	}

	/**
	 * @return Boolean function to evaluate if condition holds
	 */
	public BooleanFunction ok()
	{
		return ok;
	}

	/**
	 * @return Boolean function to evaluate if condition does not hold
	 */
	public BooleanFunction notOk()
	{
		return notOk;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		if (ok != null && !ok.isStatic())
			return false;
		
		if (notOk != null && !notOk.isStatic())
			return false;
		
		return cond.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = cond.gameFlags(game);

		if (ok != null)
			gameFlags |= ok.gameFlags(game);
		
		if (notOk != null)
			gameFlags |= notOk.gameFlags(game);
		
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(cond.concepts(game));

		if (ok != null)
			concepts.or(ok.concepts(game));

		if (notOk != null)
			concepts.or(notOk.concepts(game));

		concepts.set(Concept.ConditionalStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(cond.writesEvalContextRecursive());

		if (ok != null)
			writeEvalContext.or(ok.writesEvalContextRecursive());

		if (notOk != null)
			writeEvalContext.or(notOk.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(cond.readsEvalContextRecursive());

		if (ok != null)
			readEvalContext.or(ok.readsEvalContextRecursive());

		if (notOk != null)
			readEvalContext.or(notOk.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= cond.missingRequirement(game);

		if (cond instanceof FalseConstant)
		{
			game.addRequirementToReport("One of the condition of a (if ...) ludeme is \"false\" which is wrong.");
			missingRequirement = true;
		}

		if (ok != null)
			missingRequirement |= ok.missingRequirement(game);

		if (notOk != null)
			missingRequirement |= notOk.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= cond.willCrash(game);

		if (ok != null)
			willCrash |= ok.willCrash(game);

		if (notOk != null)
			willCrash |= notOk.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		cond.preprocess(game);
		if (ok != null)
			ok.preprocess(game);
		if (notOk != null)
			notOk.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("[If ");
		
		sb.append("cond=" + cond);
		sb.append(", ok=" + ok);
		sb.append(", notOk" + notOk);
		
		sb.append("]");
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet stateConcepts(final Context context)
	{
		if (cond.eval(context))
			return ok.stateConcepts(context);

		if (notOk != null)
			notOk.stateConcepts(context);

		return new BitSet();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String elseString = "";
		if (notOk != null)
			elseString = " else " + notOk.toEnglish(game);
		
		return "if " + cond.toEnglish(game) + " then " + ok.toEnglish(game) + elseString;
	}
	
	//-------------------------------------------------------------------------
		
}
