package search.mcts.finalmoveselection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import main.collections.FVector;
import other.move.Move;
import other.state.State;
import search.mcts.MCTS;
import search.mcts.nodes.BaseNode;

/**
 * Selects move corresponding to the most robust child (highest visit count),
 * with an additional tie-breaker based on value estimates. If the MCTS
 * has a learned selection policy, that can be used as a second tie-breaker.
 *
 * EXCEPTION: if the child with the best expected value (*not* visits) has its
 * value proven, we will prioritise that child.
 * TODO current implementation around proofs seems kind of questionable.
 *
 * @author Dennis Soemers
 */
public final class RobustChild implements FinalMoveSelectionStrategy {

  //-------------------------------------------------------------------------

  @Override
  public Move selectMove(final MCTS mcts, final BaseNode rootNode) {
    final List<Move> bestActions = new ArrayList<Move>();
    double bestActionValueEstimate = Double.NEGATIVE_INFINITY;
    float bestActionPolicyPrior = Float.NEGATIVE_INFINITY;
    final State rootState = rootNode.contextRef().state();
    final int moverAgent = rootState.playerToAgent(rootState.mover());
    int maxNumVisits = Integer.MIN_VALUE;
    boolean bestValueIsProven = false;

    final FVector priorPolicy;
    if (mcts.learnedSelectionPolicy() == null) priorPolicy = null;
    else priorPolicy = rootNode.learnedSelectionPolicy();

    final int numChildren = rootNode.numLegalMoves();
    for (int i = 0; i < numChildren; ++i) {
      final BaseNode child = rootNode.childForNthLegalMove(i);
      final int numVisits = child == null ? 0 : child.numVisits();
      final double childValueEstimate =
        child == null ? 0.0 : child.expectedScore(moverAgent);
      final float childPriorPolicy =
        priorPolicy == null ? -1.f : priorPolicy.get(i);
      final boolean childProven =
        child == null ? false : child.isValueProven(moverAgent);

      if (childProven && childValueEstimate >= bestActionValueEstimate) {
        // Value of this child is proven, and it's at least as good as any other value:
        // this means we no longer care about visit counts
        bestValueIsProven = true;

        if (childValueEstimate > bestActionValueEstimate) {
          // Strictly better value: only keep this action
          maxNumVisits = numVisits;
          bestActions.clear();
          bestActionValueEstimate = childValueEstimate;
          bestActions.add(rootNode.nthLegalMove(i));
        } else {
          // Tie between multiple equally good proven children: keep them all
          maxNumVisits = Math.max(maxNumVisits, numVisits);
          bestActions.add(rootNode.nthLegalMove(i));
        }
      } else if (
        !bestValueIsProven || childValueEstimate > bestActionValueEstimate
      ) {
        bestValueIsProven = false;

        // Only consider any other cases if our best-found value is not proven
        if (numVisits > maxNumVisits) {
          maxNumVisits = numVisits;
          bestActions.clear();
          bestActionValueEstimate = childValueEstimate;
          bestActionPolicyPrior = childPriorPolicy;
          bestActions.add(rootNode.nthLegalMove(i));
        } else if (numVisits == maxNumVisits) {
          if (childValueEstimate > bestActionValueEstimate) {
            // Tie-breaker; prefer higher value estimates
            bestActions.clear();
            bestActionValueEstimate = childValueEstimate;
            bestActionPolicyPrior = childPriorPolicy;
            bestActions.add(rootNode.nthLegalMove(i));
          } else if (childValueEstimate == bestActionValueEstimate) {
            // Tie for both num visits and also for estimated value; prefer higher prior policy
            if (childPriorPolicy > bestActionPolicyPrior) {
              bestActions.clear();
              bestActionValueEstimate = childValueEstimate;
              bestActionPolicyPrior = childPriorPolicy;
              bestActions.add(rootNode.nthLegalMove(i));
            } else if (childPriorPolicy == bestActionPolicyPrior) {
              // Tie for everything
              bestActions.add(rootNode.nthLegalMove(i));
            }
          }
        }
      }
    }

    return bestActions.get(
      ThreadLocalRandom.current().nextInt(bestActions.size())
    );
  }

  //-------------------------------------------------------------------------

  @Override
  public void customise(final String[] inputs) {
    // Do nothing
  }

  //-------------------------------------------------------------------------
}
