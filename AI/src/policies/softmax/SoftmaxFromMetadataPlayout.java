package policies.softmax;

import java.util.ArrayList;
import java.util.List;

import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import game.types.play.RoleType;
import metadata.ai.features.Features;

/**
 * A Softmax Policy that can automatically initialise itself by
 * using the Playout features embedded in a game's metadata.
 * 
 * @author Dennis Soemers
 */
public class SoftmaxFromMetadataPlayout extends SoftmaxPolicy
{
	
	/**
	 * Constructor
	 * @param epsilon Epsilon for epsilon-greedy feature-based playouts. 1 for uniform, 0 for always softmax
	 */
	public SoftmaxFromMetadataPlayout(final double epsilon)
	{
		friendlyName = "Softmax Policy (Playout features from Game metadata)";
		this.epsilon = epsilon;
	}

	@Override
	public void initAI(final Game game, final int playerID)
	{
		final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
		final List<LinearFunction> linFuncs = new ArrayList<LinearFunction>();
		
		final Features featuresMetadata = game.metadata().ai().features();
		
		for (final metadata.ai.features.FeatureSet featureSet : featuresMetadata.featureSets())
		{
			if (featureSet.role() == RoleType.Shared)
				addFeatureSetWeights(0, featureSet.featureStrings(), featureSet.playoutWeights(), featureSetsList, linFuncs);
			else
				addFeatureSetWeights(featureSet.role().owner(), featureSet.featureStrings(), featureSet.playoutWeights(), featureSetsList, linFuncs);
		}
		
		this.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
		this.linearFunctions = linFuncs.toArray(new LinearFunction[linFuncs.size()]);
		this.playoutActionLimit = 200;
		
		super.initAI(game, playerID);
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		// We support any game with appropriate features in metadata
		if (game.metadata().ai() != null && game.metadata().ai().features() != null)
		{
			final Features featuresMetadata = game.metadata().ai().features();
			if (featuresMetadata.featureSets().length == 1 && featuresMetadata.featureSets()[0].role() == RoleType.Shared)
				return true;
			else
				return (featuresMetadata.featureSets().length == game.players().count());
		}
		
		return false;
	}
	
}
