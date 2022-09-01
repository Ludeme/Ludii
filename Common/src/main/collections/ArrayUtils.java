package main.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Some utility methods for arrays
 *
 * @author Dennis Soemers
 */
public class ArrayUtils
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private ArrayUtils()
	{
		// Should not be used
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param arr
	 * @param val
	 * @return True if given array contains given value, false otherwise
	 */
	public static boolean contains(final boolean[] arr, final boolean val)
	{
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i] == val)
				return true;
		}
		
		return false;
	}
	
	/**
	 * @param arr
	 * @param val
	 * @return True if given array contains given value, false otherwise
	 */
	public static boolean contains(final int[] arr, final int val)
	{
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i] == val)
				return true;
		}
		
		return false;
	}
	
	/**
	 * @param arr
	 * @param val
	 * @return True if given array contains given value, false otherwise
	 */
	public static boolean contains(final double[] arr, final double val)
	{
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i] == val)
				return true;
		}
		
		return false;
	}
	
	/**
	 * @param arr
	 * @param val
	 * @return True if given array contains given object, false otherwise
	 */
	public static boolean contains(final Object[] arr, final Object val)
	{
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i] == null && val == null)
				return true;
			else if (arr[i] != null && arr[i].equals(val))
				return true;
		}
		
		return false;
	}
	
	/**
	 * @param val
	 * @param arr
	 * @return (First) index of given val in given array. -1 if not found
	 */
	public static int indexOf(final int val, final int[] arr)
	{
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i] == val)
				return i;
		}
		
		return -1;
	}
	
	/**
	 * @param val
	 * @param arr
	 * @return (First) index of given val in given array. -1 if not found
	 */
	public static int indexOf(final Object val, final Object[] arr)
	{
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i].equals(val))
				return i;
		}
		
		return -1;
	}
	
	/**
	 * @param arr
	 * @return Maximum value in given array
	 */
	public static int max(final int[] arr)
	{
		int max = Integer.MIN_VALUE;
		
		for (final int val : arr)
		{
			if (val > max)
				max = val;
		}
		
		return max;
	}
	
	/**
	 * @param arr
	 * @return Maximum value in given array
	 */
	public static float max(final float[] arr)
	{
		float max = Float.NEGATIVE_INFINITY;
		
		for (final float val : arr)
		{
			if (val > max)
				max = val;
		}
		
		return max;
	}
	
	/**
	 * @param arr
	 * @return Minimum value in given array
	 */
	public static float min(final float[] arr)
	{
		float min = Float.POSITIVE_INFINITY;
		
		for (final float val : arr)
		{
			if (val < min)
				min = val;
		}
		
		return min;
	}
	
	/**
	 * @param arr
	 * @param val
	 * @return Number of occurrences of given value in given array
	 */
	public static int numOccurrences(final double[] arr, final double val)
	{
		int num = 0;
		
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i] == val)
				++num;
		}
		
		return num;
	}
	
	/**
	 * Replaces all occurrences of oldVal with newVal in the given array
	 * @param arr
	 * @param oldVal
	 * @param newVal
	 */
	public static void replaceAll(final int[] arr, final int oldVal, final int newVal)
	{
		for (int i = 0; i < arr.length; ++i)
		{
			if (arr[i] == oldVal)
				arr[i] = newVal;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Note: probably kind of slow. Not intended for use in
	 * performance-sensitive situations.
	 * 
	 * @param matrix
	 * @param numDecimals
	 * @return A nicely-formatted String describing the given matrix
	 */
	public static String matrixToString(final float[][] matrix, final int numDecimals)
	{
		int maxStrLength = 0;
		for (final float[] arr : matrix)
		{
			for (final float element : arr)
			{
				final int length = String.valueOf((int) element).length();
				
				if (length > maxStrLength)
					maxStrLength = length;
			}
		}
		
		final StringBuilder sb = new StringBuilder();
		
		int digitsFormat = 1;
		for (int i = 1; i < maxStrLength; ++i)
		{
			digitsFormat *= 10;
		}
		
		for (int i = 0; i < matrix.length; ++i)
		{
			for (int j = 0; j < matrix[i].length; ++j)
			{
				sb.append(String.format(Locale.ROOT, "%" + digitsFormat + "." + numDecimals + "f", matrix[i][j]));
				if (j < matrix[i].length - 1)
					sb.append(",");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param numEntries
	 * @param comp
	 * @return A list of indices (ranging from 0 up to numEntries (exclusive), sorted
	 * using the given comparator.
	 */
	public static List<Integer> sortedIndices(final int numEntries, final Comparator<Integer> comp)
	{
		final List<Integer> list = new ArrayList<Integer>(numEntries);
		
		for (int i = 0; i < numEntries; ++i)
		{
			list.add(Integer.valueOf(i));
		}
		list.sort(comp);
		
		return list;
	}
	
	//-------------------------------------------------------------------------

}
