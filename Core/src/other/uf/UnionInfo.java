package other.uf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;


/**
 * Contains all the info/storages for the Union-find.
 *
 * @author tahmina 
 */
public class UnionInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
						
	/**  Returns the parent of the site in the union tree. */
	protected final int[] parent;
	
	protected final BitSet[] itemsList;	
	protected final int totalsize;
	
	/**
	 * Constructor
	 * @param totalElements  	The size of the game board.
	 */
	public UnionInfo(final int totalElements)
	{				
		totalsize = totalElements;
		//System.out.println("Uf :"+totalsize);
		parent = new int[totalElements];
		itemsList = new BitSet[totalElements];
			
		for (int i = 0; i < totalElements; i++)
		{
			parent[i] = i;
			itemsList[i] = null;	
		}
	}
	
	
	/**
	 * Copy constructor
	 * @param other  The Object of UnionInfo.
	 */
	public UnionInfo(final UnionInfo other)
	{
		totalsize = other.totalsize;
		parent = Arrays.copyOf(other.parent, other.parent.length);	

		itemsList = new BitSet[other.itemsList.length];

		for (int i = 0; i < other.itemsList.length; ++i)
		{
			if (other.itemsList[i] != null)
			{
				itemsList[i] = (BitSet) other.itemsList[i].clone();
			}
		}
	}
	
	/**
	 * Set the parents.
	 * 
	 * @param childIndex  The index of the child.
	 * @param parentIndex The index of the parent.
	 */
	public void setParent(final int childIndex, final int parentIndex)
	{
		parent[childIndex] = parentIndex;		
	}
	
	/**
	 * @param childIndex The index of the child.
	 * @return The index of the parent.
	 */
	public int getParent(final int childIndex)
	{
		return parent[childIndex];
	}
	
	/**
	 * @param parentIndex The index of the parent.
	 * @return A bitset.
	 */
	public BitSet getItemsList(final int parentIndex)
	{
		return itemsList[parentIndex];
	}
	
	/**
	 * Set an item.
	 * 
	 * @param parentIndex The index of the parent.
	 * @param childIndex  The index of the child.
	 */
	public void setItem(final int parentIndex, final int childIndex)
	{
		itemsList[parentIndex] = new BitSet(totalsize);	
		itemsList[parentIndex].set(childIndex);		
	}
	
	/**
	 * Merge two item lists.
	 * 
	 * @param parentIndex1 The index of the parent 1.
	 * @param parentIndex2 The index of the parent 2.
	 */
	public void mergeItemsLists(final int parentIndex1, final int parentIndex2)
	{
		itemsList[parentIndex1].or(itemsList[parentIndex2]);
		itemsList[parentIndex2].clear();
	}
	
	/**
	 * @param parentIndex The index of the parent.
	 * @param childIndex  The index of the child.
	 * @return True if they are in the same group.
	 */
	public boolean isSameGroup(final int parentIndex, final int childIndex)
	{
		if (itemsList[parentIndex] == null)
			return false;
		
		return itemsList[parentIndex].get(childIndex);
	}
	
	/**
	 * @param parentIndex The parent index.
	 * @return The size of the group.
	 */
	public int getGroupSize(final int parentIndex)
	{
		if (itemsList[parentIndex] == null)
			return 0;
		
		return itemsList[parentIndex].cardinality();
	}
}
