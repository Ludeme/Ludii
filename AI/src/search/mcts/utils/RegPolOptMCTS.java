package search.mcts.utils;

import main.collections.FVector;
import other.state.State;
import search.mcts.nodes.BaseNode;

/**
 * Utility methods for computing pi-bar as described in the
 * "Monte-Carlo tree search as regularized policy optimization" paper at 
 * ICML 2020: http://proceedings.mlr.press/v119/grill20a.html
 *
 * @author Dennis Soemers
 */
public class RegPolOptMCTS
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private RegPolOptMCTS()
	{
		// Should not instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * See Appendix B.3 of the paper for details on implementation
	 * 
	 * @param node
	 * @param explorationConstant
	 * @return pi-bar, as computed based on the search results stored in the
	 * 	given MCTS node
	 */
	public static FVector computePiBar(final BaseNode node, final double explorationConstant)
	{
		final State nodeState = node.contextRef().state();
		final int numChildren = node.numLegalMoves();
		
		double lambda = computeLambdaMultiplier(node, explorationConstant);
		if (lambda == 0.0)
			lambda = 1.0;	// NOTE: not in paper, but seems sensible?
		final FVector trainedPolicy = node.learnedSelectionPolicy();
		final FVector qVector = new FVector(numChildren);
		
		double alphaMin = -10000.0;
		double alphaMax = -10000.0;
		
		for (int i = 0; i < numChildren; ++i)
		{
			final BaseNode child = node.childForNthLegalMove(i);
			final double q;
			if (child == null)
				q = node.valueEstimateUnvisitedChildren(nodeState.mover(), nodeState);
			else
				q = node.averageScore(nodeState.mover(), nodeState);
			
			qVector.set(i, (float)q);
			alphaMin = Math.max(alphaMin, q + lambda * trainedPolicy.get(i));
			alphaMax = Math.max(alphaMax, q + lambda);
		}
		
//		System.out.println();
//		System.out.println("NEW CALL");
		final double alphaStar = alphaStarBinarySearch(alphaMin, alphaMax, lambda, trainedPolicy, qVector);
		final FVector piBar = new FVector(numChildren);
		for (int i = 0; i < numChildren; ++i)
		{
			piBar.set(i, (float) ((lambda * trainedPolicy.get(i)) / (alphaStar - qVector.get(i))));
		}
		
		// Because we maybe don't find the super perfect alpha and tolerate a small error, we'll normalise
		// just to be sure
		piBar.div(piBar.sum());
		return piBar;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param node
	 * @return The empirical visit distribution, with an extra artificial visit
	 * 	per action.
	 */
	public static FVector computePiHat(final BaseNode node)
	{
		final int numLegalMoves = node.numLegalMoves();
		final FVector piHat = new FVector(numLegalMoves);
		final float sumVisits = node.numVisits() + numLegalMoves;
		
		for (int i = 0; i < numLegalMoves; ++i)
		{
			final BaseNode child = node.childForNthLegalMove(i);
			piHat.set(i, (child == null ? 1 : child.numVisits() + 1) / sumVisits);
		}
		
		return piHat;
	}
	
	/**
	 * The lambda_N multiplier is a decreasing function of N, where N is the number
	 * of visits of the given node, and always greater than 0 (assuming that the
	 * exploration constant is greater than 0, which it should be).
	 * 
	 * @param node
	 * @param explorationConstant
	 * @return The lambda multiplier
	 */
	public static double computeLambdaMultiplier(final BaseNode node, final double explorationConstant)
	{
		final int numVisits = node.numVisits();
		return explorationConstant * (Math.sqrt(numVisits) / (node.numLegalMoves() + numVisits));
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Performs a binary search to find the correct value of alpha*.
	 * See Appendix B.3 of the paper for details on implementation
	 * 
	 * @param lower
	 * @param upper
	 * @param lambda
	 * @param trainedPolicy
	 * @param qVector
	 * @return alpha* value
	 */
	private static double alphaStarBinarySearch
	(
		final double lower, 
		final double upper, 
		final double lambda, 
		final FVector trainedPolicy, 
		final FVector qVector
	)
	{
		final double TOLERANCE = 0.0001;	// Keep binary searching until we get this close to 1.0
		final double guess = (lower + upper) / 2.0;
		
		double piAlphaSum = 0.0;
		for (int i = 0; i < trainedPolicy.dim(); ++i)
		{
			piAlphaSum += (lambda * trainedPolicy.get(i)) / (guess - qVector.get(i));
		}
		
//		System.out.println("trainedPolicy = " + trainedPolicy);
//		System.out.println("qVector = " + qVector);
//		System.out.println("lambda = " + lambda);
//		System.out.println("guess = " + guess);
//		System.out.println("piAlphaSum = " + piAlphaSum);
//		System.out.println("lower = " + lower);
//		System.out.println("upper = " + upper);
		
		if (Math.abs(piAlphaSum - 1.0) < TOLERANCE)
			return guess;
		else if (piAlphaSum < 1.0)
			return alphaStarBinarySearch(lower, guess, lambda, trainedPolicy, qVector);
		else
			return alphaStarBinarySearch(guess, upper, lambda, trainedPolicy, qVector);
	}
	
	//-------------------------------------------------------------------------

}
