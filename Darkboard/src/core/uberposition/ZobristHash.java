package core.uberposition;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ZobristHash<T extends Object> implements Iterable<T> {
	
	public class ZobristIterator<T3 extends Object> implements Iterator<T3>
	{
		int bucketNumber = 0;
		int elementNumber = 0;
		ZobristHash<T3> hash;
		
		public ZobristIterator(ZobristHash<T3> h)
		{
			hash = h;
			bucketNumber = hash.entryNumber;
			for (int k=0; k<hash.entryNumber; k++)
			{
				if (hash.entries[k].size>0) { bucketNumber=k; break; }
			}
			elementNumber = 0;
		}
		
		public boolean hasNext() 
		{
			if (bucketNumber>=hash.entryNumber) return false;
			if (hash.entries[bucketNumber].size>elementNumber) return true;
			for (int k=bucketNumber+1; k<entryNumber; k++)
			{
				if (hash.entries[k].size>0) return true;
			}
			return false;
		}

		public T3 next() 
		{
			if (bucketNumber>=hash.entryNumber) return null;
			if (hash.entries[bucketNumber].size>elementNumber) return hash.entries[bucketNumber].data[elementNumber++];
			for (int k=bucketNumber+1; k<entryNumber; k++)
			{
				if (hash.entries[k].size>0)
				{
					bucketNumber = k; elementNumber = 0; return hash.entries[bucketNumber].data[elementNumber++];
				}
			}
			bucketNumber = hash.entryNumber;
			return null;
		}

		public void remove() 
		{
			
		}
		
	}
	
	public class ZobristHashList<T2 extends Object> {
		
		ReentrantReadWriteLock lock;
		public int size;
		public long zobrist[];
		public T2 data[];
		
		public ZobristHashList()
		{
			zobrist = new long[16];
			data = (T2[])new Object[16];
			size = 0;
			lock = new ReentrantReadWriteLock();
		}
		
		public T2 get(long z)
		{
			lock.readLock().lock();
			try
			{
				for (int k=0; k<size; k++)
				{
					if (zobrist[k]==z) return data[k];
				}
				return null;
			} finally
			{
				lock.readLock().unlock();
			}
		}
		
		public void put(long z, T2 d)
		{
			lock.writeLock().lock();
			try
			{
				if (size==data.length)
				{
					Object d2[] = new Object[size*2];
					System.arraycopy(data, 0, d2, 0, size);
					long z2[] = new long[size*2];
					System.arraycopy(zobrist, 0, z2, 0, size);
					data = (T2[])d2;
					zobrist = z2;
				}
				zobrist[size] = z;
				data[size] = d;
				size++;
			} finally
			{
				lock.writeLock().unlock();
			}
		}
		
		public void checkAndPut(long z, T2 d)
		{
			lock.writeLock().lock();
			try
			{
				for (int k=0; k<size; k++)
					if (zobrist[k]==z)
					{
						data[k] = d;
						return;
					}
				if (size==data.length)
				{
					Object d2[] = new Object[size*2];
					System.arraycopy(data, 0, d2, 0, size);
					long z2[] = new long[size*2];
					System.arraycopy(zobrist, 0, z2, 0, size);
					data = (T2[])d2;
					zobrist = z2;
				}
				zobrist[size] = z;
				data[size] = d;
				size++;
			} finally
			{
				lock.writeLock().unlock();
			}
		}
		
		public void clear()
		{
			//if (data.length==16) size = 0;
			//else
			//{
				size = 0;
				zobrist = new long[16];
				data = (T2[])new Object[16];
			//}
		}
		
		
	}
	
	int entryBits;
	int entryNumber;
	int mask;
	ZobristHashList<T> entries[];
	
	public ZobristHash(int bits)
	{
		entryBits = bits;
		entryNumber = 1<<bits;
		entries = new ZobristHashList[entryNumber];
		mask = (0xFFFFFFFF >>> (32-bits));
		for (int k=0; k<entryNumber; k++) entries[k] = new ZobristHashList<T>();
	}
	
	public T get(long z)
	{
		return entries[(int)(z&mask)].get(z);
	}
	
	/**
	 * Use when certain that the entry is not already in the table.
	 * @param z
	 * @param d
	 */
	public void put(long z, T d)
	{
		entries[(int)(z&mask)].put(z, d);
	}
	
	/**
	 * Use when entry might already be in the table.
	 * @param z
	 * @param d
	 */
	public void checkAndPut(long z, T d)
	{
		entries[(int)(z&mask)].checkAndPut(z, d);
	}
	
	public void clear()
	{
		for (int k=0; k<entryNumber; k++) entries[k].clear();
	}

	public Iterator<T> iterator() 
	{
		return new ZobristIterator<T>(this);
	}

}
