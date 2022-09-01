package features;

import main.collections.FVector;

/**
 * Wrapper to represent a vector of weights. Internally stores it as just a single
 * vector, where the first N weights are for aspatial features, and the remaining
 * weights are for spatial features.
 *
 * @author Dennis Soemers
 */
public class WeightVector
{
	
	//-------------------------------------------------------------------------
	
	/** Our vector of weights */
	private final FVector weights;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param weights
	 */
	public WeightVector(final FVector weights)
	{
		this.weights = weights;
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public WeightVector(final WeightVector other)
	{
		this.weights = new FVector(other.weights);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param featureVector
	 * @return Dot product of this weight vector with given feature vector
	 */
	public float dot(final FeatureVector featureVector)
	{
		final FVector aspatialFeatureValues = featureVector.aspatialFeatureValues();
		
		// This dot product call will only use the first N weights, where N is the length
		// of the aspatial feature values vector
		final float aspatialFeaturesVal = aspatialFeatureValues.dot(weights);
		
		// For the spatial features, use this offset (to skip weights for aspatial features)
		final int offset = aspatialFeatureValues.dim();
		
		return aspatialFeaturesVal + weights.dotSparse(featureVector.activeSpatialFeatureIndices(), offset);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Vector containing all weights; first those for aspatial features, followed
	 * 	by those for spatial features.
	 */
	public FVector allWeights()
	{
		return weights;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((weights == null) ? 0 : weights.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof WeightVector))
			return false;
		
		final WeightVector other = (WeightVector) obj;
		if (weights == null)
		{
			if (other.weights != null)
				return false;
		} 
		
		return weights.equals(other.weights);
	}
	
	//-------------------------------------------------------------------------

}
