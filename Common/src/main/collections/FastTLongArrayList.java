package main.collections;

import gnu.trove.list.array.TLongArrayList;

/**
 * Even more optimised version of TLongArrayList; provides a
 * faster copy constructor.
 *
 * @author Dennis Soemers
 */
public final class FastTLongArrayList extends TLongArrayList
{
	
	//-------------------------------------------------------------------------
	
	/** Shared empty array instance used for empty instances. */
    private static final long[] EMPTY_ELEMENTDATA = {};
    
    //-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public FastTLongArrayList()
	{
		super();
	}
	
	/**
	 * Optimised copy constructor
	 * @param other
	 */
	public FastTLongArrayList(final FastTLongArrayList other)
	{
		this.no_entry_value = -99L;
		final int length = other.size();
		
		if (length > 0)
		{
			_data = new long[length];
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

}
