package features.spatial.cache;

import java.util.HashMap;
import java.util.Map;

import features.feature_sets.BaseFeatureSet;
import features.feature_sets.BaseFeatureSet.ProactiveFeaturesKey;
import features.spatial.cache.footprints.BaseFootprint;
import main.collections.ChunkSet;
import other.state.State;
import other.state.container.ContainerState;

/**
 * Given a list containing player index, from-pos, and to-pos as key, this cache
 * can look up a cached list of active features that were active the last time
 * that same index was used to compute a list of active features.
 * 
 * Given a current game state, the cache will first use the key-specific
 * footprint of the complete Feature Set to ensure that all parts of the game
 * state covered by the footprint are still identical to how they were when the
 * active features were computed and stored in the cache. The active features
 * will only be returned if all parts of the game state covered by the footprint
 * are indeed still identical.
 * 
 * Note that the cache is not linked to any specific Trial or Context, or even
 * to any specific full match of a game being played. Generally, we expect the
 * majority of the cache to remain valid throughout a single playout, with small
 * parts becoming invalid during a playout, and most of the cache having turned
 * invalid when we move on to the next playout. However, it is possible that
 * some parts of the cache remain valid over many different playouts (e.g. parts
 * of a board that trained policies are very unlikely to make moves in).
 * 
 * We keep a separate cache per Thread (using ThreadLocal), to make sure that
 * different playouts running in different Threads do not invalidate each
 * others' caches.
 * 
 * 
 * @author Dennis Soemers
 */
public class ActiveFeaturesCache
{

	//-------------------------------------------------------------------------

	/** Our caches (one per thread) */
	protected final ThreadLocal<Map<ProactiveFeaturesKey, CachedDataFootprint>> threadLocalCache;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public ActiveFeaturesCache()
	{
		threadLocalCache = new ThreadLocal<Map<ProactiveFeaturesKey, CachedDataFootprint>>()
		{

			@Override
			protected Map<ProactiveFeaturesKey, CachedDataFootprint> initialValue()
			{
				return new HashMap<ProactiveFeaturesKey, CachedDataFootprint>();
			}

		};
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Stores the given list of active feature indices in the cache, for the
	 * given state, from-, and to-positions
	 * 
	 * @param state
	 * @param from
	 * @param to
	 * @param activeFeaturesToCache
	 * @param player
	 */
	public void cache
	(
		final State state, 
		final int from, 
		final int to, 
		final int[] activeFeaturesToCache,
		final int player
	)
	{
		final ContainerState container = state.containerStates()[0];
		
		final ProactiveFeaturesKey key = new ProactiveFeaturesKey(player, from, to);
		final Map<ProactiveFeaturesKey, CachedDataFootprint> map = threadLocalCache.get();
		final CachedDataFootprint pair = map.get(key);
		final BaseFootprint footprint = pair.footprint;
		
		final ChunkSet maskedEmptyCells;
		if (container.emptyChunkSetCell() != null && footprint.emptyCell() != null)
		{
			maskedEmptyCells = container.emptyChunkSetCell().clone();
			maskedEmptyCells.and(footprint.emptyCell());
		}
		else
		{
			maskedEmptyCells = null;
		}

		final ChunkSet maskedEmptyVertices;
		if (container.emptyChunkSetVertex() != null && footprint.emptyVertex() != null)
		{
			maskedEmptyVertices = container.emptyChunkSetVertex().clone();
			maskedEmptyVertices.and(footprint.emptyVertex());
		}
		else
		{
			maskedEmptyVertices = null;
		}

		final ChunkSet maskedEmptyEdges;
		if (container.emptyChunkSetEdge() != null && footprint.emptyEdge() != null)
		{
			maskedEmptyEdges = container.emptyChunkSetEdge().clone();
			maskedEmptyEdges.and(footprint.emptyEdge());
		}
		else
		{
			maskedEmptyEdges = null;
		}
		
		final ChunkSet maskedWhoCells = container.cloneWhoCell();
		if (maskedWhoCells != null && footprint.whoCell() != null)
			maskedWhoCells.and(footprint.whoCell());
		
		final ChunkSet maskedWhoVertices = container.cloneWhoVertex();
		if (maskedWhoVertices != null && footprint.whoVertex() != null)
			maskedWhoVertices.and(footprint.whoVertex());
		
		final ChunkSet maskedWhoEdges = container.cloneWhoEdge();
		if (maskedWhoEdges != null && footprint.whoEdge() != null)
			maskedWhoEdges.and(footprint.whoEdge());
		
		final ChunkSet maskedWhatCells = container.cloneWhatCell();
		if (maskedWhatCells != null && footprint.whatCell() != null)
			maskedWhatCells.and(footprint.whatCell());
		
		final ChunkSet maskedWhatVertices = container.cloneWhatVertex();
		if (maskedWhatVertices != null && footprint.whatVertex() != null)
			maskedWhatVertices.and(footprint.whatVertex());
		
		final ChunkSet maskedWhatEdges = container.cloneWhatEdge();
		if (maskedWhatEdges != null && footprint.whatEdge() != null)
			maskedWhatEdges.and(footprint.whatEdge());
		
		final BaseCachedData data = new FullCachedData(
				activeFeaturesToCache, 
				maskedEmptyCells, 
				maskedEmptyVertices, 
				maskedEmptyEdges, 
				maskedWhoCells,
				maskedWhoVertices,
				maskedWhoEdges,
				maskedWhatCells,
				maskedWhatVertices,
				maskedWhatEdges);
		
		map.put(key, new CachedDataFootprint(data, footprint));
	}

	/**
	 * @param featureSet
	 * @param state
	 * @param from
	 * @param to
	 * @param player
	 * @return Cached list of indices of active features, or null if not in cache or if entry
	 * in cache is invalid.
	 */
	public int[] getCachedActiveFeatures
	(
		final BaseFeatureSet featureSet, 
		final State state, 
		final int from, 
		final int to,
		final int player
	)
	{
		final ProactiveFeaturesKey key = new ProactiveFeaturesKey(player, from, to);
		final Map<ProactiveFeaturesKey, CachedDataFootprint> map = threadLocalCache.get();
		final CachedDataFootprint pair = map.get(key);
		
		if (pair == null)
		{
			// we need to compute and store footprint
			final BaseFootprint footprint = featureSet.generateFootprint(state, from, to, player);
			map.put(key, new CachedDataFootprint(null, footprint));
		}
		else
		{
			final BaseCachedData cachedData = pair.data;
			
			if (cachedData != null)
			{
				// we cached something, gotta make sure it's still valid
				final ContainerState container = state.containerStates()[0];
				final BaseFootprint footprint = pair.footprint;
				
				//System.out.println("footprint empty = " + footprint.empty());
				//System.out.println("old empty state = " + cachedData.emptyState);
				//System.out.println("new empty state = " + container.empty().bitSet());
				
				if (cachedData.isDataValid(container, footprint))
					return cachedData.cachedActiveFeatureIndices();
			}
		}
		
		// no cached data, so return null
		//System.out.println("key not in cache at all");
		return null;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper, around CachedData + a Footprint for the same key in HashMaps.
	 * 
	 * @author Dennis Soemers
	 */
	private class CachedDataFootprint
	{
		/** Data we want to cache (active features, old state vectors) */
		public final BaseCachedData data;
		
		/** Footprint for the same key */
		public final BaseFootprint footprint;
		
		/**
		 * Constructor
		 * @param data
		 * @param footprint
		 */
		public CachedDataFootprint(final BaseCachedData data, final BaseFootprint footprint)
		{
			this.data = data;
			this.footprint = footprint;
		}
	}
	
	//-------------------------------------------------------------------------

}
