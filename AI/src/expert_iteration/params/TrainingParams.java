package expert_iteration.params;

/**
 * Wrapper around params for basic training setup/configuration.
 *
 * @author Dennis Soemers
 */
public class TrainingParams
{
	
	//-------------------------------------------------------------------------
	
	/** Number of training games to run */
	public int numTrainingGames;
	
	/** Max size of minibatches in training. */
	public int batchSize;
	
	/** Max size of the experience buffer. */
	public int experienceBufferSize;
	
	/** After this many moves (decision points) in training games, we update weights. */
	public int updateWeightsEvery;
	
	/** If true, we'll use prioritized experience replay */
	public boolean prioritizedExperienceReplay;
	
	/** If true, we train a single shared feature set for all players (and boosted weights per player) */
	public boolean sharedFeatureSet;
	
	//-------------------------------------------------------------------------

}
