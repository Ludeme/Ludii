package metrics.suffix_tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class PercentageMap<C extends Comparable<C>>
{

	private final HashMap<C,Integer> countmap = new HashMap<>();
	private final HashMap<C, Double> percentageMap = new HashMap<>();
	private boolean finalized = false;

	
	
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
		this.finalized = false;
		if (value==null)countmap.put(key, Integer.valueOf(1));
		else countmap.put(key, Integer.valueOf(value.intValue()+1));
	}
	
	public Double get(final C key) {
		return percentageMap.get(key);
	}

	public void countUnique(final C[] words)
	{
		for (final C word : words)
		{
			addInstance(word);
		}
	}

	public HashMap<C, Integer> getCountMap()
	{
		return countmap;
	}
	public HashMap<C, Double> getHashMap()
	{
		if (this.finalized)return percentageMap;
		percentageMap.clear();
		int sum = 0;
		
		final Collection<Integer> values = countmap.values();
		for (final Integer integer : values)
		{
			sum += integer.intValue();
		}
		for (final Entry<C, Integer> entry : countmap.entrySet())
		{
			percentageMap.put(entry.getKey(), Double.valueOf(entry.getValue().doubleValue()/sum));
		}
		
		this.finalized=true;
		return percentageMap;
	}

}
