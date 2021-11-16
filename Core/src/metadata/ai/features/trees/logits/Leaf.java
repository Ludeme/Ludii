package metadata.ai.features.trees.logits;

import main.StringRoutines;
import metadata.ai.misc.Pair;

/**
 * Describes a leaf node in a logit tree for features; it contains an array
 * of features and weights, describing a linear function to use to compute the
 * logit in this node. An intercept feature should collect all the weights
 * inferred from features evaluated in decision nodes leading up to this leaf.
 * 
 * @author Dennis Soemers
 */
public class Leaf extends LogitNode
{
	
	//-------------------------------------------------------------------------
	
	/** Remaining features to evaluate in our model */
	protected final String[] featureStrings;
	
	/** Array of weights for our remaining features */
	protected final float[] weights;
	
	//-------------------------------------------------------------------------

	/**
	 * Defines the feature (condition), and the two branches.
	 * @param features List of remaining features to evaluate and their weights.
	 * 
	 * @example (leaf { (pair "Intercept" 1.0) })
	 */
	public Leaf(final Pair[] features)
	{
		featureStrings = new String[features.length];
		weights = new float[features.length];
		
		for (int i = 0; i < features.length; ++i)
		{
			featureStrings[i] = features[i].key();
			weights[i] = features[i].floatVal();
		}
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
		
		sb.append("(leaf {\n");
		for (int i = 0; i < featureStrings.length; ++i)
		{
			sb.append(innerIndentStr + "(pair " + StringRoutines.quote(featureStrings[i]) + " " + weights[i] + ")\n");
		}
		sb.append(outerIndentStr + "})");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
