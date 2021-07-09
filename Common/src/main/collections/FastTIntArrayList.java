package main.collections;

import gnu.trove.list.array.TIntArrayList;

/**
 * Even more optimised version of TIntArrayList; provides a
 * faster copy constructor.
 *
 * @author Dennis Soemers
 */
public final class FastTIntArrayList extends TIntArrayList
{
	
	//-------------------------------------------------------------------------
	
	/** Shared empty array instance used for empty instances. */
    private static final int[] EMPTY_ELEMENTDATA = {};
    
    //-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public FastTIntArrayList()
	{
		super();
	}
	
	/**
	 * Constructor
	 * @param capacity
	 */
	public FastTIntArrayList(final int capacity)
	{
		super(capacity);
	}
	
	/**
	 * Optimised copy constructor
	 * @param other
	 */
	public FastTIntArrayList(final FastTIntArrayList other)
	{
		this.no_entry_value = -99;
		final int length = other.size();
		
		if (length > 0)
		{
			_data = new int[length];
	        System.arraycopy(other._data, 0, _data, 0, length);
	        _pos = length;
		}
		else
		{
			_data = EMPTY_ELEMENTDATA;
			_pos = 0;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Optimised implementation for adding all elements from another FastTIntArrayList
	 * @param other
	 * @return True if the collection was modified, false otherwise
	 */
	public boolean addAll(final FastTIntArrayList other) 
	{
		final int numToAdd = other.size();
		ensureCapacity(_pos + numToAdd);
        System.arraycopy(other._data, 0, _data, _pos, numToAdd);
        _pos += numToAdd;
        return numToAdd > 0;
    }
    
    //-------------------------------------------------------------------------

}
