package training.expert_iteration.params;

/**
 * Wrapper around params for feature discovery settings.
 *
 * @author Dennis Soemers
 */
public class FeatureDiscoveryParams
{
	
	//-------------------------------------------------------------------------
	
	/** After this many training games, we add a new feature. */
	public int addFeatureEvery;
	
	/** If true, we'll not grow feature set (but still train weights) */
	public boolean noGrowFeatureSet;
	
	/** At most this number of feature instances will be taken into account when combining features */
	public int combiningFeatureInstanceThreshold;
	
	/** If true, we will keep full atomic feature set and not prune anything */
	public boolean noPruneInitFeatures;
	
	/** Will only consider pruning features if they have been active at least this many times */
	public int pruneInitFeaturesThreshold;
	
	/** Number of random games to play out for determining features to prune */
	public int numPruningGames;
	
	/** Max number of seconds to spend on random games for pruning atomic features */
	public int maxNumPruningSeconds;

	/** Number of threads to use for parallel feature discovery */
	public int numFeatureDiscoveryThreads;
	
	/** Critical value used when computing confidence intervals for correlations */
	public double criticalValueCorrConf;
	
	/** If true, use a special-moves expander in addition to the normal one */
	public boolean useSpecialMovesExpander;
	
	//-------------------------------------------------------------------------

}
