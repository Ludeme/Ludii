package game.functions.booleans.math;

import java.util.BitSet;

import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Tests the Xor boolean node.
 * 
 * @author cambolbro
 * 
 */
public final class Xor extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The first condition.
	 */
	private final BooleanFunction a;

	/**
	 * The second condition.
	 */
	private final BooleanFunction b;

	/** Precomputed boolean. */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------

	/**
	 * @param a First condition.
	 * @param b Second condition.
	 * @example (xor (= (who at:(last To)) (mover)) (!= (who at:(last From))
	 *          (mover)))
	 */
	public Xor
	(
		final BooleanFunction a,
		final BooleanFunction b
	)
	{
		this.a = a;
		this.b = b;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();
		
		final boolean evalA = a.eval(context); 
		final boolean evalB = b.eval(context); 
		
		return (evalA && !evalB || !evalA && evalB);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{	
		return a.isStatic() && b.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return a.gameFlags(game) | b.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(a.concepts(game));
		concepts.or(b.concepts(game));

		concepts.set(Concept.ExclusiveDisjunction.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(a.writesEvalContextRecursive());
		writeEvalContext.or(b.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(a.readsEvalContextRecursive());
		readEvalContext.or(b.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= a.missingRequirement(game);
		missingRequirement |= b.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= a.willCrash(game);
		willCrash |= b.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		a.preprocess(game);
		b.preprocess(game);
		
		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}
}
