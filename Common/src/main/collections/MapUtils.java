package main.collections;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility methods for maps
 * 
 * @author Dennis Soemers
 */
public class MapUtils 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private MapUtils()
	{
		// Should not be used
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param <K>
	 * @param map
	 * @param key Key to the slot that we want to add something to.
	 * @param toAdd Value we want to add. If there is no value yet, we'll insert this.
	 */
	public static <K> void add(final Map<K, Double> map, final K key, final double toAdd)
	{
		if (!map.containsKey(key))
			map.put(key, Double.valueOf(toAdd));
		else
			map.put(key, Double.valueOf(map.get(key).doubleValue() + toAdd));
	}
	
	/**
	 * Divides every value in this map by the given denominator
	 * 
	 * @param <K>
	 * @param map
	 * @param denominator
	 */
	public static <K> void divide(final Map<K, Double> map, final double denominator)
	{
		for (final Entry<K, Double> entry : map.entrySet())
		{
			map.put(entry.getKey(), Double.valueOf(entry.getValue().doubleValue() / denominator));
		}
	}
	
	//-------------------------------------------------------------------------

}
