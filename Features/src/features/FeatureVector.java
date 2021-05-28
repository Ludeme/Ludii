package features;

import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;

/**
 * Wrapper to represent a "vector" of features; internally, does not just hold a 
 * single vector, but uses a sparse representation for binary (typically sparsely 
 * active) spatial features, and a dense floats representation for aspatial features 
 * (which are not necessarily binary).
 *
 * @author Dennis Soemers
 */
public class FeatureVector
{
	
	//-------------------------------------------------------------------------
	
	/** Indices of spatial features that are active */
	private final TIntArrayList activeSpatialFeatureIndices;
	
	/** Vector of values for aspatial features */
	private final FVector aspatialFeatureValues;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param activeSpatialFeatureIndices
	 * @param aspatialFeatureValues
	 */
	public FeatureVector(final TIntArrayList activeSpatialFeatureIndices, final FVector aspatialFeatureValues)
	{
		this.activeSpatialFeatureIndices = activeSpatialFeatureIndices;
		this.aspatialFeatureValues = aspatialFeatureValues;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Indices of active spatial features (sparse representation)
	 */
	public TIntArrayList activeSpatialFeatureIndices()
	{
		return activeSpatialFeatureIndices;
	}
	
	/**
	 * @return Vector of feature values for aspatial features (dense representation)
	 */
	public FVector aspatialFeatureValues()
	{
		return aspatialFeatureValues;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeSpatialFeatureIndices == null) ? 0 : activeSpatialFeatureIndices.hashCode());
		result = prime * result + ((aspatialFeatureValues == null) ? 0 : aspatialFeatureValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof FeatureVector))
			return false;
		
		final FeatureVector other = (FeatureVector) obj;
		if (activeSpatialFeatureIndices == null)
		{
			if (other.activeSpatialFeatureIndices != null)
				return false;
		} 
		else if (!activeSpatialFeatureIndices.equals(other.activeSpatialFeatureIndices))
		{
			return false;
		}
		
		if (aspatialFeatureValues == null)
		{
			if (other.aspatialFeatureValues != null)
				return false;
		} 
		else if (!aspatialFeatureValues.equals(other.aspatialFeatureValues))
		{
			return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------

}
