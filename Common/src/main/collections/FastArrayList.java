package main.collections;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;

/**
 * Less flexible, but faster alternative to ArrayList
 * 
 * @author Dennis Soemers
 * @param <E> Type of elements to contain in this list
 */
public class FastArrayList<E> implements Iterable<E>, Serializable
{
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;

	/** Our backing array */
	transient protected Object[] data;
	
	/** The size of the ArrayList (the number of elements it contains). */
    private int size;
    
    /** Default initial capacity. */
    private static final int DEFAULT_CAPACITY = 10;
    
    /** Used to check for concurrent modifications */
    protected transient int modCount = 0;
	
	//-------------------------------------------------------------------------
    
    /**
     * Constructor
     */
    public FastArrayList()
    {
    	this(DEFAULT_CAPACITY);
    }
    
    /**
     * Constructor
     * @param initialCapacity
     */
    public FastArrayList(final int initialCapacity) 
    {
    	this.data = new Object[initialCapacity];
    }
    
    /**
     * Constructor
     * @param other
     */
    public FastArrayList(final FastArrayList<E> other) {
    	data = Arrays.copyOf(other.data, other.size);
    	size = data.length;
    }
    
    /**
     * Constructor
     * @param elements
     */
    public FastArrayList(@SuppressWarnings("unchecked") final E... elements)
    {
    	data = Arrays.copyOf(elements, elements.length);
    	size = data.length;
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * Adds object e to list
     * @param e
     */
    public void add(final E e)
    {
    	++modCount;
    	ensureCapacityInternal(size + 1);
        data[size++] = e;
    }
    
    /**
	 * Adds object e to list at a specific index.
	 * 
	 * @param index
	 * @param e
	 */
	public void add(final int index, final E e)
	{
		modCount++;
		final int s;
		if ((s = size) == this.data.length)
			grow(size + 1);
		System.arraycopy(data, index, data, index + 1, s - index);
		data[index] = e;
		size = s + 1;
	}

	/**
	 * Adds all elements from the other given list
	 * 
	 * @param other
	 */
    public void addAll(final FastArrayList<E> other)
    {
    	final Object[] otherData = other.data;
    	++modCount;
    	final int numNew = other.size();
        ensureCapacityInternal(size + numNew);
        System.arraycopy(otherData, 0, data, size, numNew);
        size += numNew;
    }
    
    /**
     * @param index Index at which to remove element
     * @return Remove item at the specified index and return.
     */
    @SuppressWarnings("unchecked")
	public E remove(final int index)
    {
    	final E r = (E)data[index];
    	modCount++;
    	if (index != --size)
    		System.arraycopy(data, index + 1, data, index, size - index);
    	
    	// Aid for garbage collection by releasing this pointer.
    	data[size] = null;
    	return r;
    }
    
    /**
     * Removes element at given index, and returns it.
     * Elements behind it are not shifted to the left, but only
     * the last element is swapped into the given idx.
     * 
     * @param index
     * @return Removed element
     */
    @SuppressWarnings("unchecked")
    public E removeSwap(final int index)
    {
    	final E r = (E)data[index];
    	modCount++;
    	if (index != --size)
    		data[index] = data[size];
    	
    	// Aid for garbage collection by releasing this pointer.
    	data[size] = null;
    	return r;
    }
    
    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index
     * @param element
     */
    public void set(final int index, final E element) 
    {
        data[index] = element;
    }
    
    /**
     * Clears the list
     */
    public void clear() 
    {
    	++modCount;
        for (int i = 0; i < size; ++i)
            data[i] = null;

        size = 0;
    }
    
    /**
     * @param o
     * @return True if o is contained in this list
     */
    public boolean contains(final Object o) 
    {
        return indexOf(o) >= 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) 
    {
        if (o == this) 
        {
            return true;
        }

        if (!(o instanceof FastArrayList)) 
        {
            return false;
        }

        final int expectedModCount = modCount;
        
        try 
        {
        	@SuppressWarnings("unchecked")
    		final FastArrayList<E> other = (FastArrayList<E>) o;
            
            if (size != other.size)
            {
            	return false;
            }

            for (int i = 0; i < size; ++i) 
            {
                if (!Objects.equals(data[i], other.data[i])) 
                {
                    return false;
                }
            }
        }
        finally
        {
        	checkForComodification(expectedModCount);
        }

        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() 
    {
        final int expectedModCount = modCount;
        int hash = 1;
        
        for (int i = 0; i < size; ++i)
        {
        	final Object e = data[i];
        	hash = 31 * hash + (e == null ? 0 : e.hashCode());
        }
        
        checkForComodification(expectedModCount);
        return hash;
    }
    
    /**
     * @param i
     * @return Element at index i. WARNING: does not check index bounds, may return
     * null if i is greater than or equal to size but lower than capacity of backing array.
     */
    @SuppressWarnings("unchecked")
	public E get(final int i)
    {
    	return (E) data[i];
    }
    
    /**
     * @param o
     * @return First index of o in this list, or -1 if it is not in the list
     */
    public int indexOf(final Object o) 
    {
        if (o == null) 
        {
            for (int i = 0; i < size; i++)
                if (data[i] == null)
                    return i;
        } 
        else 
        {
            for (int i = 0; i < size; i++)
                if (o.equals(data[i]))
                    return i;
        }
        return -1;
    }
    
    /**
     * @return Whether this list is currently empty
     */
    public boolean isEmpty()
    {
    	return size == 0;
    }
    
    /**
     * Removes all elements from this list except for those that are also in the other list
     * @param other
     */
	public void retainAll(final FastArrayList<E> other) 
    {
        batchRemove(other, true);
    }
    
    /**
     * @return Current size of list
     */
    public int size()
    {
    	return size;
    }
    
    /**
     * @return Copy of the backing array, limited to size
     */
    public Object[] toArray() {
        return Arrays.copyOf(data, size);
    }
    
    /**
     * @param a
     * @return Copy of the backing array (placed inside the given array if it fits)
     */
    @SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] a) {
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(data, size, a.getClass());
        
        System.arraycopy(data, 0, a, 0, size);
        
        if (a.length > size)
            a[size] = null;
        
        return a;
    }
    
    //-------------------------------------------------------------------------
    
    @Override
    public String toString()
    {
    	final int iMax = size - 1;
        if (iMax == -1)
            return "[]";

        final StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(data[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }
    
    //-------------------------------------------------------------------------

	@Override
	public Iterator<E> iterator()
	{
		return new Itr();
	}
	
	/**
	 * Iterator for FastArrayList
	 * @author Dennis Soemers
	 */
	private class Itr implements Iterator<E>
	{
		
		private int cursor = 0;

		@Override
		public boolean hasNext()
		{
			return cursor != size;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next()
		{
			if (cursor >= data.length)
				throw new ConcurrentModificationException();
			
			return (E) data[cursor++];
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Removes a batch based on what's in the other list
	 * @param c
	 * @param complement If true, we keep elements in the other list. 
	 * If false, we remove elements in the other list
	 */
	private void batchRemove(final FastArrayList<E> other, boolean complement) 
	{
        final Object[] dataN = this.data;
        int r = 0, w = 0;
        try 
        {
            for (; r < size; r++)
                if (other.contains(dataN[r]) == complement)
                    dataN[w++] = dataN[r];
        } 
        finally 
        {
        	modCount += size - w;
            if (r != size) 
            {
                System.arraycopy(dataN, r,
                                 dataN, w,
                                 size - r);
                w += size - r;
            }
            if (w != size) 
            {
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    dataN[i] = null;
                size = w;
            }
        }
    }
	
	/**
	 * Throws ConcurrentModificationException if modCount does not equal
	 * expected modCount
	 * @param expectedModCount
	 */
	private void checkForComodification(final int expectedModCount) 
	{
        if (modCount != expectedModCount) 
        {
            throw new ConcurrentModificationException();
        }
    }
	
	/**
	 * Ensures we have at least the given amount of capacity
	 * @param minCapacity
	 */
	private void ensureCapacityInternal(final int minCapacity) 
	{
		if (minCapacity - data.length > 0)
            grow(minCapacity);
    }
	
	/**
	 * Grows the backing array
	 * @param minCapacity
	 */
	private void grow(final int minCapacity) 
	{
        final int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        
        data = Arrays.copyOf(data, newCapacity);
    }
	
	//-------------------------------------------------------------------------
	
	/**
	 * Serializes list
	 * @param s
	 * @throws java.io.IOException
	 */
    private void writeObject(final ObjectOutputStream s) throws java.io.IOException
    {
    	final int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i = 0; i < size; ++i) 
        {
            s.writeObject(data[i]);
        }
        
        if (modCount != expectedModCount) 
        {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Deserializes list
     * @param s
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(final ObjectInputStream s) throws java.io.IOException, ClassNotFoundException 
    {
        data = new Object[10];

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) 
        {
            // be like clone(), allocate array based upon size not capacity
            ensureCapacityInternal(size);

            final Object[] a = data;
            // Read in all elements in the proper order.
            for (int i = 0; i < size; i++) 
            {
                a[i] = s.readObject();
            }
        }
    }
    
    //-------------------------------------------------------------------------

}
