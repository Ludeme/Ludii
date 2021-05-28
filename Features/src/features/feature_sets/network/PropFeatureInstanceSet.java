package features.feature_sets.network;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import features.spatial.instances.FeatureInstance;
import other.state.State;

/**
 * A set of propositions and feature instances.
 *
 * @author Dennis Soemers
 */
public class PropFeatureInstanceSet
{
	
	//-------------------------------------------------------------------------
	
	/** Array of feature instances */
	protected final FeatureInstance[] featureInstances;
	
	/** Array of PropNodes */
	protected final PropNode[] propNodes;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param featureInstances
	 * @param propNodes
	 */
	public PropFeatureInstanceSet(final FeatureInstance[] featureInstances, final PropNode[] propNodes)
	{
		this.featureInstances = featureInstances;
		this.propNodes = propNodes;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param state
	 * @return List of active instances for given state
	 */
	public List<FeatureInstance> getActiveInstances(final State state)
	{
		final List<FeatureInstance> active = new ArrayList<FeatureInstance>();
		
		final BitSet activeNodes = new BitSet(propNodes.length);
		activeNodes.set(0, propNodes.length);
		
		final BitSet activeInstances = new BitSet(featureInstances.length);
		activeInstances.set(0, featureInstances.length);
		
		for (int i = activeNodes.nextSetBit(0); i >= 0; i = activeNodes.nextSetBit(i + 1))
		{
			propNodes[i].eval(state, activeNodes, activeInstances);
		}
		
		for (int i = activeInstances.nextSetBit(0); i >= 0; i = activeInstances.nextSetBit(i + 1))
		{
			active.add(featureInstances[i]);
		}
		
		return active;
	}
	
	//-------------------------------------------------------------------------

}
