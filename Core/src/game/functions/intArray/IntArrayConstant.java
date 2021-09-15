package game.functions.intArray;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.IntFunction;
import other.context.Context;

/**
 * Sets a constant array of int values.
 * 
 * @author cambolbro and Eric.Piette
 */
@Hide
public final class IntArrayConstant extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Constant value. */
	private final IntFunction[] ints;

	//-------------------------------------------------------------------------

	/**
	 * Constant int array value.
	 * 
	 * @param ints The values of the array.
	 */
	public IntArrayConstant
	(
		final IntFunction[] ints
	)
	{
		this.ints = ints;
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		final int[] toReturn = new int[ints.length];
		for (int i = 0; i < ints.length; i++)
		{
			final IntFunction intFunction = ints[i];
			toReturn[i] = intFunction.eval(context);
		}
		return toReturn;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < ints.length; i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append("" + ints[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public boolean isStatic()
	{
		for (final IntFunction function : ints)
			if (!function.isStatic())
				return false;
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0l;
		for (final IntFunction function : ints)
			flags |= function.gameFlags(game);

		return flags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		for (final IntFunction function : ints)
			concepts.or(function.concepts(game));

		return concepts;
	}

	@Override
	public void preprocess(final Game game)
	{
		for (final IntFunction function : ints)
			function.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String englishString = "[";
		
		for (final IntFunction i : ints)
			englishString += i.toEnglish(game) + ",";
		
		englishString = englishString.substring(0, englishString.length()-1);
		
		englishString += "]";
		
		return englishString;
	}
	
	//-------------------------------------------------------------------------
}
