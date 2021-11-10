package metadata.ai.features.trees.logits;

import metadata.ai.AIItem;

/**
 * Describes a node in a logit tree for features. May either be a condition
 * node (internal node), or a node with a linear model (a leaf node).
 * 
 * @author Dennis Soemers
 */
public abstract class LogitNode implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param indent Number of tabs (assuming four spaces) to indent
	 * @return String representation of this node.
	 */
	public abstract String toString(final int indent);
	
	//-------------------------------------------------------------------------

}
