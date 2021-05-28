package game.functions.booleans.math;

import java.util.BitSet;

import annotations.Anon;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Tests the not condition.
 * 
 * @author Eric.Piette
 * 
 */
public final class Not extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The boolean Function to reverse */
	private final BooleanFunction a;

	/** Precomputed boolean. */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------

	/**
	 * @param a The condition.
	 * @example (not (is In (last To) (sites Mover)))
	 */
	public Not(@Anon final BooleanFunction a)
	{
		this.a = a;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();

		return !a.eval(context);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The condition.
	 */
	public BooleanFunction a()
	{
		return a;
	}

	@Override
	public String toString()
	{
		return "Not(" + a.toString() + ")";
	}

	@Override
	public boolean isStatic()
	{
		return a.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return a.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(a.concepts(game));
		if (concepts.get(Concept.CanMove.id()))
		{
			concepts.set(Concept.CanMove.id(), false);
			concepts.set(Concept.CanNotMove.id(), true);
		}

		concepts.set(Concept.Negation.id(), true);

		if (concepts.get(Concept.IsFriend.id()))
		{
			concepts.set(Concept.IsFriend.id(), false);
			concepts.set(Concept.IsEmpty.id(), true);
			concepts.set(Concept.IsEnemy.id(), true);
		}
		else if (concepts.get(Concept.IsEnemy.id()))
		{
			concepts.set(Concept.IsEnemy.id(), false);
			concepts.set(Concept.IsEmpty.id(), true);
			concepts.set(Concept.IsFriend.id(), true);
		}
		else if (concepts.get(Concept.IsEmpty.id()))
		{
			concepts.set(Concept.IsEmpty.id(), false);
			concepts.set(Concept.IsEnemy.id(), true);
			concepts.set(Concept.IsFriend.id(), true);
		}

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(a.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(a.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		a.preprocess(game);

		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= a.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= a.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean autoFails()
	{
		return a.autoSucceeds();
	}
	
	@Override
	public boolean autoSucceeds()
	{
		return a.autoFails();
	}
}
