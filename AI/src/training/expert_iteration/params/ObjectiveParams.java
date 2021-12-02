package training.expert_iteration.params;

/**
 * Wrapper around params for objective function(s) in training runs.
 *
 * @author Dennis Soemers
 */
public class ObjectiveParams
{
	
	//-------------------------------------------------------------------------
	
	/** If true, we'll train a policy on TSPG objective (see CoG 2019 paper) */
	public boolean trainTSPG;
	
	/** If true, we'll use importance sampling weights based on episode durations for CE-loss */
	public boolean importanceSamplingEpisodeDurations;
	
	/** If true, we use Weighted Importance Sampling instead of Ordinary Importance Sampling for any of the above */
	public boolean weightedImportanceSampling;
	
	/** If true, we don't do any value function learning */
	public boolean noValueLearning;
	
	/** If true, we handle move aliasing by putting the maximum mass among all aliased moves on each of them, for training selection policy. */
	public boolean handleAliasing;
	
	/** If true, we handle move aliasing by putting the maximum mass among all aliased moves on each of them, for training playout policy. */
	public boolean handleAliasingPlayouts;
	
	/** Lambda param for weight decay (~= 2c for L2 regularisation, in absence of momentum) */
	public double weightDecayLambda;
	
	//-------------------------------------------------------------------------

}
