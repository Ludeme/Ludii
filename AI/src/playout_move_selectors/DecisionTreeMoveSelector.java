package playout_move_selectors;

import decision_trees.classifiers.DecisionTreeNode;
import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;

/**
 * PlayoutMoveSelector for playouts which uses a distribution over actions
 * computed by move-classification feature trees.
 *
 * @author Dennis Soemers
 */
public class DecisionTreeMoveSelector extends PlayoutMoveSelector
{
	
	//-------------------------------------------------------------------------
	
	/** Feature sets (one per player, or just a shared one at index 0) */
	protected final BaseFeatureSet[] featureSets;
	
	/** Classification tree root nodes (one per player, or just a shared one at index 0) */
	protected final DecisionTreeNode[] rootNodes;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param featureSets Feature sets (one per player, or just a shared one at index 0)
	 * @param rootNodes Classification tree root nodes (one per player, or just a shared one at index 0)
	 */
	public DecisionTreeMoveSelector
	(
		final BaseFeatureSet[] featureSets, 
		final DecisionTreeNode[] rootNodes
	)
	{
		this.featureSets = featureSets;
		this.rootNodes = rootNodes;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Move selectMove
	(
		final Context context, 
		final FastArrayList<Move> maybeLegalMoves, 
		final int p,
		final IsMoveReallyLegal isMoveReallyLegal
	)
	{
		final BaseFeatureSet featureSet;
		final DecisionTreeNode rootNode;
		if (featureSets.length == 1)
		{
			featureSet = featureSets[0];
			rootNode = rootNodes[0];
		}
		else
		{
			featureSet = featureSets[p];
			rootNode = rootNodes[p];
		}

		final FeatureVector[] featureVectors = featureSet.computeFeatureVectors(context, maybeLegalMoves, false);

		final float[] unnormalisedProbs = new float[featureVectors.length];

		for (int i = 0; i < featureVectors.length; ++i)
		{
			unnormalisedProbs[i] = rootNode.predict(featureVectors[i]);
		}

		final FVector distribution = FVector.wrap(unnormalisedProbs);
		distribution.normalise();
		
		int numLegalMoves = maybeLegalMoves.size();
		
		while (numLegalMoves > 0)
		{
			--numLegalMoves;	// We're trying a move; if this one fails, it's actually not legal
			
			final int n = distribution.sampleFromDistribution();
			final Move move = maybeLegalMoves.get(n);
			
			if (isMoveReallyLegal.checkMove(move))
				return move;	// Only return this move if it's really legal
			else
				distribution.updateSoftmaxInvalidate(n);	// Incrementally update the softmax, move n is invalid
		}
		
		// No legal moves?
		return null;
	}
	
	//-------------------------------------------------------------------------

}
