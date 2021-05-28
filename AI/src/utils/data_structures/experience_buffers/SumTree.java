package utils.data_structures.experience_buffers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import main.collections.FVector;
import main.math.BitTwiddling;

//-----------------------------------------------------------------------------

/**
 * Sum tree data structure for Prioritized Experience Replay.
 * 
 * Implementation based on that from Dopamine (but translated to Java):
 * https://github.com/google/dopamine/blob/master/dopamine/replay_memory/sum_tree.py
 * 
 * 
 * 
 * 
 *   A sum tree is a complete binary tree whose leaves contain values called
 *	 priorities. Internal nodes maintain the sum of the priorities of all leaf
 *	 nodes in their subtree.
 *	 For capacity = 4, the tree may look like this:
 *
 *	              +---+
 *	              |2.5|
 *	              +-+-+
 *	                |
 *	        +-------+--------+
 *	        |                |
 *	      +-+-+            +-+-+
 *	      |1.5|            |1.0|
 *	      +-+-+            +-+-+
 *	        |                |
 *	   +----+----+      +----+----+
 *	   |         |      |         |
 *	 +-+-+     +-+-+  +-+-+     +-+-+
 *	 |0.5|     |1.0|  |0.5|     |0.5|
 *	 +---+     +---+  +---+     +---+
 *
 *	 This is stored in a list of FVectors:
 *	 self.nodes = [ [2.5], [1.5, 1], [0.5, 1, 0.5, 0.5] ]
 *	 For conciseness, we allocate arrays as powers of two, and pad the excess
 *	 elements with zero values.
 *	 This is similar to the usual array-based representation of a complete binary
 *	 tree, but is a little more user-friendly.
 * 
 * 
 * @author Dennis Soemers
 */
public class SumTree implements Serializable
{
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** Our list of nodes (stored in FVectors) */
	protected final List<FVector> nodes;
	
	/** Max recorded priority throughout the tree */
	protected float maxRecordedPriority;
	
	//-------------------------------------------------------------------------
	

	/**
	 * Constructor
	 * @param capacity
	 */
	public SumTree(final int capacity)
	{
		assert (capacity > 0);
		
		nodes = new ArrayList<FVector>();
		final int treeDepth = BitTwiddling.log2RoundUp(capacity);
		int levelSize = 1;
		
		for (int i = 0; i < (treeDepth + 1); ++i)
		{
			final FVector nodesAtThisDepth = new FVector(levelSize);
			nodes.add(nodesAtThisDepth);
			
			levelSize *= 2;
		}
		
		assert (nodes.get(nodes.size() - 1).dim() == BitTwiddling.nextPowerOf2(capacity));
		
		maxRecordedPriority = 1.f;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sample from the sum tree.
	 * 
	 * Each element has a probability p_i / sum_j p_j of being picked, where p_i
	 * is the (positive) value associated with node i (possibly unnormalised).
	 * 
	 * @return Sampled index
	 */
	public int sample()
	{
		return sample(ThreadLocalRandom.current().nextDouble());
	}
	
	/**
	 * Sample from the sum tree
	 * 
	 * Each element has a probability p_i / sum_j p_j of being picked, where p_i
	 * is the (positive) value associated with node i (possibly unnormalised).
	 * 
	 * @param inQueryValue A value in [0, 1], used for sampling
	 * @return Sampled index
	 */
	public int sample(final double inQueryValue)
	{
		assert (totalPriority() != 0.f);
		assert (inQueryValue >= 0.0);
		assert (inQueryValue <= 1.0);
		
		double queryValue = inQueryValue * totalPriority();
		
		// Traverse the sum tree
		int nodeIdx = 0;
		for (int i = 1; i < nodes.size(); ++i)
		{
			final FVector nodesAtThisDepth = nodes.get(i);
			
			// Compute children of previous depth's node.
			final int leftChild = nodeIdx * 2;
			final float leftSum = nodesAtThisDepth.get(leftChild);
			
			// Each subtree describes a range [0, a), where a is its value.
			if (queryValue < leftSum)	// Recurse into left subtree.
			{
				nodeIdx = leftChild;
			}
			else						// Recurse into right subtree.
			{
				nodeIdx = leftChild + 1;
				// Adjust query to be relative to right subtree.
				queryValue -= leftSum;
			}
		}
		
		return nodeIdx;
	}
	
	/**
	 * Sample a stratified batch of given size. 
	 * 
	 * Let R be the value at the root (total value of sum tree). This method
	 * will divide [0, R) into batchSize segments, pick a random number from
	 * each of those segments, and use that random number to sample from the
	 * SumTree. This is as specified in Schaul et al. (2015).
	 * 
	 * @param batchSize
	 * @return Array of size batchSize, sampled from the sum tree.
	 */
	public int[] stratifiedSample(final int batchSize)
	{
		assert (totalPriority() != 0.0);
		
		final FVector bounds = FVector.linspace(0.f, 1.f, batchSize + 1, true);
		assert (bounds.dim() == batchSize + 1);
		
		final int[] result = new int[batchSize];
		for (int i = 0; i < batchSize; ++i)
		{
			final float segmentStart = bounds.get(i);
			final float segmentEnd = bounds.get(i + 1);
			final double queryVal = ThreadLocalRandom.current().nextDouble(segmentStart, segmentEnd);
			result[i] = sample(queryVal);
		}
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param nodeIdx
	 * @return Value of the leaf node corresponding to given node index
	 */
	public float get(final int nodeIdx)
	{
		return nodes.get(nodes.size() - 1).get(nodeIdx);
	}
	
	/**
	 * Sets the value of a given leaf node, and updates internal nodes accordingly.
	 * 
	 * This operation takes O(log(capacity)).
	 * 
	 * @param inNodeIdx Index of leaf node to be updated
	 * @param value Nonnegative value to be assigned to node. A value
	 * of 0 causes a node to never be sampled.
	 */
	public void set(final int inNodeIdx, final float value)
	{
		assert (value >= 0.f);
		
		int nodeIdx = inNodeIdx;
		maxRecordedPriority = Math.max(maxRecordedPriority, value);
		final float deltaValue = value - get(nodeIdx);
		
		// Now traverse back the tree, adjusting all sums along the way.
		for (int i = nodes.size() - 1; i >= 0; --i)
		{
			final FVector nodesAtThisDepth = nodes.get(i);
			nodesAtThisDepth.addToEntry(nodeIdx, deltaValue);
			nodeIdx /= 2;
		}
		
		assert (nodeIdx == 0);
	}
	
	/**
	 * @return Our max recorded priority
	 */
	public float maxRecordedPriority()
	{
		return maxRecordedPriority;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Total priority summed up over the entire tree
	 */
	public float totalPriority()
	{
		return nodes.get(0).get(0);
	}
	
	//-------------------------------------------------------------------------
	
}
