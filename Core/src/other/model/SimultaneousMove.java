package other.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import game.rules.play.moves.Moves;
import main.collections.FastArrayList;
import other.AI;
import other.ThinkingThread;
import other.action.Action;
import other.action.others.ActionPass;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;

/**
 * Model for simultaneous-move games.
 *
 * @author Dennis Soemers
 */
public final class SimultaneousMove extends Model
{

	//-------------------------------------------------------------------------

	/** Are we ready with our current step? */
	protected transient boolean ready = true;

	/** Are we ready to receive external actions? */
	protected transient boolean running = false;

	/** Moves selected per player so far */
	protected transient Move[] movesPerPlayer = null;

	/** Currently-running thinking threads */
	protected transient ThinkingThread[] currentThinkingThreads = null;

	/** AIs used to select a move in the last step */
	protected transient AI[] lastStepAIs;

	/** Moves selected in the last step */
	protected transient Move[] lastStepMoves;
	
	/** Callback function to call before agents apply moves */
	protected transient AgentMoveCallback preAgentMoveCallback;

	/** Callback function to call after agents apply moves */
	protected transient AgentMoveCallback postAgentMoveCallback;

	//-------------------------------------------------------------------------

	@Override
	public Move applyHumanMove(final Context context, final Move move, final int player)
	{
		if (currentThinkingThreads[player] != null)
		{
			// Don't interrupt AI
			return null;
		}

		if (movesPerPlayer[player] == null)
		{
			addMoveForPlayer(context, move, player);
			return move;
		}

		return null;
	}

	@Override
	public Model copy()
	{
		return new SimultaneousMove();
	}

	@Override
	public boolean expectsHumanInput()
	{
		if (!ready && running)
		{
			for (int p = 1; p < currentThinkingThreads.length; ++p)
			{
				if (currentThinkingThreads[p] == null && movesPerPlayer[p] == null)
					return true;
			}

			return false;
		}
		else
		{
			return false;
		}
	}

	@Override
	public synchronized void interruptAIs()
	{
		if (!ready)
		{
			final List<AI> interruptedAIs = new ArrayList<>();
			boolean stillHaveLiveAIs = false;

			for (int p = 1; p < currentThinkingThreads.length; ++p)
			{
				if (currentThinkingThreads[p] != null)
				{
					final AI ai = currentThinkingThreads[p].interruptAI();

					if (ai != null)
						interruptedAIs.add(ai);
				}
			}

			stillHaveLiveAIs = true;

			while (stillHaveLiveAIs)
			{
				stillHaveLiveAIs = false;
				for (int p = 1; p < currentThinkingThreads.length; ++p)
				{
					if (currentThinkingThreads[p] != null)
					{
						if (currentThinkingThreads[p].isAlive())
						{
							stillHaveLiveAIs = true;
							break;
						}
						else
						{
							currentThinkingThreads[p] = null;
						}
					}
				}

				if (stillHaveLiveAIs)
				{
					try
					{
						Thread.sleep(15L);
					}
					catch (final InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}

			for (final AI ai : interruptedAIs)
			{
				ai.setWantsInterrupt(false);
			}

			lastStepAIs = new AI[lastStepAIs.length];
			lastStepMoves = new Move[lastStepMoves.length];
			ready = true;
			running = false;
		}
	}

	@Override
	public List<AI> getLastStepAIs()
	{
		if (!ready)
			return null;

		return Arrays.asList(lastStepAIs);
	}

	@Override
	public List<Move> getLastStepMoves()
	{
		if (!ready)
			return null;

		return Arrays.asList(lastStepMoves);
	}

	@Override
	public boolean isReady()
	{
		return ready;
	}

	@Override
	public boolean isRunning()
	{
		return running;
	}

	@Override
	public synchronized void randomStep
	(
		final Context context, 
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback
	)
	{
		if (!ready)
		{
			final FastArrayList<Move> legalMoves = context.game().moves(context).moves();

			for (int p = 1; p < currentThinkingThreads.length; ++p)
			{
				if (currentThinkingThreads[p] == null && movesPerPlayer[p] == null)
				{
					final FastArrayList<Move> playerMoves = new FastArrayList<>(legalMoves.size());

					for (final Move move : legalMoves)
					{
						if (move.mover() == p)
							playerMoves.add(move);
					}

					if (playerMoves.size() == 0)
					{
						final ActionPass actionPass = new ActionPass(true);
						actionPass.setDecision(true);
						final Move passMove = new Move(actionPass);
						passMove.setMover(p);
						
						if (inPreAgentMoveCallback != null)
							inPreAgentMoveCallback.call(passMove);
						
						applyHumanMove(context, passMove, p);
						
						if (inPostAgentMoveCallback != null)
							inPostAgentMoveCallback.call(passMove);
						
						return;
					}

					final int r = ThreadLocalRandom.current().nextInt(playerMoves.size());
					final Move move = playerMoves.get(r);
					
					if (inPreAgentMoveCallback != null)
						inPreAgentMoveCallback.call(move);
					
					applyHumanMove(context, move, p);

					if (inPostAgentMoveCallback != null)
						inPostAgentMoveCallback.call(move);
				}
			}
		}
	}

	//-------------------------------------------------------------------------
	
	@Override
	public synchronized void startNewStep
	(
		final Context context,
		final List<AI> ais,
		final double[] maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds,
		final boolean block,
		final boolean forceThreaded,
		final boolean forceNotThreaded,
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback
	)
	{
		startNewStep
		(
			context, ais, maxSeconds, maxIterations, maxSearchDepth,
			minSeconds, block, forceThreaded, forceNotThreaded,
			inPreAgentMoveCallback, inPostAgentMoveCallback, false, null
		);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public synchronized void startNewStep
	(
		final Context context,
		final List<AI> ais,
		final double[] maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds,
		final boolean block,
		final boolean forceThreaded,
		final boolean forceNotThreaded,
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback,
		final boolean checkMoveValid,
		final MoveMessageCallback moveMessageCallback
	)
	{
		if (!ready)
		{
			// we're already running our current step, so don't start a new one
			return;
		}

		ready = false;
		preAgentMoveCallback = inPreAgentMoveCallback;
		postAgentMoveCallback = inPostAgentMoveCallback;

		final int numPlayers = context.game().players().count();
		movesPerPlayer = new Move[numPlayers + 1];
		lastStepAIs = new AI[numPlayers + 1];
		lastStepMoves = new Move[numPlayers + 1];
		currentThinkingThreads = new ThinkingThread[numPlayers + 1];

		// compute legal moves once, then the list of legal moves can get
		// copied into copies of trials for different AIs (more efficient)
		final FastArrayList<Move> legalMoves = context.game().moves(context).moves();

		if (block && forceNotThreaded)
		{
			// we're not allowed to use threads, so compute moves one by one here
			for (int p = 1; p <= numPlayers; ++p)
			{
				if (context.active(p))
				{
					lastStepAIs[p] = ais.get(p);

					Move move = ais.get(p).selectAction
							(
								context.game(),
								ais.get(p).copyContext(context),
								maxSeconds[p],
								maxIterations,
								maxSearchDepth
							);
					
					move = checkMoveValid(checkMoveValid, context, move, p, moveMessageCallback);
					
					movesPerPlayer[p] = move;
					lastStepMoves[p] = move;
				}
			}

			// now we should be ready to advance state
			applyCombinedMove(context);
		}
		else
		{
			// we're allowed to use threads to have any AIs run simultaneously
			for (int p = 1; p <= numPlayers; ++p)
			{
				final AI agent;
				if (ais == null || p >= ais.size())
					agent = null;
				else
					agent = ais.get(p);

				if (ais != null)
					lastStepAIs[p] = agent;

				if (context.active(p) && agent != null)
				{
					currentThinkingThreads[p] =
							ThinkingThread.construct
							(
								agent,
								context.game(),
								agent.copyContext(context),
								maxSeconds[p],
								maxIterations,
								maxSearchDepth,
								minSeconds,
								createPostThinking(context, block, p, checkMoveValid, moveMessageCallback)
							);
					currentThinkingThreads[p].setDaemon(true);
					currentThinkingThreads[p].start();
				}
				else if (context.active(p))
				{
					// a human; auto-pass if no legal moves for this player
					boolean humanHasMoves = false;
					for (final Move move : legalMoves)
					{
						if (move.mover() == p)
						{
							humanHasMoves = true;
							break;
						}
					}

					if (!humanHasMoves)
					{
						final ActionPass actionPass = new ActionPass(true);
						actionPass.setDecision(true);
						final Move passMove = new Move(actionPass);
						passMove.setMover(p);
						
						if (inPreAgentMoveCallback != null)
							inPreAgentMoveCallback.call(passMove);
						
						addMoveForPlayer(context, passMove, p);
						
						if (inPostAgentMoveCallback != null)
							inPostAgentMoveCallback.call(passMove);
					}
				}
			}

			if (block)
			{
				// We're allowed to block, so let's wait until all threads are done
				boolean threadsAlive = true;
				while (threadsAlive)
				{
					threadsAlive = false;

					for (int p = 1; p < currentThinkingThreads.length; ++p)
					{
						if (currentThinkingThreads[p] != null && currentThinkingThreads[p].isAlive())
						{
							threadsAlive = true;
							break;
						}
					}
				}

				// all threads done, extract their moves
				// no need to go through the synchronized method in this case
				for (int p = 1; p < currentThinkingThreads.length; ++p)
				{
					if (currentThinkingThreads[p] != null)
					{
						movesPerPlayer[p] = currentThinkingThreads[p].move();
						currentThinkingThreads[p] = null;
						lastStepMoves[p] = movesPerPlayer[p];
					}
				}

				// now we should be ready to advance state
				applyCombinedMove(context);
			}
			else
			{
				running = true;
			}
		}
	}

	@Override
	public void unpauseAgents
	(
		final Context context,
		final List<AI> ais,
		final double[] maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds,
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback,
		final boolean checkMoveValid,
		final MoveMessageCallback moveMessageCallback
	)
	{
		final int numPlayers = context.game().players().count();
		currentThinkingThreads = new ThinkingThread[numPlayers + 1];
		preAgentMoveCallback = inPreAgentMoveCallback;
		postAgentMoveCallback = inPostAgentMoveCallback;

		for (int p = 1; p <= numPlayers; ++p)
		{
			final AI agent;
			if (ais == null || p >= ais.size())
				agent = null;
			else
				agent = ais.get(p);

			if (ais != null)
				lastStepAIs[p] = agent;

			if (context.active(p) && agent != null && movesPerPlayer[p] == null)
			{
				currentThinkingThreads[p] =
						ThinkingThread.construct
						(
							agent,
							context.game(),
							agent.copyContext(context),
							maxSeconds[p],
							maxIterations,
							maxSearchDepth,
							minSeconds,
							createPostThinking(context, false, p, checkMoveValid, moveMessageCallback)
						);
				currentThinkingThreads[p].setDaemon(true);
				currentThinkingThreads[p].start();
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public List<AI> getLiveAIs()
	{
		final List<AI> ais = new ArrayList<>(currentThinkingThreads.length);

		for (final ThinkingThread thinkingThread : currentThinkingThreads)
			if (thinkingThread != null)
				ais.add(thinkingThread.ai());

		return ais;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean verifyMoveLegal(final Context context, final Move move)
	{
		boolean validMove = false;
		final FastArrayList<Move> legal = new FastArrayList<Move>(context.game().moves(context).moves());
		
		final List<Action> moveActions = move.getActionsWithConsequences(context);
		
		final int mover = move.mover();
		boolean noLegalMoveForMover = true;

		for (final Move m : legal)
		{			
			if (movesEqual(move, moveActions, m, context))
			{
				validMove = true;
				break;
			}
			else if (m.mover() == mover)
			{
				noLegalMoveForMover = false;
			}
		}

		if (noLegalMoveForMover && move.isPass())
			validMove = true;

		return validMove;
	}

	//-------------------------------------------------------------------------

	/**
	 * Adds move selected by player
	 *
	 * @param context
	 * @param move
	 * @param p
	 */
	void addMoveForPlayer(final Context context, final Move move, final int p)
	{
		movesPerPlayer[p] = move;
		lastStepMoves[p] = move;
		maybeApplyCombinedMove(context);
	}

	/**
	 * Checks if we have all the moves we need, and if so, applies the combined move.
	 * Synchronized for thread-safety.
	 * @param context
	 */
	private synchronized void maybeApplyCombinedMove(final Context context)
	{
		// only do something if we're not ready, otherwise we've already done this
		if (!ready)
		{
			final int numPlayers = context.game().players().count();

			// check if we have moves for all active players now
			for (int i = 1; i <= numPlayers; ++i)
			{
				if (context.active(i) && movesPerPlayer[i] == null)
				{
					// still missing at least one move, so we return
					return;
				}
			}

			// we have all the moves we need, so we can apply our combined move
			applyCombinedMove(context);
		}
	}

	/**
	 * Applies combined move and sets the model as being ready, having completed
	 * processing for the current time step.
	 * @param context
	 */
	private void applyCombinedMove(final Context context)
	{
		// gather all the actions into a single move
		final List<Action> actions = new ArrayList<>();
		final List<Moves> topLevelCons = new ArrayList<Moves>();
		
		int numSubmoves = 0;
		for (int p = 1; p < movesPerPlayer.length; ++p)
		{
			final Move move = movesPerPlayer[p];
			if (move != null)
			{
				final Move moveToAdd = new Move(move.actions());
				actions.add(moveToAdd);
				++numSubmoves;

				if (move.then() != null)
				{
					for (int i = 0; i < move.then().size(); ++i)
					{
						if (move.then().get(i).applyAfterAllMoves())
							topLevelCons.add(move.then().get(i));
						else
							moveToAdd.then().add(move.then().get(i));
					}
				}
			}
		}
		
		final Move combinedMove = new Move(actions);
		combinedMove.setMover(movesPerPlayer.length);
		combinedMove.then().addAll(topLevelCons);
		
		//System.out.println(combinedMove.toDetailedString(context));

		// apply move
		context.game().apply(context, combinedMove);
		context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + numSubmoves);
		
		// clear movesPerPlayer
		Arrays.fill(movesPerPlayer, null);

		// now we're done with this time step
		ready = true;
		running = false;
	}

	/**
	 * @param context
	 * @param p
	 * @return A post-thinking callback for an AI thinking thread, or null
	 * if we're allowed to block (then we don't need callback)
	 */
	private Runnable createPostThinking
	(	
		final Context context, final boolean block, final int p, 
		final boolean checkMoveValid, final MoveMessageCallback callBack
	)
	{
		if (block)
			return null;
		
		return () -> 
		{
			Move move = currentThinkingThreads[p].move();
			currentThinkingThreads[p] = null;
			
			move = checkMoveValid(checkMoveValid, context, move, p, callBack);

			if (preAgentMoveCallback != null)
				preAgentMoveCallback.call(move);

			addMoveForPlayer(context, move, p);

			if (postAgentMoveCallback != null)
				postAgentMoveCallback.call(move);
		};
	}

	//-------------------------------------------------------------------------

	@Override
	public Trial playout
	(
		final Context context, final List<AI> ais, final double thinkingTime,
		final PlayoutMoveSelector playoutMoveSelector, final int maxNumBiasedActions, 
		final int maxNumPlayoutActions, final Random random
	)
	{
		final Game game = context.game();
		final int numPlayers = game.players().count();
		int numActionsApplied = 0;
		
		while
		(
			!context.trial().over()
			&&
			(maxNumPlayoutActions < 0 || maxNumPlayoutActions > numActionsApplied)
		)
		{
			final Move[] movesPerPlayerPlayout = new Move[numPlayers + 1];
			final Moves legal = game.moves(context);

			final List<FastArrayList<Move>> legalPerPlayer = new ArrayList<>(numPlayers + 1);
			legalPerPlayer.add(null);
			for (int p = 1; p <= numPlayers; ++p)
				legalPerPlayer.add(new FastArrayList<Move>());

			for (final Move move : legal.moves())
				legalPerPlayer.get(move.mover()).add(move);

			for (int p = 1; p <= numPlayers; ++p)
			{
				if (context.active(p))
				{
					final AI ai;

					if (ais != null)
						ai = ais.get(p);
					else
						ai = null;

					if (ai != null)
					{
						// Select AI move
						movesPerPlayerPlayout[p] = ai.selectAction(game, ai.copyContext(context), thinkingTime, -1, -1);
					}
					else
					{
						final FastArrayList<Move> playerMoves = legalPerPlayer.get(p);

						if (playerMoves.size() == 0)
						{
							final ActionPass actionPass = new ActionPass(true);
							actionPass.setDecision(true);
							final Move passMove = new Move(actionPass);
							passMove.setMover(p);
							playerMoves.add(passMove);
						}
						
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
							final int r = random.nextInt(playerMoves.size());
							movesPerPlayerPlayout[p] = playerMoves.get(r);
						}
						else
						{
							// Let our playout move selector pick a move
							movesPerPlayerPlayout[p] = playoutMoveSelector.selectMove(context, playerMoves, p, (final Move m) -> {return true;});
						}
					}
				}
			}
			
			// gather all the actions into a single move
			final List<Action> actions = new ArrayList<>();
			final List<Moves> topLevelCons = new ArrayList<Moves>();
			
			for (int p = 1; p < movesPerPlayerPlayout.length; ++p)
			{
				final Move move = movesPerPlayerPlayout[p];
				if (move != null)
				{
					final Move moveToAdd = new Move(move.actions());
					actions.add(moveToAdd);
					
					if (move.then() != null)
					{
						for (int i = 0; i < move.then().size(); ++i)
						{
							if (move.then().get(i).applyAfterAllMoves())
								topLevelCons.add(move.then().get(i));
							else
								moveToAdd.then().add(move.then().get(i));
						}
					}
				}
			}
			
			final Move combinedMove = new Move(actions);
			combinedMove.setMover(movesPerPlayerPlayout.length);
			combinedMove.then().addAll(topLevelCons);

			game.apply(context, combinedMove);
			++numActionsApplied;
		}
		return context.trial();
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean callsGameMoves()
	{
		return true;
	}
	
	@Override
	public Move[] movesPerPlayer()
	{
		return movesPerPlayer;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The specified move, else a random move if it's not valid.
	 */
	private static Move checkMoveValid
	(
		final boolean checkMoveValid, final Context context, final Move move, 
		final int player, final MoveMessageCallback callBack
	) 
	{
		if (checkMoveValid && !context.model().verifyMoveLegal(context, move))
		{
			final FastArrayList<Move> legal = extractMovesForMover(context.game().moves(context).moves(), player);
			final Move randomMove = legal.get(ThreadLocalRandom.current().nextInt(legal.size()));
			
			final String msg =  "illegal move detected: " + move.actions() + 
								", instead applying: " + randomMove;
			callBack.call(msg);
			System.out.println(msg);
			
			return randomMove;
		}
		return move;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param allMoves
	 * @param mover
	 * @return moves for player.
	 */
	public static FastArrayList<Move> extractMovesForMover
	(
		final FastArrayList<Move> allMoves, final int mover
	)
	{
		final FastArrayList<Move> moves = new FastArrayList<Move>(allMoves.size());
	
		for (final Move move : allMoves)
			if (move.mover() == mover)
				moves.add(move);
		
		return moves;
	}
	
	//-------------------------------------------------------------------------

}
