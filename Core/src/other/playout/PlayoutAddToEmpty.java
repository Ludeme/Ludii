package other.playout;

import java.util.List;
import java.util.Random;

import game.Game;
import game.functions.ints.IntConstant;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import game.rules.play.moves.decision.MoveSwapType;
import game.rules.play.moves.nonDecision.effect.state.swap.SwapPlayersType;
import game.types.board.SiteType;
import main.Constants;
import main.collections.FastArrayList;
import other.AI;
import other.Sites;
import other.action.Action;
import other.action.move.ActionAdd;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * @author cambolbro
 */
public class PlayoutAddToEmpty implements Playout
{
	
	//-------------------------------------------------------------------------
	
	/** Move cache (indexed by component first, site second) */
	private Move[][] moveCache = null;
	
	/** The Site Type we want to add to */
	private final SiteType type;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param type
	 */
	public PlayoutAddToEmpty(final SiteType type)
	{
		this.type = type;
	}
	
	//-------------------------------------------------------------------------

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
		
		// make sure we have an action cache
		if (moveCache == null)
		{
			moveCache = new Move[currentGame.players().count() + 1][currentGame.board().topology().numSites(type)];
		}
		
		// Get empty board region
		// convert to Sites representation because we can more efficiently
		// fetch random entries from that representation
		final Sites sites = new Sites(context.state().containerStates()[0].emptyRegion(type).sites());
		
		final Phase startPhase = currentGame.rules().phases()[context.state().currentPhase(context.state().mover())];
		
		int numActionsApplied = 0;
		
		double probSwap = 0.0;
		
		final Trial trial = context.trial();
		while (!trial.over() && (maxNumPlayoutActions < 0 || maxNumPlayoutActions > numActionsApplied))
		{
			final int remaining = sites.count();
			final int mover = context.state().mover();
			
			final Phase currPhase = currentGame.rules().phases()[context.state().currentPhase(mover)];
			
			if (currPhase != startPhase)
			{
				// May have to switch over to new playout implementation
				return trial;
			}

			if (remaining < 1)
			{
				// No more moves: automatic pass
				if (context.active())
					context.state().setStalemated(mover, true);
				
				// System.out.println("Game.playout(): No legal moves for player " +
				// context.trial().state().mover() + ".");
				currentGame.apply(context, Game.createPassMove(context,true));
				++numActionsApplied;
				continue;
			}
			else
			{
				if (context.active())
					context.state().setStalemated(mover, false);
			}
			
			final boolean canSwap = (context.game().metaRules().usesSwapRule()
					&& trial.moveNumber() == currentGame.players().count() - 1);
			
			if (canSwap)
			{
				probSwap = 1.0 / (remaining + 1);
			}

			AI ai = null;
			if (ais != null)
			{
				ai = ais.get(context.state().playerToAgent(mover));
			}

			final Move move;
			if (ai != null)
			{
				// make AI move
				move = ai.selectAction(context.game(), ai.copyContext(context), thinkingTime, -1, -1);
				
				if (!move.isSwap())
					sites.remove(move.from());
			}
			else
			{
				final Move[] playerMoveCache = moveCache[mover];
				
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
					final int n = random.nextInt(remaining);
					final int site = sites.nthValue(n);
					
					if (playerMoveCache[site] == null)
					{
						final Action actionAdd = new ActionAdd(type, site, mover, 1, Constants.UNDEFINED,
								Constants.UNDEFINED, Constants.UNDEFINED, null);
						actionAdd.setDecision(true);
						move = new Move(actionAdd);
						move.setFromNonDecision(site);
						move.setToNonDecision(site);
						move.setMover(mover);
						
						assert (currPhase.play().moves().then() == null);
						
						playerMoveCache[site] = move;
					} 
					else
					{
						move = playerMoveCache[site];
					}
					
					sites.removeNth(n);
				}
				else
				{
					// Let our playout move selector pick a move
					final FastArrayList<Move> legalMoves = new FastArrayList<Move>(remaining + 1);
					for (int i = 0; i < remaining; ++i)
					{
						final int site = sites.nthValue(i);
						if (playerMoveCache[site] == null)
						{
							final Action actionAdd = new ActionAdd(type, site, mover, 1, Constants.UNDEFINED,
								Constants.UNDEFINED, Constants.UNDEFINED, null);
							actionAdd.setDecision(true);
							final Move m = new Move(actionAdd);
							m.setFromNonDecision(site);
							m.setToNonDecision(site);
							m.setMover(mover);
							
							assert (currPhase.play().moves().then() == null);
							
							playerMoveCache[site] = m;
							legalMoves.add(m);
						}
						else
						{
							legalMoves.add(playerMoveCache[site]);
						}
					}
					
					if (canSwap)
					{
						// Add a swap move
						final int moverLastTurn = context.trial().lastTurnMover(mover);
						if(mover != moverLastTurn && moverLastTurn != Constants.UNDEFINED)
						{
							final Moves swapMove = game.rules.play.moves.decision.Move.construct(
									MoveSwapType.Swap,
									SwapPlayersType.Players, 
									new IntConstant(mover), 
									null, 
									new IntConstant(moverLastTurn), 
									null, 
									null // new Then(replay, null)
									);
							legalMoves.addAll(swapMove.eval(context).moves());
						}
					}
					
					move = playoutMoveSelector.selectMove(context, legalMoves, mover, (final Move m) -> {return true;});
					
					if (move.isSwap())
					{
						// Since we're already executing the swap move here,
						// set prob of swapping at end of playout to 0
						assert (canSwap);
						probSwap = 0.0;
					}
					else
					{
						sites.remove(move.to());
					}
				}
			}

			currentGame.apply(context, move);
			++numActionsApplied;
		}
		
		if (random.nextDouble() < probSwap)
		{
			// we actually should have swapped players in turn 2, do so now
			assert (currentGame.players().count() == 2);
			context.state().swapPlayerOrder(1, 2);
		}

		return trial;
	}
	
	@Override
	public boolean callsGameMoves()
	{
		return false;
	}
}
