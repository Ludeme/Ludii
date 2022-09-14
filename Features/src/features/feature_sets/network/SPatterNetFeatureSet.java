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
import java.util.Map.Entry;
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
import main.collections.FastTIntArrayList;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * Implementation of Feature Set based on SPatterNets
 *
 * @author Dennis Soemers
 */
public class SPatterNetFeatureSet extends BaseFeatureSet
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reactive instance sets, indexed by:
	 * 	player index,
	 * 	last-from-pos
	 * 	last-to-pos,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a feature instance set.
	 */
	protected HashMap<ReactiveFeaturesKey, PropFeatureInstanceSet> reactiveInstances;
	
	/**
	 * Proactive instances, indexed by:
	 * 	player index,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a feature instance set.
	 */
	protected HashMap<ProactiveFeaturesKey, PropFeatureInstanceSet> proactiveInstances;
	
	/**
	 * Reactive features, indexed by:
	 * 	player index,
	 * 	last-from-pos
	 * 	last-to-pos,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a feature set.
	 */
	protected HashMap<ReactiveFeaturesKey, SPatterNet> reactiveFeatures;
	
	/**
	 * Proactive features, indexed by:
	 * 	player index,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a feature set.
	 */
	protected HashMap<ProactiveFeaturesKey, SPatterNet> proactiveFeatures;
	
	/**
	 * Same as reactive features above, but only retaining features with absolute
	 * weights that exceed the threshold defined by BaseFeatureSet.
	 */
	protected HashMap<ReactiveFeaturesKey, SPatterNet> reactiveFeaturesThresholded;
	
	/**
	 * Same as proactive features above, but only retaining features with absolute
	 * weights that exceed the threshold defined by BaseFeatureSet.
	 */
	protected HashMap<ProactiveFeaturesKey, SPatterNet> proactiveFeaturesThresholded;
	
	/** Cache with indices of active proactive features previously computed */
	protected ActiveFeaturesCache activeProactiveFeaturesCache;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Construct feature set from lists of features
	 * @param aspatialFeatures
	 * @param spatialFeatures
	 */
	public SPatterNetFeatureSet(final List<AspatialFeature> aspatialFeatures, final List<SpatialFeature> spatialFeatures)
	{
		this.spatialFeatures = new SpatialFeature[spatialFeatures.size()];
		
		for (int i = 0; i < this.spatialFeatures.length; ++i)
		{
			this.spatialFeatures[i] = spatialFeatures.get(i);
			this.spatialFeatures[i].setSpatialFeatureSetIndex(i);
		}
		
		this.aspatialFeatures = aspatialFeatures.toArray(new AspatialFeature[aspatialFeatures.size()]);
		
		reactiveInstances = null;
		proactiveInstances = null;
		
		reactiveFeatures = null;
		proactiveFeatures = null;
		
		reactiveFeaturesThresholded = null;
		proactiveFeaturesThresholded = null;
	}
	
	/**
	 * Loads a feature set from a given filename
	 * @param filename
	 */
	public SPatterNetFeatureSet(final String filename)
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
			exception.printStackTrace();
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
		Map<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> reactiveInstancesSet = 
				new HashMap<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet>();
		Map<ProactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> proactiveInstancesSet = 
				new HashMap<ProactiveFeaturesKey, BipartiteGraphFeatureInstanceSet>();
		
		// Create a dummy context because we need some context for 
		// feature generation
		final Context featureGenContext = new Context(game.get(), new Trial(game.get()));
		
		// Collect features that should be ignored when running in thresholded mode
		final BitSet thresholdedFeatures = new BitSet();
		if (spatialFeatureInitWeights != null)
		{
			// Doing following loop in reverse order ensures that thresholdedFeatures bitset size
			// does not grow larger than necessary
			for (int i = spatialFeatures.length - 1; i >= 0; --i)
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
							-1,
							-1,
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
		
		reactiveInstances = new HashMap<ReactiveFeaturesKey, PropFeatureInstanceSet>();
		reactiveFeatures = new HashMap<ReactiveFeaturesKey, SPatterNet>();
		reactiveFeaturesThresholded = new HashMap<ReactiveFeaturesKey, SPatterNet>();
		for (final Entry<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> entry : reactiveInstancesSet.entrySet())
		{
			reactiveInstances.put(entry.getKey(), entry.getValue().toPropFeatureInstanceSet());
			reactiveFeatures.put(entry.getKey(), entry.getValue().toSPatterNet(getNumSpatialFeatures(), new BitSet(), game.get(), entry.getKey().playerIdx()));
			reactiveFeaturesThresholded.put(entry.getKey(), entry.getValue().toSPatterNet(getNumSpatialFeatures(), thresholdedFeatures, game.get(), entry.getKey().playerIdx()));
		}
		
		proactiveInstances = new HashMap<ProactiveFeaturesKey, PropFeatureInstanceSet>();
		proactiveFeatures = new HashMap<ProactiveFeaturesKey, SPatterNet>();
		proactiveFeaturesThresholded = new HashMap<ProactiveFeaturesKey, SPatterNet>();
		for (final Entry<ProactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> entry : proactiveInstancesSet.entrySet())
		{
			proactiveInstances.put(entry.getKey(), entry.getValue().toPropFeatureInstanceSet());
			proactiveFeatures.put(entry.getKey(), entry.getValue().toSPatterNet(getNumSpatialFeatures(), new BitSet(), game.get(), entry.getKey().playerIdx()));
			proactiveFeaturesThresholded.put(entry.getKey(), entry.getValue().toSPatterNet(getNumSpatialFeatures(), thresholdedFeatures, game.get(), entry.getKey().playerIdx()));
		}
	}
	
	@Override
	public void closeCache()
	{
		activeProactiveFeaturesCache.close();
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
		final HashMap<ReactiveFeaturesKey, SPatterNet> reactiveFeaturesMap;
		final HashMap<ProactiveFeaturesKey, SPatterNet> proactiveFeaturesMap;
		if (thresholded)
		{
			reactiveFeaturesMap = reactiveFeaturesThresholded;
			proactiveFeaturesMap = proactiveFeaturesThresholded;
		}
		else
		{
			reactiveFeaturesMap = reactiveFeatures;
			proactiveFeaturesMap = proactiveFeatures;
		}
		
		final FastTIntArrayList featureIndices = new FastTIntArrayList(this.getNumSpatialFeatures());
		
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
		
		if (!proactiveFeaturesMap.isEmpty())
		{
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
							final SPatterNet set = proactiveFeaturesMap.get(key);
							
							if (set != null)
								featureIndices.addAll(set.getActiveFeatures(state));
						}
					}
				}
				
				if (thresholded && !featureIndices.isEmpty())
					activeProactiveFeaturesCache.cache(state, from, to, featureIndices.toArray(), player);
			}
		}
		
		if (!reactiveFeatures.isEmpty())
		{
			final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
			
			if (lastFrom >= 0 || lastTo >= 0)
			{
				for (int i = 0; i < lastFroms.length; ++i)
				{
					final int lastFromPos = lastFroms[i];
					
					for (int j = 0; j < lastTos.length; ++j)
					{
						final int lastToPos = lastTos[j];
						
						if (lastToPos >= 0 || lastFromPos >= 0)
						{
							for (int k = 0; k < froms.length; ++k)
							{
								final int fromPos = froms[k];
								
								for (int l = 0; l < tos.length; ++l)
								{
									final int toPos = tos[l];
									
									if (toPos >= 0 || fromPos >= 0)
									{
										// Reactive instances
										reactiveKey.resetData(player, lastFromPos, lastToPos, fromPos, toPos);
										final SPatterNet set = reactiveFeaturesMap.get(reactiveKey);
										
										if (set != null)
											featureIndices.addAll(set.getActiveFeatures(state));
									}
								}
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
		for (int i = 0; i < lastFroms.length; ++i)
		{
			final int lastFromPos = lastFroms[i];

			for (int j = 0; j < lastTos.length; ++j)
			{
				final int lastToPos = lastTos[j];

				if (lastToPos >= 0 || lastFromPos >= 0)
				{
					for (int k = 0; k < froms.length; ++k)
					{
						final int fromPos = froms[k];

						for (int l = 0; l < tos.length; ++l)
						{
							final int toPos = tos[l];

							if (toPos >= 0 || fromPos >= 0)
							{
								// Reactive instances
								reactiveKey.resetData(player, lastFromPos, lastToPos, fromPos, toPos);
								final PropFeatureInstanceSet set = reactiveInstances.get(reactiveKey);

								if (set != null)
									instances.addAll(set.getActiveInstances(state));
							}
						}
					}
				}
			}
		}
		
		final ProactiveFeaturesKey proactiveKey = new ProactiveFeaturesKey();
		for (int k = 0; k < froms.length; ++k)
		{
			final int fromPos = froms[k];

			for (int l = 0; l < tos.length; ++l)
			{
				final int toPos = tos[l];

				if (toPos >= 0 || fromPos >= 0)
				{
					// Proactive instances
					proactiveKey.resetData(player, fromPos, toPos);
					final PropFeatureInstanceSet set = proactiveInstances.get(proactiveKey);
					
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
		SPatterNet set = proactiveFeaturesThresholded.get(key);
		
		if (set == null)
		{
			set = 
					new SPatterNet
					(
						new int[0], 
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
					
		final BaseFootprint footprint = set.generateFootprint(container);
		
		if (from >= 0)
		{
			// Also add footprints for from alone, and for to alone
			key.resetData(player, from, -1);
			set = proactiveFeaturesThresholded.get(key);
			if (set != null)
				footprint.union(set.generateFootprint(container));
			
			key.resetData(player, -1, to);
			set = proactiveFeaturesThresholded.get(key);
			if (set != null)
				footprint.union(set.generateFootprint(container));
		}
		
		return footprint;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public SPatterNetFeatureSet createExpandedFeatureSet
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
				allowedRotations = new TFloatArrayList(Walk.allGameRotations(targetGame));
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
			
			return new SPatterNetFeatureSet(Arrays.asList(aspatialFeatures), newFeatureList);
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Map of SPatterNets for reactive features
	 */
	public HashMap<ReactiveFeaturesKey, SPatterNet> reactiveFeatures()
	{
		return reactiveFeatures;
	}
	
	/**
	 * @return Map of SPatterNets for proactive features
	 */
	public HashMap<ProactiveFeaturesKey, SPatterNet> proactiveFeatures()
	{
		return proactiveFeatures;
	}
	
	//-------------------------------------------------------------------------

}
