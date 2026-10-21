package search.mcts.nodes;

import java.util.Arrays;
import main.math.MathRoutines;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import search.mcts.MCTS;
import search.mcts.nodes.GPNMCTSNode.GPN_MCTSNodeTypes;

/**
 * Node for combined Score Bounds + GPN-MCTS tree.
 *
 * TODO we have a lot of code duplication with both the ScoreBoundsNode
 * and the GPN-MCTSNode. Should think about a way to fix this.
 */
public final class ScoreBoundsGPNMCTSNode extends IPNMCTSNode {

  //-------------------------------------------------------------------------

  /** Proof numbers for this node (one per player) */
  protected double[] proofNumbers;

  /** The player to move in this node. */
  protected int currentPlayer;

  /** Number of players in the game. */
  protected int numPlayers;

  /** The best rank that is still unclaimed. This will be the target rank (what we consider "winning" for proving nodes) in our children */
  protected final double bestAvailableRank;

  /** For every agent, a pessimistic score bound */
  private final double[] pessimisticScores;

  /** For every agent, an optimistic score bound */
  private final double[] optimisticScores;

  /**
   * We'll "soft" prune a node N (make it return very negative exploitation scores)
   * whenever, for the agent to make a move in its parent node, the pessimistic
   * score of the parent is greater than or equal to the optimistic score of N.
   */
  private boolean pruned = false;

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
  public ScoreBoundsGPNMCTSNode(
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
    numPlayers = context.game().players().count();

    proofNumbers = new double[numPlayers + 1];

    for (int p = 1; p <= numPlayers; p++) {
      proofNumbers[p] = 1.0;
    }

    if (parent != null) evaluate();

    pessimisticScores = new double[numPlayers + 1];
    optimisticScores = new double[numPlayers + 1];

    final double nextWorstScore = RankUtils.rankToUtil(
      context.computeNextLossRank(),
      numPlayers
    );
    final double nextBestScore = RankUtils.rankToUtil(
      context.computeNextWinRank(),
      numPlayers
    );
    final double[] currentUtils = RankUtils.agentUtilities(context);

    for (int p = 1; p <= numPlayers; ++p) {
      if (
        !context.active(p) // Have a proven outcome
      ) {
        pessimisticScores[p] = currentUtils[p];
        optimisticScores[p] = currentUtils[p];
      } else {
        pessimisticScores[p] = nextWorstScore;
        optimisticScores[p] = nextBestScore;
      }
    }

    // Update bounds in parents (need to do this in a separate loop for correct pruning)
    if (parent != null) {
      for (int p = 1; p <= numPlayers; ++p) {
        if (currentUtils[p] != 0.0) {
          ((ScoreBoundsGPNMCTSNode) parent).updatePessBounds(
            p,
            pessimisticScores[p],
            this
          );
          ((ScoreBoundsGPNMCTSNode) parent).updateOptBounds(
            p,
            optimisticScores[p],
            this
          );
        }
      }
    }

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

        if (rank == ((ScoreBoundsGPNMCTSNode) parent).bestAvailableRank) {
          // As we proved best available rank for p, we can immediately
          // disprove all others
          Arrays.fill(proofNumbers, Double.POSITIVE_INFINITY);

          // Proven node for player p
          proofNumbers[p] = 0.0;

          // Break out of the loop through players, as we already handled all
          break;
        } else {
          // disproven node
          proofNumbers[p] = Double.POSITIVE_INFINITY;
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
        if (
          proofNumbers[playerNum] == 0.0 ||
          proofNumbers[playerNum] == Double.POSITIVE_INFINITY
        ) continue;

        final GPN_MCTSNodeTypes playerType =
          playerNum == currentPlayer
            ? GPN_MCTSNodeTypes.OR_NODE
            : GPN_MCTSNodeTypes.AND_NODE;
        switch (playerType) {
          case AND_NODE:
            proof = 0.0;
            for (final BaseNode child : children) {
              final ScoreBoundsGPNMCTSNode childNode =
                (ScoreBoundsGPNMCTSNode) child;
              if (childNode != null) {
                proof += childNode.proofNumber(playerNum);
              } else {
                // An unexpanded child
                proof += 1.0;
              }
            }

            // If nothing changed return false
            if (this.proofNumbers[playerNum] != proof) {
              this.proofNumbers[playerNum] = proof;

              if (proof == 0.0) {
                for (int p = 1; p <= numPlayers; p++) {
                  if (p != playerNum) {
                    proofNumbers[p] = Double.POSITIVE_INFINITY;
                  }
                }
              }

              changed = true;
            }
            break;
          case OR_NODE:
            proof = Double.POSITIVE_INFINITY;

            for (final BaseNode child : children) {
              final ScoreBoundsGPNMCTSNode childNode =
                (ScoreBoundsGPNMCTSNode) child;
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
                for (int p = 1; p <= numPlayers; p++) {
                  if (p != playerNum) {
                    proofNumbers[p] = Double.POSITIVE_INFINITY;
                  }
                }
              }

              changed = true;
            }

            break;
          default:
            System.err.println(
              "Unknown node type in ScoreBoundsGPNMCTSNode.setProofNumbers()"
            );
            break;
        }
      }

      return changed;
    } else {
      // Terminal node!
      final double treatAsWinUtil = RankUtils.rankToUtil(
        ((ScoreBoundsGPNMCTSNode) parent).bestAvailableRank,
        numPlayers
      );

      for (int playerNum = 1; playerNum <= numPlayers; playerNum++) {
        if (pessimisticScores[playerNum] == treatAsWinUtil) {
          this.proofNumbers[playerNum] = 0.0;
        } else if (optimisticScores[playerNum] < treatAsWinUtil) {
          this.proofNumbers[playerNum] = Double.POSITIVE_INFINITY;
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
    return pessimisticScores[agent] == optimisticScores[agent];
  }

  @Override
  public double expectedScore(final int agent) {
    if (
      pessimisticScores[agent] == optimisticScores[agent]
    ) return pessimisticScores[agent]; // Solved this score

    return MathRoutines.clip(
      super.expectedScore(agent),
      pessimisticScores[agent],
      optimisticScores[agent]
    );
  }

  @Override
  public double exploitationScore(final int agent) {
    if (pruned && parent != null) {
      final ScoreBoundsGPNMCTSNode sbParent = (ScoreBoundsGPNMCTSNode) parent;
      if (sbParent.optBound(agent) > pessBound(agent)) return -10_000.0;
    }

    return super.exploitationScore(agent);
  }

  public int getCurrentPlayer() {
    return currentPlayer;
  }

  //-------------------------------------------------------------------------

  /**
   * One of our children has an updated pessimistic bound for the given agent;
   * check if we should also update now
   *
   * @param agent
   * @param pessBound
   * @param fromChild Child from which we receive update
   */
  public void updatePessBounds(
    final int agent,
    final double pessBound,
    final ScoreBoundsGPNMCTSNode fromChild
  ) {
    final double oldPess = pessimisticScores[agent];

    if (
      pessBound > oldPess // May be able to increase pessimistic bounds
    ) {
      final int moverAgent = contextRef()
        .state()
        .playerToAgent(contextRef().state().mover());

      if (moverAgent == agent) {
        // The agent for which one of our children has a new pessimistic bound
        // is the agent to move in this node. Hence, we can update directly
        pessimisticScores[agent] = pessBound;

        // Mark any children with an optimistic bound less than or equal to our
        // new pessimistic bound as pruned
        for (int i = 0; i < children.length; ++i) {
          final ScoreBoundsGPNMCTSNode child =
            (ScoreBoundsGPNMCTSNode) children[i];

          if (child != null) {
            if (child.optBound(agent) <= pessBound) child.markPruned();
          }
        }

        if (parent != null) ((ScoreBoundsGPNMCTSNode) parent).updatePessBounds(
          agent,
          pessBound,
          this
        );
      } else {
        // The agent for which one of our children has a new pessimistic bound
        // is NOT the agent to move in this node. Hence, we only update to
        // the minimum pessimistic bound over all children.
        //
        // Technically, if the real value (opt = pess) were proven for the
        // agent to move, we could restrict the set of children over
        // which we take the minimum to just those that have the optimal
        // value for the agent to move.
        //
        // This is more expensive to implement though, and only relevant in
        // games with more than 2 players, and there likely also only occurs very
        // rarely, so we don't bother doing this.
        double minPess = pessBound;

        for (int i = 0; i < children.length; ++i) {
          final ScoreBoundsGPNMCTSNode child =
            (ScoreBoundsGPNMCTSNode) children[i];

          if (child == null) {
            return; // Can't update anything if we have an unvisited child left
          } else {
            final double pess = child.pessBound(agent);
            if (pess < minPess) {
              if (pess == oldPess) return; // Won't be able to update

              minPess = pess;
            }
          }
        }

        if (minPess < oldPess) {
          System.err.println("ERROR in updatePessBounds()!");
          System.err.println("oldPess = " + oldPess);
          System.err.println("minPess = " + minPess);
          System.err.println("pessBound = " + pessBound);
        }

        // We can update
        pessimisticScores[agent] = minPess;
        if (parent != null) ((ScoreBoundsGPNMCTSNode) parent).updatePessBounds(
          agent,
          minPess,
          this
        );
      }
    }
  }

  /**
   * One of our children has an updated optimistic bound for the given agent;
   * check if we should also update now
   *
   * @param agent
   * @param optBound
   * @param fromChild Child from which we receive update
   */
  public void updateOptBounds(
    final int agent,
    final double optBound,
    final ScoreBoundsGPNMCTSNode fromChild
  ) {
    final int moverAgent = contextRef()
      .state()
      .playerToAgent(contextRef().state().mover());
    if (moverAgent == agent) {
      if (optBound <= pessimisticScores[agent]) {
        // The optimistic bound propagated up from the child is at best as good
        // as our pessimistic score for the agent to move in this node, so
        // we can prune the child
        fromChild.markPruned();
      }
    }

    final double oldOpt = optimisticScores[agent];

    if (
      optBound < oldOpt // May be able to decrease optimistic bounds
    ) {
      // Regardless of who the mover in this node is, any given agent's optimistic
      // bound should always just be the maximum over all their children
      double maxOpt = optBound;

      for (int i = 0; i < children.length; ++i) {
        final ScoreBoundsGPNMCTSNode child = (ScoreBoundsGPNMCTSNode) children[
          i
        ];

        if (child == null) {
          return; // Can't update anything if we have an unvisited child left
        } else {
          final double opt = child.optBound(agent);
          if (opt > maxOpt) {
            if (opt == oldOpt) return; // Won't be able to update

            maxOpt = opt;
          }
        }
      }

      if (maxOpt > oldOpt) System.err.println("ERROR in updateOptBounds()!");

      // We can update
      optimisticScores[agent] = maxOpt;
      if (parent != null) ((ScoreBoundsGPNMCTSNode) parent).updateOptBounds(
        agent,
        maxOpt,
        this
      );
    }
  }

  //-------------------------------------------------------------------------

  /**
   * @param agent
   * @return Current pessimistic bound for given agent
   */
  public double pessBound(final int agent) {
    return pessimisticScores[agent];
  }

  /**
   * @param agent
   * @return Current optimistic bound for given agent
   */
  public double optBound(final int agent) {
    return optimisticScores[agent];
  }

  /**
   * Mark this node as being "pruned"
   */
  public void markPruned() {
    pruned = true;
    //    	final ScoreBoundsNode sbParent = (ScoreBoundsNode) parent;
    //    	final int parentMover = sbParent.deterministicContextRef().state().playerToAgent(sbParent.deterministicContextRef().state().mover());
    //    	System.out.println();
    //    	System.out.println("Marked as pruned");
    //    	System.out.println("Parent agent to move = " + parentMover);
    //    	System.out.println("My pessimistic bound for agent " + parentMover + " = " + pessBound(parentMover));
    //    	System.out.println("My optimistic bound for agent " + parentMover + " = " + optBound(parentMover));
    //    	System.out.println("Parent pessimistic bound for agent " + parentMover + " = " + sbParent.pessBound(parentMover));
    //    	System.out.println("Parent optimistic bound for agent " + parentMover + " = " + sbParent.optBound(parentMover));
    //    	System.out.println("My status = " + deterministicContextRef().trial().status());
  }

  /**
   * @return Did this node get marked as "pruned"?
   */
  public boolean isPruned() {
    return pruned;
  }

  //-------------------------------------------------------------------------
}
