package game.functions.booleans.math;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanConstant.TrueConstant;
import game.functions.booleans.BooleanFunction;
import other.concept.Concept;
import other.context.Context;
import other.location.Location;

/**
 * Tests the Or boolean node. True if at least one condition is true between the two conditions.
 * 
 * @author cambolbro
 */
public final class Or extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * List of BooleanFunction.
	 */
	private final BooleanFunction[] list;

	/**
	 * Precomputed boolean.
	 */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------

	/**
	 * For an or between two booleans.
	 * 
	 * @param a First condition.
	 * @param b Second condition.
	 * 
	 * @example (or (= (who at:(last To)) (mover) )(!= (who at:(last From))
	 *          (mover)))
	 */
	public Or
	(
		final BooleanFunction a,
		final BooleanFunction b
	)
	{
		list = new BooleanFunction[] {a, b};
	}

	/**
	 * For an or between many booleans.
	 * 
	 * @param list The list of conditions.
	 * 
	 * @example (or {(= (who at:(last To)) (mover)) (!= (who at:(last From))
	 *          (mover)) (is Pending)})
	 */
	public Or
	(
		final BooleanFunction[] list
	)
	{
		this.list = list;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();
		
		for (final BooleanFunction elem: list)
			if (elem.eval(context))
				return true;
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{	
		for (final BooleanFunction elem: list)
			if (!elem.isStatic())
				return false;
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;
		for (final BooleanFunction elem : list)
			gameFlags |= elem.gameFlags(game);
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		for (final BooleanFunction elem : list)
			concepts.or(elem.concepts(game));

		concepts.set(Concept.Disjunction.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		for (final BooleanFunction elem : list)
			writeEvalContext.or(elem.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		for (final BooleanFunction elem : list)
			readEvalContext.or(elem.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		for (final BooleanFunction elem : list)
			elem.preprocess(game);
		
		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		for (final BooleanFunction cond : list)
			if (cond instanceof FalseConstant)
			{
				game.addRequirementToReport("One of the condition in an (or ...) ludeme is \"false\".");
				missingRequirement = true;
				break;
			}

		for (final BooleanFunction cond : list)
		{
			if (cond instanceof TrueConstant)
			{
				game.addRequirementToReport("One of the condition in an (or ...) ludeme is \"true\".");
				missingRequirement = true;
				break;
			}
		}

		for (final BooleanFunction elem : list)
			missingRequirement |= elem.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		for (final BooleanFunction elem : list)
			willCrash |= elem.willCrash(game);
		return willCrash;
	}

	/**
	 * @return Our list of Boolean Functions
	 */
	public BooleanFunction[] list()
	{
		return list;
	}

	// -------------------------------------------------------------------------

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		if (!eval(context))
			return new ArrayList<Location>();

		for (final BooleanFunction cond : list)
			if (cond.eval(context))
				return cond.satisfyingSites(context);

		return new ArrayList<Location>();
	}

	@Override
	public BitSet stateConcepts(final Context context)
	{
		final BitSet stateConcepts = new BitSet();
		
		for (final BooleanFunction elem : list)
			if(elem.eval(context))
				stateConcepts.or(elem.stateConcepts(context));

		return stateConcepts;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text = "";
		int count=0;
		
		for (final BooleanFunction func : list) 
		{
			text += func.toEnglish(game);
            count++;
            
            if(count == list.length-1)
                text += " or ";
            else if(count < list.length)
            	text += ", ";
		}
		
		return text;
	}
}
