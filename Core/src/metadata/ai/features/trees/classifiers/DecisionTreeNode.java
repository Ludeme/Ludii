package metadata.ai.features.trees.classifiers;

import metadata.ai.AIItem;

/**
 * Describes a node in a decision tree for features. May either be a condition
 * node (internal node), or a node with class predictions (leaf node).
 * 
 * @author Dennis Soemers
 */
public abstract class DecisionTreeNode implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param indent Number of tabs (assuming four spaces) to indent
	 * @return String representation of this node.
	 */
	public abstract String toString(final int indent);
	
	//-------------------------------------------------------------------------

}
