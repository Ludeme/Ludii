package metadata.ai.features.trees.classifiers;

import annotations.Name;

/**
 * Describes a leaf node in a binary classification tree for features; it contains
 * only a predicted probability for "best move".
 * 
 * @author Dennis Soemers
 */
public class Leaf extends DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** Predicted probability of being a bottom-25% move */
	protected final float bottom25Prob;
	
	/** Predicted probability of being a move in the Interquartile Range */
	protected final float iqrProb;
	
	/** Predicted probability of being a top-25% move */
	protected final float top25Prob;
	
	//-------------------------------------------------------------------------

	/**
	 * Defines the feature (condition) and the predicted probabilities for different classes.
	 * @param bottom25 Predicted probability of being a bottom-25% move
	 * @param iqr Predicted probability of being a move in the Interquartile Range
	 * @param top25 Predicted probability of being a top-25% move
	 * 
	 * @example (leaf bottom25:0.0 iqr:0.2 top25:0.8)
	 */
	public Leaf
	(
		@Name final Float bottom25, 
		@Name final Float iqr, 
		@Name final Float top25
	)
	{
		bottom25Prob = bottom25.floatValue();
		iqrProb = iqr.floatValue();
		top25Prob = top25.floatValue();
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
		return "(leaf bottom25:" + bottom25Prob + " iqr:" + iqrProb + " top25:" + top25Prob + ")";
	}
	
	//-------------------------------------------------------------------------
	
}
