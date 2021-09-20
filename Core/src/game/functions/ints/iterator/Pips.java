package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the number of pips of a die.
 * 
 * @author Eric.Piette
 * 
 * @Remarks That ludeme has to be used under the ForEachDie ludeme as an
 *          iterator for each die.
 */
public final class Pips extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Our singleton instance */
	private static final Pips INSTANCE = new Pips();

	/**
	 * @return Returns our singleton instance as a ludeme
	 * @example (pips)
	 */
	public static Pips construct()
	{
		return INSTANCE;
	}

	//-------------------------------------------------------------------------

	private Pips()
	{
		// Private constructor; singleton!
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Singleton instance
	 */
	public static Pips instance()
	{
		return INSTANCE;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.pipCount();
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
		return 0;
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
		return readsEvalContextFlat();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.set(EvalContextData.PipCount.id(), true);
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasHandDice())
		{
			game.addRequirementToReport("The ludeme (pips) is used but the equipment has no dice.");
			missingRequirement = true;
		}
		return missingRequirement;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Pips()";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the number of pips on the dice";
	}

	//-------------------------------------------------------------------------
}
