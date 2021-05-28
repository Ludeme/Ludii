package game.functions.ints.size.array;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.BaseIntFunction;
import other.context.Context;

/**
 * Returns the size of an IntArrayFunction.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SizeArray extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which array. */
	private final IntArrayFunction array;

	/**
	 * @param array The IntArrayFunction to return the size.
	 */
	public SizeArray
	(
		final IntArrayFunction array
	)
	{
		this.array   = array;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int[] intArray = array.eval(context);
		return intArray.length;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return array.isStatic();
	}


	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = array.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(array.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(array.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(array.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		array.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= array.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= array.willCrash(game);
		return willCrash;
	}
}
