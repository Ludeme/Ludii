package features.feature_sets.network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import features.Feature;
import features.aspatial.AspatialFeature;
import features.feature_sets.BaseFeatureSet;
import features.spatial.SpatialFeature;
import features.spatial.Walk;
import features.spatial.cache.ActiveFeaturesCache;
import features.spatial.cache.footprints.BaseFootprint;
import features.spatial.instances.AtomicProposition;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * Implementation of Feature Set based on SPatterNets, with JIT (Just-In-Time)
 * construction of the SPatterNets
 *
 * @author Dennis Soemers
 */
public class JITSPatterNetFeatureSet extends BaseFeatureSet
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reactive JIT map, indexed by:
	 * 	player index,
	 * 	last-from-pos
	 * 	last-to-pos,
	 * 	from-pos
	 * 	to-pos
	 */
	protected JITMap<ReactiveFeaturesKey> reactiveJITMap;
	
	/**
	 * Proactive JIT map, indexed by:
	 * 	player index,
	 * 	from-pos
	 * 	to-pos
	 */
	protected JITMap<ProactiveFeaturesKey> proactiveJITMap;
	
	/** Cache with indices of active proactive features previously computed */
	protected ActiveFeaturesCache activeProactiveFeaturesCache;
	
	/** Bitset of features that should be thresholded based on their absolute weights */
	protected BitSet thresholdedFeatures = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Construct feature set from lists of features
	 * @param aspatialFeatures
	 * @param spatialFeatures
	 */
	public JITSPatterNetFeatureSet(final List<AspatialFeature> aspatialFeatures, final List<SpatialFeature> spatialFeatures)
	{
		this.spatialFeatures = new SpatialFeature[spatialFeatures.size()];
		
		for (int i = 0; i < this.spatialFeatures.length; ++i)
		{
			this.spatialFeatures[i] = spatialFeatures.get(i);
			this.spatialFeatures[i].setSpatialFeatureSetIndex(i);
		}
		
		this.aspatialFeatures = aspatialFeatures.toArray(new AspatialFeature[aspatialFeatures.size()]);
		
		reactiveJITMap = null;
		proactiveJITMap = null;
	}
	
	/**
	 * Loads a feature set from a given filename
	 * @param filename
	 */
	public JITSPatterNetFeatureSet(final String filename)
	{
		Feature[] tempFeatures;
		
		//System.out.println("loading feature set from " + filename);
		try (Stream<String> stream = Files.lines(Paths.get(filename)))
		{
			tempFeatures = stream.map(s -> Feature.fromString(s)).toArray(Feature[]::new);
		} 
		catch (final IOException exception) 
		{
			tempFeatures = null;
		}
		
		final List<AspatialFeature> aspatialFeaturesList = new ArrayList<AspatialFeature>();
		final List<SpatialFeature> spatialFeaturesList = new ArrayList<SpatialFeature>();
		
		for (final Feature feature : tempFeatures)
		{
			if (feature instanceof AspatialFeature)
			{
				aspatialFeaturesList.add((AspatialFeature)feature);
			}
			else
			{
				((SpatialFeature)feature).setSpatialFeatureSetIndex(spatialFeaturesList.size());
				spatialFeaturesList.add((SpatialFeature)feature);
			}
		}
		
		this.aspatialFeatures = aspatialFeaturesList.toArray(new AspatialFeature[aspatialFeaturesList.size()]);
		this.spatialFeatures = spatialFeaturesList.toArray(new SpatialFeature[spatialFeaturesList.size()]);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected void instantiateFeatures(final int[] supportedPlayers)
	{
		activeProactiveFeaturesCache = new ActiveFeaturesCache();
		
		// Start out creating feature (instance) sets represented as bipartite graphs
		final Map<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> reactiveInstancesSet = 
				new HashMap<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet>();
		final Map<ProactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> proactiveInstancesSet = 
				new HashMap<ProactiveFeaturesKey, BipartiteGraphFeatureInstanceSet>();
		
		// Create a dummy context because we need some context for 
		// feature generation
		final Context featureGenContext = new Context(game.get(), new Trial(game.get()));
		
		// Collect features that should be ignored when running in thresholded mode
		thresholdedFeatures = new BitSet();
		if (spatialFeatureInitWeights != null)
		{
			for (int i = 0; i < spatialFeatures.length; ++i)
			{
				if (Math.abs(spatialFeatureInitWeights.get(i)) < SPATIAL_FEATURE_WEIGHT_THRESHOLD)
					thresholdedFeatures.set(i);
			}
		}
		
		final ProactiveFeaturesKey proactiveKey = new ProactiveFeaturesKey();
		final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
		for (int i = 0; i < supportedPlayers.length; ++i)
		{
			final int player = supportedPlayers[i];
			
			for (final SpatialFeature feature : spatialFeatures)
			{
				final List<FeatureInstance> newInstances = 
						feature.instantiateFeature
						(
							game.get(), 
							featureGenContext.state().containerStates()[0], 
							player, 
							-1, 
							-1
						);
				
				for (final FeatureInstance instance : newInstances)
				{
					final int lastFrom = instance.lastFrom();
					final int lastTo = instance.lastTo();
					final int from = instance.from();
					final int to = instance.to();
					
					if (lastFrom >= 0 || lastTo >= 0)	// Reactive feature
					{
						reactiveKey.resetData(player, lastFrom, lastTo, from, to);
						BipartiteGraphFeatureInstanceSet instancesSet = reactiveInstancesSet.get(reactiveKey);
						
						if (instancesSet == null)
						{
							instancesSet = new BipartiteGraphFeatureInstanceSet();
							reactiveInstancesSet.put(new ReactiveFeaturesKey(reactiveKey), instancesSet);
						}
						
						instancesSet.insertInstance(instance);
					}
					else								// Proactive feature
					{
						proactiveKey.resetData(player, from, to);
						BipartiteGraphFeatureInstanceSet instancesSet = proactiveInstancesSet.get(proactiveKey);
						
						if (instancesSet == null)
						{
							instancesSet = new BipartiteGraphFeatureInstanceSet();
							proactiveInstancesSet.put(new ProactiveFeaturesKey(proactiveKey), instancesSet);
						}
						
						instancesSet.insertInstance(instance);
					}
				}
			}
		}
		
		reactiveJITMap = new JITMap<ReactiveFeaturesKey>(reactiveInstancesSet);
		proactiveJITMap = new JITMap<ProactiveFeaturesKey>(proactiveInstancesSet);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public TIntArrayList getActiveSpatialFeatureIndices
	(
		final State state,
		final int lastFrom,
		final int lastTo,
		final int from,
		final int to,
		final int player,
		final boolean thresholded
	)
	{
		final TIntArrayList featureIndices = new TIntArrayList();
		
//		System.out.println("lastFrom = " + lastFrom);
//		System.out.println("lastTo = " + lastTo);
//		System.out.println("from = " + from);
//		System.out.println("to = " + to);
//		System.out.println("player = " + player);
//		final List<FeatureInstance> instances = getActiveFeatureInstances(state, lastFrom, lastTo, from, to, player);
//		for (final FeatureInstance instance : instances)
//		{
//			if (!featureIndices.contains(instance.feature().featureSetIndex()))
//				featureIndices.add(instance.feature().featureSetIndex());
//		}
		
		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
		final int[] lastFroms = lastFrom >= 0 ? new int[]{-1, lastFrom} : new int[]{-1};
		final int[] lastTos = lastTo >= 0 ? new int[]{-1, lastTo} : new int[]{-1};
		
		final int[] cachedActiveFeatureIndices;

		if (thresholded)
			cachedActiveFeatureIndices = activeProactiveFeaturesCache.getCachedActiveFeatures(this, state, from, to, player);
		else
			cachedActiveFeatureIndices = null;

		if (cachedActiveFeatureIndices != null)
		{
			// Successfully retrieved from cache
			featureIndices.add(cachedActiveFeatureIndices);
			//System.out.println("cache hit!");
		}
		else
		{
			final ProactiveFeaturesKey key = new ProactiveFeaturesKey();
			for (int k = 0; k < froms.length; ++k)
			{
				final int fromPos = froms[k];

				for (int l = 0; l < tos.length; ++l)
				{
					final int toPos = tos[l];

					if (toPos >= 0 || fromPos >= 0)
					{
						// Proactive instances
						key.resetData(player, fromPos, toPos);
						final SPatterNet set = thresholded ? proactiveJITMap.spatterNetThresholded(key) : proactiveJITMap.spatterNet(key);

						if (set != null)
							featureIndices.addAll(set.getActiveFeatures(state));
					}
				}
			}

			if (thresholded && !featureIndices.isEmpty())
				activeProactiveFeaturesCache.cache(state, from, to, featureIndices.toArray(), player);
		}
		
		final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
		if (lastFrom >= 0 || lastTo >= 0)
		{
			for (int i = 0; i < lastFroms.length; ++i)
			{
				final int lastFromPos = lastFroms[i];

				for (int j = 0; j < lastTos.length; ++j)
				{
					final int lastToPos = lastTos[j];

					for (int k = 0; k < froms.length; ++k)
					{
						final int fromPos = froms[k];

						for (int l = 0; l < tos.length; ++l)
						{
							final int toPos = tos[l];

							if (lastToPos >= 0 || lastFromPos >= 0)
							{
								// Reactive instances
								reactiveKey.resetData(player, lastFromPos, lastToPos, fromPos, toPos);
								final SPatterNet set = reactiveJITMap.spatterNet(reactiveKey);

								if (set != null)
									featureIndices.addAll(set.getActiveFeatures(state));
							}
						}
					}
				}
			}
		}
		
		return featureIndices;
	}

	@Override
	public List<FeatureInstance> getActiveSpatialFeatureInstances
	(
		final State state,
		final int lastFrom,
		final int lastTo,
		final int from,
		final int to,
		final int player
	)
	{
		final List<FeatureInstance> instances = new ArrayList<FeatureInstance>();
		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
		final int[] lastFroms = lastFrom >= 0 ? new int[]{-1, lastFrom} : new int[]{-1};
		final int[] lastTos = lastTo >= 0 ? new int[]{-1, lastTo} : new int[]{-1};
		
		final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
		if (lastFrom >= 0 || lastTo >= 0)
		{
			for (int i = 0; i < lastFroms.length; ++i)
			{
				final int lastFromPos = lastFroms[i];
				
				for (int j = 0; j < lastTos.length; ++j)
				{
					final int lastToPos = lastTos[j];
					
					for (int k = 0; k < froms.length; ++k)
					{
						final int fromPos = froms[k];
						
						for (int l = 0; l < tos.length; ++l)
						{
							final int toPos = tos[l];
							
							if (lastToPos >= 0 || lastFromPos >= 0)
							{
								// Reactive instances
								reactiveKey.resetData(player, lastFromPos, lastToPos, fromPos, toPos);
								final PropFeatureInstanceSet set = reactiveJITMap.propFeatureInstanceSet(reactiveKey);
								
								if (set != null)
									instances.addAll(set.getActiveInstances(state));
							}
						}
					}
				}
			}
		}
		
		final ProactiveFeaturesKey key = new ProactiveFeaturesKey();
		for (int k = 0; k < froms.length; ++k)
		{
			final int fromPos = froms[k];

			for (int l = 0; l < tos.length; ++l)
			{
				final int toPos = tos[l];

				if (toPos >= 0 || fromPos >= 0)
				{
					// Proactive instances
					key.resetData(player, fromPos, toPos);
								
					final PropFeatureInstanceSet set = proactiveJITMap.propFeatureInstanceSet(key);
					
					if (set != null)
						instances.addAll(set.getActiveInstances(state));
				}
			}
		}
		
		return instances;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public BaseFootprint generateFootprint
	(
		final State state, 
		final int from, 
		final int to,
		final int player
	)
	{
		final ContainerState container = state.containerStates()[0];
				
		// NOTE: only using caching with thresholding
		final ProactiveFeaturesKey key = new ProactiveFeaturesKey();
		key.resetData(player, from, to);
		SPatterNet set = proactiveJITMap.spatterNet(key);
		
		if (set == null)
		{
			set = 
					new SPatterNet
					(
						new FeatureInstance[0], 
						new AtomicProposition[0], 
						new BitSet[0], 
						new BitSet[0], 
						new BitSet[0], 
						new int[0], 
						new BitSet(),
						new BitSet[0], 
						new BitSet[0], 
						new BitSet[0], 
						new BitSet[0]
					);
		}
					
		return set.generateFootprint(container);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public JITSPatterNetFeatureSet createExpandedFeatureSet
	(
		final Game targetGame,
		final SpatialFeature newFeature
	)
	{
		boolean featureAlreadyExists = false;
		for (final SpatialFeature oldFeature : spatialFeatures)
		{
			if (newFeature.equals(oldFeature))
			{
				featureAlreadyExists = true;
				break;
			}
			
			// also try all legal rotations of the generated feature, see
			// if any of those turn out to be equal to an old feature
			TFloatArrayList allowedRotations = newFeature.pattern().allowedRotations();
			
			if (allowedRotations == null)
			{
				allowedRotations = Walk.allGameRotations(targetGame);
			}
			
			for (int i = 0; i < allowedRotations.size(); ++i)
			{
				final SpatialFeature rotatedCopy = 
						newFeature.rotatedCopy(allowedRotations.getQuick(i));
				
				if (rotatedCopy.equals(oldFeature))
				{
					// TODO only break if for every possible anchor this works?
					featureAlreadyExists = true;
					break;
				}
			}
		}
		
		if (!featureAlreadyExists)
		{
			// create new feature set with this feature, and return it
			final List<SpatialFeature> newFeatureList = new ArrayList<SpatialFeature>(spatialFeatures.length + 1);
			
			// all old features...
			for (final SpatialFeature feature : spatialFeatures)
			{
				newFeatureList.add(feature);
			}
			
			// and our new feature
			newFeatureList.add(newFeature);
			
			return new JITSPatterNetFeatureSet(Arrays.asList(aspatialFeatures), newFeatureList);
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper around a collection of maps from keys (reactive or proactive) to SPatterNets,
	 * PropFeatureInstanceSets, or BipartiteGraphFeatureInstanceSets.
	 * 
	 * The bipartite graph representations must be precomputed, but the other representations
	 * can be computed in a JIT-manner from the bipartite graph representations.
	 *
	 * @author Dennis Soemers
	 */
	private class JITMap<K extends MoveFeaturesKey>
	{
		
		/** Map to bipartite graph representation */
		private final Map<K, BipartiteGraphFeatureInstanceSet> bipartiteGraphMap;
		
		/** Map to prop-feature-instance-set represenation */
		private final Map<K, PropFeatureInstanceSet> propInstanceSetMap;
		
		/** Map to SPatterNet represenation (without thresholding) */
		private final Map<K, SPatterNet> spatterNetMap;
		
		/** Map to SPatterNet represenation (thresholded) */
		private final Map<K, SPatterNet> spatterNetMapThresholded;
		
		/**
		 * Constructor
		 * @param bipartiteGraphMap
		 */
		public JITMap(final Map<K, BipartiteGraphFeatureInstanceSet> bipartiteGraphMap)
		{
			this.bipartiteGraphMap = bipartiteGraphMap;
			this.propInstanceSetMap = new HashMap<K, PropFeatureInstanceSet>();
			this.spatterNetMap = new HashMap<K, SPatterNet>();
			this.spatterNetMapThresholded = new HashMap<K, SPatterNet>();
		}
		
		/**
		 * @param key
		 * @return PropFeatureInstanceSet for given key
		 */
		public PropFeatureInstanceSet propFeatureInstanceSet(final K key)
		{
			PropFeatureInstanceSet set = propInstanceSetMap.get(key);
			
			if (set == null)
			{
				// JIT: instantiate set for this key
				final BipartiteGraphFeatureInstanceSet bipartiteGraph = bipartiteGraphMap.get(key);
				if (bipartiteGraph != null)
				{
					set = bipartiteGraph.toPropFeatureInstanceSet();
					propInstanceSetMap.put(key, set);
				}
			}
			
			return set;
		}
		
		/**
		 * @param key
		 * @return SPatterNet for given key
		 */
		public SPatterNet spatterNet(final K key)
		{
			SPatterNet net = spatterNetMap.get(key);
			
			if (net == null)
			{
				// JIT: instantiate net for this key
				final BipartiteGraphFeatureInstanceSet bipartiteGraph = bipartiteGraphMap.get(key);
				if (bipartiteGraph != null)
				{
					net = bipartiteGraph.toSPatterNet(getNumSpatialFeatures(), new BitSet(), gameRef().get(), key.playerIdx());
					spatterNetMap.put(key, net);
				}
			}
			
			return net;
		}
		
		/**
		 * @param key
		 * @return SPatterNet (with thresholding) for given key
		 */
		public SPatterNet spatterNetThresholded(final K key)
		{
			SPatterNet net = spatterNetMapThresholded.get(key);
			
			if (net == null)
			{
				// JIT: instantiate net for this key
				final BipartiteGraphFeatureInstanceSet bipartiteGraph = bipartiteGraphMap.get(key);
				if (bipartiteGraph != null)
				{
					net = bipartiteGraph.toSPatterNet(getNumSpatialFeatures(), thresholdedFeatures, gameRef().get(), key.playerIdx());
					spatterNetMapThresholded.put(key, net);
				}
			}
			
			return net;
		}
	}
	
	//-------------------------------------------------------------------------

}
