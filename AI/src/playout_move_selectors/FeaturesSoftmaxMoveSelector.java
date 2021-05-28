package playout_move_selectors;

import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;

/**
 * PlayoutMoveSelector for playouts which uses a softmax over actions with logits
 * computed by features.
 *
 * @author Dennis Soemers
 */
public class FeaturesSoftmaxMoveSelector extends PlayoutMoveSelector		// TODO also a greedy version?
{
	
	//-------------------------------------------------------------------------
	
	/** Feature sets (one per player, or just a shared one at index 0) */
	protected final BaseFeatureSet[] featureSets;
	
	/** Weight vectors (one per player, or just a shared one at index 0) */
	protected final WeightVector[] weights;
	
	/** Do we want to use thresholding to ignore low-weight features? */
	protected final boolean thresholded;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param featureSets Feature sets (one per player, or just a shared one at index 0)
	 * @param weights Weight vectors (one per player, or just a shared one at index 0)
	 * @param thresholded Do we want to use thresholding to ignore low-weight features?
	 */
	public FeaturesSoftmaxMoveSelector
	(
		final BaseFeatureSet[] featureSets, 
		final WeightVector[] weights,
		final boolean thresholded
	)
	{
		this.featureSets = featureSets;
		this.weights = weights;
		this.thresholded = thresholded;
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
		final WeightVector weightVector;
		if (featureSets.length == 1)
		{
			featureSet = featureSets[0];
			weightVector = weights[0];
		}
		else
		{
			featureSet = featureSets[p];
			weightVector = weights[p];
		}

		final FeatureVector[] featureVectors = featureSet.computeFeatureVectors(context, maybeLegalMoves, thresholded);

		final float[] logits = new float[featureVectors.length];

		for (int i = 0; i < featureVectors.length; ++i)
		{
			logits[i] = weightVector.dot(featureVectors[i]);
		}

		final FVector distribution = FVector.wrap(logits);
		distribution.softmax();
		
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
