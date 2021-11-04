package main.grammar;

import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

//-----------------------------------------------------------------------------

/**
 * Lists of combinations of M null args out of N, for compiling.
 * 
 * @author cambolbro
 */
public class ArgCombos
{
	public static final int MAX_ARGS = 20;
	
	private static volatile ArgCombos singleton = null;
	
	@SuppressWarnings("unchecked")
	private static final List<BitSet>[][] combos = new ArrayList[MAX_ARGS+1][MAX_ARGS+1];
	
	//-------------------------------------------------------------------------

	private ArgCombos()
	{		
//		create();
		//dump();
	}

	//-------------------------------------------------------------------------

	/**
	 * Access argCombos here.
	 * @return The singleton grammar object.
	 */
	public static ArgCombos get()
	{
		if (singleton == null)
		{
			synchronized(ArgCombos.class) 
			{
				if (singleton == null)
					singleton = new ArgCombos();
			}
		}
		return singleton;
	}

	//-------------------------------------------------------------------------

	public List<BitSet>[][] combos()
	{
		return combos;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create the arg combos.
	 */
	private static void create()
	{
		for (int m = 0; m < MAX_ARGS+1; m++)
			for (int n = 0; n < MAX_ARGS+1; n++)
				combos[m][n] = new ArrayList<BitSet>();
		
		// Extract the bit patterns out of sequential numbers in range
		for (int seed = 0; seed < (0x1 << (MAX_ARGS)); seed++)
		{
			final int on = Integer.bitCount(seed);
			for (int n = on; n < MAX_ARGS+1; n++)
				if (seed < (0x1 << n))  // all on-bits are within range
					combos[on][n].add(BitSet.valueOf(new long[] { seed }));	
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Test combos.
	 */
	public static void dump()
	{
		for (int m = 0; m < MAX_ARGS+1; m++)
			for (int n = 0; n < MAX_ARGS+1; n++)
			{
				System.out.println("\nm=" + m + ", n=" + n + ":");
				if (combos[m][n].isEmpty())
					continue;
				for (final BitSet bits : combos[m][n])
				{
					int index = 0;
					for (int b = 0; b < n; b++)
						System.out.print((bits.get(b) ? (++index) : "-") + " ");
					System.out.println();
				}
			}
	}

	//-------------------------------------------------------------------------
	
}
