package common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CountMap<C extends Comparable<C>>
{

	private final HashMap<C,Integer> countmap = new HashMap<>();

	public ArrayList<Entry<C, Integer>> getSortedByKey()
	{
		final ArrayList<Entry<C, Integer>> es = new ArrayList<>(countmap.entrySet());
		es.sort(new Comparator<Entry<C, Integer>>()
		{

			@Override
			public int compare(
					final Entry<C, Integer> o1, final Entry<C, Integer> o2
			)
			{
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		return es;
	}
	
	public ArrayList<Entry<C, Integer>> getSortedByKey(final Comparator<C> comparator)
	{
		final ArrayList<Entry<C, Integer>> es = new ArrayList<>(countmap.entrySet());
		es.sort(new Comparator<Entry<C, Integer>>()
		{

			@Override
			public int compare(
					final Entry<C, Integer> o1, final Entry<C, Integer> o2
			)
			{
				return comparator.compare(o1.getKey(), o2.getKey());
			}
		});

		return es;
	}

	public void addInstance(final C key)
	{
		final Integer value = countmap.get(key);
		if (value==null)countmap.put(key, Integer.valueOf(1));
		else countmap.put(key, Integer.valueOf(value.intValue()+1));
	}
	
	public Integer get(final C key) {
		return countmap.get(key);
	}

	public void countUnique(final C[] words)
	{
		for (final C word : words)
		{
			addInstance(word);
		}
	}

	public Map<C, Integer> getHashMap()
	{
		return countmap;
	}

}
