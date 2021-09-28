package policy_gradients;

import expert_iteration.feature_discovery.FeatureSetExpander;
import features.feature_sets.BaseFeatureSet;
import game.Game;
import policies.softmax.SoftmaxPolicy;

/**
 * Self-play feature (pre-)training and discovery with REINFORCE
 * 
 * @author Dennis Soemers
 */
public class Reinforce
{
	
	/**
	 * Runs self-play with Policy Gradient training of features
	 */
	public static void runSelfPlayPG
	(
		final Game game,
		final SoftmaxPolicy policy,
		final BaseFeatureSet[] featureSets,
		final FeatureSetExpander featureSetExpander
	)
	{
		// TODO
	}

}
