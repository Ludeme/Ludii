package features.feature_sets.network.decision_tree;

/**
 * A Decision-Tree variant of the JIT SPAtterNet Feature Set. Only computes active
 * features as necessary when following the branches of a Decision or Regression tree.
 *
 * @author Dennis Soemers
 */
public class JITSPatterNetFeatureSetDecisionTree
{
	
//	//-------------------------------------------------------------------------
//	
//	/** Cache of feature set objects */
//	protected static final Map<FeatureLists, JITSPatterNetFeatureSetDecisionTree> featureSetsCache = 
//			new ConcurrentHashMap<FeatureLists, JITSPatterNetFeatureSetDecisionTree>();
//	
//	//-------------------------------------------------------------------------
//	
//	/** JIT map (mix of proactive and reactive keys) */
//	protected JITMap jitMap;
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * Clear the entire cache of feature set objects.
//	 */
//	public static void clearFeatureSetCache()
//	{
//		featureSetsCache.clear();
//	}
//	
//	/**
//	 * Construct feature set from lists of features
//	 * @param aspatialFeatures
//	 * @param spatialFeatures
//	 */
//	public static JITSPatterNetFeatureSetDecisionTree construct(final List<AspatialFeature> aspatialFeatures, final List<SpatialFeature> spatialFeatures)
//	{
//		if (JITSPatterNetFeatureSet.ALLOW_FEATURE_SET_CACHE)
//		{
//			final FeatureLists key = new FeatureLists(aspatialFeatures, spatialFeatures);
//			final JITSPatterNetFeatureSetDecisionTree cached = featureSetsCache.get(key);
//			
//			if (cached != null)
//				return cached;
//			
//			final JITSPatterNetFeatureSetDecisionTree newSet = new JITSPatterNetFeatureSetDecisionTree(aspatialFeatures, spatialFeatures);
//			featureSetsCache.put(key, newSet);
//			return newSet;
//		}
//		
//		return new JITSPatterNetFeatureSetDecisionTree(aspatialFeatures, spatialFeatures);
//	}
//	
////	/**
////	 * Loads a feature set from a given filename
////	 * @param filename
////	 */
////	public static JITSPatterNetFeatureSetDecisionTree construct(final String filename)
////	{
////		Feature[] tempFeatures;
////		
////		//System.out.println("loading feature set from " + filename);
////		try (Stream<String> stream = Files.lines(Paths.get(filename)))
////		{
////			tempFeatures = stream.map(s -> Feature.fromString(s)).toArray(Feature[]::new);
////		} 
////		catch (final IOException exception) 
////		{
////			tempFeatures = null;
////			exception.printStackTrace();
////		}
////		
////		final List<AspatialFeature> aspatialFeaturesList = new ArrayList<AspatialFeature>();
////		final List<SpatialFeature> spatialFeaturesList = new ArrayList<SpatialFeature>();
////		
////		for (final Feature feature : tempFeatures)
////		{
////			if (feature instanceof AspatialFeature)
////			{
////				aspatialFeaturesList.add((AspatialFeature)feature);
////			}
////			else
////			{
////				((SpatialFeature)feature).setSpatialFeatureSetIndex(spatialFeaturesList.size());
////				spatialFeaturesList.add((SpatialFeature)feature);
////			}
////		}
////		
////		return construct(aspatialFeaturesList, spatialFeaturesList);
////	}
//	
//	/**
//	 * Construct feature set from lists of features
//	 * @param aspatialFeatures
//	 * @param spatialFeatures
//	 */
//	private JITSPatterNetFeatureSetDecisionTree(final List<AspatialFeature> aspatialFeatures, final List<SpatialFeature> spatialFeatures)
//	{
//		this.spatialFeatures = new SpatialFeature[spatialFeatures.size()];
//		
//		for (int i = 0; i < this.spatialFeatures.length; ++i)
//		{
//			this.spatialFeatures[i] = spatialFeatures.get(i);
//			this.spatialFeatures[i].setSpatialFeatureSetIndex(i);
//		}
//		
//		this.aspatialFeatures = aspatialFeatures.toArray(new AspatialFeature[aspatialFeatures.size()]);
//		
//		jitMap = null;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	@Override
//	protected void instantiateFeatures(final int[] supportedPlayers)
//	{
//		activeProactiveFeaturesCache = new ActiveFeaturesCache();
//		
//		// Collect features that should be ignored when running in thresholded mode
//		thresholdedFeatures = new BitSet();
//		if (spatialFeatureInitWeights != null)
//		{
//			// Doing following loop in reverse order ensures that thresholdedFeatures bitset size
//			// does not grow larger than necessary
//			for (int i = spatialFeatures.length - 1; i >= 0; --i)
//			{
//				if (Math.abs(spatialFeatureInitWeights.get(i)) < SPATIAL_FEATURE_WEIGHT_THRESHOLD)
//					thresholdedFeatures.set(i);
//			}
//		}
//		
//		jitMap = new JITMap();
//		
//		// We'll use a dummy context to kickstart our JIT compilation on some real moves
//		final Context jitContext = new Context(game.get(), new Trial(game.get()));
//		
//		// We'll do 3 random (partial, very short) trials
//		for (int i = 0; i < 3; ++i)
//		{
//			game.get().start(jitContext);
//			
//			// We'll do 5 moves max per trial
//			for (int j = 0; j < 10; ++j)
//			{
//				if (jitContext.trial().over())
//					break;
//				
//				for (final Move move : game.get().moves(jitContext).moves())
//				{
//					final int mover = move.mover();
//					if (ArrayUtils.contains(supportedPlayers, mover))
//					{
//						final boolean thresholding = (spatialFeatureInitWeights != null);
//						this.computeFeatureVector(jitContext, move, thresholding);	// This lets us do JIT instantiation
//					}
//				}
//				
//				jitContext.model().startNewStep(jitContext, null, 0.1);
//			}
//		}
//	}
//	
//	//-------------------------------------------------------------------------
//
//	@Override
//	public TIntArrayList getActiveSpatialFeatureIndices
//	(
//		final State state,
//		final int lastFrom,
//		final int lastTo,
//		final int from,
//		final int to,
//		final int player,
//		final boolean thresholded
//	)
//	{
//		final FastTIntArrayList featureIndices = new FastTIntArrayList(this.getNumSpatialFeatures());
//		
////		System.out.println("lastFrom = " + lastFrom);
////		System.out.println("lastTo = " + lastTo);
////		System.out.println("from = " + from);
////		System.out.println("to = " + to);
////		System.out.println("player = " + player);
////		final List<FeatureInstance> instances = getActiveFeatureInstances(state, lastFrom, lastTo, from, to, player);
////		for (final FeatureInstance instance : instances)
////		{
////			if (!featureIndices.contains(instance.feature().featureSetIndex()))
////				featureIndices.add(instance.feature().featureSetIndex());
////		}
//		
//		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
//		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
//		final int[] lastFroms = lastFrom >= 0 ? new int[]{-1, lastFrom} : new int[]{-1};
//		final int[] lastTos = lastTo >= 0 ? new int[]{-1, lastTo} : new int[]{-1};
//		
//		final ProactiveFeaturesKey key = new ProactiveFeaturesKey();
//		for (int k = 0; k < froms.length; ++k)
//		{
//			final int fromPos = froms[k];
//
//			for (int l = 0; l < tos.length; ++l)
//			{
//				final int toPos = tos[l];
//
//				if (toPos >= 0 || fromPos >= 0)
//				{
//					// Proactive instances
//					key.resetData(player, fromPos, toPos);
//					final SPatterNet set = thresholded ? jitMap.spatterNetThresholded(key, state) : jitMap.spatterNet(key, state);
//
//					if (set != null)
//						featureIndices.addAll(set.getActiveFeatures(state));
//				}
//			}
//		}
//
//		final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
//
//		for (int i = 0; i < lastFroms.length; ++i)
//		{
//			final int lastFromPos = lastFroms[i];
//
//			for (int j = 0; j < lastTos.length; ++j)
//			{
//				final int lastToPos = lastTos[j];
//				
//				if (lastToPos >= 0 || lastFromPos >= 0)
//				{
//					for (int k = 0; k < froms.length; ++k)
//					{
//						final int fromPos = froms[k];
//	
//						for (int l = 0; l < tos.length; ++l)
//						{
//							final int toPos = tos[l];
//	
//							if (toPos >= 0 || fromPos >= 0)
//							{
//								// Reactive instances
//								reactiveKey.resetData(player, lastFromPos, lastToPos, fromPos, toPos);
//								final SPatterNet set = jitMap.spatterNet(reactiveKey, state);
//	
//								if (set != null)
//									featureIndices.addAll(set.getActiveFeatures(state));
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		return featureIndices;
//	}
//
//	@Override
//	public List<FeatureInstance> getActiveSpatialFeatureInstances
//	(
//		final State state,
//		final int lastFrom,
//		final int lastTo,
//		final int from,
//		final int to,
//		final int player
//	)
//	{
//		final List<FeatureInstance> instances = new ArrayList<FeatureInstance>();
//
//		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
//		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
//		final int[] lastFroms = lastFrom >= 0 ? new int[]{-1, lastFrom} : new int[]{-1};
//		final int[] lastTos = lastTo >= 0 ? new int[]{-1, lastTo} : new int[]{-1};
//		
//		final ProactiveFeaturesKey key = new ProactiveFeaturesKey();
//		for (int k = 0; k < froms.length; ++k)
//		{
//			final int fromPos = froms[k];
//
//			for (int l = 0; l < tos.length; ++l)
//			{
//				final int toPos = tos[l];
//
//				if (toPos >= 0 || fromPos >= 0)
//				{
//					// Proactive instances
//					key.resetData(player, fromPos, toPos);
//								
//					final PropFeatureInstanceSet set = jitMap.propFeatureInstanceSet(key, state);
//					
//					if (set != null)
//						instances.addAll(set.getActiveInstances(state));
//				}
//			}
//		}
//		
//		final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
//
//		for (int i = 0; i < lastFroms.length; ++i)
//		{
//			final int lastFromPos = lastFroms[i];
//
//			for (int j = 0; j < lastTos.length; ++j)
//			{
//				final int lastToPos = lastTos[j];
//				
//				if (lastToPos >= 0 || lastFromPos >= 0)
//				{
//					for (int k = 0; k < froms.length; ++k)
//					{
//						final int fromPos = froms[k];
//	
//						for (int l = 0; l < tos.length; ++l)
//						{
//							final int toPos = tos[l];
//	
//							if (toPos >= 0 || fromPos >= 0)
//							{
//								// Reactive instances
//								reactiveKey.resetData(player, lastFromPos, lastToPos, fromPos, toPos);
//								final PropFeatureInstanceSet set = jitMap.propFeatureInstanceSet(reactiveKey, state);
//	
//								if (set != null)
//									instances.addAll(set.getActiveInstances(state));
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		return instances;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * Wrapper around a collection of maps from keys (reactive or proactive) to SPatterNets
//	 * or PropFeatureInstanceSets.
//	 *
//	 * @author Dennis Soemers
//	 */
//	private class JITMap
//	{
//		
//		/** Map to prop-feature-instance-set representation */
//		private final Map<MoveFeaturesKey, PropFeatureInstanceSet> propInstanceSetMap;
//		
//		/** Map to SPatterNet representation (without thresholding) */
//		private final Map<MoveFeaturesKey, SPatterNet> spatterNetMap;
//		
//		/**
//		 * Constructor
//		 */
//		public JITMap()
//		{
//			this.propInstanceSetMap = new ConcurrentHashMap<MoveFeaturesKey, PropFeatureInstanceSet>();
//			this.spatterNetMap = new ConcurrentHashMap<MoveFeaturesKey, SPatterNet>();
//		}
//		
//		/**
//		 * @param key
//		 * @param state
//		 * @return PropFeatureInstanceSet for given key
//		 */
//		public PropFeatureInstanceSet propFeatureInstanceSet(final MoveFeaturesKey key, final State state)
//		{
//			PropFeatureInstanceSet set = propInstanceSetMap.get(key);
//			
//			final boolean isKeyReactive = (key.lastFrom() >= 0 || key.lastTo() >= 0);
//			
//			if (set == null && !isKeyReactive)	// NOTE: we assume that proactive features are always computed before reactive ones
//			{
//				// JIT: instantiate net for this key
//				final BipartiteGraphFeatureInstanceSet proactiveBipartiteGraph = new BipartiteGraphFeatureInstanceSet();
//				final Map<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> reactiveGraphs = 
//						new HashMap<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet>();
//				
//				for (final SpatialFeature feature : JITSPatterNetFeatureSetDecisionTree.this.spatialFeatures())
//				{
//					final RelativeFeature relFeature = (RelativeFeature)feature;
//					
//					final List<FeatureInstance> newInstances = new ArrayList<FeatureInstance>();
//
//					if 
//					(
//						key.from() >= 0 
//						&& 
//						relFeature.fromPosition() != null 
//						&&
//						((key.to() >= 0) == (relFeature.toPosition() != null))
//					)
//					{
//						// Try instantiating with from as anchor
//						newInstances.addAll
//						(
//							feature.instantiateFeature
//							(
//								JITSPatterNetFeatureSetDecisionTree.this.gameRef().get(), 
//								state.containerStates()[0], 
//								state.mover(), 
//								key.from(), 
//								key.from(),
//								key.to(),
//								-1,
//								-1
//							)
//						);
//					}
//
//					if 
//					(
//						key.to() >= 0
//						&& 
//						relFeature.toPosition() != null
//						&&
//						((key.from() >= 0) == (relFeature.fromPosition() != null))
//					)
//					{
//						// Try instantiating with to as anchor
//						newInstances.addAll
//						(
//							feature.instantiateFeature
//							(
//								JITSPatterNetFeatureSetDecisionTree.this.gameRef().get(), 
//								state.containerStates()[0], 
//								state.mover(), 
//								key.to(), 
//								key.from(),
//								key.to(),
//								-1,
//								-1
//							)
//						);
//					}
//
//					if (feature.isReactive())
//					{
//						// Can have different instances for different reactive keys
//						final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
//						
//						for (final FeatureInstance instance : newInstances)
//						{
//							reactiveKey.resetData(key.playerIdx(), instance.lastFrom(), instance.lastTo(), key.from(), key.to());
//							BipartiteGraphFeatureInstanceSet bipartite = reactiveGraphs.get(reactiveKey);
//							
//							if (bipartite == null)
//							{
//								bipartite = new BipartiteGraphFeatureInstanceSet();
//								reactiveGraphs.put(new ReactiveFeaturesKey(reactiveKey), bipartite);
//							}
//							
//							bipartite.insertInstance(instance);
//						}
//					}
//					else
//					{
//						// Just collect them all in the bipartite graph for proactive features
//						for (final FeatureInstance instance : newInstances)
//						{
//							proactiveBipartiteGraph.insertInstance(instance);
//						}
//					}
//				}
//				
//				for (final Entry<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> entry : reactiveGraphs.entrySet())
//				{
//					propInstanceSetMap.put
//					(
//						entry.getKey(), 
//						entry.getValue().toPropFeatureInstanceSet()
//					);
//				}
//
//				set = proactiveBipartiteGraph.toPropFeatureInstanceSet();
//				propInstanceSetMap.put(new ProactiveFeaturesKey((ProactiveFeaturesKey)key), set);
//			}
//			
//			return set;
//		}
//		
//		/**
//		 * @param key
//		 * @param state
//		 * @return SPatterNet for given key
//		 */
//		public SPatterNet spatterNet(final MoveFeaturesKey key, final State state)
//		{
//			SPatterNet net = spatterNetMap.get(key);
//			
//			final boolean isKeyReactive = (key.lastFrom() >= 0 || key.lastTo() >= 0);
//			
//			if (net == null && !isKeyReactive)	// NOTE: we assume that proactive features are always computed before reactive ones
//			{
//				// JIT: instantiate net for this key
//				final BipartiteGraphFeatureInstanceSet proactiveBipartiteGraph = new BipartiteGraphFeatureInstanceSet();
//				final Map<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> reactiveGraphs = 
//						new HashMap<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet>();
//				
//				for (final SpatialFeature feature : JITSPatterNetFeatureSetDecisionTree.this.spatialFeatures())
//				{
//					final RelativeFeature relFeature = (RelativeFeature)feature;
//					
//					final List<FeatureInstance> newInstances = new ArrayList<FeatureInstance>();
//
//					if 
//					(
//						key.from() >= 0 
//						&& 
//						relFeature.fromPosition() != null 
//						&&
//						((key.to() >= 0) == (relFeature.toPosition() != null))
//					)
//					{
//						// Try instantiating with from as anchor
//						newInstances.addAll
//						(
//							feature.instantiateFeature
//							(
//								JITSPatterNetFeatureSetDecisionTree.this.gameRef().get(), 
//								state.containerStates()[0], 
//								state.mover(), 
//								key.from(), 
//								key.from(),
//								key.to(),
//								-1,
//								-1
//							)
//						);
//					}
//
//					if 
//					(
//						key.to() >= 0
//						&& 
//						relFeature.toPosition() != null
//						&&
//						((key.from() >= 0) == (relFeature.fromPosition() != null))
//					)
//					{
//						// Try instantiating with to as anchor
//						newInstances.addAll
//						(
//							feature.instantiateFeature
//							(
//								JITSPatterNetFeatureSetDecisionTree.this.gameRef().get(), 
//								state.containerStates()[0], 
//								state.mover(), 
//								key.to(), 
//								key.from(),
//								key.to(),
//								-1,
//								-1
//							)
//						);
//					}
//
//					if (feature.isReactive())
//					{
//						// Can have different instances for different reactive keys
//						final ReactiveFeaturesKey reactiveKey = new ReactiveFeaturesKey();
//						
//						for (final FeatureInstance instance : newInstances)
//						{
//							reactiveKey.resetData(key.playerIdx(), instance.lastFrom(), instance.lastTo(), key.from(), key.to());
//							BipartiteGraphFeatureInstanceSet bipartite = reactiveGraphs.get(reactiveKey);
//							
//							if (bipartite == null)
//							{
//								bipartite = new BipartiteGraphFeatureInstanceSet();
//								reactiveGraphs.put(new ReactiveFeaturesKey(reactiveKey), bipartite);
//							}
//							
//							bipartite.insertInstance(instance);
//						}
//					}
//					else
//					{
//						// Just collect them all in the bipartite graph for proactive features
//						for (final FeatureInstance instance : newInstances)
//						{
//							proactiveBipartiteGraph.insertInstance(instance);
//						}
//					}
//				}
//				
//				for (final Entry<ReactiveFeaturesKey, BipartiteGraphFeatureInstanceSet> entry : reactiveGraphs.entrySet())
//				{
//					spatterNetMap.put
//					(
//						entry.getKey(), 
//						entry.getValue().toSPatterNet(getNumSpatialFeatures(), new BitSet(), gameRef().get(), key.playerIdx())
//					);
//				}
//
//				net = proactiveBipartiteGraph.toSPatterNet(getNumSpatialFeatures(), new BitSet(), gameRef().get(), key.playerIdx());
//				spatterNetMap.put(new ProactiveFeaturesKey((ProactiveFeaturesKey)key), net);
//			}
//			
//			return net;
//		}
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * Wrapper around a list of aspatial features and a list of spatial features.
//	 * 
//	 * @author Dennis Soemers
//	 */
//	private static class FeatureLists
//	{
//		/** List of aspatial features */
//		protected final List<AspatialFeature> aspatialFeatures;
//		/** List of spatial features */
//		protected final List<SpatialFeature> spatialFeatures;
//		
//		/**
//		 * Constructor
//		 * @param aspatialFeatures
//		 * @param spatialFeatures
//		 */
//		public FeatureLists(final List<AspatialFeature> aspatialFeatures, final List<SpatialFeature> spatialFeatures)
//		{
//			this.aspatialFeatures = aspatialFeatures;
//			this.spatialFeatures = spatialFeatures;
//		}
//
//		@Override
//		public int hashCode() 
//		{
//			return Objects.hash(aspatialFeatures, spatialFeatures);
//		}
//
//		@Override
//		public boolean equals(final Object obj) 
//		{
//			if (this == obj)
//				return true;
//			if (!(obj instanceof FeatureLists))
//				return false;
//			final FeatureLists other = (FeatureLists) obj;
//			return Objects.equals(aspatialFeatures, other.aspatialFeatures)
//					&& Objects.equals(spatialFeatures, other.spatialFeatures);
//		}
//	}
//	
//	//-------------------------------------------------------------------------

}
