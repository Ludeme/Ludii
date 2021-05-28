package game.util.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bucket for sorting coordinates along a dimension.
 * 
 * @author cambolbro
 */
public class Bucket
{
	private final List<ItemScore> items = new ArrayList<ItemScore>();
	private double total = 0;  // item total score of items

	//-------------------------------------------------------------------------
	
	/**
	 * @return a List of ItemScore.
	 */
	public List<ItemScore> items()
	{
		return Collections.unmodifiableList(items);
	}
	
	/**
	 * @return The mean.
	 */
	public double mean()
	{
		return items.isEmpty() ? 0 : total / items.size();
	}

	//-------------------------------------------------------------------------

	/**
	 * To add an item.
	 * 
	 * @param item The item.
	 */
	public void addItem(final ItemScore item)
	{
		items.add(item);
		total += item.score();
	}
	
	//-------------------------------------------------------------------------

}
