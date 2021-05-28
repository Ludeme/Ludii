package game.functions.booleans.is.integer;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns true if any die is equal to the value.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsAnyDie extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The site to check. */
	private final IntFunction valueFn;

	// -------------------------------------------------------------------------

	/**
	 * @param value The value to check.
	 */
	public IsAnyDie(final IntFunction value)
	{
		this.valueFn = value;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (valueFn == null)
			return false;

		final int value = valueFn.eval(context);
		final int[] dieValues = context.state().currentDice(0);
		for (final int dieValue : dieValues)
			if (value == dieValue)
				return true;

		return false;
	}

	// -------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsDie";
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Stochastic | valueFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(valueFn.concepts(game));
		concepts.set(Concept.Dice.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(valueFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(valueFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		valueFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasHandDice())
		{
			game.addRequirementToReport("The ludeme (is AnyDie ...) is used but the equipment has no dice.");
			missingRequirement = true;
		}
		missingRequirement |= valueFn.missingRequirement(game);
		return missingRequirement;
	}
}
