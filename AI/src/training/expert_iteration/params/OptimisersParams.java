package training.expert_iteration.params;

/**
 * Wrapper around params for optimisers in training runs.
 *
 * @author Dennis Soemers
 */
public class OptimisersParams
{
	
	//-------------------------------------------------------------------------
	
	/** Optimiser to use when optimising policy for Selection phase */
	public String selectionOptimiserConfig;
	
	/** Optimiser to use when optimising policy for Playout phase */
	public String playoutOptimiserConfig;
	
	/** Optimiser to use when optimising the Cross-Entropy Exploration policy */
	public String ceExploreOptimiserConfig;
	
	/** Optimiser to use when optimising policy on TSPG objective (see CoG 2019 paper) */
	public String tspgOptimiserConfig;
	
	/** Optimiser to use when optimising value function */
	public String valueOptimiserConfig;
	
	//-------------------------------------------------------------------------

}
