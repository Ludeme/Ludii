package training;

import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.move.Move;
import other.state.State;

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
	
	/**
	 * Should be implemented to return an expert distribution over actions.
	 * 
	 * @return Expert distribution over actions
	 */
	public abstract FVector expertDistribution();
	
	/**
	 * @return Game state
	 */
	public abstract State gameState();
	
	/**
	 * @return Last decision move (which lead to game state stored in this sample)
	 */
	public abstract Move lastDecisionMove();
	
	/**
	 * @return List of legal moves
	 */
	public abstract FastArrayList<Move> moves();
	
	//-------------------------------------------------------------------------

}
