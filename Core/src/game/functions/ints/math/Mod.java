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
 * Returns the modulo of a value.
 * 
 * @author Eric Piette
 */
@Alias(alias = "%")
public final class Mod extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Value. */
	private final IntFunction value;

	/** Modulos. */
	private final IntFunction modulus;

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * @param value  The value.
	 * @param modulo The modulo.
	 * @example (% (count Moves) 3)
	 */
	public Mod
	(
		final IntFunction value, 
		final IntFunction modulo
	)
	{
		this.value   = value;
		modulus = modulo;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		return value.eval(context) % modulus.eval(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return value.isStatic() && modulus.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return value.gameFlags(game) | modulus.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(value.concepts(game));
		concepts.or(modulus.concepts(game));
		concepts.set(Concept.Modulo.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(value.writesEvalContextRecursive());
		writeEvalContext.or(modulus.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(value.readsEvalContextRecursive());
		readEvalContext.or(modulus.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		value.preprocess(game);
		modulus.preprocess(game);
				
		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= value.missingRequirement(game);
		missingRequirement |= modulus.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= value.willCrash(game);
		willCrash |= modulus.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return value.toEnglish(game) + " modulo " + modulus.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
