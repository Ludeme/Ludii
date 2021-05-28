package game.util.math;

import game.functions.ints.IntFunction;
import other.BaseLudeme;

  /**
  * Associates an item with a count.
  * 
  * @author cambolbro
  * 
  * @remarks This ludeme is used for lists of items with counts, such as (placeRandom ...). 
  */
 public class Count extends BaseLudeme
 {
 	final String      item;
 	final IntFunction count;

  	//-------------------------------------------------------------------------

  	/**
	 * @param item  Item description.
	 * @param count Number of items.
	 * 
	 * @example (count "Pawn1" 8)
	 */
 	public Count
 	(
 		final String      item, 
 		final IntFunction count
 	)
 	{
 		this.item  = item;
 		this.count = count;
 	}

  	//-------------------------------------------------------------------------

	/**
	 * @return The item.
	 */
  	public String item()
 	{
 		return item;
 	}

	/**
	 * @return The count.
	 */
  	public IntFunction count()
 	{
 		return count;
 	}

  	//-------------------------------------------------------------------------

}
 