package game.functions.ints.math;

import java.util.BitSet;

import annotations.Alias;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import main.Constants;
import other.concept.Concept;
import other.context.Context;

/**
 * To divide a value by another.
 * 
 * @author Eric.Piette and cambolbro
 * @remarks The result will be an integer and round down the result.
 */
@Alias(alias = "/")
public final class Div extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value to divide. */
	private final IntFunction a;

	/** To divide by b. */
	private final IntFunction b;

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * To divide a value by another.
	 * 
	 * @param a The value to divide.
	 * @param b To divide by b.
	 * @example (/ (value Piece at:(from)) (value Piece at:(to)))
	 */
	public Div
	(
		 final IntFunction a,
		 final IntFunction b
	)
	{
		this.a = a;
		this.b = b;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int evalB = b.eval(context);
		if (evalB == 0)
			throw new IllegalArgumentException("Division by zero.");
		
		return a.eval(context) / evalB;
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
		concepts.set(Concept.Division.id(), true);
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
	public void preprocess(final Game game)
	{		
		a.preprocess(game);
		b.preprocess(game);

		if (isStatic())
			precomputedValue = eval(new Context(game, null));
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return a.toEnglish(game) + " divided by " + b.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}
