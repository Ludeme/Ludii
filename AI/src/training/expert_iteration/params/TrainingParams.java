package training.expert_iteration.params;

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
	
	/** If not null/empty, will try to find a good value function to start with from this directory */
	public String initValueFuncDir;
	
	/** Number of epochs to run for policy gradients. */
	public int numPolicyGradientEpochs;
	
	/** Number of trials to run per epoch for policy gradients */
	public int numTrialsPerPolicyGradientEpoch;
	
	/** Discount factor gamma for policy gradients */
	public double pgGamma;
	
	/** Weight for entropy regularisation */
	public double entropyRegWeight;
	
	/** Number of threads to use for parallel trials for policy gradients */
	public int numPolicyGradientThreads;
	
	//-------------------------------------------------------------------------

}
