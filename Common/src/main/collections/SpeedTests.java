package main.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;

/**
 * Speed tests for custom collection types.
 * @author cambolbro
 */
public class SpeedTests
{
	@SuppressWarnings("unused")
	void test()
	{
//		// Stephen's (mrraow) "Integer == Integer" quirk
//		Integer a = 100;
//		Integer b = 100;
//		System.out.println(a == b);
//		
//		a = 200;
//		b = 200;
//		System.out.println(a == b);		
		
		//=====================================================================

//		System.out.println("Working Directory = " + System.getProperty("user.dir"));
//		
//		final String name = "../Core/src/game/functions/ints/IntConstant.java";
//		final File file = new File(name);
//		System.out.println("File " + name + " loaded, length is " + file.length() + ".");
		
		//=====================================================================
		
		final int N = 1000000;
		long startAt, stopAt;
		double secs;
		
		final List<Integer> ints = new ArrayList<>(); 
		final FastArrayList<Integer> fints = new FastArrayList<>();
		final TIntArrayList tints = new TIntArrayList();

		final List<String> strings = new ArrayList<>(); 
		final FastArrayList<String> fstrings = new FastArrayList<>();
		
		//---------------------------------
		// Warm them up...
		
		for (int n = 0; n < N; n++)
			ints.add(n);
		
		for (int n = 0; n < N; n++)
			fints.add(n);
		
		for (int n = 0; n < N; n++)
			tints.add(n);
		
		for (int n = 0; n < N; n++)
			strings.add("" + n);
		
		for (int n = 0; n < N; n++)
			fstrings.add("" + n);
		
		//---------------------------------
		
		System.out.println("Adding integers to list:");
		
		startAt = System.nanoTime();
		
		ints.clear(); 
		for (int n = 0; n < N; n++)
			ints.add(n);
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Integers added to ArrayList in " + secs + "s.");
				
		startAt = System.nanoTime();
		
		fints.clear(); 
		for (int n = 0; n < N; n++)
			fints.add(n);
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Integers added to FastArrayList in " + secs + "s.");
				
		startAt = System.nanoTime();
		
		tints.clear(); 
		for (int n = 0; n < N; n++)
			tints.add(n);
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " ints added to TIntArrayList in " + secs + "s.");
		
		//---------------------------------
		
		System.out.println("\nAdding strings to list:");
		
		startAt = System.nanoTime();
		
		strings.clear(); 
		for (int n = 0; n < N; n++)
			strings.add("" + n);
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Strings added to ArrayList in " + secs + "s.");
				
		startAt = System.nanoTime();
		
		fstrings.clear(); 
		for (int n = 0; n < N; n++)
			fstrings.add("" + n);
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Strings added to FastArrayList in " + secs + "s.");
		
		//---------------------------------
		
		System.out.println("\nAdding ints to list and retrieving then as ints:");
		
		startAt = System.nanoTime();
		
		ints.clear(); 
		for (int n = 0; n < N; n++)
			ints.add(n);
		for (final Integer i : ints)
		{
			final int x = i.intValue();
		}
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Integers added to ArrayList and retrieved in " + secs + "s.");
				
		startAt = System.nanoTime();
		
		fints.clear(); 
		for (int n = 0; n < N; n++)
			fints.add(n);
		for (final Integer i : fints)
		{
			final int x = i.intValue();
		}
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Integers added to FastArrayList and retrieved in " + secs + "s.");
				
		startAt = System.nanoTime();
		
		tints.clear(); 
		for (int n = 0; n < N; n++)
			tints.add(n);
		for (int i = 0; i < tints.size(); i++)
		{
			final int x = tints.get(i);
		}

		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " ints added to TIntArrayList and retrieved in " + secs + "s.");
		
		//---------------------------------
		
		System.out.println("\nAdding and sorting integers:");
		
		startAt = System.nanoTime();
		
		ints.clear(); 
		for (int n = 0; n < N; n++)
			ints.add(N - n);
		Collections.sort(ints);
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Integers added to ArrayList and sorted in " + secs + "s.");
		
//		startAt = System.nanoTime();
//		
//		fints.clear(); 
//		for (int n = 0; n < N; n++)
//			fints.add(N - n);
//		
//		stopAt = System.nanoTime();
//		secs = (stopAt - startAt) / 1000000000.0;
//		System.out.println(N + " Integers added to FastArrayList and sorted in " + secs + "s.");
		System.out.println("** Sort not implemented for FastArrayList.");
				
		startAt = System.nanoTime();
		
		tints.clear(); 
		for (int n = 0; n < N; n++)
			tints.add(N - n);
		tints.sort();
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " int added to TIntArrayList and sorted in " + secs + "s.");

		//---------------------------------
		
		System.out.println("\nAdding and sorting strings:");
		
		startAt = System.nanoTime();
		
		strings.clear(); 
		for (int n = 0; n < N; n++)
			strings.add("" + (N - n));
		Collections.sort(strings);
		
		stopAt = System.nanoTime();
		secs = (stopAt - startAt) / 1000000000.0;
		System.out.println(N + " Strings added to ArrayList and sorted in " + secs + "s.");
		
//		startAt = System.nanoTime();
//		
//		fstrings.clear(); 
//		for (int n = 0; n < N; n++)
//			fstrings.add("" + (N - n));
//		
//		stopAt = System.nanoTime();
//		secs = (stopAt - startAt) / 1000000000.0;
//		System.out.println(N + " Integers added to FastArrayList and sorted in " + secs + "s.");
		System.out.println("** Sort not implemented for FastArrayList.");
		
		//---------------------------------
		
		
	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final SpeedTests app = new SpeedTests();
		app.test();
	}
}
