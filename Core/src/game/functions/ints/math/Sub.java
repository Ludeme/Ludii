package game.functions.ints.math;

import java.util.BitSet;

import annotations.Alias;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import main.Constants;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the subtraction A minus B.
 * 
 * @author Eric Piette
  */
@Alias(alias = "-")
public final class Sub extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value A. */
	private final IntFunction valueA;

	/** Which value B. */
	private final IntFunction valueB;
	
	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * @param valueA The first value (to subtract from) [0].
	 * @param valueB The second value (to be subtracted from the first value).
	 * 
	 * @example (- 1)
	 * @example (- (value Piece at:(last To)) (value Piece at:(last From)))
	 */
	public Sub
	(
		@Opt final IntFunction valueA,
		     final IntFunction valueB
	)
	{
		this.valueA = valueA != null ? valueA : new IntConstant(0);
		this.valueB = valueB;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		return valueA.eval(context) - valueB.eval(context);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The original value.
	 */
	public IntFunction a()
	{
		return valueA;
	}

	/**
	 * @return The value to subtract.
	 */
	public IntFunction b()
	{
		return valueB;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return valueA.isStatic() && valueB.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return valueA.gameFlags(game) | valueB.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(valueA.concepts(game));
		concepts.or(valueB.concepts(game));
		concepts.set(Concept.Subtraction.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(valueA.writesEvalContextRecursive());
		writeEvalContext.or(valueB.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(valueA.readsEvalContextRecursive());
		readEvalContext.or(valueB.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		valueA.preprocess(game);
		valueB.preprocess(game);
		
		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= valueA.missingRequirement(game);
		missingRequirement |= valueB.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= valueA.willCrash(game);
		willCrash |= valueB.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return valueA.toEnglish(game) + " minus " + valueB.toEnglish(game);
	}
}
