package main.grammar;

//import java.util.BitSet;

/**
 * Combination for assigning null and non-null arguments in parameter lists.
 * 
 * @author cambolbro
 */
public class Combo
{
	private final int[] array;
	private final int count;
//	private final BitSet bits = new BitSet();
	
	//-------------------------------------------------------------------------
	
	public Combo(final int n, final int seed)
	{
		array = new int[n];
		
		int on = 0;
		for (int b = 0; b < n; b++)
			if ((seed & (0x1 << b)) != 0)
			{
				array[b] = ++on;			
//				bits.set(b, true);
			}
		count = on;
	}
	
	//-------------------------------------------------------------------------
	
	public int[] array()
	{
		return array;
	}
	
//	public BitSet bits()
//	{
//		return bits;
//	}

	//-------------------------------------------------------------------------

	/**
	 * @return Total length of combo include on-bits and off-bits.
	 */
	public int length()
	{
		return array.length;
	}
		
//	/**
//	 * @return Number of on-bits.
//	 */
//	public int count()
//	{
//		return bits.cardinality();
//	}

	/**
	 * @return Number of on-bits.
	 */
	public int count()
	{
		return count;
	}

	//-------------------------------------------------------------------------
	
}
