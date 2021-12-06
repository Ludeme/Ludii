package game.functions.ints.board;

import java.util.BitSet;

import annotations.Name;
import game.Game;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import main.Constants;
import other.context.Context;

/**
 * Returns one value of an array.
 * 
 * @author Eric Piette
 */
public final class ArrayValue extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which array. */
	private final IntArrayFunction array;

	/** Which site. */
	private final IntFunction indexFn;

	/** Precomputed value if possible. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * @param array  The array.
	 * @param index  The index of the site in the region.
	 * @example (arrayValue (results from:(last To) to:(sites Empty) (who at:(to))) index:(value))
	 */
	public ArrayValue
	(
		     final IntArrayFunction array,
	   @Name final IntFunction    index
	)
	{
		this.array = array;	
		indexFn = index;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int[] sites = array.eval(context);
		final int index = indexFn.eval(context);

		if (index < 0)
		{
			System.out.println("** Negative index in (regionSite ...).");
			return Constants.OFF;
		}
		else if (index < sites.length)
		{
			return sites[index];
		}


		return Constants.OFF;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return array.isStatic() && indexFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return array.gameFlags(game) | indexFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(array.concepts(game));
		concepts.or(indexFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(array.writesEvalContextRecursive());
		writeEvalContext.or(indexFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(array.readsEvalContextRecursive());
		readEvalContext.or(indexFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		array.preprocess(game);
		indexFn.preprocess(game);

		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= array.missingRequirement(game);
		missingRequirement |= indexFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= array.willCrash(game);
		willCrash |= indexFn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "site " + indexFn.toEnglish(game) + " of array " + array.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}
