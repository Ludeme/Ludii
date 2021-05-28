package other.uf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import main.Constants;

/**
 * Contains all the info/storages for the Union-find-delete.
 *
 * @author tahmina
 */
public class UnionInfoD implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Returns the parent of the site in the union tree. */
	protected final int[] parent;

	/**
	 * Indexed by parent of a group. Gives a BitSet of all sites that are part of
	 * that group
	 */
	protected final BitSet[] itemsList;

	/**
	 * Indexed by parent of a group. Gives a BitSet of all sites that are part of
	 * that group, plus the list of all orthogonal neighbours around the group.
	 */
	protected final BitSet[] itemWithOrthoNeighbors;

	protected final int totalsize;

	/**
	 * Constructor.
	 * 
	 * @param totalElements   The size of the game board.
	 * @param numberOfPlayers The total number of players.
	 * @param blocking        True if this data is for blocking tests.
	 */
	public UnionInfoD(final int totalElements, final int numberOfPlayers, final boolean blocking)
	{
		totalsize = totalElements;
		// System.out.println(" UFD totalSize : "+ totalsize);

		parent = new int[totalElements];
		itemsList = new BitSet[totalElements];
		itemWithOrthoNeighbors = new BitSet[totalElements];

		if (blocking)
		{
			itemsList[0] = new BitSet(totalsize);
			itemsList[0].set(0, totalElements);
		}
		else
		{
			for (int i = 0; i < totalElements; i++)
			{
				parent[i] = Constants.UNUSED;
			}
		}
	}

	/**
	 * Copy constructor
	 * 
	 * @param other The Object of UnionInfo.
	 */
	public UnionInfoD(final UnionInfoD other)
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

		if (other.itemWithOrthoNeighbors != null)
		{
			itemWithOrthoNeighbors = new BitSet[other.itemWithOrthoNeighbors.length];

			for (int i = 0; i < other.itemWithOrthoNeighbors.length; ++i)
			{
				if (other.itemWithOrthoNeighbors[i] != null)
				{
					itemWithOrthoNeighbors[i] = (BitSet) other.itemWithOrthoNeighbors[i].clone();
				}
			}
		}
		else
		{
			itemWithOrthoNeighbors = null;
		}
	}

	/**
	 * Set the parent of the given child index to the given parent index.
	 * 
	 * @param childIndex  The index of the child.
	 * @param parentIndex The index of the parent.
	 */
	public void setParent(final int childIndex, final int parentIndex)
	{
		parent[childIndex] = parentIndex;
	}

	/**
	 * Clear the parents.
	 * 
	 * @param childIndex The index of the child.
	 */
	public void clearParent(final int childIndex)
	{
		parent[childIndex] = Constants.UNUSED;
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
	 * @param parentIndex The index of the parent of a group.
	 * @return BitSet of all sites that are part of the group.
	 */
	public BitSet getItemsList(final int parentIndex)
	{
		if (itemsList[parentIndex] == null)
		{
			itemsList[parentIndex] = new BitSet(totalsize);
		}

		return itemsList[parentIndex];
	}

	/**
	 * Clear the items in the list.
	 * 
	 * @param parentIndex The index of the parent.
	 */
	public void clearItemsList(final int parentIndex)
	{
		itemsList[parentIndex] = null;
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
	 * Tell items list of given parentIndex that its group contains given childIndex
	 * 
	 * @param parentIndex The index of the parent.
	 * @param childIndex  The index of the child.
	 */
	public void setItem(final int parentIndex, final int childIndex)
	{
		if (itemsList[parentIndex] == null)
		{
			itemsList[parentIndex] = new BitSet(totalsize);
		}

		itemsList[parentIndex].set(childIndex);
	}

	/**
	 * Merge two lists.
	 * 
	 * @param parentIndex1 The index of the parent 1.
	 * @param parentIndex2 The index of the parent 2.
	 */
	public void mergeItemsLists(final int parentIndex1, final int parentIndex2)
	{
		getItemsList(parentIndex1).or(getItemsList(parentIndex2));
		itemsList[parentIndex2] = null;
	}

	/**
	 * @param parentIndex The index of the parent.
	 * @return The size of the group from an index.
	 */
	public int getGroupSize(final int parentIndex)
	{
		if (itemsList[parentIndex] == null)
			return 0;

		return itemsList[parentIndex].cardinality();
	}

	// ----------------------------------- For Liberty Calculations-----------------

	/**
	 * @param parentIndex The parent index.
	 * @return The items which are orthogonally neighbours.
	 */
	public BitSet getAllItemWithOrthoNeighbors(final int parentIndex)
	{
		if (itemWithOrthoNeighbors[parentIndex] == null)
		{
			itemWithOrthoNeighbors[parentIndex] = new BitSet(totalsize);
		}

		return itemWithOrthoNeighbors[parentIndex];
	}

	/**
	 * Clear the list of orthogonal neighbours.
	 * 
	 * @param parentIndex The parent index.
	 */
	public void clearAllitemWithOrthoNeighbors(final int parentIndex)
	{
		itemWithOrthoNeighbors[parentIndex] = null;
	}

	/**
	 * Tell the itemWithOrthoNeighbors group of given parent index that it includes
	 * the given child index
	 * 
	 * @param parentIndex The index of the parent.
	 * @param childIndex  The index of the child.
	 */
	public void setItemWithOrthoNeighbors(final int parentIndex, final int childIndex)
	{
		if (itemWithOrthoNeighbors[parentIndex] == null)
		{
			itemWithOrthoNeighbors[parentIndex] = new BitSet(totalsize);
		}

		itemWithOrthoNeighbors[parentIndex].set(childIndex);
	}

	/**
	 * Merge two list of orthogonal neighbours.
	 * 
	 * @param parentIndex1 The parent 1.
	 * @param parentIndex2 The parent 2.
	 */
	public void mergeItemWithOrthoNeighbors(final int parentIndex1, final int parentIndex2)
	{
		getAllItemWithOrthoNeighbors(parentIndex1).or(getAllItemWithOrthoNeighbors(parentIndex2));
		itemWithOrthoNeighbors[parentIndex2] = null;
	}

}
