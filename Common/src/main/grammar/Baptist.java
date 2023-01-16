package main.grammar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//-----------------------------------------------------------------------------

/**
 * Creates random but plausible names.
 * 
 * @author cambolbro
 */
public class Baptist
{
	private final List<String> names = new ArrayList<String>();
	
	final char[] chars = 
	{ 
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',  
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 
		'u', 'v', 'w', 'x', 'y', 'z', '.',
	};

	private final int DOT = chars.length - 1;
	
	private final int[][][] counts = new int[chars.length][chars.length][chars.length];
	private final int[][] totals = new int[chars.length][chars.length];
	
	//-------------------------------------------------------------------------

	private static volatile Baptist singleton = null;

	//-------------------------------------------------------------------------

	private Baptist()
	{
		loadNames("/npp-names-2.txt");
		//loadNames("src/main/grammar/latin-out.txt");
		//loadNames("src/main/grammar/english-2000-2.txt");
		processNames();
	}
	
	//-------------------------------------------------------------------------

	public static Baptist baptist()
	{
		if (singleton == null)
		{
			synchronized(Baptist.class) 
			{	
				singleton = new Baptist();
			}
		}
		return singleton;
	}

	//-------------------------------------------------------------------------

	void loadNames(final String filePath)
	{
		names.clear();
		try(InputStream is = getClass().getResourceAsStream(filePath))
		{
			String line;
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) 
            {                            
                while ((line = reader.readLine()) != null) 
                {    
                	names.add(new String(line));
                }                
            }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
//		System.out.println(names.size() + " names loaded.");
//		System.out.println("Name 100 is " + names.get(100) + ".");
	}
	
	//-------------------------------------------------------------------------

	void processNames()
	{
		for (final String name : names)
			processName(name);
		
//		for (int c0 = 0; c0 < chars.length; c0++)
//			for (int c1 = 0; c1 < chars.length; c1++)
//			{
//				if (totals[c0][c1] == 0)
//					continue;
//				
//				System.out.println("\n" + chars[c0] + chars[c1] + " has " + totals[c0][c1] + " hits:");
//				for	(int c2 = 0; c2 < chars.length; c2++)
//					if (table[c0][c1][c2] > 0)
//						System.out.println("" + chars[c0] + chars[c1] + chars[c2] + " = " + table[c0][c1][c2]);
//			}
		
//		for (int n = 0; n < chars.length; n++)
//			System.out.println("counts[.][.][" + chars[n] + "]=" + counts[DOT][DOT][n]);
	}
	
	void processName(final String name)
	{
		final String str = ".." + name.toLowerCase() + "..";
		for (int c = 0; c < str.length() - 3; c++)
		{
			int ch0 = str.charAt(c)     - 'a';
			int ch1 = str.charAt(c + 1) - 'a';
			int ch2 = str.charAt(c + 2) - 'a';
			
			if (ch0 < 0 || ch0 >= 26)
				ch0 = DOT;
			if (ch1 < 0 || ch1 >= 26)
				ch1 = DOT;
			if (ch2 < 0 || ch2 >= 26)
				ch2 = DOT;
			
			counts[ch0][ch1][ch2]++;
			totals[ch0][ch1]++;
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Generate a name from the given seed, of minimum length.
	 */
	public String name(final long seed, final int minLength)
	{
		String result = "";
		
		final Random rng = new Random(seed);		
		rng.nextInt();  // Burn the first value, or will get the same first nextInt(R) 
						// if the seed is small and the range R is a power of 2!

//		int iteration = 0;
		do
		{
			if (result != "")
				result += " ";
			
//			rng.setSeed(seed + iteration++);
//			rng.nextInt();  // Burn the first value, or will get the same first nextInt(R) 
							// if the seed is small and the range R is a power of 2!

			result += name(rng);
			
		} while (result.length() < minLength);
		
		return result;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Generate a name using the given RNG.
	 */
	public String name(final Random rng)
	{
		final int[] token = { DOT, DOT, DOT };
		
		String str = "";
		while (true)
		{
			if (token[2] != DOT)
				str += (str == "") ? Character.toUpperCase(chars[token[2]]) : chars[token[2]];	
			
			token[0] = token[1];
			token[1] = token[2];

			final int total = totals[token[0]][token[1]];
			if (total == 0)
				break;

			final int target = rng.nextInt(total) + 1;
			int tally = 0;
			for (int n = 0; n < chars.length; n++)
			{
				if (counts[token[0]][token[1]][n] == 0)
					continue;
				
				tally += counts[token[0]][token[1]][n];
				if (tally >= target)
				{
					token[2] = n;
					break;
				}
			}
		}
		return str;
	}
	
	//-------------------------------------------------------------------------

	public static void main(final String[] args)
	{
		for (int n = 0; n < 20; n++)
			System.out.println(baptist().name(n, 5));
	
		System.out.println();
	
		String str = "Yavalath";
		System.out.println("'" + str + "' is called: " + baptist().name(str.hashCode(), 5));
		str = "Cameron";
		System.out.println("'" + str + "' is called: " + baptist().name(str.hashCode(), 5));
		
		System.out.println();
	
		for (int n = 0; n < 100; n++)
			System.out.println(baptist().name((int)System.nanoTime(), 5));
	
		System.out.println();
		
		for (int n = 0; n < 10000000; n++)
		{
			final int seed = (int)System.nanoTime();
			final String name = baptist().name(seed, 5);
			if (name.equals("Yavalath"))
			{
				System.out.println(name + " found after " + n + " tries (seed = " + seed + ").");
				break;
			}
		}
		System.out.println("Done.");
	}
	
	//-------------------------------------------------------------------------
	
}
