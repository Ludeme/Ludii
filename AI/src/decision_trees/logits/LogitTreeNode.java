package decision_trees.logits;

import metadata.ai.features.trees.logits.LogitNode;

/**
 * Abstract class for a node in a feature-based regression tree
 * that should output logits.
 * 
 * @author Dennis Soemers
 */
public abstract class LogitTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Convert to tree in metadata format.
	 * @return logit node.
	 */
	public abstract LogitNode toMetadataNode();
	
	//-------------------------------------------------------------------------

}
