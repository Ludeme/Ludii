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

	/** Number of threads to use for parallel feature discovery */
	public int numFeatureDiscoveryThreads;
	
	/** Critical value used when computing confidence intervals for correlations */
	public double criticalValueCorrConf;
	
	/** If true, use a special-moves expander in addition to the normal one */
	public boolean useSpecialMovesExpander;
	
	/** If true, use a special-moves expander in addition to the normal one, but split time with the normal one (so same number of total features) */
	public boolean useSpecialMovesExpanderSplit;
	
	/** Type of feature set expander to use */
	public String expanderType;
	
	//-------------------------------------------------------------------------

}
