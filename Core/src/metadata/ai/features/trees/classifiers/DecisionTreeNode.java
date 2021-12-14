package metadata.ai.features.trees.classifiers;

import java.util.Set;

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
	 * Collect strings for all features under this node.
	 * @param outFeatureStrings Set to put all the feature strings in.
	 */
	public abstract void collectFeatureStrings(final Set<String> outFeatureStrings);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param indent Number of tabs (assuming four spaces) to indent
	 * @return String representation of this node.
	 */
	public abstract String toString(final int indent);
	
	//-------------------------------------------------------------------------

}
