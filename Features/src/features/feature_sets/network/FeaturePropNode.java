package features.feature_sets.network;

import java.util.BitSet;

import features.spatial.instances.AtomicProposition;
import other.state.State;

/**
 * A feature prop node in the FeaturePropSet representation
 *
 * @author Dennis Soemers
 */
public class FeaturePropNode
{
	
	//-------------------------------------------------------------------------
	
	/** Unique index of this node in array */
	protected final int index;
	
	/** Atomic proposition which must be true for this node to be true */
	protected final AtomicProposition proposition;
	
	/** Bitset of feature indices to deactivate if this node is false */
	protected final BitSet dependentFeatures = new BitSet();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param index
	 * @param proposition
	 */
	public FeaturePropNode(final int index, final AtomicProposition proposition)
	{
		this.index = index;
		this.proposition = proposition;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Evaluate the given state.
	 * @param state
	 * @param activeNodes Bitset of nodes that are still active.
	 * @param activeFeatures Bitset of feature indices that are active.
	 */
	public void eval(final State state, final BitSet activeNodes, final BitSet activeFeatures)
	{
		if (activeFeatures.intersects(dependentFeatures))		// if false, might as well not check anything
		{
			if (!proposition.matches(state))		// Requirement not satisfied
				activeFeatures.andNot(dependentFeatures);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Mark a feature ID that we should set to false if our proposition is false
	 * @param featureID
	 */
	public void setDependentFeature(final int featureID)
	{
		dependentFeatures.set(featureID);
	}
	
	/**
	 * @return Our proposition
	 */
	public AtomicProposition proposition()
	{
		return proposition;
	}
	
	//-------------------------------------------------------------------------

}
