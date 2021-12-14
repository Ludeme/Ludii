package metadata.ai.features.trees.classifiers;

import java.util.Set;

import annotations.Name;
import main.StringRoutines;

/**
 * Describes a decision node in a decision tree for features; it contains one
 * feature (the condition we check), and two branches; one for the case
 * where the condition is true, and one for the case where the condition is false.
 * 
 * @author Dennis Soemers
 */
public class If extends DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** String description of the feature we want to evaluate as condition */
	protected final String feature;
	
	/** Node we navigate to when the condition is satisfied */
	protected final DecisionTreeNode thenNode;
	
	/** Node we navigate to when condition is not satisfied */
	protected final DecisionTreeNode elseNode;
	
	//-------------------------------------------------------------------------

	/**
	 * Defines the feature (condition), and the two branches.
	 * @param feature The feature to evaluate (the condition).
	 * @param then The branch to take if the feature is active.
	 * @param Else The branch to take if the feature is not active.
	 * 
	 * @example (if "rel:to=<{}>:pat=<els=[f{0}]>" then:(leaf bottom25:0.0 iqr:0.2 top25:0.8) else:(leaf bottom25:0.6 iqr:0.35 top25:0.05))
	 */
	public If
	(
				final String feature,
		@Name	final DecisionTreeNode then,
		@Name	final DecisionTreeNode Else
	)
	{
		this.feature = feature;
		this.thenNode = then;
		this.elseNode = Else;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void collectFeatureStrings(final Set<String> outFeatureStrings)
	{
		outFeatureStrings.add(feature);
		thenNode.collectFeatureStrings(outFeatureStrings);
		elseNode.collectFeatureStrings(outFeatureStrings);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return String of our feature
	 */
	public String featureString()
	{
		return feature;
	}
	
	/**
	 * @return Node we traverse to if condition holds
	 */
	public DecisionTreeNode thenNode()
	{
		return thenNode;
	}
	
	/**
	 * @return Node we traverse to if condition does not hold
	 */
	public DecisionTreeNode elseNode()
	{
		return elseNode;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
	@Override
	public String toString(final int indent)
	{
		final StringBuilder sb = new StringBuilder();
		final String outerIndentStr = StringRoutines.indent(4, indent);
		final String innerIndentStr = StringRoutines.indent(4, indent + 1);
		
		sb.append("(if " + StringRoutines.quote(feature) + "\n");
		sb.append(innerIndentStr + "then:" + thenNode.toString(indent + 1) + "\n");
		sb.append(innerIndentStr + "else:" + elseNode.toString(indent + 1) + "\n");
		sb.append(outerIndentStr + ")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
