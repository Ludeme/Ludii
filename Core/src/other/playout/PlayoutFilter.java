package other.playout;

import java.util.List;
import java.util.Random;

import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.rules.meta.Automove;
import game.rules.meta.Gravity;
import game.rules.phase.Phase;
import game.rules.play.Play;
import game.rules.play.moves.Moves;
import game.rules.play.moves.decision.MoveSwapType;
import game.rules.play.moves.nonDecision.effect.requirement.Do;
import game.rules.play.moves.nonDecision.effect.state.swap.SwapPlayersType;
import main.Constants;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector.IsMoveReallyLegal;
import other.trial.Trial;

/**
 * Optimised playout strategy for alternating-move games with a Filter rule.
 * Also support for Filter as "else" component of an outer If, because
 * we often have this in many chess-like games (for promotion).
 * 
 * @author Dennis Soemers
 */
public class PlayoutFilter implements Playout
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
		
		final Play playRules = startPhase.play();
		final Do doRule;
		final game.rules.play.moves.nonDecision.operators.logical.If ifRule;
		final game.rules.play.moves.nonDecision.effect.Pass passRule;
		final game.rules.play.moves.nonDecision.operators.logical.Or orRule;
		
		if (playRules.moves() instanceof Do)
		{
			doRule = (Do) playRules.moves();
			ifRule = null;
			passRule = null;
			orRule = null;
		}
		else if (playRules.moves() instanceof game.rules.play.moves.nonDecision.operators.logical.If)
		{
			ifRule = (game.rules.play.moves.nonDecision.operators.logical.If) playRules.moves();
			passRule = null;
			orRule = null;
			
			if (ifRule.elseList() instanceof Do)
			{
				doRule = (Do) ifRule.elseList();
			}
			else
			{
				throw new UnsupportedOperationException
				(
					"Cannot use FilterPlayout for phase with else-rules of type: " 
					+ 
					ifRule.elseList().getClass()
				);
			}
		}
		else if (playRules.moves() instanceof game.rules.play.moves.nonDecision.operators.logical.Or)
		{
			orRule = (game.rules.play.moves.nonDecision.operators.logical.Or) playRules.moves();
			ifRule = null;
			
			if 
			(
				orRule.list().length == 2 &&
				orRule.list()[0] instanceof Do &&
				orRule.list()[1] instanceof game.rules.play.moves.nonDecision.effect.Pass
			)
			{
				doRule = (Do) orRule.list()[0];
				passRule = (game.rules.play.moves.nonDecision.effect.Pass) orRule.list()[1];
			}
			else
			{
				throw new UnsupportedOperationException("Invalid Or-rules for FilterPlayout!");
			}
		}
		else
		{
			throw new UnsupportedOperationException(
					"Cannot use FilterPlayout for phase with play rules of type: " + playRules.moves().getClass());
		}
		
		final Moves priorMoves;
		final Moves mainMovesGenerator;
		
		if (doRule.after() == null)
		{
			priorMoves = null;
			mainMovesGenerator = doRule.prior();
		}
		else
		{
			priorMoves = doRule.prior();
			mainMovesGenerator = doRule.after();
		}
		
		final BooleanFunction condition = doRule.ifAfter();
		
		int numActionsApplied = 0;
		final Trial trial = context.trial();
		while 
		(
			!trial.over() 
			&& 
			(maxNumPlayoutActions < 0 || maxNumPlayoutActions > numActionsApplied)
		)
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
				// Compute list of maybe-legal-moves and functor to filter out illegal ones
				final boolean mustCheckCondition;
				final boolean ifConditionIsTrue;
				final boolean usedDoRule;
				final Moves legalMoves;

				final Context movesGenContext;

				if (ifRule != null && ifRule.cond().eval(context))
				{
					// We should play according to if-rules (non-checkmove)
					legalMoves = ifRule.list().eval(context);
					mustCheckCondition = false;
					ifConditionIsTrue = true;
					usedDoRule = false;
					movesGenContext = context;

					if (ifRule.then() != null)
						for (int j = 0; j < legalMoves.moves().size(); j++)
							legalMoves.moves().get(j).then().add(ifRule.then().moves());
				}
				else
				{
					// We should play according to checkmove rules
					final Moves priorMovesGenerated;

					if (priorMoves != null)
					{
						movesGenContext = new Context(context);
						priorMovesGenerated = doRule.generateAndApplyPreMoves(context, movesGenContext);
					}
					else
					{
						movesGenContext = context;
						priorMovesGenerated = null;
					}

					legalMoves = mainMovesGenerator.eval(movesGenContext);
					mustCheckCondition = !(condition.autoSucceeds());
					ifConditionIsTrue = false;
					usedDoRule = true;

					if (priorMovesGenerated != null)
						Do.prependPreMoves(priorMovesGenerated, legalMoves, movesGenContext);
				}

				if (passRule != null)
				{
					// Add pass move
					legalMoves.moves().addAll(passRule.eval(movesGenContext).moves());
				}

				if (orRule != null)
				{
					// Add Or rule consequents
					if (orRule.then() != null)
						for (int j = 0; j < legalMoves.moves().size(); j++)
							legalMoves.moves().get(j).then().add(orRule.then().moves());
				}
				
				if (context.game().metaRules().usesSwapRule()
						&& trial.moveNumber() == movesGenContext.game().players().count() - 1)
				{
					final int moverLastTurn = context.trial().lastTurnMover(mover);
					if (mover != moverLastTurn && moverLastTurn != Constants.UNDEFINED)
					{
						final Moves swapMove = game.rules.play.moves.decision.Move.construct(
								MoveSwapType.Swap,
								SwapPlayersType.Players, 
								new IntConstant(mover), 
								null, 
								new IntConstant(moverLastTurn), 
								null, 
								null
								);
						legalMoves.moves().addAll(swapMove.eval(movesGenContext).moves());
					}
				}
				
				final FastArrayList<Move> moves = legalMoves.moves();
				
				if (currentGame.metaRules().automove() || currentGame.metaRules().gravityType() != null)
				{
					for (final Move legalMove : moves)
					{
						// Meta-rule: We apply the auto move rules if existing.
						Automove.apply(context, legalMove);
	
						// Meta-rule: We apply the gravity rules if existing.
						Gravity.apply(context, legalMove);
					}
				}
				
				// Functor to filter out illegal moves
				final IsMoveReallyLegal isMoveReallyLegal = (final Move m) -> { 
					if (!mustCheckCondition)
						return true;
					
					if (passRule != null && m.isPass())
						return true;
					
					if (m.isSwap())
						return true;
					
					return doRule.movePassesCond(m, movesGenContext, true); 
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
					// couldn't find a legal move, so will have to pass
					move = Game.createPassMove(context,true);

					if (context.active())
						context.state().setStalemated(mover, true);
				}
				else
				{
					// Add consequents (which shouldn't have been included yet in condition check)
					if (usedDoRule && doRule.then() != null)
						move.then().add(doRule.then().moves());

					if (ifRule != null && ifRule.then() != null && !ifConditionIsTrue)
						move.then().add(ifRule.then().moves());

					if (context.active())
						context.state().setStalemated(mover, false);
				}
			}

			if (move == null)
			{
				System.err.println("FilterPlayout.playout(): No move found.");
				break;
			}
			
//			final FastArrayList<Move> legalMoves = applyGame.moves(context).moves();
//			boolean foundLegal = false;
//			for (final Move legal : legalMoves)
//			{
//				if (legal.getAllActions(context).equals(move.getAllActions(context)))
//				{
//					foundLegal = true;
//					break;
//				}
//			}
//			
//			if (!foundLegal)
//			{
//				System.out.println("tried applying illegal move: " + move);
//				return trial;
//			}

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
