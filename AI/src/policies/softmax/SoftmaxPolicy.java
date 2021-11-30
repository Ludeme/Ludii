package policies.softmax;

import other.context.Context;
import other.move.Move;
import policies.Policy;

/**
 * Abstract class for softmax policies; policies that compute
 * logits for moves, and then pass them through a softmax to
 * obtain a probability distribution over moves.
 * 
 * @author Dennis Soemers
 */
public abstract class SoftmaxPolicy extends Policy
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param move
	 * @return Logit for a single move in a single state
	 */
	public abstract float computeLogit(final Context context, final Move move);
	
	//-------------------------------------------------------------------------

}
