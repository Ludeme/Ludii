package search.mcts.nodes;

import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;

/**
 * Common interface for various types of PN-MCTS nodes
 *
 * @author Dennis Soemers
 */
public abstract class IPNMCTSNode extends DeterministicNode {

  //-------------------------------------------------------------------------

  /** Are the cached PNS-based terms of childrens' selection scores outdated? */
  protected boolean childSelectionScoresDirty = false;

  /** Cached PNS-based terms for selection scores for all our children (including unexpanded ones) */
  protected final double[] childrenPNSSelectionTerms;

  //-------------------------------------------------------------------------

  /**
   * Constructor
   *
   * @param mcts
   * @param parent
   * @param parentMove
   * @param parentMoveWithoutConseq
   * @param context
   */
  public IPNMCTSNode(
    final MCTS mcts,
    final BaseNode parent,
    final Move parentMove,
    final Move parentMoveWithoutConseq,
    final Context context
  ) {
    super(mcts, parent, parentMove, parentMoveWithoutConseq, context);
    childrenPNSSelectionTerms = new double[numLegalMoves()];
  }

  //-------------------------------------------------------------------------

  /**
   * Sets the proof values of the current node as it is done for
   * PNS in L. V. Allis' "Searching for Solutions in Games and Artificial
   * Intelligence". Set differently depending on if the node has children yet.
   *
   * @return Returns true if something was changed and false if not.
   * Used to improve GPN-MCTS speed.
   */
  public abstract boolean setProofNumbers();

  /**
   * @return Do our childrens' PNS-based selection terms need updating?
   */
  public boolean childSelectionScoresDirty() {
    return childSelectionScoresDirty;
  }

  /**
   * Store a flag saying whether the cached PNS-based terms of our childrens'
   * selection scores are (potentially) outdated.
   *
   * @param newFlag
   */
  public void setSelectionScoresDirtyFlag(final boolean newFlag) {
    childSelectionScoresDirty = newFlag;
  }

  /**
   * @return Array of PNS-based terms for selection strategy for all of our children.
   */
  public double[] childrenPNSSelectionTerms() {
    return childrenPNSSelectionTerms;
  }

  //-------------------------------------------------------------------------
}
