package features.spatial.cache;

import features.spatial.cache.footprints.BaseFootprint;
import other.state.container.ContainerState;

/**
 * Abstract class for data cached in active-feature-caches
 *
 * @author Dennis Soemers
 */
public abstract class BaseCachedData
{
	
	//-------------------------------------------------------------------------
	
	/** Active features as previously computed and stored in cache */
	protected final int[] activeFeatureIndices;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param activeFeatureIndices
	 */
	public BaseCachedData(final int[] activeFeatureIndices)
	{
		this.activeFeatureIndices = activeFeatureIndices;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Array of active feature indices as previously cached
	 */
	public final int[] cachedActiveFeatureIndices()
	{
		return activeFeatureIndices;
	}
	
	/**
	 * @param containerState
	 * @param footprint
	 * @return Is this cached data still valid for the given container state and footprint?
	 */
	public abstract boolean isDataValid(final ContainerState containerState, final BaseFootprint footprint);
	
	//-------------------------------------------------------------------------

}
