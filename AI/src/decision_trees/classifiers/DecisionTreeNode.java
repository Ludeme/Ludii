package decision_trees.classifiers;

/**
 * Abstract class for a node in a feature-based decision tree
 * that should output class probabilities.
 * 
 * @author Dennis Soemers
 */
public abstract class DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Convert to tree in metadata format.
	 * @return Decision tree node.
	 */
	public abstract metadata.ai.features.trees.classifiers.DecisionTreeNode toMetadataNode();
	
	//-------------------------------------------------------------------------

}
