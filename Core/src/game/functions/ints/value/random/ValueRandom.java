package game.functions.ints.value.random;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.range.RangeFunction;
import game.types.state.GameType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns a random value within a range.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ValueRandom extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The range. */
	private final RangeFunction range;

	/**
	 * @param range The range allowed.
	 */
	public ValueRandom(final RangeFunction range)
	{
		this.range = range;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int min = range.minFn().eval(context);
		final int max = range.maxFn().eval(context);

		if (min > max)
			return Constants.UNDEFINED;

		final int randomValue = context.rng().nextInt(max - min + 1);
		
		return randomValue + min;
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
		return range.gameFlags(game) | GameType.Stochastic;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Stochastic.id(), true);
		range.concepts(game);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(range.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(range.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= range.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= range.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		range.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "a random value in the range " + range.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}
