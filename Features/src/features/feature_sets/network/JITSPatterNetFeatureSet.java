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
import features.spatial.RelativeFeature;
import features.spatial.SpatialFeature;
import features.spatial.Walk;
import features.spatial.cache.ActiveFeaturesCache;
import features.spatial.cache.footprints.BaseFootprint;
import features.spatial.instances.AtomicProposition;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.collections.ArrayUtils;
import main.collections.FastTIntArrayList;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * Implementation of Feature Set based on SPatterNets, with JIT (Just-In-Time)
 * construction of the SPatterNets
 * 
 * NOTE: for better JIT behaviour, we assume that every feature has either
 * the to or the from position as anchor
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
		
		reactiveJITMap = new JITMap<ReactiveFeaturesKey>();
		proactiveJITMap = new JITMap<ProactiveFeaturesKey>();
		
		// We'll use a dummy context to kickstart our JIT compilation on some real moves
		final Context jitContext = new Context(game.get(), new Trial(game.get()));
		
		// We'll do 3 random (partial, very short) trials
		for (int i = 0; i < 3; ++i)
		{
			game.get().start(jitContext);
			
			// We'll do 5 moves max per trial
			for (int j = 0; j < 10; ++j)
			{
				if (jitContext.trial().over())
					break;
				
				for (final Move move : game.get().moves(jitContext).moves())
				{
					final int mover = move.mover();
					if (ArrayUtils.contains(supportedPlayers, mover))
					{
						final boolean thresholding = (spatialFeatureInitWeights != null);
						this.computeFeatureVector(jitContext, move, thresholding);	// This lets us do JIT instantiation
					}
				}
				
				jitContext.model().startNewStep(jitContext, null, 0.1);
			}
		}
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
						final SPatterNet set = thresholded ? proactiveJITMap.spatterNetThresholded(key, state) : proactiveJITMap.spatterNet(key, state);

						if (set != null)
							featureIndices.addAll(set.getActiveFeatures(state));
					}
				}
			}

			if (thresholded && !featureIndices.isEmpty())
				activeProactiveFeaturesCache.cache(state, from, to, featureIndices.toArray(), player);
		}
		
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
								final SPatterNet set = reactiveJITMap.spatterNet(reactiveKey, state);
	
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
		// TODO could consider a variant where we remove the -1s from froms and tos?
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
								final PropFeatureInstanceSet set = reactiveJITMap.propFeatureInstanceSet(reactiveKey, state);
								
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
								
					final PropFeatureInstanceSet set = proactiveJITMap.propFeatureInstanceSet(key, state);
					
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
		SPatterNet set = proactiveJITMap.spatterNetThresholded(key, state);
		
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
	 * Wrapper around a collection of maps from keys (reactive or proactive) to SPatterNets
	 * or PropFeatureInstanceSets.
	 *
	 * @author Dennis Soemers
	 */
	private class JITMap<K extends MoveFeaturesKey>
	{
		
		/** Map to prop-feature-instance-set represenation */
		private final Map<K, PropFeatureInstanceSet> propInstanceSetMap;
		
		/** Map to SPatterNet represenation (without thresholding) */
		private final Map<K, SPatterNet> spatterNetMap;
		
		/** Map to SPatterNet represenation (thresholded) */
		private final Map<K, SPatterNet> spatterNetMapThresholded;
		
		/**
		 * Constructor
		 */
		public JITMap()
		{
			this.propInstanceSetMap = new HashMap<K, PropFeatureInstanceSet>();
			this.spatterNetMap = new HashMap<K, SPatterNet>();
			this.spatterNetMapThresholded = new HashMap<K, SPatterNet>();
		}
		
		/**
		 * @param key
		 * @param state
		 * @return PropFeatureInstanceSet for given key
		 */
		@SuppressWarnings("unchecked")
		public PropFeatureInstanceSet propFeatureInstanceSet(final K key, final State state)
		{
			PropFeatureInstanceSet set = propInstanceSetMap.get(key);
			
			if (set == null)
			{
				// JIT: instantiate set for this key
				final boolean reactiveKey = (key.lastFrom() >= 0 || key.lastTo() >= 0);
				
				final BipartiteGraphFeatureInstanceSet bipartiteGraph = new BipartiteGraphFeatureInstanceSet();
				
				for (final SpatialFeature feature : JITSPatterNetFeatureSet.this.spatialFeatures())
				{
					final RelativeFeature relFeature = (RelativeFeature)feature;
					
					if (feature.isReactive() == reactiveKey)
					{
						final List<FeatureInstance> newInstances = new ArrayList<FeatureInstance>();
						
						if 
						(
							key.from() >= 0 
							&& 
							relFeature.fromPosition() != null 
							&&
							((key.to() >= 0) == (relFeature.toPosition() == null))
						)
						{
							// Try instantiating with from as anchor
							newInstances.addAll
							(
								feature.instantiateFeature
								(
									JITSPatterNetFeatureSet.this.gameRef().get(), 
									state.containerStates()[0], 
									state.mover(), 
									key.from(), 
									key.from(),
									key.to(),
									key.lastFrom(),
									key.lastTo()
								)
							);
						}
						
						if 
						(
							key.to() >= 0
							&& 
							relFeature.toPosition() != null
							&&
							((key.from() >= 0) == (relFeature.fromPosition() == null))
						)
						{
							// Try instantiating with to as anchor
							newInstances.addAll
							(
								feature.instantiateFeature
								(
									JITSPatterNetFeatureSet.this.gameRef().get(), 
									state.containerStates()[0], 
									state.mover(), 
									key.to(), 
									key.from(),
									key.to(),
									key.lastFrom(),
									key.lastTo()
								)
							);
						}
						
						for (final FeatureInstance instance : newInstances)
						{
							bipartiteGraph.insertInstance(instance);
						}
					}
				}

				set = bipartiteGraph.toPropFeatureInstanceSet();
				if (reactiveKey)
					propInstanceSetMap.put((K)(new ReactiveFeaturesKey((ReactiveFeaturesKey)key)), set);
				else
					propInstanceSetMap.put((K)(new ProactiveFeaturesKey((ProactiveFeaturesKey)key)), set);
			}
			
			return set;
		}
		
		/**
		 * @param key
		 * @param state
		 * @return SPatterNet for given key
		 */
		@SuppressWarnings("unchecked")
		public SPatterNet spatterNet(final K key, final State state)
		{
			SPatterNet net = spatterNetMap.get(key);
			
			if (net == null)
			{
				// JIT: instantiate net for this key
				final boolean reactiveKey = (key.lastFrom() >= 0 || key.lastTo() >= 0);
				
				final BipartiteGraphFeatureInstanceSet bipartiteGraph = new BipartiteGraphFeatureInstanceSet();
				
				for (final SpatialFeature feature : JITSPatterNetFeatureSet.this.spatialFeatures())
				{
					final RelativeFeature relFeature = (RelativeFeature)feature;
					
					if (feature.isReactive() == reactiveKey)
					{
						final List<FeatureInstance> newInstances = new ArrayList<FeatureInstance>();
						
						if 
						(
							key.from() >= 0 
							&& 
							relFeature.fromPosition() != null 
							&&
							((key.to() >= 0) == (relFeature.toPosition() == null))
						)
						{
							// Try instantiating with from as anchor
							newInstances.addAll
							(
								feature.instantiateFeature
								(
									JITSPatterNetFeatureSet.this.gameRef().get(), 
									state.containerStates()[0], 
									state.mover(), 
									key.from(), 
									key.from(),
									key.to(),
									key.lastFrom(),
									key.lastTo()
								)
							);
						}
						
						if 
						(
							key.to() >= 0
							&& 
							relFeature.toPosition() != null
							&&
							((key.from() >= 0) == (relFeature.fromPosition() == null))
						)
						{
							// Try instantiating with to as anchor
							newInstances.addAll
							(
								feature.instantiateFeature
								(
									JITSPatterNetFeatureSet.this.gameRef().get(), 
									state.containerStates()[0], 
									state.mover(), 
									key.to(), 
									key.from(),
									key.to(),
									key.lastFrom(),
									key.lastTo()
								)
							);
						}
						
						for (final FeatureInstance instance : newInstances)
						{
							bipartiteGraph.insertInstance(instance);
						}
					}
				}
				
				net = bipartiteGraph.toSPatterNet(getNumSpatialFeatures(), new BitSet(), gameRef().get(), key.playerIdx());
				if (reactiveKey)
					spatterNetMap.put((K)(new ReactiveFeaturesKey((ReactiveFeaturesKey)key)), net);
				else
					spatterNetMap.put((K)(new ProactiveFeaturesKey((ProactiveFeaturesKey)key)), net);
			}
			
			return net;
		}
		
		/**
		 * @param key
		 * @param state
		 * @return SPatterNet (with thresholding) for given key
		 */
		@SuppressWarnings("unchecked")
		public SPatterNet spatterNetThresholded(final K key, final State state)
		{
			SPatterNet net = spatterNetMapThresholded.get(key);
			
			if (net == null)
			{
				// JIT: instantiate net for this key
				final boolean reactiveKey = (key.lastFrom() >= 0 || key.lastTo() >= 0);
				
				final BipartiteGraphFeatureInstanceSet bipartiteGraph = new BipartiteGraphFeatureInstanceSet();
				
				for (final SpatialFeature feature : JITSPatterNetFeatureSet.this.spatialFeatures())
				{
					final RelativeFeature relFeature = (RelativeFeature)feature;
					
					if (feature.isReactive() == reactiveKey)
					{
						final List<FeatureInstance> newInstances = new ArrayList<FeatureInstance>();
						
						if 
						(
							key.from() >= 0 
							&& 
							relFeature.fromPosition() != null 
							&&
							((key.to() >= 0) == (relFeature.toPosition() == null))
						)
						{
							// Try instantiating with from as anchor
							newInstances.addAll
							(
								feature.instantiateFeature
								(
									JITSPatterNetFeatureSet.this.gameRef().get(), 
									state.containerStates()[0], 
									state.mover(), 
									key.from(), 
									key.from(),
									key.to(),
									key.lastFrom(),
									key.lastTo()
								)
							);
						}
						
						if 
						(
							key.to() >= 0
							&& 
							relFeature.toPosition() != null
							&&
							((key.from() >= 0) == (relFeature.fromPosition() == null))
						)
						{
							// Try instantiating with to as anchor
							newInstances.addAll
							(
								feature.instantiateFeature
								(
									JITSPatterNetFeatureSet.this.gameRef().get(), 
									state.containerStates()[0], 
									state.mover(), 
									key.to(), 
									key.from(),
									key.to(),
									key.lastFrom(),
									key.lastTo()
								)
							);
						}
						
						for (final FeatureInstance instance : newInstances)
						{
							bipartiteGraph.insertInstance(instance);
						}
					}
				}
				
				net = bipartiteGraph.toSPatterNet(getNumSpatialFeatures(), thresholdedFeatures, gameRef().get(), key.playerIdx());
				if (reactiveKey)
					spatterNetMapThresholded.put((K)(new ReactiveFeaturesKey((ReactiveFeaturesKey)key)), net);
				else
					spatterNetMapThresholded.put((K)(new ProactiveFeaturesKey((ProactiveFeaturesKey)key)), net);
			}
			
			return net;
		}
	}
	
	//-------------------------------------------------------------------------

}
