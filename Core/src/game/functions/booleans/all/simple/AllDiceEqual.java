package game.functions.booleans.all.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns true if all the dice are equal when they are rolled.
 * 
 * @author Eric.Piette
 * 
 * @remarks That data is modified only when the dice are rolled.
 */
@Hide
public final class AllDiceEqual extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	public AllDiceEqual()
	{
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.state().isDiceAllEqual();
	}

	// -------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "AllDiceEqual()";
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
		return GameType.Stochastic;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Dice.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		if (!game.hasHandDice())
		{
			game.addRequirementToReport("The ludeme (all DiceEqual) is used but the equipment has no dice.");
			return true;
		}

		return false;
	}
}