package game.functions.intArray.math;

import java.util.BitSet;

import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.IntArrayFunction;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the intersection of many regions.
 * 
 * @author Eric.Piette and cambolbro
 */
public class Intersection extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which array 1. */
	private final IntArrayFunction array1;

	/** Which array 2. */
	private final IntArrayFunction array2;

	/** Which array. */
	private final IntArrayFunction[] arrays;

	/** If we can, we'll precompute once and cache */
	private int[] precomputedArray = null;

	//-------------------------------------------------------------------------

	/**
	 * For the intersection of two arrays.
	 * 
	 * @param array1 The first array.
	 * @param array2 The second array.
	 * @example (intersection (values Remembered "Earth") (values Remembered "Sea"))
	 */
	public Intersection
	(
		final IntArrayFunction array1, 
		final IntArrayFunction array2
	)
	{ 
		this.array1 = array1;
		this.array2 = array2;
		arrays = null;
	}
	
	/**
	 * For the intersection of many arrays.
	 * 
	 * @param arrays The different arrays.
	 * 
	 * @example (intersection {(values Remembered "Earth") (values Remembered "Sea")
	 *          (values Remembered "Sky")})
	 */
	public Intersection(final IntArrayFunction[] arrays)
	{
		array1 = null;
		array2 = null;
		this.arrays = arrays;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		if (precomputedArray != null)
			return precomputedArray;

		if (arrays == null)
		{
			final TIntArrayList values1 = new TIntArrayList(array1.eval(context));
			final TIntArrayList values2 = new TIntArrayList(array2.eval(context));

			for (int i = values2.size() - 1; i >= 0 ; i--)
			{
				final int value = values2.get(i);
				if (!values1.contains(value))
					values2.removeAt(i);
			}
			return values2.toArray();
		}
		else
		{
			if (arrays.length == 0)
				return new int[0];

			final TIntArrayList values1 = new TIntArrayList(arrays[0].eval(context));
			for (int i = 1; i < arrays.length; i++)
			{
				final TIntArrayList values2 = new TIntArrayList(arrays[i].eval(context));
				for (int j = values1.size() - 1; j >= 0 ; j--)
				{
					final int value = values1.get(j);
					if (!values2.contains(value))
						values1.removeAt(j);
				}
			}
			return values1.toArray();
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (arrays == null)
		{
			return array1.isStatic() && array2.isStatic();
		}
		else
		{
			for (final IntArrayFunction array : arrays)
				if (!array.isStatic())
					return false;
			return true;
		}
	}

	@Override
	public long gameFlags(final Game game)
	{
		if (arrays == null)
		{
			return array1.gameFlags(game) | array2.gameFlags(game);
		}
		else
		{
			long gameFlags = 0;
			for (final IntArrayFunction array : arrays)
				gameFlags |= array.gameFlags(game);
			return gameFlags;
		}
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (arrays == null)
		{
			concepts.or(array1.concepts(game));
			concepts.or(array2.concepts(game));
		}
		else
		{
			for (final IntArrayFunction array : arrays)
				concepts.or(array.concepts(game));
		}

		concepts.set(Concept.Union.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (arrays == null)
		{
			writeEvalContext.or(array1.writesEvalContextRecursive());
			writeEvalContext.or(array2.writesEvalContextRecursive());
		}
		else
		{
			for (final IntArrayFunction array : arrays)
				writeEvalContext.or(array.writesEvalContextRecursive());
		}
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (arrays == null)
		{
			readEvalContext.or(array1.readsEvalContextRecursive());
			readEvalContext.or(array2.readsEvalContextRecursive());
		}
		else
		{
			for (final IntArrayFunction array : arrays)
				readEvalContext.or(array.readsEvalContextRecursive());
		}
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (arrays == null)
		{
			missingRequirement |= array1.missingRequirement(game);
			missingRequirement |= array2.missingRequirement(game);
		}
		else
		{
			for (final IntArrayFunction array : arrays)
				missingRequirement |= array.missingRequirement(game);
		}
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (arrays == null)
		{
			willCrash |= array1.willCrash(game);
			willCrash |= array2.willCrash(game);
		}
		else
		{
			for (final IntArrayFunction array : arrays)
				willCrash |= array.willCrash(game);
		}
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (arrays == null)
		{
			array1.preprocess(game);
			array2.preprocess(game);
		}
		else
		{
			for (final IntArrayFunction array : arrays)
				array.preprocess(game);
		}

		if (isStatic())
		{
			precomputedArray = eval(new Context(game, null));
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		if (arrays != null)
		{
			String arrayString = "[";
			for (final IntArrayFunction i : arrays)
				arrayString += i.toEnglish(game) + ",";
			arrayString = arrayString.substring(0,arrayString.length()-1) + "]";
			return "the intersectin of all arrays in " + arrayString;
		}
		else
		{
			return "the intersection of " + array1.toEnglish(game) + " and " + array2.toEnglish(game);
		}
	}
	
	//-------------------------------------------------------------------------

}
