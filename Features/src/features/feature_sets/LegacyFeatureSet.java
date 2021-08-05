package features.feature_sets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import features.Feature;
import features.aspatial.AspatialFeature;
import features.spatial.SpatialFeature;
import features.spatial.Walk;
import features.spatial.cache.ActiveFeaturesCache;
import features.spatial.cache.footprints.BaseFootprint;
import features.spatial.cache.footprints.FullFootprint;
import features.spatial.instances.BitwiseTest;
import features.spatial.instances.FeatureInstance;
import features.spatial.instances.OneOfMustEmpty;
import features.spatial.instances.OneOfMustWhat;
import features.spatial.instances.OneOfMustWho;
import game.Game;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.collections.ChunkSet;
import main.collections.FVector;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * NOTE: legacy version, old implementation based on intuition, should be retired in favour
 * of the more principled PropSet implementation.
 * 
 * A collection of features which can be loaded/saved from/to files, can be instantiated for
 * any game, and has consistent indices per feature (which means it can be used in a consistent
 * manner in combination with a file of learned weights/parameters for example)
 * 
 * @author Dennis Soemers
 */
public class LegacyFeatureSet extends BaseFeatureSet 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reactive instances, indexed by:
	 * 	player index,
	 * 	last-from-pos
	 * 	last-to-pos,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a forest
	 * containing all relevant instances.
	 */
	protected HashMap<ReactiveFeaturesKey, FastFeatureInstanceNode[]> reactiveInstances;
	
	/**
	 * Proactive instances, indexed by:
	 * 	player index,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a forest
	 * containing all relevant instances
	 */
	protected HashMap<ProactiveFeaturesKey, FastFeatureInstanceNode[]> proactiveInstances;
	
	/**
	 * Reactive Features, indexed by:
	 * 	player index,
	 * 	last-from-pos
	 * 	last-to-pos,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a forest
	 * containing all relevant Features.
	 */
	protected HashMap<ReactiveFeaturesKey, FastFeaturesNode[]> reactiveFeatures;
	
	/**
	 * Proactive Features, indexed by:
	 * 	player index,
	 * 	from-pos
	 * 	to-pos
	 * 
	 * When indexed according to all of the above, we're left with a forest
	 * containing all relevant Features.
	 */
	protected HashMap<ProactiveFeaturesKey, FastFeaturesNode[]> proactiveFeatures;
	
	/**
	 * Same as above, but only includes features with absolute weights that exceed the above
	 * threshold.
	 */
	protected HashMap<ReactiveFeaturesKey, FastFeaturesNode[]> reactiveFeaturesThresholded;
	
	/**
	 * Same as above, but only includes features with absolute weights that exceed the above
	 * threshold.
	 */
	protected HashMap<ProactiveFeaturesKey, FastFeaturesNode[]> proactiveFeaturesThresholded;
	
	/** Cache with indices of active proactive features previously computed */
	protected ActiveFeaturesCache activeProactiveFeaturesCache;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Construct feature set from lists of features
	 * @param aspatialFeatures
	 * @param spatialFeatures
	 */
	public LegacyFeatureSet(final List<AspatialFeature> aspatialFeatures, final List<SpatialFeature> spatialFeatures)
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
	public LegacyFeatureSet(final String filename)
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
		
		// First we create ''WIP'' versions of these maps, with the slower
		// node class (necessary when the structure of the tree may still
		// change)
		Map<ReactiveFeaturesKey, List<FeatureInstanceNode>> reactiveInstancesWIP = 
				new HashMap<ReactiveFeaturesKey, List<FeatureInstanceNode>>();
		Map<ProactiveFeaturesKey, List<FeatureInstanceNode>> proactiveInstancesWIP = 
				new HashMap<ProactiveFeaturesKey, List<FeatureInstanceNode>>();
		
		// Create a dummy context because we need some context for 
		// feature generation
		final Context featureGenContext = new Context(game.get(), new Trial(game.get()));
		
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
					
					if (lastFrom >= 0 || lastTo >= 0)	// reactive feature
					{
						reactiveKey.resetData(player, lastFrom, lastTo, from, to);
						List<FeatureInstanceNode> instanceNodes = reactiveInstancesWIP.get(reactiveKey);
						
						if (instanceNodes == null)
						{
							instanceNodes = new ArrayList<FeatureInstanceNode>(1);
							reactiveInstancesWIP.put(new ReactiveFeaturesKey(reactiveKey), instanceNodes);
						}
						
						insertInstanceInForest(instance, instanceNodes);
					}
					else	// proactive feature
					{
						proactiveKey.resetData(player, from, to);
						List<FeatureInstanceNode> instanceNodes = proactiveInstancesWIP.get(proactiveKey);
						
						if (instanceNodes == null)
						{
							instanceNodes = new ArrayList<FeatureInstanceNode>(1);
							proactiveInstancesWIP.put(new ProactiveFeaturesKey(proactiveKey), instanceNodes);
						}
						
						insertInstanceInForest(instance, instanceNodes);
					}
				}
			}
		}
		
		// simplify all our WIP forests
		simplifyInstanceForests(reactiveInstancesWIP, proactiveInstancesWIP);
		
		// now convert all our forests to variants with the more efficient
		// node class, and store them permanently
		reactiveInstances = 
				new HashMap<ReactiveFeaturesKey, FastFeatureInstanceNode[]>(
						(int) Math.ceil(reactiveInstancesWIP.size() / 0.75f), 
						0.75f);
		
		for 
		(
			final Entry<ReactiveFeaturesKey, List<FeatureInstanceNode>> entry : 
			reactiveInstancesWIP.entrySet()
		)
		{
			final FastFeatureInstanceNode[] roots = new FastFeatureInstanceNode[entry.getValue().size()];
			
			for (int i = 0; i < roots.length; ++i)
			{
				roots[i] = new FastFeatureInstanceNode(entry.getValue().get(i));
			}
			
			reactiveInstances.put(entry.getKey(), roots);
		}
		
		proactiveInstances = 
				new HashMap<ProactiveFeaturesKey, FastFeatureInstanceNode[]>
				(
					(int) Math.ceil(proactiveInstancesWIP.size() / 0.75f), 0.75f
				);
							
		for 
		(
			final Entry<ProactiveFeaturesKey, List<FeatureInstanceNode>> entry : 
			proactiveInstancesWIP.entrySet()
		)
		{
			final FastFeatureInstanceNode[] roots = new FastFeatureInstanceNode[entry.getValue().size()];

			for (int i = 0; i < roots.length; ++i)
			{
				roots[i] = new FastFeatureInstanceNode(entry.getValue().get(i));
			}

			proactiveInstances.put(entry.getKey(), roots);
		}
		
		// and also create the even more efficient forests for cases where
		// we only need to distinguish between features, rather than feature
		// instances
		reactiveFeatures = 
				new HashMap<ReactiveFeaturesKey, FastFeaturesNode[]>
				(
					(int) Math.ceil(reactiveInstances.size() / 0.75f), 0.75f
				);
		
		for 
		(
			final Entry<ReactiveFeaturesKey, FastFeatureInstanceNode[]> entry : 
			reactiveInstances.entrySet()
		)
		{
			final FastFeaturesNode[] roots = new FastFeaturesNode[entry.getValue().length];
			
			for (int i = 0; i < roots.length; ++i)
			{
				roots[i] = new FastFeaturesNode(entry.getValue()[i]);
			}
			
			reactiveFeatures.put(entry.getKey(), roots);
		}
		
		proactiveFeatures = 
				new HashMap<ProactiveFeaturesKey, FastFeaturesNode[]>
				(
					(int) Math.ceil(proactiveInstances.size() / 0.75f), 0.75f
				);
				
		for 
		(
			final Entry<ProactiveFeaturesKey, FastFeatureInstanceNode[]> entry : 
			proactiveInstances.entrySet()
		)
		{
			final FastFeaturesNode[] roots = new FastFeaturesNode[entry.getValue().length];

			for (int i = 0; i < roots.length; ++i)
			{
				roots[i] = new FastFeaturesNode(entry.getValue()[i]);
			}

			proactiveFeatures.put(entry.getKey(), roots);
		}
		
//		System.out.println("---");
//		proactiveFeatures.get(new ProactiveFeaturesKey(1, -1, 0))[0].print(0);
//		System.out.println("---");
		
		// finally, even more optimised forests where we remove nodes that
		// are irrelevant due to low feature weights
		reactiveFeaturesThresholded = 
				new HashMap<ReactiveFeaturesKey, FastFeaturesNode[]>
				(
					(int) Math.ceil(reactiveFeatures.size() / 0.75f), 0.75f
				);
		
		for 
		(
			final Entry<ReactiveFeaturesKey, FastFeaturesNode[]> entry : 
			reactiveFeatures.entrySet()
		)
		{
			final List<FastFeaturesNode> roots = new ArrayList<FastFeaturesNode>(entry.getValue().length);
			
			for (final FastFeaturesNode node : entry.getValue())
			{
				final FastFeaturesNode optimisedNode = FastFeaturesNode.thresholdedNode(node, spatialFeatureInitWeights);
				
				if (optimisedNode != null)
					roots.add(optimisedNode);
			}
			
			reactiveFeaturesThresholded.put(entry.getKey(), roots.toArray(new FastFeaturesNode[0]));
		}
		
		proactiveFeaturesThresholded = 
				new HashMap<ProactiveFeaturesKey, FastFeaturesNode[]>
				(
					(int) Math.ceil(proactiveFeatures.size() / 0.75f), 0.75f
				);
				
		for 
		(
			final Entry<ProactiveFeaturesKey, FastFeaturesNode[]> entry : 
			proactiveFeatures.entrySet()
		)
		{
			final List<FastFeaturesNode> roots = new ArrayList<FastFeaturesNode>(entry.getValue().length);

			for (final FastFeaturesNode node : entry.getValue())
			{
				final FastFeaturesNode optimisedNode = FastFeaturesNode.thresholdedNode(node, spatialFeatureInitWeights);
				
				if (optimisedNode != null)
					roots.add(optimisedNode);
			}

			proactiveFeaturesThresholded.put(entry.getKey(), roots.toArray(new FastFeaturesNode[0]));
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
		// a lot of code duplication with getActiveFeatureInstances() here, 
		// but more efficient		
		final boolean[] featuresActive = new boolean[spatialFeatures.length];
		
		// try to get proactive features immediately from cache
		final TIntArrayList activeFeatureIndices;
		
		if (proactiveFeatures.size() > 0)
		{
			final int[] cachedActiveFeatureIndices;
			
			if (thresholded)
				cachedActiveFeatureIndices = activeProactiveFeaturesCache.getCachedActiveFeatures(this, state, from, to, player);
			else
				cachedActiveFeatureIndices = null;
		
			if (cachedActiveFeatureIndices != null)
			{
				// successfully retrieved from cache
				activeFeatureIndices = new TIntArrayList(cachedActiveFeatureIndices);
				//System.out.println("cache hit!");
			}
			else
			{
				// did not retrieve from cache, so need to compute the proactive features first
				activeFeatureIndices = new TIntArrayList();
				
				//System.out.println("cache miss!");
				final List<FastFeaturesNode[]> featuresNodesToCheck = 
						getFeaturesNodesToCheckProactive(state, from, to, thresholded);
				
				for (int i = 0; i < featuresNodesToCheck.size(); ++i)
				{
					final FastFeaturesNode[] nodesArray = featuresNodesToCheck.get(i);
					
					for (int j = 0; j < nodesArray.length; ++j)
					{
						final FastFeaturesNode node = nodesArray[j];
						final BitwiseTest test = node.test;
	
						if (test.matches(state))
						{
							final int[] featureIndices = node.activeFeatureIndices;
							
							for (int idx = 0; idx < featureIndices.length; ++idx)
							{
								featuresActive[featureIndices[idx]] = true;
							}
	
							// will also have to test all the children of our 
							// current node
							featuresNodesToCheck.add(node.children);
						}
					}
				}
				
				// we'll want to cache these results as well
				for (int i = 0; i < featuresActive.length; ++i)
				{
					if (featuresActive[i])
					{
						activeFeatureIndices.add(i);
					}
				}
				
				if (thresholded)
					activeProactiveFeaturesCache.cache(state, from, to, activeFeatureIndices.toArray(), player);
				
				// clear the featuresActive bools, we've already 
				// added these to the activeFeatureIndices list
				Arrays.fill(featuresActive, false);
			}
		}
		else
		{
			activeFeatureIndices = new TIntArrayList();
		}
		
		// now still need to compute the reactive features, which aren't cached
		final List<FastFeaturesNode[]> featuresNodesToCheck = 
				getFeaturesNodesToCheckReactive(state, lastFrom, lastTo, from, to, thresholded);

		for (int i = 0; i < featuresNodesToCheck.size(); ++i)
		{
			final FastFeaturesNode[] nodesArray = featuresNodesToCheck.get(i);
			
			for (int j = 0; j < nodesArray.length; ++j)
			{
				final FastFeaturesNode node = nodesArray[j];
				final BitwiseTest test = node.test;

				if (test.matches(state))
				{
					final int[] featureIndices = node.activeFeatureIndices;
					
					for (int idx = 0; idx < featureIndices.length; ++idx)
					{
						featuresActive[featureIndices[idx]] = true;
					}

					// will also have to test all the children of our 
					// current node
					featuresNodesToCheck.add(node.children);
				}
			}
		}
		
		for (int i = 0; i < featuresActive.length; ++i)
		{
			if (featuresActive[i])
			{
				activeFeatureIndices.add(i);
			}
		}
		
		return activeFeatureIndices;
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
		final List<FeatureInstance> activeInstances = new ArrayList<FeatureInstance>();
		
		final List<FastFeatureInstanceNode[]> instanceNodesToCheck = 
				getInstanceNodesToCheck(state, lastFrom, lastTo, from, to, player);
		
		for (int i = 0; i < instanceNodesToCheck.size(); ++i)
		{
			final FastFeatureInstanceNode[] nodesArray = instanceNodesToCheck.get(i);
			
			for (int j = 0; j < nodesArray.length; ++j)
			{
				final FeatureInstance instance = nodesArray[j].featureInstance;
								
				if (instance.matches(state))
				{
					activeInstances.add(instance);
					
					// will also have to test all the children of our 
					// current node
					instanceNodesToCheck.add(nodesArray[j].children);
				}
			}
		}
		
//		if (activeInstances.size() == 0)
//		{
//			System.out.println("lastFrom = " + lastFrom);
//			System.out.println("lastTo = " + lastTo);
//			System.out.println("from = " + from);
//			System.out.println("to = " + to);
//			System.out.println("player = " + player);
//			System.out.println("instanceNodesToCheck.size() = " + instanceNodesToCheck.size());
//			System.out.println("returning num active instances = " + activeInstances.size());
//		}
		
		return activeInstances;
	}
	
	/**
	 * @param context 
	 * @param lastFrom
	 * @param lastTo
	 * @param from
	 * @param to
	 * @param player
	 * @param thresholded
	 * 
	 * @return A list of all features that are active for a given state+action 
	 * pair (where action is defined by from and to positions)
	 */
	public List<SpatialFeature> getActiveFeatures
	(
		final Context context, 
		final int lastFrom, 
		final int lastTo, 
		final int from, 
		final int to,
		final int player,
		final boolean thresholded
	)
	{
		final TIntArrayList activeFeatureIndices = 
				getActiveSpatialFeatureIndices(context.state(), lastFrom, lastTo, from, to, player, thresholded);
		final List<SpatialFeature> activeFeatures = new ArrayList<SpatialFeature>(activeFeatureIndices.size());
		
		final TIntIterator it = activeFeatureIndices.iterator();
		while (it.hasNext())
		{
			activeFeatures.add(spatialFeatures[it.next()]);
		}
		
		return activeFeatures;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method to collect a list of all instance nodes to start with
	 * when checking which instances match a given state+action pair
	 * 
	 * We return a list of arrays rather than a flat list due to efficiency
	 * (lets us work much more with raw arrays rather than a large and
	 * frequently-growing ArrayList)
	 * 
	 * @param state
	 * @param lastFrom
	 * @param lastTo
	 * @param from
	 * @param to
	 * @param player
	 * @return List of arrays of root nodes to start checking
	 */
	private List<FastFeatureInstanceNode[]> getInstanceNodesToCheck
	(
		final State state, 
		final int lastFrom, 
		final int lastTo, 
		final int from, 
		final int to,
		final int player
	)
	{
		final List<FastFeatureInstanceNode[]> instanceNodesToCheck = 
				new ArrayList<FastFeatureInstanceNode[]>();
				
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
								
								final FastFeatureInstanceNode[] nodes = reactiveInstances.get(reactiveKey);
								
								if (nodes != null)
								{
									instanceNodesToCheck.add(nodes);
								}
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
					final FastFeatureInstanceNode[] nodes = proactiveInstances.get(proactiveKey);

					if (nodes != null)
					{
						instanceNodesToCheck.add(nodes);
					}
				}
			}
		}
		
		return instanceNodesToCheck;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method to collect a list of all Features nodes to start with
	 * when checking which proactive Features match a given state+action pair
	 * 
	 * We return a list of arrays rather than a flat list due to efficiency
	 * (lets us work much more with raw arrays rather than a large and
	 * frequently-growing ArrayList)
	 * 
	 * @param state
	 * @param from
	 * @param to
	 * @param thresholded
	 * 
	 * @return List of arrays of root nodes to start checking
	 */
	private List<FastFeaturesNode[]> getFeaturesNodesToCheckProactive
	(
		final State state, 
		final int from, 
		final int to,
		final boolean thresholded
	)
	{
		final List<FastFeaturesNode[]> featuresNodesToCheck = new ArrayList<FastFeaturesNode[]>();
		final int mover = state.mover();
				
		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
		
		final HashMap<ProactiveFeaturesKey, FastFeaturesNode[]> featuresMap;
		if (thresholded)
			featuresMap = proactiveFeaturesThresholded;
		else
			featuresMap = proactiveFeatures;
		
		final ProactiveFeaturesKey key = new ProactiveFeaturesKey();
		for (int k = 0; k < froms.length; ++k)
		{
			final int fromPos = froms[k];
			
			for (int l = 0; l < tos.length; ++l)
			{
				final int toPos = tos[l];

				if (toPos >= 0 || fromPos >= 0)
				{
					key.resetData(mover, fromPos, toPos);
					final FastFeaturesNode[] nodes = featuresMap.get(key);
					
					if (nodes != null)
					{
						featuresNodesToCheck.add(nodes);
					}
				}
			}
		}
		
		return featuresNodesToCheck;
	}
	
	/**
	 * Helper method to collect a list of all Features nodes to start with
	 * when checking which reactive Features match a given state+action pair
	 * 
	 * We return a list of arrays rather than a flat list due to efficiency
	 * (lets us work much more with raw arrays rather than a large and
	 * frequently-growing ArrayList)
	 * 
	 * @param state
	 * @param lastFrom
	 * @param lastTo
	 * @param from
	 * @param to
	 * @param thresholded
	 * 
	 * @return List of arrays of root nodes to start checking
	 */
	private List<FastFeaturesNode[]> getFeaturesNodesToCheckReactive
	(
		final State state, 
		final int lastFrom, 
		final int lastTo, 
		final int from, 
		final int to,
		final boolean thresholded
	)
	{		
		final List<FastFeaturesNode[]> featuresNodesToCheck = new ArrayList<FastFeaturesNode[]>();
		
		if (reactiveFeatures.isEmpty())
			return featuresNodesToCheck;
		
		final HashMap<ReactiveFeaturesKey, FastFeaturesNode[]> featuresMap;
		if (thresholded)
			featuresMap = reactiveFeaturesThresholded;
		else
			featuresMap = reactiveFeatures;
		
		final int mover = state.mover();
		
		if (from >= 0)
		{
			if (to >= 0)
			{
				if (lastFrom >= 0)
				{
					if (lastTo >= 0)
					{
						addFeaturesNodes(mover, lastFrom, lastTo, from, to, featuresMap, featuresNodesToCheck);
						addFeaturesNodes(mover, lastFrom, lastTo, -1, to, featuresMap, featuresNodesToCheck);
						addFeaturesNodes(mover, lastFrom, lastTo, from, -1, featuresMap, featuresNodesToCheck);
						
						addFeaturesNodes(mover, -1, lastTo, from, to, featuresMap, featuresNodesToCheck);
						addFeaturesNodes(mover, -1, lastTo, -1, to, featuresMap, featuresNodesToCheck);
						addFeaturesNodes(mover, -1, lastTo, from, -1, featuresMap, featuresNodesToCheck);
					}
					
					addFeaturesNodes(mover, lastFrom, -1, from, to, featuresMap, featuresNodesToCheck);
					addFeaturesNodes(mover, lastFrom, -1, -1, to, featuresMap, featuresNodesToCheck);
					addFeaturesNodes(mover, lastFrom, -1, from, -1, featuresMap, featuresNodesToCheck);
				}
				else
				{
					if (lastTo >= 0)
					{
						addFeaturesNodes(mover, -1, lastTo, from, to, featuresMap, featuresNodesToCheck);
						addFeaturesNodes(mover, -1, lastTo, -1, to, featuresMap, featuresNodesToCheck);
						addFeaturesNodes(mover, -1, lastTo, from, -1, featuresMap, featuresNodesToCheck);
					}
				}
			}
			else
			{
				if (lastFrom >= 0)
				{
					if (lastTo >= 0)
					{
						addFeaturesNodes(mover, lastFrom, lastTo, from, -1, featuresMap, featuresNodesToCheck);
						addFeaturesNodes(mover, -1, lastTo, from, -1, featuresMap, featuresNodesToCheck);
					}
					
					addFeaturesNodes(mover, lastFrom, -1, from, -1, featuresMap, featuresNodesToCheck);
				}
				else
				{
					if (lastTo >= 0)
						addFeaturesNodes(mover, -1, lastTo, from, -1, featuresMap, featuresNodesToCheck);
				}
			}
		}
		else
		{
			if (to >= 0)
			{
				if (lastFrom >= 0)
				{
					if (lastTo >= 0)
					{
						addFeaturesNodes(mover, lastFrom, lastTo, -1, to, featuresMap, featuresNodesToCheck);
						
						addFeaturesNodes(mover, -1, lastTo, -1, to, featuresMap, featuresNodesToCheck);
					}
					
					addFeaturesNodes(mover, lastFrom, -1, -1, to, featuresMap, featuresNodesToCheck);
				}
				else
				{
					if (lastTo >= 0)
						addFeaturesNodes(mover, -1, lastTo, -1, to, featuresMap, featuresNodesToCheck);
				}
			}
		}
		
		return featuresNodesToCheck;
	}
	
	/**
	 * Helper method for collecting feature instances
	 * 
	 * @param mover
	 * @param lastFrom
	 * @param lastTo
	 * @param from
	 * @param to
	 * @param featuresMap
	 * @param outFeaturesNodes
	 */
	private static void addFeaturesNodes
	(
		final int mover,
		final int lastFrom,
		final int lastTo,
		final int from,
		final int to,
		final HashMap<ReactiveFeaturesKey, FastFeaturesNode[]> featuresMap,
		final List<FastFeaturesNode[]> outFeaturesNodes
	)
	{
		final ReactiveFeaturesKey key = new ReactiveFeaturesKey();
		key.resetData(mover, lastFrom, lastTo, from, to);
		final FastFeaturesNode[] nodes = featuresMap.get(key);

		if (nodes != null)
			outFeaturesNodes.add(nodes);
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
		
		final ChunkSet footprintEmptyCells = 
				container.emptyChunkSetCell() != null ?
				new ChunkSet(container.emptyChunkSetCell().chunkSize(), 1) :
				null;
		final ChunkSet footprintEmptyVertices = 
				container.emptyChunkSetVertex() != null ?
				new ChunkSet(container.emptyChunkSetVertex().chunkSize(), 1) :
				null;
		final ChunkSet footprintEmptyEdges = 
				container.emptyChunkSetEdge() != null ?
				new ChunkSet(container.emptyChunkSetEdge().chunkSize(), 1) :
				null;
				
		final ChunkSet footprintWhoCells = 
				container.chunkSizeWhoCell() > 0 ? 
				new ChunkSet(container.chunkSizeWhoCell(), 1) : 
				null;
		final ChunkSet footprintWhoVertices = 
				container.chunkSizeWhoVertex() > 0 ? 
				new ChunkSet(container.chunkSizeWhoVertex(), 1) : 
				null;
		final ChunkSet footprintWhoEdges = 
				container.chunkSizeWhoEdge() > 0 ? 
				new ChunkSet(container.chunkSizeWhoEdge(), 1) : 
				null;
				
		final ChunkSet footprintWhatCells = 
				container.chunkSizeWhatCell() > 0 ?
				new ChunkSet(container.chunkSizeWhatCell(), 1) :
				null;
		final ChunkSet footprintWhatVertices = 
				container.chunkSizeWhatVertex() > 0 ?
				new ChunkSet(container.chunkSizeWhatVertex(), 1) :
				null;
		final ChunkSet footprintWhatEdges = 
				container.chunkSizeWhatEdge() > 0 ?
				new ChunkSet(container.chunkSizeWhatEdge(), 1) :
				null;
		
		// the two -1s ensure we only get proactive feature instances here
		final List<FastFeatureInstanceNode[]> instanceNodes = 
				getInstanceNodesToCheck(state, -1, -1, from, to, player);
		
		// loop through complete tree of instances, OR all the tests
		for (int i = 0; i < instanceNodes.size(); ++i)
		{
			final FastFeatureInstanceNode[] nodesArray = instanceNodes.get(i);
			
			for (int j = 0; j < nodesArray.length; ++j)
			{
				final FeatureInstance instance = nodesArray[j].featureInstance;
				
				if (instance.mustEmpty() != null)
				{
					switch (instance.graphElementType())
					{
					case Cell:
						footprintEmptyCells.or(instance.mustEmpty());
						break;
					case Vertex:
						footprintEmptyVertices.or(instance.mustEmpty());
						break;
					case Edge:
						footprintEmptyEdges.or(instance.mustEmpty());
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				if (instance.mustNotEmpty() != null)
				{
					switch (instance.graphElementType())
					{
					case Cell:
						footprintEmptyCells.or(instance.mustNotEmpty());
						break;
					case Vertex:
						footprintEmptyVertices.or(instance.mustNotEmpty());
						break;
					case Edge:
						footprintEmptyEdges.or(instance.mustNotEmpty());
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				if (instance.mustWhoMask() != null)
				{
					switch (instance.graphElementType())
					{
					case Cell:
						footprintWhoCells.or(instance.mustWhoMask());
						break;
					case Vertex:
						footprintWhoVertices.or(instance.mustWhoMask());
						break;
					case Edge:
						footprintWhoEdges.or(instance.mustWhoMask());
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				if (instance.mustNotWhoMask() != null)
				{
					switch (instance.graphElementType())
					{
					case Cell:
						footprintWhoCells.or(instance.mustNotWhoMask());
						break;
					case Vertex:
						footprintWhoVertices.or(instance.mustNotWhoMask());
						break;
					case Edge:
						footprintWhoEdges.or(instance.mustNotWhoMask());
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				if (instance.mustWhatMask() != null)
				{
					switch (instance.graphElementType())
					{
					case Cell:
						footprintWhatCells.or(instance.mustWhatMask());
						break;
					case Vertex:
						footprintWhatVertices.or(instance.mustWhatMask());
						break;
					case Edge:
						footprintWhatEdges.or(instance.mustWhatMask());
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				if (instance.mustNotWhatMask() != null)
				{
					switch (instance.graphElementType())
					{
					case Cell:
						footprintWhatCells.or(instance.mustNotWhatMask());
						break;
					case Vertex:
						footprintWhatVertices.or(instance.mustNotWhatMask());
						break;
					case Edge:
						footprintWhatEdges.or(instance.mustNotWhatMask());
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				// will also have to test all the children of our current node
				instanceNodes.add(nodesArray[j].children);
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
	
	/**
	 * Attempts to create an expanded feature set which contains all of the current features,
	 * plus one new feature by combining a pair of features from the given list of active instances 
	 * into a new feature. Will return null if no such pair can be found such that it results in a 
	 * really new feature. We only allow pairs of instances that have a shared anchor.
	 * 
	 * @param activeFeatureInstances
	 * @param combineMaxWeightedFeatures
	 * 	If true, we will prioritise creating new pairs of features such that at least one
	 * 	of them has the highest possible absolute weight in our linear function approximator
	 * 	(i.e. we try to extend features that are already highly informative with additional
	 * 	elements). If false, we'll select pairs of features randomly.
	 * @param featureWeights
	 * @return New Feature Set with one extra feature, or null if cannot be expanded
	 */
	public LegacyFeatureSet createExpandedFeatureSet
	(
		final List<FeatureInstance> activeFeatureInstances,
		final boolean combineMaxWeightedFeatures,
		final FVector featureWeights
	)
	{
		// generate all possible pairs of two different features (order does not matter)
		final int numActiveInstances = activeFeatureInstances.size();
		final List<FeatureInstancePair> allPairs = new ArrayList<FeatureInstancePair>();
		
		for (int i = 0; i < numActiveInstances; ++i)
		{
			final FeatureInstance firstInstance = activeFeatureInstances.get(i);
			
			for (int j = i + 1; j < numActiveInstances; ++j)
			{
				final FeatureInstance secondInstance = activeFeatureInstances.get(j);
				
				if (firstInstance.anchorSite() == secondInstance.anchorSite())
				{
					allPairs.add(new FeatureInstancePair(firstInstance, secondInstance));
				}
			}
		}
		
		if (combineMaxWeightedFeatures)
		{
			// sort feature pairs in increasing order of max(abs(weight(a), abs(weight(b))))
			final FVector absWeights = featureWeights.copy();
			absWeights.abs();
			
			allPairs.sort(new Comparator<FeatureInstancePair>() {

				@Override
				public int compare(FeatureInstancePair o1, FeatureInstancePair o2) {
					final float score1 = Math.max(
							absWeights.get(o1.a.feature().spatialFeatureSetIndex()), 
							absWeights.get(o1.b.feature().spatialFeatureSetIndex()));
					
					final float score2 = Math.max(
							absWeights.get(o2.a.feature().spatialFeatureSetIndex()), 
							absWeights.get(o2.b.feature().spatialFeatureSetIndex()));
					
					if (score1 == score2)
					{
						return 0;
					}
					else if (score1 < score2)
					{
						return -1;
					}
					else
					{
						return 1;
					}
				}
				
			});
		}
		else
		{
			// just shuffle them for random combining
			Collections.shuffle(allPairs);
		}
		
		// keep trying to combine pairs of features, until we find a new one or
		// until we have tried them all
		while (!allPairs.isEmpty())
		{
			final FeatureInstancePair pair = allPairs.remove(allPairs.size() - 1);
			final LegacyFeatureSet newFeatureSet = createExpandedFeatureSet
					(
						game.get(), 
						SpatialFeature.combineFeatures
						(
							game.get(), 
							pair.a, 
							pair.b
						)
					);
			
			if (newFeatureSet != null)
			{
				return newFeatureSet;
			}
		}
		
		// failed to construct a new feature set with a new feature
		return null;
	}
	
	@Override
	public LegacyFeatureSet createExpandedFeatureSet
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
				allowedRotations = Walk.allGameRotations(game.get());
			}
			
			for (int i = 0; i < allowedRotations.size(); ++i)
			{
				final SpatialFeature rotatedCopy = newFeature.rotatedCopy(allowedRotations.getQuick(i));
				
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
			
			return new LegacyFeatureSet(Arrays.asList(aspatialFeatures), newFeatureList);
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * We'll simplify all of our forests of Feature Instances by:
	 * 	1) for every Feature Instance, removing all tests already included
	 *	in ancestor nodes
	 *	2) collapsing deep sequences of instances that have no remaining tests
	 *	after the above simplification into a wide list of children for the
	 *	first ancestor that still has meaningful tests.
	 *
	 * @param reactiveInstancesWIP 
	 * @param proactiveInstancesWIP 
	 */
	private static void simplifyInstanceForests
	(
		final Map<ReactiveFeaturesKey, List<FeatureInstanceNode>> reactiveInstancesWIP,
		final Map<ProactiveFeaturesKey, List<FeatureInstanceNode>> proactiveInstancesWIP
	)
	{
		final List<List<FeatureInstanceNode>> allForests = 
				new ArrayList<List<FeatureInstanceNode>>(2);
		
		allForests.addAll(proactiveInstancesWIP.values());
		allForests.addAll(reactiveInstancesWIP.values());
		
//		System.out.println("---");
//		proactiveInstancesWIP.get(new ProactiveFeaturesKey(1, 3, 2)).get(0).print(0);
//		System.out.println("---");
		
//		System.out.println("---");
//		reactiveInstancesWIP.get(new ReactiveFeaturesKey(1, -1, 45, 3, 0)).get(0).print(0);
//		System.out.println("---");
		
		// for every forest...
		for (final List<FeatureInstanceNode> forest : allForests)
		{
			// for every tree...
			for (final FeatureInstanceNode root : forest)
			{
				// traverse entire tree; for every node, remove all tests
				// from all descendants of that node
				final List<FeatureInstanceNode> rootsToProcess = 
						new ArrayList<FeatureInstanceNode>();
				rootsToProcess.add(root);
				
				while (!rootsToProcess.isEmpty())
				{
					final FeatureInstanceNode rootToProcess = rootsToProcess.remove(0);
					
					if (!rootToProcess.featureInstance.hasNoTests())
					{
						//System.out.println("removing " + rootToProcess.featureInstance + " tests from all descendants...");
						
						final List<FeatureInstanceNode> descendants = rootToProcess.collectDescendants();
						
						for (final FeatureInstanceNode descendant : descendants)
						{
							//System.out.println("before removal: " + descendant.featureInstance);
							descendant.featureInstance.removeTests(rootToProcess.featureInstance);
							//System.out.println("after removal: " + descendant.featureInstance);
						}
					}
					
					// all children of current ''root'' become new ''roots''
					rootsToProcess.addAll(rootToProcess.children);
				}
				
				// and now collapse long sequences of no-test-instances
				final List<FeatureInstanceNode> allNodes = root.collectDescendants();
				
				for (final FeatureInstanceNode node : allNodes)
				{
					FeatureInstanceNode ancestor = node.parent;

					// find first ancestor with meaningful tests
					while (ancestor.featureInstance.hasNoTests())
					{
						if (ancestor == root)
						{
							// can't go further up
							break;
						}
						else
						{
							//System.out.println(node.featureInstance + " skipping ancestor: " + ancestor.featureInstance);
							ancestor = ancestor.parent;
						}
					}

					if (ancestor != node.parent)
					{
						// need to change parent
						//System.out.println(node.featureInstance + " swapping parent to " + ancestor.featureInstance);

						// first remove ourselves from the current parent's
						// list of children
						node.parent.children.remove(node);

						// now add ourselves to the new ancestor's list
						// of children
						ancestor.children.add(node);

						// and change our parent reference
						node.parent = ancestor;
					}
				}
			}
		}
		
//		System.out.println("---");
//		proactiveInstancesWIP.get(new ProactiveFeaturesKey(1, 3, 0)).get(0).print(0);
//		System.out.println("---");
		
//		System.out.println("---");
//		reactiveInstancesWIP.get(new ReactiveFeaturesKey(1, -1, 45, 3, 0)).get(0).print(0);
//		System.out.println("---");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Inserts the given Feature Instance into a forest defined by a list
	 * of root nodes. May modify existing instances if the new instance
	 * becomes an ancestor of them.
	 * 
	 * @param instance
	 * @param instanceNodes
	 */
	private static void insertInstanceInForest
	(
		final FeatureInstance instance, 
		final List<FeatureInstanceNode> instanceNodes
	)
	{
		final FeatureInstanceNode parentNode = findDeepestParent(instance, instanceNodes);
		
		if (parentNode == null)
		{
			// need to add a new root for this feature instance
			instanceNodes.add(new FeatureInstanceNode(instance, null));
		}
		else
		{
			// this instance will become a new child of parentNode
			final FeatureInstanceNode newNode = new FeatureInstanceNode(instance, parentNode);
			
			// see if our new instance generalises any instances that were
			// previously children of the parent we found; if so, our new
			// node becomes their parent instead
			for (int i = 0; i < parentNode.children.size(); /**/)
			{
				final FeatureInstanceNode child = parentNode.children.get(i);
				
				if (instance.generalises(child.featureInstance))
				{
					// we generalise this child and should become its parent
					parentNode.children.remove(i);
					newNode.children.add(child);
					child.parent = newNode;
				}
				else
				{
					// didn't remove entry from list, so increment index
					++i;
				}
			}
			
			// finally, add the new node to the parent's list of children
			parentNode.children.add(newNode);
			parentNode.children.trimToSize();
		}
	}
	
	/**
	 * Finds the deepest node in the forest defined by the given list of
	 * root nodes which would be a valid parent for the given new instance.
	 * 
	 * @param instance
	 * @param instanceNodes
	 * @return Deepest parent node for given new instance
	 */
	private static FeatureInstanceNode findDeepestParent
	(
		final FeatureInstance instance, 
		final List<FeatureInstanceNode> instanceNodes
	)
	{
		FeatureInstanceNode deepestParent = null;
		
		int deepestParentDepthLevel = -1;
		int currDepthLevel = 0;
		List<FeatureInstanceNode> currDepthNodes = instanceNodes;
		List<FeatureInstanceNode> nextDepthNodes = 
				new ArrayList<FeatureInstanceNode>();
		
		while (!currDepthNodes.isEmpty())
		{
			for (final FeatureInstanceNode node : currDepthNodes)
			{
				if (node.featureInstance.generalises(instance))
				{
					// this node could be a parent
					if (currDepthLevel > deepestParentDepthLevel)
					{
						// don't have a parent at this depth level yet,
						// so pick this one for now
						deepestParent = node;
						deepestParentDepthLevel = currDepthLevel;
					}
					
					// see if any of our children still could lead to a deeper
					// parent
					nextDepthNodes.addAll(node.children);
				}
			}
			
			currDepthNodes = nextDepthNodes;
			nextDepthNodes = new ArrayList<FeatureInstanceNode>();
			++currDepthLevel;
		}
		
		return deepestParent;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Prints complete tree of tests for proactive features. Useful for debugging
	 * @param player
	 * @param from
	 * @param to
	 */
	public void printProactiveFeaturesTree(final int player, final int from, final int to)
	{
		System.out.println("---");
		final ProactiveFeaturesKey key = new ProactiveFeaturesKey();
		key.resetData(player, from, to);
		proactiveFeatures.get(key)[0].print(0);
		System.out.println("---");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper class for a pair of feature instances
	 * 
	 * @author Dennis Soemers
	 */
	private static class FeatureInstancePair
	{
		//--------------------------------------------------------------------
		
		/** First instance */
		protected final FeatureInstance a;
		/** Second instance */
		protected final FeatureInstance b;
		
		//---------------------------------------------------------------------
		
		/**
		 * Constructor
		 * @param a
		 * @param b
		 */
		protected FeatureInstancePair
		(
			final FeatureInstance a, 
			final FeatureInstance b
		)
		{
			this.a = a;
			this.b = b;
		}
		
		//---------------------------------------------------------------------
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * A node in a tree of related Feature Instances. Feature Instances
	 * in a node of this type will no longer contain checks that are also
	 * already performed by Feature Instances in ancestor nodes (parent,
	 * grandparent, etc.)
	 * 
	 * This class includes a reference to parent node, and stores children
	 * in a resizable ArrayList, which makes it convenient for usage during
	 * the feature instantiating process (when we're still building the tree)
	 * 
	 * @author Dennis Soemers
	 */
	private static class FeatureInstanceNode
	{
		
		//--------------------------------------------------------------------
		
		/** Our feature instance */
		protected final FeatureInstance featureInstance;
		
		/** Child nodes */
		protected final ArrayList<FeatureInstanceNode> children = new ArrayList<FeatureInstanceNode>(2);
		
		/** Parent node */
		protected FeatureInstanceNode parent;
		
		//--------------------------------------------------------------------
		
		/**
		 * Constructor
		 * @param featureInstance
		 * @param parent
		 */
		public FeatureInstanceNode
		(
			final FeatureInstance featureInstance,
			final FeatureInstanceNode parent
		)
		{
			this.featureInstance = featureInstance;
			this.parent = parent;
		}
		
		//--------------------------------------------------------------------
		
		/**
		 * @return All descendants of this node (the entire subtree below it)
		 */
		public List<FeatureInstanceNode> collectDescendants()
		{
			final List<FeatureInstanceNode> result = new ArrayList<FeatureInstanceNode>();
			final List<FeatureInstanceNode> nodesToCheck = new ArrayList<FeatureInstanceNode>();
			nodesToCheck.addAll(children);
			
			while (!nodesToCheck.isEmpty())
			{
				final FeatureInstanceNode node = nodesToCheck.remove(nodesToCheck.size() - 1);
				result.add(node);
				nodesToCheck.addAll(node.children);
			}
			
			return result;
		}
		
		//--------------------------------------------------------------------
		
		/**
		 * Prints the entire subtree rooted at this node. Useful for debugging.
		 * @param depthLevel
		 */
		@SuppressWarnings("unused")		// Please do NOT remove; frequently used for debugging!
		public void print(final int depthLevel)
		{
			for (int i = 0; i < depthLevel; ++i)
			{
				System.out.print("\t");
			}
			
			System.out.println(featureInstance);
			
			for (final FeatureInstanceNode child : children)
			{
				child.print(depthLevel + 1);
			}
		}
		
		//--------------------------------------------------------------------
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * A very similar kind of node to the class above. This node class is
	 * heavily optimised for speed, and should only be used after a tree
	 * of Feature Instances has been completely built.
	 * 
	 * It no longer contains a reference to the parent node, and stores
	 * children in a fixed-size array.
	 * 
	 * @author Dennis Soemers
	 */
	private static class FastFeatureInstanceNode
	{

		//--------------------------------------------------------------------

		/** Our feature instance */
		protected final FeatureInstance featureInstance;

		/** Child nodes */
		protected final FastFeatureInstanceNode[] children;

		//--------------------------------------------------------------------

		/**
		 * Constructor
		 * @param slowNode
		 */
		public FastFeatureInstanceNode(final FeatureInstanceNode slowNode)
		{
			this.featureInstance = slowNode.featureInstance;
			this.children = new FastFeatureInstanceNode[slowNode.children.size()];
			
			for (int i = 0; i < children.length; ++i)
			{
				children[i] = new FastFeatureInstanceNode(slowNode.children.get(i));
			}
		}

		//--------------------------------------------------------------------

	}

	//-------------------------------------------------------------------------
	
	/**
	 * Again a similar node to the above node types, but this time even more
	 * optimised by only keeping track of lists of features that are active
	 * if certain tests (still contained in Feature Instances) succeed.
	 * 
	 * This node type allows for complete removal of zero-test instances,
	 * instead letting their parent nodes immediately return multiple active
	 * features.
	 * 
	 * @author Dennis Soemers
	 */
	private static class FastFeaturesNode
	{
		
		//--------------------------------------------------------------------

		/** Our bitwise test(s) to execute */
		protected BitwiseTest test;

		/** Child nodes */
		protected final FastFeaturesNode[] children;
		
		/** 
		 * Indices of features that are active if the tests of this 
		 * node's instance succeed. 
		 */
		protected final int[] activeFeatureIndices;

		//--------------------------------------------------------------------

		/**
		 * Constructor
		 * @param instanceNode
		 */
		public FastFeaturesNode(final FastFeatureInstanceNode instanceNode)
		{
			this.test = instanceNode.featureInstance;
			
			final FastFeatureInstanceNode[] instanceChildren = instanceNode.children;
			
			final List<FastFeaturesNode> childrenList = new ArrayList<FastFeaturesNode>(instanceChildren.length);
			final TIntArrayList featureIndicesList = new TIntArrayList();
			
			// we'll definitely need the index of the instance contained 
			// in the node
			featureIndicesList.add(instanceNode.featureInstance.feature().spatialFeatureSetIndex());
			
			for (final FastFeatureInstanceNode instanceChild : instanceChildren)
			{
				final FeatureInstance instance = instanceChild.featureInstance;
				
				if (instance.hasNoTests())
				{
					// this child does not need to exist anymore, just absorb
					// its feature index
					final int featureIdx = instance.feature().spatialFeatureSetIndex();
					
					if (!featureIndicesList.contains(featureIdx))
					{
						featureIndicesList.add(featureIdx);
					}
				}
				else
				{
					// the child has meaningful tests, so it should still exist
					childrenList.add(new FastFeaturesNode(instanceChild));
				}
			}
			
			// this sorting is useful for the optimisation implemented in the
			// following lines of code for our parent node
			featureIndicesList.sort();
			
			// try to merge children which all have the same list of active
			// features into a single child that performs multiple tests at
			// once
			final int numChildren = childrenList.size();
			final boolean[] skipIndices = new boolean[numChildren];
			
			for (int i = 0; i < numChildren; ++i)
			{
				if (skipIndices[i])
				{
					continue;
				}
				
				final FastFeaturesNode child = childrenList.get(i);
				
				if (child.children.length == 0)
				{
					for (int j = i + 1; j < numChildren; ++j)
					{
						if (skipIndices[j])
							continue;
						
						final FastFeaturesNode otherChild = childrenList.get(j);
						
						if (otherChild.children.length == 0)
						{
							if 
							(
								Arrays.equals
								(
									child.activeFeatureIndices, 
									otherChild.activeFeatureIndices
								)
							)
							{
								final BitwiseTest testA = child.test;
								final BitwiseTest testB = otherChild.test;
								
								assert (testA.graphElementType() == testB.graphElementType());
								
								if 
								(
									testA.onlyRequiresSingleMustEmpty() && 
									testB.onlyRequiresSingleMustEmpty()
								)
								{
									// merge B's test into A's test
									if (testA instanceof FeatureInstance)
									{
										final FeatureInstance instanceA = (FeatureInstance) testA;
										final FeatureInstance instanceB = (FeatureInstance) testB;
										
										final ChunkSet combinedTest = instanceA.mustEmpty().clone();
										combinedTest.or(instanceB.mustEmpty());
										child.test = new OneOfMustEmpty(combinedTest, testA.graphElementType());
									}
									else
									{
										final OneOfMustEmpty A = (OneOfMustEmpty) testA;
										
										if (testB instanceof FeatureInstance)
										{
											final FeatureInstance instanceB = (FeatureInstance) testB;
											A.mustEmpties().or(instanceB.mustEmpty());
											
											child.test = new OneOfMustEmpty(A.mustEmpties(), testA.graphElementType());
										}
										else
										{
											final OneOfMustEmpty B = (OneOfMustEmpty) testB;
											A.mustEmpties().or(B.mustEmpties());
											
											child.test = new OneOfMustEmpty(A.mustEmpties(), testA.graphElementType());
										}
									}
									
									skipIndices[j] = true;
								}
								else if 
								(
									testA.onlyRequiresSingleMustWho() && 
									testB.onlyRequiresSingleMustWho()
								)
								{
									// merge B's test into A's test
									
									if (testA instanceof FeatureInstance)
									{
										final FeatureInstance instanceA = (FeatureInstance) testA;
										final FeatureInstance instanceB = (FeatureInstance) testB;
										
										final ChunkSet whoA = instanceA.mustWho();
										final ChunkSet whoMaskA = instanceA.mustWhoMask();
										
										final ChunkSet whoB = instanceB.mustWho();
										final ChunkSet whoMaskB = instanceB.mustWhoMask();
										
										final ChunkSet combinedMask = whoMaskA.clone();
										combinedMask.or(whoMaskB);
										
										if (whoMaskA.intersects(whoMaskB))
										{
											final ChunkSet cloneB = whoB.clone();
											cloneB.and(whoMaskB);
											
											if (!whoA.matches(combinedMask, cloneB))
											{
												// conflicting tests, can't
												// merge these
												continue;
											}
										}
										
										final ChunkSet combinedWhos = whoA.clone();
										combinedWhos.or(whoB);
										
										child.test = new OneOfMustWho(combinedWhos, combinedMask, testA.graphElementType());
									}
									else
									{
										final OneOfMustWho A = (OneOfMustWho) testA;
										
										final ChunkSet whosA = A.mustWhos();
										final ChunkSet whosMaskA = A.mustWhosMask();
										
										if (testB instanceof FeatureInstance)
										{
											final FeatureInstance instanceB = (FeatureInstance) testB;
											
											final ChunkSet whoB = instanceB.mustWho();
											final ChunkSet whoMaskB = instanceB.mustWhoMask();
											
											if (whosMaskA.intersects(whoMaskB))
											{												
												if (!whosA.matches(whoMaskB, whoB))
												{
													// conflicting tests, can't
													// merge these
													continue;
												}
											}
											
											whosA.or(whoB);
											whosMaskA.or(whoMaskB);
											
											child.test = new OneOfMustWho(whosA, whosMaskA, testA.graphElementType());
										}
										else
										{
											final OneOfMustWho B = (OneOfMustWho) testB;
											
											final ChunkSet whosB = B.mustWhos();
											final ChunkSet whosMaskB = B.mustWhosMask();
											
											if (whosMaskA.intersects(whosMaskB))
											{												
												if (!whosA.matches(whosMaskB, whosB))
												{
													// conflicting tests, can't
													// merge these
													continue;
												}
												
												if (!whosB.matches(whosMaskA, whosA))
												{
													// conflicting tests, can't
													// merge these
													continue;
												}
											}
											
											whosA.or(whosB);
											whosMaskA.or(whosMaskB);
											
											child.test = new OneOfMustWho(whosA, whosMaskA, testA.graphElementType());
										}
									}
									
									skipIndices[j] = true;
								}
								else if 
								(
									testA.onlyRequiresSingleMustWhat() && 
									testB.onlyRequiresSingleMustWhat()
								)
								{
									// merge B's test into A's test
									
									if (testA instanceof FeatureInstance)
									{
										final FeatureInstance instanceA = (FeatureInstance) testA;
										final FeatureInstance instanceB = (FeatureInstance) testB;
										
										final ChunkSet whatA = instanceA.mustWhat();
										final ChunkSet whatMaskA = instanceA.mustWhatMask();
										
										final ChunkSet whatB = instanceB.mustWhat();
										final ChunkSet whatMaskB = instanceB.mustWhatMask();
										
										final ChunkSet combinedMask = whatMaskA.clone();
										combinedMask.or(whatMaskB);
										
										if (whatMaskA.intersects(whatMaskB))
										{
											final ChunkSet cloneB = whatB.clone();
											cloneB.and(whatMaskB);
											
											if (!whatA.matches(combinedMask, cloneB))
											{
												// conflicting tests, can't
												// merge these
												continue;
											}
										}
										
										final ChunkSet combinedWhats = whatA.clone();
										combinedWhats.or(whatB);
										
										child.test = new OneOfMustWhat(combinedWhats, combinedMask, testA.graphElementType());
									}
									else
									{
										final OneOfMustWhat A = (OneOfMustWhat) testA;
										
										final ChunkSet whatsA = A.mustWhats();
										final ChunkSet whatsMaskA = A.mustWhatsMask();
										
										if (testB instanceof FeatureInstance)
										{
											final FeatureInstance instanceB = (FeatureInstance) testB;
											
											final ChunkSet whatB = instanceB.mustWhat();
											final ChunkSet whatMaskB = instanceB.mustWhatMask();
											
											if (whatsMaskA.intersects(whatMaskB))
											{												
												if (!whatsA.matches(whatMaskB, whatB))
												{
													// conflicting tests, can't
													// merge these
													continue;
												}
											}
											
											whatsA.or(whatB);
											whatsMaskA.or(whatMaskB);
											
											child.test = new OneOfMustWhat(whatsA, whatsMaskA, testA.graphElementType());
										}
										else
										{
											final OneOfMustWhat B = (OneOfMustWhat) testB;
											
											final ChunkSet whatsB = B.mustWhats();
											final ChunkSet whatsMaskB = B.mustWhatsMask();
											
											if (whatsMaskA.intersects(whatsMaskB))
											{												
												if (!whatsA.matches(whatsMaskB, whatsB))
												{
													// conflicting tests, can't
													// merge these
													continue;
												}
												
												if (!whatsB.matches(whatsMaskA, whatsA))
												{
													// conflicting tests, can't
													// merge these
													continue;
												}
											}
											
											whatsA.or(whatsB);
											whatsMaskA.or(whatsMaskB);
											
											child.test = new OneOfMustWhat(whatsA, whatsMaskA, testA.graphElementType());
										}
									}
									
									skipIndices[j] = true;
								}
							}
						}
					}
				}
			}
			
			// create replacement list only containing children that haven't
			// been merged into another child
			final List<FastFeaturesNode> remainingChildren = new ArrayList<FastFeaturesNode>();
			for (int i = 0; i < numChildren; ++i)
			{
				if (!skipIndices[i])
				{
					remainingChildren.add(childrenList.get(i));
				}
			}
			
			// convert lists to arrays
			children = new FastFeaturesNode[remainingChildren.size()];
			remainingChildren.toArray(children);
			activeFeatureIndices = featureIndicesList.toArray();
		}
		
		/**
		 * Constructor
		 * @param test
		 * @param children
		 * @param activeFeatureIndices
		 */
		private FastFeaturesNode
		(
			final BitwiseTest test, 
			final FastFeaturesNode[] children, 
			final int[] activeFeatureIndices
		)
		{
			this.test = test;
			this.children = children;
			this.activeFeatureIndices = activeFeatureIndices;
		}

		//--------------------------------------------------------------------
		
		/**
		 * Creates a copy of the given other subtree, with 
		 * features removed if their absolute weights in the 
		 * given weight vector do not exceed our threshold.
		 * 
		 * @param other
		 * @param weights null if we want to ignore thresholding
		 * @return Constructed node, or null if no longer relevant after thresholding
		 */
		public static FastFeaturesNode thresholdedNode(final FastFeaturesNode other, final FVector weights)
		{
			final List<FastFeaturesNode> thresholdedChildren = new ArrayList<FastFeaturesNode>(other.children.length);
			
			for (final FastFeaturesNode child : other.children)
			{
				final FastFeaturesNode thresholdedChild = thresholdedNode(child, weights);
				
				if (thresholdedChild != null)
					thresholdedChildren.add(thresholdedChild);
			}
			
			final TIntArrayList thresholdedFeatures = new TIntArrayList(other.activeFeatureIndices.length);
			
			for (final int activeFeature : other.activeFeatureIndices)
			{
				if (weights == null || Math.abs(weights.get(activeFeature)) >= SPATIAL_FEATURE_WEIGHT_THRESHOLD)
					thresholdedFeatures.add(activeFeature);
			}
			
			if (thresholdedChildren.isEmpty() && thresholdedFeatures.isEmpty())
				return null;
			
			return new FastFeaturesNode
					(
						other.test, 
						thresholdedChildren.toArray(new FastFeaturesNode[0]), 
						thresholdedFeatures.toArray()
					);
		}
		
		//--------------------------------------------------------------------
		
		/**
		 * Prints the entire subtree rooted at this node. Useful
		 * for debugging.
		 * @param depthLevel
		 */
		public void print(final int depthLevel)
		{
			for (int i = 0; i < depthLevel; ++i)
			{
				System.out.print("\t");
			}
			
			System.out.println(this);
			
			for (final FastFeaturesNode child : children)
			{
				child.print(depthLevel + 1);
			}
		}
		
		@Override
		public String toString()
		{
			return String.format(
					"%s %s", 
					test, 
					Arrays.toString(activeFeatureIndices));
		}
		
		//--------------------------------------------------------------------
		
	}
	
	//-------------------------------------------------------------------------

}
