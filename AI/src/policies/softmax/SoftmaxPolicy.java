package policies.softmax;

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
	
	/** Epsilon for epsilon-greedy playouts */
	protected double epsilon = 0.0;
	
	/** 
	 * If >= 0, we'll only actually use this softmax policy in MCTS play-outs
	 * for up to this many actions. If a play-out still did not terminate
	 * after this many play-out actions, we revert to a random play-out
	 * strategy as fallback
	 */
	protected int playoutActionLimit = -1;
	
	/** Auto-end playouts in a draw if they take more turns than this */
	protected int playoutTurnLimit = -1;
	
	//-------------------------------------------------------------------------

}
