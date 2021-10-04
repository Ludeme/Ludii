package training;

import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;

/**
 * Abstract class for a sample of experience
 * 
 * @author Dennis Soemers
 */
public abstract class ExperienceSample
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Should be implemented to (generate and) return feature vectors corresponding
	 * to the moves that were legal in this sample of experience. Can use the given
	 * feature set to generate them, but can also return already-cached ones.
	 * 
	 * @param featureSet
	 * @return Feature vectors corresponding to this sample of experience
	 */
	public abstract FeatureVector[] generateFeatureVectors(final BaseFeatureSet featureSet);
	
	//-------------------------------------------------------------------------

}
