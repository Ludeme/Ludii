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
import java.util.Map.Entry;
import java.util.stream.Stream;

import features.Feature;
import features.aspatial.AspatialFeature;
import features.spatial.SpatialFeature;
import features.spatial.Walk;
import features.spatial.cache.ActiveFeaturesCache;
import features.spatial.cache.footprints.BaseFootprint;
import features.spatial.cache.footprints.FullFootprint;
import features.spatial.instances.FeatureInstance;
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
 * A naive implementation of a feature set, which does instantiate features,
 * but simply evaluates all of them for every move.
 *
 * @author Dennis Soemers
 */
public class NaiveFeatureSet extends BaseFeatureSet
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reactive instances, indexed by:
	 * 	player index,
	 * 	last-from-pos
	 * 	last-to-pos,
	 * 	from-pos
	 * 	to-pos
	 */
	protected HashMap<ReactiveFeaturesKey, List<FeatureInstance>> reactiveInstances;
	
	/**
	 * Proactive instances, indexed by:
	 * 	player index,
	 * 	from-pos
	 * 	to-pos
	 */
	protected HashMap<ProactiveFeaturesKey, List<FeatureInstance>> proactiveInstances;
	
	/**
	 * Reactive Features, indexed by:
	 * 	player index,
	 * 	last-from-pos
	 * 	last-to-pos,
	 * 	from-pos
	 * 	to-pos
	 */
	protected HashMap<ReactiveFeaturesKey, List<FeatureInstance>[]> reactiveFeatures;
	
	/**
	 * Proactive Features, indexed by:
	 * 	player index,
	 * 	from-pos
	 * 	to-pos
	 */
	protected HashMap<ProactiveFeaturesKey, List<FeatureInstance>[]> proactiveFeatures;
	
	/**
	 * Same as above, but only includes features with absolute weights that exceed the above
	 * threshold.
	 */
	protected HashMap<ReactiveFeaturesKey, List<FeatureInstance>[]> reactiveFeaturesThresholded;
	
	/**
	 * Same as above, but only includes features with absolute weights that exceed the above
	 * threshold.
	 */
	protected HashMap<ProactiveFeaturesKey, List<FeatureInstance>[]> proactiveFeaturesThresholded;
	
	/** Cache with indices of active proactive features previously computed */
	protected ActiveFeaturesCache activeProactiveFeaturesCache;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Construct feature set from lists of features
	 * @param aspatialFeatures
	 * @param spatialFeatures
	 */
	public NaiveFeatureSet(final List<AspatialFeature> aspatialFeatures, final List<SpatialFeature> spatialFeatures)
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
	public NaiveFeatureSet(final String filename)
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
	
	@SuppressWarnings("unchecked")
	@Override
	protected void instantiateFeatures(final int[] supportedPlayers)
	{
		activeProactiveFeaturesCache = new ActiveFeaturesCache();

		reactiveInstances = new HashMap<ReactiveFeaturesKey, List<FeatureInstance>>();
		proactiveInstances = new HashMap<ProactiveFeaturesKey, List<FeatureInstance>>();
		
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
						List<FeatureInstance> instances = reactiveInstances.get(reactiveKey);
						
						if (instances == null)
						{
							instances = new ArrayList<FeatureInstance>(1);
							reactiveInstances.put(new ReactiveFeaturesKey(reactiveKey), instances);
						}
						
						instances.add(instance);
					}
					else	// proactive feature
					{
						proactiveKey.resetData(player, from, to);
						List<FeatureInstance> instances = proactiveInstances.get(proactiveKey);
						
						if (instances == null)
						{
							instances = new ArrayList<FeatureInstance>(1);
							proactiveInstances.put(new ProactiveFeaturesKey(proactiveKey), instances);
						}
						
						instances.add(instance);
					}
				}
			}
		}
		
		reactiveFeatures = new HashMap<ReactiveFeaturesKey, List<FeatureInstance>[]>();
		reactiveFeaturesThresholded = new HashMap<ReactiveFeaturesKey, List<FeatureInstance>[]>();
		for (final Entry<ReactiveFeaturesKey, List<FeatureInstance>> entry : reactiveInstances.entrySet())
		{
			final List<FeatureInstance>[] unthresholdedInstanceLists = new List[spatialFeatures.length];
			for (int i = 0; i < unthresholdedInstanceLists.length; ++i)
			{
				unthresholdedInstanceLists[i] = new ArrayList<FeatureInstance>();
			}
			for (final FeatureInstance instance : entry.getValue())
			{
				unthresholdedInstanceLists[instance.feature().spatialFeatureSetIndex()].add(instance);
			}
			reactiveFeatures.put(entry.getKey(), unthresholdedInstanceLists);
			
			final List<FeatureInstance>[] thresholdedInstanceLists = new List[spatialFeatures.length];
			for (int i = 0; i < thresholdedInstanceLists.length; ++i)
			{
				thresholdedInstanceLists[i] = new ArrayList<FeatureInstance>();
			}
			for (final FeatureInstance instance : entry.getValue())
			{
				final int featureIdx = instance.feature().spatialFeatureSetIndex();
				if (spatialFeatureInitWeights == null || Math.abs(spatialFeatureInitWeights.get(featureIdx)) >= SPATIAL_FEATURE_WEIGHT_THRESHOLD)
				{
					thresholdedInstanceLists[featureIdx].add(instance);
				}
			}
			reactiveFeaturesThresholded.put(entry.getKey(), thresholdedInstanceLists);
		}
		
		proactiveFeatures = new HashMap<ProactiveFeaturesKey, List<FeatureInstance>[]>();
		proactiveFeaturesThresholded = new HashMap<ProactiveFeaturesKey, List<FeatureInstance>[]>();
		for (final Entry<ProactiveFeaturesKey, List<FeatureInstance>> entry : proactiveInstances.entrySet())
		{
			final List<FeatureInstance>[] unthresholdedInstanceLists = new List[spatialFeatures.length];
			for (int i = 0; i < unthresholdedInstanceLists.length; ++i)
			{
				unthresholdedInstanceLists[i] = new ArrayList<FeatureInstance>();
			}
			for (final FeatureInstance instance : entry.getValue())
			{
				unthresholdedInstanceLists[instance.feature().spatialFeatureSetIndex()].add(instance);
			}
			proactiveFeatures.put(entry.getKey(), unthresholdedInstanceLists);
			
			final List<FeatureInstance>[] thresholdedInstanceLists = new List[spatialFeatures.length];
			for (int i = 0; i < thresholdedInstanceLists.length; ++i)
			{
				thresholdedInstanceLists[i] = new ArrayList<FeatureInstance>();
			}
			for (final FeatureInstance instance : entry.getValue())
			{
				final int featureIdx = instance.feature().spatialFeatureSetIndex();
				if (spatialFeatureInitWeights == null || Math.abs(spatialFeatureInitWeights.get(featureIdx)) >= SPATIAL_FEATURE_WEIGHT_THRESHOLD)
				{
					thresholdedInstanceLists[featureIdx].add(instance);
				}
			}
			proactiveFeaturesThresholded.put(entry.getKey(), thresholdedInstanceLists);
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
		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
		final int[] lastFroms = lastFrom >= 0 ? new int[]{-1, lastFrom} : new int[]{-1};
		final int[] lastTos = lastTo >= 0 ? new int[]{-1, lastTo} : new int[]{-1};
		
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
				// Did not retrieve from cache, so need to compute the proactive features first
				activeFeatureIndices = new TIntArrayList();
				
				//System.out.println("cache miss!");
				
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
							final List<FeatureInstance>[] instanceLists;
							if (thresholded)
								instanceLists = proactiveFeaturesThresholded.get(key);
							else
								instanceLists = proactiveFeatures.get(key);
								
							if (instanceLists != null)
							{
								for (int i = 0; i < instanceLists.length; ++i)
								{
									for (final FeatureInstance instance : instanceLists[i])
									{
										if (instance.matches(state))
										{
											activeFeatureIndices.add(i);
											break;
										}
									}
								}
							}
						}
					}
				}

				if (thresholded)
					activeProactiveFeaturesCache.cache(state, from, to, activeFeatureIndices.toArray(), player);
			}
		}
		else
		{
			activeFeatureIndices = new TIntArrayList();
		}
		
		// Now still need to compute the reactive features, which aren't cached
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
								final List<FeatureInstance>[] instanceLists;
								
								reactiveKey.resetData(player, lastFromPos, lastToPos, fromPos, toPos);
								if (thresholded)
									instanceLists = reactiveFeaturesThresholded.get(reactiveKey);
								else
									instanceLists = reactiveFeatures.get(reactiveKey);
								
								if (instanceLists != null)
								{
									for (int f = 0; f < instanceLists.length; ++f)
									{
										for (final FeatureInstance instance : instanceLists[f])
										{
											if (instance.matches(state))
											{
												activeFeatureIndices.add(f);
												break;
											}
										}
									}
								}
							}
						}
					}
				}
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
		
		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
		final int[] lastFroms = lastFrom >= 0 ? new int[]{-1, lastFrom} : new int[]{-1};
		final int[] lastTos = lastTo >= 0 ? new int[]{-1, lastTo} : new int[]{-1};
		
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
					final List<FeatureInstance>[] instanceLists = proactiveFeatures.get(proactiveKey);

					for (int i = 0; i < instanceLists.length; ++i)
					{
						for (final FeatureInstance instance : instanceLists[i])
						{
							if (instance.matches(state))
							{
								activeInstances.add(instance);
							}
						}
					}
				}
			}
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
								final List<FeatureInstance>[] instanceLists = reactiveFeatures.get(reactiveKey);
								
								if (instanceLists != null)
								{
									for (int f = 0; f < instanceLists.length; ++f)
									{
										for (final FeatureInstance instance : instanceLists[f])
										{
											if (instance.matches(state))
											{
												activeInstances.add(instance);
											}
										}
									}
								}
							}
						}
					}
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
				
		final int[] froms = from >= 0 ? new int[]{-1, from} : new int[]{-1};
		final int[] tos = to >= 0 ? new int[]{-1, to} : new int[]{-1};
		
		// Loop through all instances, OR all the tests
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
					final List<FeatureInstance>[] instanceLists = proactiveFeatures.get(key);

					if (instanceLists != null)
					{
						for (int i = 0; i < instanceLists.length; ++i)
						{
							for (final FeatureInstance instance : instanceLists[i])
							{
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
							}
						}
					}
				}
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
	public NaiveFeatureSet createExpandedFeatureSet
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
			final NaiveFeatureSet newFeatureSet = createExpandedFeatureSet
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
	public NaiveFeatureSet createExpandedFeatureSet
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
				allowedRotations = new TFloatArrayList(Walk.allGameRotations(game.get()));
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
			
			return new NaiveFeatureSet(Arrays.asList(aspatialFeatures), newFeatureList);
		}
		
		return null;
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

}
