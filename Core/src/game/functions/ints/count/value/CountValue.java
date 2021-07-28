package game.functions.ints.count.value;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import game.Game;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import other.context.Context;

/**
 * Returns the number of a specific value in an array.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountValue extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which array. */
	private final IntArrayFunction arrayFn;
	
	/** Which value. */
	private final IntFunction valueFn;

	/**
	 * 	@param of The value to count.
	 *  @param in The array.
	 */
	public CountValue
	(
	          final IntFunction of, 
	    @Name final IntArrayFunction in 
	)
	{
		this.valueFn = of;
		this.arrayFn = in;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int value = valueFn.eval(context);
		final int[] array = arrayFn.eval(context);
		int count = 0;
		
		for(int v: array)
			if(v == value)
				count++;
		
		return count;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if(!valueFn.isStatic())
			return false;
		
		if(!arrayFn.isStatic())
			return false;
		
		return true;
	}

	@Override
	public String toString()
	{
		return "CountValue()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		gameFlags |= valueFn.gameFlags(game);
		gameFlags |= arrayFn.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(valueFn.concepts(game));
		concepts.or(arrayFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(valueFn.writesEvalContextRecursive());
		writeEvalContext.or(arrayFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(valueFn.readsEvalContextRecursive());
		readEvalContext.or(arrayFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		valueFn.preprocess(game);
		arrayFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= valueFn.missingRequirement(game);
		missingRequirement |= arrayFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= valueFn.willCrash(game);
		willCrash |= arrayFn.willCrash(game);
		return willCrash;
	}
}
