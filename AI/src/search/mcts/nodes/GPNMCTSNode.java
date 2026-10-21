package search.mcts.nodes;

import other.RankUtils;
import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;

/**
 * Node for GPN-MCTS tree (ECAI 2025 paper).
 */
public final class GPNMCTSNode extends IPNMCTSNode {

  //-------------------------------------------------------------------------

  /**
   * Nodes types in search trees in GPN-MCTS
   */
  public enum GPN_MCTSNodeTypes {
    /** An OR node */
    OR_NODE,

    /** An AND node */
    AND_NODE,
  }

  /**
   * Values of nodes in search trees in GPN-MCTS
   */
  public enum GPN_MCTSNodeValues {
    /** A proven node */
    TRUE,

    /** A disproven node */
    FALSE,

    /** Unknown node (yet to prove or disprove) */
    UNKNOWN,
  }

  //-------------------------------------------------------------------------

  /** Proof numbers for this node (one per player) */
  protected double[] proofNumbers;

  /** The value (in terms of proven/disproven/dont know) for this node (one per player) */
  protected GPN_MCTSNodeValues[] proofValue;

  /** The player to move in this node. */
  protected int currentPlayer;

  /** Number of players in the game. */
  protected int numPlayers;

  /** The best rank that is still unclaimed. This will be the target rank (what we consider "winning" for proving nodes) in our children */
  protected final double bestAvailableRank;

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
  public GPNMCTSNode(
    final MCTS mcts,
    final BaseNode parent,
    final Move parentMove,
    final Move parentMoveWithoutConseq,
    final Context context
  ) {
    super(mcts, parent, parentMove, parentMoveWithoutConseq, context);

    bestAvailableRank = context.computeNextWinRank();

    currentPlayer = context.state().mover();

    // Player 0 is not considered, players start from index 1
    numPlayers = context.trial().ranking().length - 1;

    proofNumbers = new double[numPlayers + 1];
    proofValue = new GPN_MCTSNodeValues[numPlayers + 1];

    for (int p = 1; p <= numPlayers; p++) {
      proofNumbers[p] = 1.0;
      proofValue[p] = GPN_MCTSNodeValues.UNKNOWN;
    }

    if (parent != null) evaluate();

    setProofNumbers();
  }

  //-------------------------------------------------------------------------

  /**
   * Evaluates a node as in PNS according to L. V. Allis'
   * "Searching for Solutions in Games and Artificial Intelligence"
   */
  public void evaluate() {
    for (int p = 1; p <= numPlayers; ++p) {
      if (!context.active(p)) {
        // TODO check if this handles swap rule correctly
        final double rank = context.trial().ranking()[p];

        if (rank == ((GPNMCTSNode) parent).bestAvailableRank) {
          // proven node
          proofNumbers[p] = 0.0;
          proofValue[p] = GPN_MCTSNodeValues.TRUE;
        } else {
          // disproven node
          proofNumbers[p] = Double.POSITIVE_INFINITY;
          proofValue[p] = GPN_MCTSNodeValues.FALSE;
        }
      }
    }
  }

  @Override
  public boolean setProofNumbers() {
    if (legalMoves.length > 0) {
      // Not a terminal node
      double proof;
      boolean changed = false;

      for (int playerNum = 1; playerNum <= numPlayers; playerNum++) {
        if (proofValue[playerNum] != GPN_MCTSNodeValues.UNKNOWN) continue;

        final GPN_MCTSNodeTypes playerType =
          playerNum == currentPlayer
            ? GPN_MCTSNodeTypes.OR_NODE
            : GPN_MCTSNodeTypes.AND_NODE;
        switch (playerType) {
          case AND_NODE:
            proof = 0.0;
            for (final BaseNode child : children) {
              final GPNMCTSNode childNode = (GPNMCTSNode) child;
              if (childNode != null) {
                proof += childNode.proofNumber(playerNum);
              } else {
                // An unexpanded child
                proof += 1.0;
              }
            }

            if (this.proofNumbers[playerNum] != proof) {
              this.proofNumbers[playerNum] = proof;

              if (proof == 0.0) {
                proofValue[playerNum] = GPN_MCTSNodeValues.TRUE;
                for (int p = 1; p <= numPlayers; p++) {
                  if (p != playerNum) {
                    proofValue[p] = GPN_MCTSNodeValues.FALSE;
                    proofNumbers[p] = Double.POSITIVE_INFINITY;
                  }
                }
              } else if (proof == Double.POSITIVE_INFINITY) {
                proofValue[playerNum] = GPN_MCTSNodeValues.FALSE;
              }

              changed = true;
            }
            break;
          case OR_NODE:
            proof = Double.POSITIVE_INFINITY;

            for (final BaseNode child : children) {
              final GPNMCTSNode childNode = (GPNMCTSNode) child;
              if (childNode != null) {
                if (childNode.proofNumber(playerNum) < proof) {
                  proof = childNode.proofNumber(playerNum);
                }
              } else {
                // An unexpanded child
                proof = Math.min(1.0, proof);
              }
            }

            if (this.proofNumbers[playerNum] != proof) {
              this.proofNumbers[playerNum] = proof;

              if (proof == 0.0) {
                proofValue[playerNum] = GPN_MCTSNodeValues.TRUE;
                for (int p = 1; p <= numPlayers; p++) {
                  if (p != playerNum) {
                    proofValue[p] = GPN_MCTSNodeValues.FALSE;
                    proofNumbers[p] = Double.POSITIVE_INFINITY;
                  }
                }
              } else if (proof == Double.POSITIVE_INFINITY) {
                proofValue[playerNum] = GPN_MCTSNodeValues.FALSE;
              }

              changed = true;
            }

            break;
          default:
            System.err.println(
              "Unknown node type in GPNMCTSNode.setProofNumbers()"
            );
            break;
        }
      }

      return changed;
    } else {
      // Terminal node!

      for (int playerNum = 1; playerNum <= numPlayers; playerNum++) {
        switch (proofValue[playerNum]) {
          case FALSE:
            this.proofNumbers[playerNum] = Double.POSITIVE_INFINITY;
            break;
          case TRUE:
            this.proofNumbers[playerNum] = 0.0;
            break;
          case UNKNOWN:
            System.err.println(
              "Terminal node has UNKNOWN proof value in GPNMCTSNode!"
            );
            break;
          default:
            System.err.println(
              "Unknown proof value in GPNMCTSNode.setProofNumbers()"
            );
            break;
        }
      }
    }

    // If we haven't expanded yet it will definitely be changed so return true
    return true;
  }

  /**
   * @return Current proof number for this node.
   */
  public double proofNumber(final int player) {
    return proofNumbers[player];
  }

  //-------------------------------------------------------------------------

  @Override
  public boolean isValueProven(final int agent) {
    return proofValue[agent] == GPN_MCTSNodeValues.TRUE;
  }

  @Override
  public double expectedScore(final int agent) {
    if (proofValue[agent] == GPN_MCTSNodeValues.TRUE && parent != null) {
      //System.out.println("returning " + RankUtils.rankToUtil(((MP_PNMCTSNode) parent).bestAvailableRank, numPlayers) + " instead of " + super.expectedScore(agent));
      return RankUtils.rankToUtil(
        ((GPNMCTSNode) parent).bestAvailableRank,
        numPlayers
      );
    }

    return super.expectedScore(agent);
  }

  public int getCurrentPlayer() {
    return currentPlayer;
  }

  //-------------------------------------------------------------------------
}
