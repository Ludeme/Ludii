package playout_move_selectors;

import decision_trees.logits.LogitTreeNode;
import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;

/**
 * PlayoutMoveSelector for playouts which uses a softmax over actions with logits
 * computed by feature regression trees.
 *
 * @author Dennis Soemers
 */
public class LogitTreeMoveSelector extends PlayoutMoveSelector
{
	
	//-------------------------------------------------------------------------
	
	/** Feature sets (one per player, or just a shared one at index 0) */
	protected final BaseFeatureSet[] featureSets;
	
	/** Regression tree root nodes (one per player, or just a shared one at index 0) */
	protected final LogitTreeNode[] rootNodes;
	
	/** Do we want to play greedily? */
	protected final boolean greedy;
	
	/** Temperature for the distribution */
	protected final double temperature;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param featureSets Feature sets (one per player, or just a shared one at index 0)
	 * @param rootNodes Regression tree root nodes (one per player, or just a shared one at index 0)
	 * @param greedy Do we want to play greedily?
	 * @param temperature
	 */
	public LogitTreeMoveSelector
	(
		final BaseFeatureSet[] featureSets, 
		final LogitTreeNode[] rootNodes,
		final boolean greedy,
		final double temperature
	)
	{
		this.featureSets = featureSets;
		this.rootNodes = rootNodes;
		this.greedy = greedy;
		this.temperature = temperature;
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
		final LogitTreeNode rootNode;
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

		final float[] logits = new float[featureVectors.length];

		for (int i = 0; i < featureVectors.length; ++i)
		{
			logits[i] = rootNode.predict(featureVectors[i]);
		}

		final FVector distribution = FVector.wrap(logits);
		distribution.softmax(temperature);
		
		int numLegalMoves = maybeLegalMoves.size();
		
		while (numLegalMoves > 0)
		{
			--numLegalMoves;	// We're trying a move; if this one fails, it's actually not legal
			
			final int n = greedy ? distribution.argMaxRand() : distribution.sampleFromDistribution();
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
