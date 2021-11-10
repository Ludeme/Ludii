package metadata.ai.features.trees.logits;

import annotations.Name;

/**
 * Describes a decision node in a logit tree for features; it contains one
 * feature (the condition we check), and two branches; one for the case
 * where the condition is true, and one for the case where the condition is false.
 * 
 * @author Dennis Soemers
 */
public class If extends LogitNode
{
	
	//-------------------------------------------------------------------------
	
	/** String description of the feature we want to evaluate as condition */
	protected final String feature;
	
	/** Node we navigate to when the condition is satisfied */
	protected final LogitNode thenNode;
	
	/** Node we navigate to when condition is not satisfied */
	protected final LogitNode elseNode;
	
	//-------------------------------------------------------------------------

	/**
	 * Defines the feature (condition), and the two branches.
	 * @param feature The feature to evaluate (the condition).
	 * @param then The branch to take if the feature is active.
	 * @param Else The branch to take if the feature is not active.
	 */
	public If
	(
				final String feature,
		@Name	final LogitNode then,
		@Name	final LogitNode Else
	)
	{
		this.feature = feature;
		this.thenNode = then;
		this.elseNode = Else;
	}
	
	//-------------------------------------------------------------------------
	
}
