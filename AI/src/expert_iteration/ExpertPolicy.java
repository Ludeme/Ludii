package expert_iteration;

import main.collections.FVector;
import main.collections.FastArrayList;
import other.AI;
import other.move.Move;

/**
 * Abstract class for policies that can serve as experts in Expert Iteration
 * 
 * @author Dennis Soemers
 */
public abstract class ExpertPolicy extends AI
{
	
	/**
	 * @return Should return a list of the moves considered at the 
	 * 	"root" state during the last search executed by this expert.
	 */
	public abstract FastArrayList<Move> lastSearchRootMoves();
	
	/**
	 * @param tau Temperature parameter that may or may not be used
	 * 	by some experts. For MCTS, tau = 1.0 means proportional to
	 * 	visit counts, whereas tau --> 0.0 means greedy with respect
	 * 	to visit counts.
	 * @return Policy / distribution over actions as computed by expert
	 */
	public abstract FVector computeExpertPolicy(final double tau);
	
	/**
	 * @return A sample of experience for Expert Iteration, based on
	 * 	the last search executed by this expert.
	 */
	public abstract ExItExperience generateExItExperience();

}
