package features.feature_sets.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import features.spatial.cache.footprints.BaseFootprint;
import features.spatial.cache.footprints.FullFootprint;
import features.spatial.instances.AtomicProposition;
import features.spatial.instances.FeatureInstance;
import main.collections.ChunkSet;
import main.collections.FastTIntArrayList;
import other.state.State;
import other.state.container.ContainerState;

/**
 * A set of propositions which can (dis)prove feature instances, which
 * in turn can prove features, with propositions and instances implicitly
 * arranged in a network (but using flat arrays for improved cache locality)
 *
 * @author Dennis Soemers
 */
public class SPatterNet
{
	
	//-------------------------------------------------------------------------
	
	/** Array of feature instances (appropriately sorted) */
	protected final FeatureInstance[] featureInstances;
	
	/** Array of propositions to test */
	protected final AtomicProposition[] propositions;
	
	/** For every proposition, a bitset of feature instances that depend on that proposition */
	protected final BitSet[] instancesPerProp;
	
	/** For every feature, a bitset containing the instances for that feature */
	protected final BitSet[] instancesPerFeature;
	
	/** For every feature instance, an array of the propositions required for that feature instance */
	protected final int[][] propsPerInstance;
	
	/** Array of feature indices that are always active */
	protected final int[] autoActiveFeatures;
	
	/** For every proposition, if it's true, an array of other propositions that are then proven */
	protected final int[][] provesPropsIfTruePerProp;
	
	/** For every proposition, if it's true, a bitset of instances that are then deactivated (either disproven, or features already proven) */
	protected final BitSet[] deactivateInstancesIfTrue;
	
	/** For every proposition, if it's false, an array of other propositions that are then proven */
	protected final int[][] provesPropsIfFalsePerProp;
	
	/** For every proposition, if it's false, a bitset of instances that are then deactivated (either disproven, or features already proven) */
	protected final BitSet[] deactivateInstancesIfFalse;
	
	/** Bitset with a 1 entry for every single proposition */
	protected final boolean[] ALL_PROPS_ACTIVE;
	
	/** Bitset with a 1 entry for every single instance, except those for features that are always active */
	protected final BitSet INIT_INSTANCES_ACTIVE;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param featureInstances
	 * @param propositions
	 * @param dependentFeatureInstances
	 * @param instancesPerFeature
	 * @param propsPerInstance
	 * @param autoActiveFeatures
	 * @param thresholdedFeatures
	 * @param provesPropsIfTruePerProp
	 * @param disprovesPropsIfTruePerProp
	 * @param provesPropsIfFalsePerProp
	 * @param disprovesPropsIfFalsePerProp
	 */
	public SPatterNet
	(
		final FeatureInstance[] featureInstances,
		final AtomicProposition[] propositions, 
		final BitSet[] dependentFeatureInstances,
		final BitSet[] instancesPerFeature,
		final BitSet[] propsPerInstance,
		final int[] autoActiveFeatures,
		final BitSet thresholdedFeatures,
		final BitSet[] provesPropsIfTruePerProp,
		final BitSet[] disprovesPropsIfTruePerProp,
		final BitSet[] provesPropsIfFalsePerProp,
		final BitSet[] disprovesPropsIfFalsePerProp
	)
	{
//		System.out.println();
//		for (int i = 0; i < propositions.length; ++i)
//		{
//			System.out.println("Prop " + i + " = " + propositions[i]);
//		}
//		System.out.println();
		
		this.featureInstances = featureInstances;
		
		if (featureInstances.length == 0)
			this.instancesPerFeature = new BitSet[0];	// Waste less memory in this case
		else
			this.instancesPerFeature = instancesPerFeature;
		
		this.propositions = propositions;
		this.instancesPerProp = dependentFeatureInstances;
		this.autoActiveFeatures = autoActiveFeatures;
		
		this.provesPropsIfTruePerProp = new int[provesPropsIfTruePerProp.length][];
		for (int i = 0; i < provesPropsIfTruePerProp.length; ++i)
		{
			this.provesPropsIfTruePerProp[i] = new int[provesPropsIfTruePerProp[i].cardinality()];
			int k = 0;
			for (int j = provesPropsIfTruePerProp[i].nextSetBit(0); j >= 0; j = provesPropsIfTruePerProp[i].nextSetBit(j + 1))
			{
				this.provesPropsIfTruePerProp[i][k++] = j;
			}
		}
		
		this.provesPropsIfFalsePerProp = new int[provesPropsIfFalsePerProp.length][];
		for (int i = 0; i < provesPropsIfFalsePerProp.length; ++i)
		{
			this.provesPropsIfFalsePerProp[i] = new int[provesPropsIfFalsePerProp[i].cardinality()];
			int k = 0;
			for (int j = provesPropsIfFalsePerProp[i].nextSetBit(0); j >= 0; j = provesPropsIfFalsePerProp[i].nextSetBit(j + 1))
			{
				this.provesPropsIfFalsePerProp[i][k++] = j;
			}
		}
		
		this.deactivateInstancesIfTrue = new BitSet[disprovesPropsIfTruePerProp.length];
		for (int i = 0; i < deactivateInstancesIfTrue.length; ++i)
		{
			deactivateInstancesIfTrue[i] = new BitSet();
			for (int j = disprovesPropsIfTruePerProp[i].nextSetBit(0); j >= 0; j = disprovesPropsIfTruePerProp[i].nextSetBit(j + 1))
			{
				deactivateInstancesIfTrue[i].or(instancesPerProp[j]);
			}
		}
		
		this.deactivateInstancesIfFalse = new BitSet[disprovesPropsIfFalsePerProp.length];
		for (int i = 0; i < deactivateInstancesIfFalse.length; ++i)
		{
			deactivateInstancesIfFalse[i] = new BitSet();
			for (int j = disprovesPropsIfFalsePerProp[i].nextSetBit(0); j >= 0; j = disprovesPropsIfFalsePerProp[i].nextSetBit(j + 1))
			{
				deactivateInstancesIfFalse[i].or(instancesPerProp[j]);
			}
			
			// Also incorporate any instances that require the proposition itself as disprove-if-false instances
			deactivateInstancesIfFalse[i].or(instancesPerProp[i]);
		}
		
		ALL_PROPS_ACTIVE = new boolean[propositions.length];
		Arrays.fill(ALL_PROPS_ACTIVE, true);
		
		INIT_INSTANCES_ACTIVE = new BitSet(featureInstances.length);
		INIT_INSTANCES_ACTIVE.set(0, featureInstances.length);
		
		// TODO following two little loops should be unnecessary, all those instances should already be gone
		for (final int feature : autoActiveFeatures)
		{
			assert (instancesPerFeature[feature].isEmpty());
			INIT_INSTANCES_ACTIVE.andNot(instancesPerFeature[feature]);
		}
		for (int i = thresholdedFeatures.nextSetBit(0); i >= 0; i = thresholdedFeatures.nextSetBit(i + 1))
		{
			assert (instancesPerFeature[i].isEmpty());
			INIT_INSTANCES_ACTIVE.andNot(instancesPerFeature[i]);
		}
		
		// Remove propositions for instances if those propositions also appear in earlier propositions,
		// and those other instances are guaranteed to get checked before the later instances.
		//
		// An earlier instance is guaranteed to get checked before a later instance if the earlier instance 
		// is the first instance of its feature
		//
		// The tracking of active props already would avoid re-evaluating these props, but this optimisation
		// step removes even more overhead by removing the bits altogether
		final BitSet firstInstancesOfFeature = new BitSet(featureInstances.length);
		final boolean[] featuresObserved = new boolean[instancesPerFeature.length];
		for (int i = 0; i < featureInstances.length; ++i)
		{
			final int featureIdx = featureInstances[i].feature().spatialFeatureSetIndex();

			if (!featuresObserved[featureIdx])
			{
				firstInstancesOfFeature.set(i);
				featuresObserved[featureIdx] = true;
				final BitSet instanceProps = propsPerInstance[i];
				
				for (int j = i + 1; j < featureInstances.length; ++j)
				{
					if (featureInstances[j].feature().spatialFeatureSetIndex() == featureIdx)
						propsPerInstance[j].andNot(instanceProps);
				}
			}
		}
		
		this.propsPerInstance = new int[propsPerInstance.length][];
		for (int i = 0; i < propsPerInstance.length; ++i)
		{
			this.propsPerInstance[i] = new int[propsPerInstance[i].cardinality()];
			int k = 0;
			for (int j = propsPerInstance[i].nextSetBit(0); j >= 0; j = propsPerInstance[i].nextSetBit(j + 1))
			{
				this.propsPerInstance[i][k++] = j;
			}
		}
		
		// TODO keeping the following "optimisation" in, but it never actually seems to trigger
		// maybe this is provably unnecessary???
		//
		// For every proposition i, collect all propositions that we know for sure must be true
		// if that proposition gets evaluated as true (resp. false), i.e.:
		//
		//	1) 	earlier propositions that cannot themselves be deactivated and, if they were false, 
		//		would deactivate all instances that check proposition i, and
		//	2)	later propositions that get directly proven by i being true (resp. false)
		//
		// Any instances that are fully covered by that collection of propositions (+ i if true)
		// will for sure also be true, which means their features must for sure be true and we can
		// skip their other instances, i.e. deactivate them.
		//
		// The instance(s) that can potentially get proven by a proposition i through this mechanism
		// will be marked "immune" and cannot get deactivated again through the same mechanism (proving
		// a difference instance of the same feature) by a later proposition j > i. This is because
		// we want to avoid having to track which features are already "proven" (but not yet included
		// because their single remaining instance still needs to be visited), and also want to avoid
		// explicitly checking for duplicate features
		final BitSet immuneInstances = new BitSet(featureInstances.length);
		
		// A prop is undeactivatable if at least one of its instances is undeactivatable,
		// so we should track that for instances, and if it's the first instance of its feature
		final BitSet undeactivatableInstances = new BitSet(featureInstances.length);
		for (final BitSet bitset : deactivateInstancesIfTrue)
		{
			undeactivatableInstances.or(bitset);
		}
		for (final BitSet bitset : deactivateInstancesIfFalse)
		{
			undeactivatableInstances.or(bitset);
		}
		undeactivatableInstances.flip(0, featureInstances.length);
		undeactivatableInstances.and(firstInstancesOfFeature);
		
		for (int i = 0; i < propositions.length; ++i)
		{
			// Collect propositions that must be true because, if they were false,
			// they would deactivate every single instance that uses proposition i
			final BitSet mustTrueProps = new BitSet(propositions.length);
			
			for (int j = 0; j < i; ++j)
			{
				// Can only include prop j if prop j is undeactivatable, i.e. if it has at least one
				// undeactivatable instance
				final BitSet jInstances = (BitSet) instancesPerProp[j].clone();
				jInstances.and(undeactivatableInstances);
				if (jInstances.isEmpty())
					continue;
				
				final BitSet jDeactivatesIfFalse = deactivateInstancesIfFalse[j];
				final BitSet iInstances = (BitSet) instancesPerProp[i].clone();
				iInstances.andNot(jDeactivatesIfFalse);
				
				if (iInstances.isEmpty())
				{
					// i would never get checked if j were false, so j must be true if i gets checked
					mustTrueProps.set(j);
				}
			}
			
			for (int j = 0; j < featureInstances.length; ++j)
			{
				// First the case where we assume that i evaluates to false
				BitSet jProps = (BitSet) propsPerInstance[j].clone();
				jProps.andNot(mustTrueProps);
				jProps.andNot(provesPropsIfFalsePerProp[i]);
				
				if (jProps.isEmpty())
				{
					// Instance j must for sure be true if prop i gets checked and evaluates to false
					// we'll deactivate all other instances of this feature (except immune ones)
					//
					// j will become immune
					immuneInstances.set(j);
					final BitSet deactivateInstances = (BitSet) instancesPerFeature[featureInstances[j].feature().spatialFeatureSetIndex()].clone();
					deactivateInstances.andNot(immuneInstances);
//					deactivateInstances.andNot(deactivateInstancesIfFalse[i]);
//					deactivateInstances.andNot(INIT_INSTANCES_ACTIVE);
					deactivateInstancesIfFalse[i].or(deactivateInstances);
					
//					for (int k = deactivateInstances.nextSetBit(0); k >= 0; k = deactivateInstances.nextSetBit(k + 1))
//					{
//						System.out.println(featureInstances[i] + " deactivates " + featureInstances[k] + " if false.");
//					}
					
					// More instances have become deactivatable...
					undeactivatableInstances.andNot(deactivateInstances);
				}
				
				// Now the case where we assume that i evaluates to true
				jProps = (BitSet) propsPerInstance[j].clone();
				jProps.andNot(mustTrueProps);
				jProps.andNot(provesPropsIfTruePerProp[i]);
				jProps.clear(i);
				
				if (jProps.isEmpty())
				{
					// Instance j must for sure be true if prop i gets checked and evaluates to true
					// we'll deactivate all other instances of this feature (except immune ones)
					//
					// j will become immune
					immuneInstances.set(j);
					final BitSet deactivateInstances = (BitSet) instancesPerFeature[featureInstances[j].feature().spatialFeatureSetIndex()].clone();
					deactivateInstances.andNot(immuneInstances);
//					deactivateInstances.andNot(deactivateInstancesIfTrue[i]);
//					deactivateInstances.andNot(INIT_INSTANCES_ACTIVE);
					deactivateInstancesIfTrue[i].or(deactivateInstances);
					
//					for (int k = deactivateInstances.nextSetBit(0); k >= 0; k = deactivateInstances.nextSetBit(k + 1))
//					{
//						System.out.println(featureInstances[i] + " deactivates " + featureInstances[k] + " if true.");
//					}
					
					// More instances have become deactivatable...
					undeactivatableInstances.andNot(deactivateInstances);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param state
	 * @return List of active instances for given state
	 */
	public List<FeatureInstance> getActiveInstances(final State state)
	{
		final List<FeatureInstance> active = new ArrayList<FeatureInstance>();
		
		final boolean[] activeProps = ALL_PROPS_ACTIVE.clone();
		final BitSet activeInstances = (BitSet) INIT_INSTANCES_ACTIVE.clone();
		
		for (int i = 0; i < activeProps.length; ++i)
		{
			if (!activeProps[i])
				continue;
			
			if (activeInstances.intersects(instancesPerProp[i]))		// If false, might as well not check anything
			{
				if (!propositions[i].matches(state))		// Requirement not satisfied
					activeInstances.andNot(instancesPerProp[i]);
			}
		}
		
		for (int i = activeInstances.nextSetBit(0); i >= 0; i = activeInstances.nextSetBit(i + 1))
		{
			active.add(featureInstances[i]);
		}
		
		return active;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param state
	 * @return List of active instances for given state
	 */
	public FastTIntArrayList getActiveFeatures(final State state)
	{
		final FastTIntArrayList activeFeatures = new FastTIntArrayList(instancesPerFeature.length);
		activeFeatures.add(autoActiveFeatures);
		
		final boolean[] activeProps = ALL_PROPS_ACTIVE.clone();
		final BitSet activeInstances = (BitSet) INIT_INSTANCES_ACTIVE.clone();
		//System.out.println();
		//System.out.println("Auto-active features: " + Arrays.toString(autoActiveFeatures));

		outer:
		for 
		(
			int instanceToCheck = activeInstances.nextSetBit(0); 
			instanceToCheck >= 0; 
			instanceToCheck = activeInstances.nextSetBit(instanceToCheck + 1))
		{
			final int[] instanceProps = propsPerInstance[instanceToCheck];
			//System.out.println("Checking feature instance " + instanceToCheck + ": " + featureInstances[instanceToCheck]);
			//System.out.println("instance props = " + Arrays.toString(instanceProps));
			
			// Keep checking props for this instance in order
			for (int i = 0; i < instanceProps.length; ++i)
			{
				final int propID = instanceProps[i];
				
				if (!activeProps[propID])
					continue;	// Prop already known to be true
				
				// We're checking propID now, so mark it as inactive
				activeProps[propID] = false;
				
				//System.out.println("evaluating prop " + propID + ": " + propositions[propID]);
				
				// Check the proposition
				if (!propositions[propID].matches(state))
				{
					// Proposition is false
					//System.out.println("evaluated to false!");
					
					// Prove other propositions that get proven by this one being false; simply switch them
					// off in the list of active props
					for (final int j : provesPropsIfFalsePerProp[propID])
					{
						activeProps[j] = false;
					}
					
					// Disprove propositions that get disproven by this one being false; switch off any instances 
					// that require them
					// And at the same time, also deactivate instances of features that get auto-proven
					activeInstances.andNot(deactivateInstancesIfFalse[propID]);
					
					// No point in continuing with props for this instance, instance is false anyway
					continue outer;
				}
				else
				{
					// Proposition is true
					//System.out.println("evaluated to true");
					
					// Prove other propositions that get proven by this one being true; simply switch them
					// off in the list of active props
					for (final int j : provesPropsIfTruePerProp[propID])
					{
						activeProps[j] = false;
					}
					
					// Disprove propositions that get disproven by this one being true; switch off any
					// instances that require them
					// And at the same time, also deactivate instances of features that get auto-proven
					activeInstances.andNot(deactivateInstancesIfTrue[propID]);
				}
			}
			
			// If we reach this point, the feature instance (and hence the feature) is active
			final int newActiveFeature = featureInstances[instanceToCheck].feature().spatialFeatureSetIndex();
			activeFeatures.add(newActiveFeature);
			
			// This also means that we can skip any remaining instances for the same feature
			activeInstances.andNot(instancesPerFeature[newActiveFeature]);
		}
		
		//System.out.println();
		return activeFeatures;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param containerState
	 * @return Footprint of the state members that may be tested by this set.
	 */
	public BaseFootprint generateFootprint(final ContainerState containerState)
	{
		final ChunkSet footprintEmptyCells = 
				containerState.emptyChunkSetCell() != null ?
				new ChunkSet(containerState.emptyChunkSetCell().chunkSize(), 1) :
				null;
		final ChunkSet footprintEmptyVertices = 
				containerState.emptyChunkSetVertex() != null ?
				new ChunkSet(containerState.emptyChunkSetVertex().chunkSize(), 1) :
				null;
		final ChunkSet footprintEmptyEdges = 
				containerState.emptyChunkSetEdge() != null ?
				new ChunkSet(containerState.emptyChunkSetEdge().chunkSize(), 1) :
				null;
				
		final ChunkSet footprintWhoCells = 
				containerState.chunkSizeWhoCell() > 0 ? 
				new ChunkSet(containerState.chunkSizeWhoCell(), 1) : 
				null;
		final ChunkSet footprintWhoVertices = 
				containerState.chunkSizeWhoVertex() > 0 ? 
				new ChunkSet(containerState.chunkSizeWhoVertex(), 1) : 
				null;
		final ChunkSet footprintWhoEdges = 
				containerState.chunkSizeWhoEdge() > 0 ? 
				new ChunkSet(containerState.chunkSizeWhoEdge(), 1) : 
				null;
				
		final ChunkSet footprintWhatCells = 
				containerState.chunkSizeWhatCell() > 0 ?
				new ChunkSet(containerState.chunkSizeWhatCell(), 1) :
				null;
		final ChunkSet footprintWhatVertices = 
				containerState.chunkSizeWhatVertex() > 0 ?
				new ChunkSet(containerState.chunkSizeWhatVertex(), 1) :
				null;
		final ChunkSet footprintWhatEdges = 
				containerState.chunkSizeWhatEdge() > 0 ?
				new ChunkSet(containerState.chunkSizeWhatEdge(), 1) :
				null;
				
		for (final AtomicProposition prop : propositions)
		{
			switch (prop.graphElementType())
			{
			case Cell:
				switch (prop.stateVectorType())
				{
				case Empty:
					prop.addMaskTo(footprintEmptyCells);
					break;
				case Who:
					prop.addMaskTo(footprintWhoCells);
					break;
				case What:
					prop.addMaskTo(footprintWhatCells);
					break;
				}
				break;
			case Edge:
				switch (prop.stateVectorType())
				{
				case Empty:
					prop.addMaskTo(footprintEmptyEdges);
					break;
				case Who:
					prop.addMaskTo(footprintWhoEdges);
					break;
				case What:
					prop.addMaskTo(footprintWhatEdges);
					break;
				}
				break;
			case Vertex:
				switch (prop.stateVectorType())
				{
				case Empty:
					prop.addMaskTo(footprintEmptyVertices);
					break;
				case Who:
					prop.addMaskTo(footprintWhoVertices);
					break;
				case What:
					prop.addMaskTo(footprintWhatVertices);
					break;
				}
				break;
			}
		}
				
		return new FullFootprint
				(
					footprintEmptyCells, 
					footprintEmptyVertices, 
					footprintEmptyEdges, 
					footprintWhoCells, 
					footprintWhoVertices,
					footprintWhoEdges,
					footprintWhatCells,
					footprintWhatVertices,
					footprintWhatEdges
				);
	}
	
	//-------------------------------------------------------------------------

}
