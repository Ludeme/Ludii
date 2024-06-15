package main.collections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Utility methods for lists
 * 
 * @author Dennis Soemers
 */
public class ListUtils 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private ListUtils()
	{
		// Should not be used
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param list A single list
	 * @return A list containing all possible permutations of the given list
	 */
	public static List<TIntArrayList> generatePermutations(final TIntArrayList list)
	{
		if (list.size() == 0)
		{
			List<TIntArrayList> perms = new ArrayList<TIntArrayList>(1);
			perms.add(new TIntArrayList(0, list.getNoEntryValue()));
			return perms;
		}
		
		final int lastElement = list.removeAt(list.size() - 1);
		final List<TIntArrayList> perms = new ArrayList<TIntArrayList>();
		
		final List<TIntArrayList> smallPerms = generatePermutations(list);
		for (final TIntArrayList smallPerm : smallPerms)
		{
			for (int i = smallPerm.size(); i >= 0; --i)
			{
				TIntArrayList newPerm = new TIntArrayList(smallPerm);
				newPerm.insert(i, lastElement);
				perms.add(newPerm);
			}
		}
		
		return perms;
	}
	
	/**
	 * NOTE: it's theoretically possible that we generate duplicate permutations, but for large lists
	 * and a small number of samples this is very unlikely.
	 * 
	 * @param list A single list
	 * @param numPermutations Number of permutations we want to generate
	 * @return A list containing a sample of all possible permutations of the given list (with replacement)
	 */
	public static List<TIntArrayList> samplePermutations(final TIntArrayList list, final int numPermutations)
	{
		final List<TIntArrayList> perms = new ArrayList<TIntArrayList>(numPermutations);
		
		for (int i = 0; i < numPermutations; ++i)
		{
			final TIntArrayList randomPerm = new TIntArrayList(list);
			randomPerm.shuffle(ThreadLocalRandom.current());
			perms.add(randomPerm);
		}
		
		return perms;
	}
	
	/**
	 * NOTE: this may not be the most efficient implementation. Do NOT use
	 * for performance-sensitive situations
	 * 
	 * @param optionsLists List of n lists of options.
	 * @return List containing all possible n-tuples. Officially, this is called Cartesian Product :)
	 */
	public static <E> List<List<E>> generateTuples(final List<List<E>> optionsLists)
	{
		final List<List<E>> allTuples = new ArrayList<List<E>>();
		
		if (optionsLists.size() > 0)
		{
			final List<E> firstEntryOptions = optionsLists.get(0);
			final List<List<E>> remainingOptionsLists = new ArrayList<List<E>>();
			
			for (int i = 1; i < optionsLists.size(); ++i)
			{
				remainingOptionsLists.add(optionsLists.get(i));
			}
			
			final List<List<E>> nMinOneTuples = generateTuples(remainingOptionsLists);
			
			for (int i = 0; i < firstEntryOptions.size(); ++i)
			{
				for (final List<E> nMinOneTuple : nMinOneTuples)
				{
					final List<E> newTuple = new ArrayList<E>(nMinOneTuple);
					newTuple.add(0, firstEntryOptions.get(i));
					allTuples.add(newTuple);
				}
			}
		}
		else
		{
			allTuples.add(new ArrayList<E>(0));
		}
		
		return allTuples;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param maxExclusive
	 * @return Exactly like python's range() function, generates a list from 0 to maxExclusive
	 */
	public static TIntArrayList range(final int maxExclusive)
	{
		final TIntArrayList list = new TIntArrayList(maxExclusive);
		for (int i = 0; i < maxExclusive; ++i)
		{
			list.add(i);
		}
		return list;
	}
	
	/**
	 * @param minInclusive
	 * @param maxExclusive
	 * @return Exactly like python's range() function, generates a list from minInclusive to maxExclusive
	 */
	public static TIntArrayList range(final int minInclusive, final int maxExclusive)
	{
		final TIntArrayList list = new TIntArrayList(maxExclusive);
		for (int i = minInclusive; i < maxExclusive - minInclusive; ++i)
		{
			list.add(i);
		}
		return list;
	}
	
	/**
	 * Splits the given list into numLists different sublists. The sublists will be
	 * equally-sized (except for, possibly, the last one, which may be smaller than the others).
	 * 
	 * @param list
	 * @param numLists
	 * @return
	 */
	public static TIntArrayList[] split(final TIntArrayList list, final int numLists)
	{
		final TIntArrayList[] sublists = new TIntArrayList[numLists];
		final int sublistSize = (int) Math.ceil((double) list.size() / numLists);
		
		for (int i = 0; i < numLists; ++i)
		{
			final TIntArrayList sublist = new TIntArrayList();
			
			for (int j = 0; j < sublistSize; ++j)
			{
				sublist.add(list.getQuick(i * sublistSize + j));
				
				if (i * sublistSize + j + 1 >= list.size())
					break;
			}
			
			sublists[i] = sublist;
		}
		
		return sublists;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param list
	 * @return Index of maximum entry in the list
	 * 	(breaks ties by taking the lowest index).
	 */
	public static int argMax(final TFloatArrayList list)
	{
		int argMax = 0;
		float maxVal = list.getQuick(0);
		
		for (int i = 1; i < list.size(); ++i)
		{
			final float val = list.getQuick(i);
			
			if (val > maxVal)
			{
				maxVal = val;
				argMax = i;
			}
		}
		
		return argMax;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Removes element at given index. Does not shift all subsequent elements,
	 * but only swaps the last element into the removed index.
	 * @param <E>
	 * @param list
	 * @param idx
	 */
	public static <E> void removeSwap(final List<E> list, final int idx)
	{
		final int lastIdx = list.size() - 1;
		list.set(idx, list.get(lastIdx));
		list.remove(lastIdx);
	}
	
	/**
	 * Removes element at given index. Does not shift all subsequent elements,
	 * but only swaps the last element into the removed index.
	 * @param list
	 * @param idx
	 */
	public static void removeSwap(final TIntArrayList list, final int idx)
	{
		final int lastIdx = list.size() - 1;
		list.setQuick(idx, list.getQuick(lastIdx));
		list.removeAt(lastIdx);
	}
	
	/**
	 * Removes element at given index. Does not shift all subsequent elements,
	 * but only swaps the last element into the removed index.
	 * @param list
	 * @param idx
	 */
	public static void removeSwap(final TFloatArrayList list, final int idx)
	{
		final int lastIdx = list.size() - 1;
		list.setQuick(idx, list.getQuick(lastIdx));
		list.removeAt(lastIdx);
	}
	
	/**
	 * Removes all elements from the given list that satisfy the given predicate, using
	 * remove-swap (which means that the order of the list may not be preserved).
	 * @param list
	 * @param predicate
	 */
	public static <E> void removeSwapIf(final List<E> list, final Predicate<E> predicate)
	{
		for (int i = list.size() - 1; i >= 0; --i)
		{
			if (predicate.test(list.get(i)))
				removeSwap(list, i);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates all combinations of given target combination-length from
	 * the given list of candidates (without replacement, order does not
	 * matter). Typical initial call would look like:<br>
	 * <br>
	 * <code>generateAllCombinations(candidates, targetLength, 0, new int[targetLength], outList)</code>
	 * 
	 * @param candidates
	 * @param combinationLength
	 * @param startIdx Index at which to start filling up results array
	 * @param currentCombination (partial) combination constructed so far
	 * @param combinations List of all result combinations
	 */
	public static void generateAllCombinations
	(
		final TIntArrayList candidates,
		final int combinationLength,
		final int startIdx,
		final int[] currentCombination,
		final List<TIntArrayList> combinations
	)
	{
		if (combinationLength == 0)
		{
			combinations.add(new TIntArrayList(currentCombination));
		}
		else
		{
			for (int i = startIdx; i <= candidates.size() - combinationLength; ++i)
			{
				currentCombination[currentCombination.length - combinationLength] = candidates.getQuick(i);
				generateAllCombinations(candidates, combinationLength - 1, i + 1, currentCombination, combinations);
			}
		}
	}
	
	/**
	 * @param numItems Number of items from which we can pick
	 * @param combinationLength How many items should we pick per combination
	 * @return How many combinations of N items are there, if we sample with replacement
	 * 	(and order does not matter)?
	 */
	public static final int numCombinationsWithReplacement(final int numItems, final int combinationLength)
	{
		// We have to compute:
		//
		// (n + r - 1)!
		// -------------
		// r! * (n - 1)!
		//
		// Where n = numItems, r = combinationLength
		
		long numerator = 1L;
		long denominator = 1L;
		
		if (combinationLength >= (numItems - 1))
		{
			// Divide numerator and denominator by r!
			// Retain (n - 1)! as denominator
			// Retain (r + 1) * (r + 2) * (r + 3) * ... * (n + r - 1) as numerator
			for (int i = combinationLength + 1; i <= (numItems + combinationLength - 1); ++i)
			{
				numerator *= i;
			}
			
			for (int i = 1; i <= (numItems - 1); ++i)
			{
				denominator *= i;
			}
		}
		else
		{
			// Divide numerator and denominator by (n - 1)!
			// Retain r! as denominator
			// Retain n * (n + 1) * (n + 2) * ... * (n + r - 1) as 
			for (int i = numItems; i <= (numItems + combinationLength - 1); ++i)
			{
				numerator *= i;
			}
			
			for (int i = 1; i <= combinationLength; ++i)
			{
				denominator *= i;
			}
		}
		
		return (int) (numerator / denominator);
	}
	
	/**
	 * @param items
	 * @param combinationLength
	 * @return All possible combinations of n selections of given array of items,
	 * sampled with replacement. Order does not matter.
	 */
	public static Object[][] generateCombinationsWithReplacement
	(
		final Object[] items,
		final int combinationLength
	)
	{
		if (combinationLength == 0)
			return new Object[0][];
		
		final int numCombinations = numCombinationsWithReplacement(items.length, combinationLength);
		final Object[][] combinations = new Object[numCombinations][combinationLength];
		
		int nextCombIdx = 0;
		final int[] indices = new int[combinationLength];
		int idxToIncrement = indices.length - 1;
		while (true)
		{
			final Object[] arr = new Object[combinationLength];
			
			for (int i = 0; i < indices.length; ++i)
			{
				arr[i] = items[indices[i]];
			}
			
			combinations[nextCombIdx++] = arr;
			
			while (idxToIncrement >= 0)
			{
				if (++indices[idxToIncrement] == items.length)
				{
					indices[idxToIncrement--] = 0;
				}
				else
				{
					break;
				}
			}
			
			if (idxToIncrement < 0)
				break;
			
			// Order does not matter
			for (int i = idxToIncrement + 1; i < indices.length; ++i)
			{
				indices[i] = indices[idxToIncrement];
			}
			
			idxToIncrement = indices.length - 1;
		}
		
		if (nextCombIdx != numCombinations)
			System.err.println("ERROR: Expected to generate " + numCombinations + " combinations, but only generated " + nextCombIdx);
		
		return combinations;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * NOTE: this should only be used for diagnosing performance issues / temporary code!
	 * This uses reflection and is way too slow and should not be necessary for 
	 * any permanent code.
	 * 
	 * @param l
	 * @return The capacity (maximum size before requiring re-allocation) of given ArrayList
	 */
	public static int getCapacity(final ArrayList<?> l)
	{
		try
		{
			final Field dataField = ArrayList.class.getDeclaredField("elementData");
	        dataField.setAccessible(true);
	        return ((Object[]) dataField.get(l)).length;
		}
		catch 
		(
			final NoSuchFieldException 	| 
			SecurityException 			| 
			IllegalArgumentException 	| 
			IllegalAccessException exception
		)
		{
			exception.printStackTrace();
		} 
        
		return -1;
    }
	
	//-------------------------------------------------------------------------

}
