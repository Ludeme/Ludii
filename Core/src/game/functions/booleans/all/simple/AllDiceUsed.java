package game.functions.booleans.all.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns true if all the dice are used during your turn.
 * 
 * @author Eric.Piette
 * 
 * @remarks In dice games when the dice can be used one by one. True if all the
 *          values of a dice container are equal to zero.
 */
@Hide
public final class AllDiceUsed extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public AllDiceUsed()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int[][] diceValues = context.state().currentDice();

		for (int indexHand = 0; indexHand < diceValues.length; indexHand++)
			for (int indexDie = 0; indexDie < diceValues[indexHand].length; indexDie++)
				if (diceValues[indexHand][indexDie] != 0)
					return false;

		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "AllDiceUsed()";
	}

	//-------------------------------------------------------------------------

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
			game.addRequirementToReport("The ludeme (all DiceUsed) is used but the equipment has no dice.");
			return true;
		}

		return false;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "all dice have been used";
	}
}
