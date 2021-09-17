package game.rules.end;

import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import other.concept.EndConcepts;
import other.context.Context;

/**
 * Implements the condition(s) for ending the game, and deciding its result.
 * 
 * @author cambolbro and Eric.Piette
 * 
 * @remarks If the stopping condition is met then this rule will return a result,
 *          whether any sub-conditions are defined or not. 
 */
public class If extends BaseEndRule
{
	private static final long serialVersionUID = 1L;

	/** Test for whether the game ends or not. */
	private final BooleanFunction endCondition;

	/** Sub-conditions (one or many) to decide some more complex result. */
	private final If[] subconditions;

	//-------------------------------------------------------------------------

	/**
	 * @param test   Condition to end the game.
	 * @param sub    Sub-condition to check.
	 * @param subs   Sub-conditions to check.
	 * @param result Default result to return if no sub-condition is satisfied.
	 * 
	 * @example (if (is Mover (next)) (result Mover Win))
	 */
	public If
	(
	         	 final BooleanFunction test,
		@Opt @Or final If              sub, 
		@Opt @Or final If[]            subs, 
		@Opt     final Result          result
	)
	{
		super(result);
		
		int numNonNull = 0;
		if (sub != null)
			numNonNull++;
		if (subs != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Can't have more than one non-null Or parameter.");

		endCondition  = test;
		subconditions = (subs != null) 
								? subs 
								: (sub != null) 
									? new If[]{ sub } 
									: null;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @return The result of the game.
	 */
	@Override
	public EndRule eval(final Context context)
	{
		// Multiple sub-conditions to test
		if (endCondition.eval(context))
		{
			if (subconditions == null)
			{
				result().eval(context);
				return new BaseEndRule(result());
			}
			else
			{
				for (final If sub : subconditions)
				{
					final EndRule subResult = sub.eval(context);
					if (subResult != null)
						return subResult;
				}
			}
			
			// No other result, so fall through to default result
			return new BaseEndRule(result());
		}
		
		return null;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return The gameFlags of that ludeme.
	 */
	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;

		if (endCondition != null)
			gameFlags |= endCondition.gameFlags(game);
		
		if (subconditions != null)
			for (final If sub : subconditions)
				gameFlags |= sub.gameFlags(game);

		if (result() != null)
			gameFlags |= result().gameFlags(game);

		return gameFlags;
	}

	/**
	 * @param game
	 * @return The gameFlags of that ludeme.
	 */
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (endCondition != null)
			concepts.or(endCondition.concepts(game));

		if (subconditions != null)
			for (final If sub : subconditions)
				concepts.or(sub.concepts(game));

		if (result() != null)
			concepts.or(result().concepts(game));

		concepts.or(EndConcepts.get(endCondition, null, game, result()));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (endCondition != null)
			writeEvalContext.or(endCondition.writesEvalContextRecursive());

		if (subconditions != null)
			for (final If sub : subconditions)
				writeEvalContext.or(sub.writesEvalContextRecursive());

		if (result() != null)
			writeEvalContext.or(result().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (endCondition != null)
			readEvalContext.or(endCondition.readsEvalContextRecursive());

		if (subconditions != null)
			for (final If sub : subconditions)
				readEvalContext.or(sub.readsEvalContextRecursive());

		if (result() != null)
			readEvalContext.or(result().readsEvalContextRecursive());
		return readEvalContext;
	}

	/**
	 * To preprocess the condition of that ludeme.
	 */
	@Override
	public void preprocess(final Game game)
	{
		if (endCondition != null)
			endCondition.preprocess(game);
	
		if (subconditions != null)
			for (final If sub : subconditions)
				sub.preprocess(game);
	
		if (result() != null)
			result().preprocess(game);
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (endCondition instanceof FalseConstant)
		{
			game.addRequirementToReport("One of the ending condition is \"false\" which is wrong.");
			missingRequirement = true;
		}

		if (!hasAResult())
		{
			game.addRequirementToReport("One of the ending rule has no result.");
			missingRequirement = true;
		}

		if (endCondition != null)
			missingRequirement |= endCondition.missingRequirement(game);

		if (subconditions != null)
			for (final If sub : subconditions)
				missingRequirement |= sub.missingRequirement(game);
	
		if (result() != null)
			missingRequirement |= result().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (endCondition != null)
			willCrash |= endCondition.willCrash(game);

		if (subconditions != null)
			for (final If sub : subconditions)
				willCrash |= sub.willCrash(game);

		if (result() != null)
			willCrash |= result().willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return endCondition.
	 */
	public BooleanFunction endCondition()
	{
		return endCondition;
	}
	
	/**
	 * @return true if the condition has a result.
	 */
	public boolean hasAResult()
	{
		if (result() != null)
			return true;
		else if (subconditions != null)
			for (final If subcondition : subconditions)
				return subcondition.hasAResult();
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet stateConcepts(final Context context)
	{
		// Multiple sub-conditions to test
		if (endCondition.eval(context))
		{
			if (subconditions == null)
			{
				return EndConcepts.get(endCondition, context, context.game(), result());
			}
			else
			{
				for (final If sub : subconditions)
				{
					final EndRule subResult = sub.eval(context);
					if (subResult != null)
						return subResult.stateConcepts(context);
				}
			}
		}

		return new BitSet();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String endConditionString = "";
		
		if (result() != null)
			endConditionString = ", " + result().toEnglish(game);
		
		return "If " + endCondition.toEnglish(game) + endConditionString;
	}
	
}