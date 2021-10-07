package utils.data_structures.support.zhang_shasha;

import java.util.ArrayList;

/**
 * Code originally from: https://github.com/ijkilchenko/ZhangShasha
 * 
 * Afterwards modified for style / various improvements
 *
 * @author Dennis Soemers
 */
public class Node 
{
	
	/** Label of this node */
	public String label;
	
	/** Index of this node for pre-order traversal of tree */
	public int index;
	
	// note: trees need not be binary
	
	/** List of children */
	public ArrayList<Node> children = new ArrayList<Node>();
	
	/** Leftmost node in subtree rooted in this node (or this node if it's a leaf) */
	public Node leftmost; 		// Used by the recursive O(n) leftmost() function

	/**
	 * Constructor
	 */
	public Node() 
	{
		// Do nothing
	}

	/**
	 * Constructor
	 * @param label
	 */
	public Node(final String label) 
	{
		this.label = label;
	}
}
