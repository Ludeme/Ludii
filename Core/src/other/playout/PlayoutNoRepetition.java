package other.playout;

import java.util.List;
import java.util.Random;

import game.Game;
import game.functions.ints.IntConstant;
import game.rules.meta.no.repeat.NoRepeat;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import game.rules.play.moves.decision.MoveSwapType;
import game.rules.play.moves.nonDecision.effect.state.swap.SwapPlayersType;
import main.Constants;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector.IsMoveReallyLegal;
import other.trial.Trial;

/**
 * Optimised playout strategy for alternating-move games with 
 * no-repetition rules. 
 * 
 * @author Dennis Soemers
 */
public class PlayoutNoRepetition implements Playout
{

	@Override
	public Trial playout
	(
		final Context context, 
		final List<AI> ais, 
		final double thinkingTime, 
		final PlayoutMoveSelector playoutMoveSelector, 
		final int maxNumBiasedActions, 
		final int maxNumPlayoutActions, 
		final Random random
	)
	{
		final Game currentGame = context.game();
		final Phase startPhase = currentGame.rules().phases()[context.state().currentPhase(context.state().mover())];
		final Moves movesRule = startPhase.play().moves();
		
		int numActionsApplied = 0;
		final Trial trial = context.trial();
		while (!trial.over() && (maxNumPlayoutActions < 0 || maxNumPlayoutActions > numActionsApplied))
		{
			Move move = null;
			AI ai = null;
			final int mover = context.state().mover();
			
			final Phase currPhase = currentGame.rules().phases()[context.state().currentPhase(mover)];
			
			if (currPhase != startPhase)
			{
				// May have to switch over to new playout implementation
				return trial;
			}

			if (ais != null)
			{
				ai = ais.get(context.state().playerToAgent(mover));
			}
			
			if (ai != null)
			{
				// Make AI move
				move = ai.selectAction(currentGame, ai.copyContext(context), thinkingTime, -1, -1);
			}
			else
			{
				// Compute list of maybe-legal-moves
				final Moves legalMoves = movesRule.eval(context);
					
				if 
				(
					context.game().metaRules().usesSwapRule()
					&& 
					trial.moveNumber() == context.game().players().count() - 1
				)
				{
					final int moverLastTurn = context.trial().lastTurnMover(mover);
					if (mover != moverLastTurn && moverLastTurn != Constants.UNDEFINED)
					{
						final Moves swapMove = 
								game.rules.play.moves.decision.Move.construct
								(
									MoveSwapType.Swap,
									SwapPlayersType.Players, 
									new IntConstant(mover), 
									null, 
									new IntConstant(moverLastTurn), 
									null, 
									null
								);
						legalMoves.moves().addAll(swapMove.eval(context).moves());
					}
				}

				final FastArrayList<Move> moves = legalMoves.moves();
				
				// Functor to filter out illegal moves
				final IsMoveReallyLegal isMoveReallyLegal = 
					(final Move m) -> 
					{ 
						return NoRepeat.apply(context, m); 
					};
					
				if 
				(
					playoutMoveSelector == null 
					|| 
					(maxNumBiasedActions >= 0 && maxNumBiasedActions < numActionsApplied) 
					|| 
					playoutMoveSelector.wantsPlayUniformRandomMove()
				)
				{
					// Select move uniformly at random
					move = PlayoutMoveSelector.selectUniformlyRandomMove(context, moves, isMoveReallyLegal, random);
				}
				else
				{
					// Let our playout move selector pick a move
					move = playoutMoveSelector.selectMove(context, moves, mover, isMoveReallyLegal);
				}
				
				if (move == null)
				{
					// Couldn't find a legal move, so will have to pass
					move = Game.createPassMove(context,true);

					if (context.active())
						context.state().setStalemated(mover, true);
				}
				else
				{
					if (context.active())
						context.state().setStalemated(mover, false);
				}
			}

			if (move == null)
			{
				System.err.println("NoRepetitionPlayout.playout(): No move found.");
				break;
			}

			currentGame.apply(context, move);
			++numActionsApplied;
		}
		
		return trial;
	}
	
	@Override
	public boolean callsGameMoves()
	{
		return false;
	}

}
