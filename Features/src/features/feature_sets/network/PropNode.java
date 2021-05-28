package features.feature_sets.network;

import java.util.BitSet;

import features.spatial.instances.AtomicProposition;
import other.state.State;

/**
 * A prop node in the PropFeatureInstanceSet representation.
 *
 * @author Dennis Soemers
 */
public class PropNode
{
	
	//-------------------------------------------------------------------------
	
	/** Unique index of this node in array */
	protected final int index;
	
	/** Atomic proposition which must be true for this node to be true */
	protected final AtomicProposition proposition;
	
	/** Bitset of instances to deactivate if this node is false */
	protected final BitSet dependentInstances = new BitSet();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param index
	 * @param proposition
	 */
	public PropNode(final int index, final AtomicProposition proposition)
	{
		this.index = index;
		this.proposition = proposition;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Evaluate the given state.
	 * @param state
	 * @param activeNodes Bitset of nodes that are still active.
	 * @param activeInstances Bitset of feature instances that are active.
	 */
	public void eval(final State state, final BitSet activeNodes, final BitSet activeInstances)
	{
		if (activeInstances.intersects(dependentInstances))		// if false, might as well not check anything
		{
			if (!proposition.matches(state))		// Requirement not satisfied
				activeInstances.andNot(dependentInstances);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Mark an instance ID that we should set to false if our proposition is false
	 * @param instanceID
	 */
	public void setDependentInstance(final int instanceID)
	{
		dependentInstances.set(instanceID);
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
