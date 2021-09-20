package other.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import main.collections.FastArrayList;
import other.AI;
import other.ThinkingThread;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;

/**
 * Model for alternating-move games
 *
 * @author Dennis Soemers
 */
public final class AlternatingMove extends Model
{
	//-------------------------------------------------------------------------

	/** Are we ready with our current step? */
	protected transient volatile boolean ready = true;

	/** Are we ready to receive external actions? */
	protected transient volatile boolean running = false;

	/** Currently-running thinking thread */
	protected transient volatile ThinkingThread currentThinkingThread = null;

	/** AI used to select a move in the last step */
	protected transient AI lastStepAI = null;

	/** Move selected in last step */
	protected transient Move lastStepMove = null;

	//-------------------------------------------------------------------------

	@Override
	public Move applyHumanMove(final Context context, final Move move, final int player)
	{
		if (currentThinkingThread != null)
		{
			// Don't interrupt AI
			return null;
		}

		if (!ready)
		{
			// only apply move if we've actually started a new step
			final Move appliedMove = context.game().apply(context, move);
			context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1);
			lastStepMove = move;
			ready = true;
			running = false;
			return appliedMove;
		}

		return null;
	}

	@Override
	public Model copy()
	{
		return new AlternatingMove();
	}

	@Override
	public boolean expectsHumanInput()
	{
		return (!ready && running && (currentThinkingThread == null));
	}

	@Override
	public List<AI> getLastStepAIs()
	{
		if (!ready)
			return null;

		return Arrays.asList(lastStepAI);
	}

	@Override
	public List<Move> getLastStepMoves()
	{
		if (!ready)
			return null;

		return Arrays.asList(lastStepMove);
	}

	@Override
	public synchronized void interruptAIs()
	{
		if (!ready)
		{
			if (currentThinkingThread != null)
			{
				AI ai = null;

				try
				{
					ai = currentThinkingThread.interruptAI();

					while (currentThinkingThread.isAlive())
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

					currentThinkingThread = null;
				}
				catch (final NullPointerException e)
				{
					// Do nothing
				}

				if (ai != null)
					ai.setWantsInterrupt(false);
			}

			lastStepAI = null;
			ready = true;
			running = false;
		}
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
		if (!ready && currentThinkingThread == null)
		{
			final FastArrayList<Move> legalMoves = context.game().moves(context).moves();
			final int r = ThreadLocalRandom.current().nextInt(legalMoves.size());
			final Move move = legalMoves.get(r);
			
			if (inPreAgentMoveCallback != null)
			{
				final long waitMillis = inPreAgentMoveCallback.call(move);
				
				if (waitMillis > 0L)
				{
					try
					{
						Thread.sleep(waitMillis);
					} 
					catch (final InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}

			final Move appliedMove = applyHumanMove(context, move, context.state().mover());
			ready = true;
			
			if (inPostAgentMoveCallback != null)
				inPostAgentMoveCallback.call(appliedMove);
			
			running = false;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public void startNewStep
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
	public void startNewStep
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
//		context.getLock().lock();
//		
//		try
//		{
			if (!ready)
			{
				// we're already running our current step, so don't start a new one
				return;
			}
			
			while (true)
			{
				final ThinkingThread thinkingThread = currentThinkingThread;
				
				if (thinkingThread == null || !thinkingThread.isAlive())
					break;
				
				// TODO uncomment below if we move to Java >= 9
				//Thread.onSpinWait();
			}
	
			ready = false;
	
			final int mover = context.state().mover();
			final AI agent;
			
			if (ais == null || mover >= ais.size())	
				agent = null;
			else
				agent = ais.get(context.state().playerToAgent(mover));
	
			lastStepAI = agent;
	
			if (block)
			{
				// we're allowed to block, so just compute the move here
				if (agent == null)
				{
					randomStep(context, inPreAgentMoveCallback, inPostAgentMoveCallback);
					return;
				}
				
				Move move;
	
				if (!forceThreaded)
				{
					// we don't have to run AI in separate thread, so run it here
					move = agent.selectAction
							(
								context.game(),
								agent.copyContext(context),
								maxSeconds[context.state().playerToAgent(mover)],
								maxIterations,
								maxSearchDepth
							);
				}
				else
				{
					// we have to run AI in different thread
					currentThinkingThread =
							ThinkingThread.construct
							(
								agent,
								context.game(),
								agent.copyContext(context),
								maxSeconds[context.state().playerToAgent(mover)],
								maxIterations,
								maxSearchDepth,
								minSeconds,
								null
							);
					currentThinkingThread.setDaemon(true);
					currentThinkingThread.start();
	
					while (currentThinkingThread.isAlive())
					{
						// TODO uncomment below if we move to Java >= 9
						//Thread.onSpinWait();
					}
	
					move = currentThinkingThread.move();
					currentThinkingThread = null;
				}
				
				move = checkMoveValid(checkMoveValid, context, move, moveMessageCallback);
				
				if (inPreAgentMoveCallback != null)
				{
					final long waitMillis = inPreAgentMoveCallback.call(move);
				
					if (waitMillis > 0L)
					{
						try
						{
							Thread.sleep(waitMillis);
						} 
						catch (final InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
	
				// apply chosen move
				final Move appliedMove = context.game().apply(context, move);
				context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1);
				lastStepMove = move;
	
				// we're done
				ready = true;
	
				if (inPostAgentMoveCallback != null)
					inPostAgentMoveCallback.call(appliedMove);
	
				running = false;
			}
			else
			{
				// we're not allowed to block, must return immediately
				// this implies we have to run a thread for AI
				if (agent != null)
				{
					// only create Thread if agent is not null (no human)
					currentThinkingThread =
							ThinkingThread.construct
							(
								agent,
								context.game(),
								agent.copyContext(context),
								maxSeconds[context.state().playerToAgent(mover)],
								maxIterations,
								maxSearchDepth,
								minSeconds,
								new Runnable()
								{
	
									@Override
									public void run()
									{
										// this code runs when AI finished selecting move
										Move move = currentThinkingThread.move();
										
										move = checkMoveValid(checkMoveValid, context, move, moveMessageCallback);
										
										while (!running)
										{
											// TODO uncomment below if we move to Java >= 9
											//Thread.onSpinWait();
										}
	
										if (inPreAgentMoveCallback != null)
										{
											final long waitMillis = inPreAgentMoveCallback.call(move);
				
											if (waitMillis > 0L)
											{
												try
												{
													Thread.sleep(waitMillis);
												} 
												catch (final InterruptedException e)
												{
													e.printStackTrace();
												}
											}
										}
	
										final Move appliedMove = context.game().apply(context, move);
										context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1);
										lastStepMove = move;
										ready = true;
	
										if (inPostAgentMoveCallback != null)
											inPostAgentMoveCallback.call(appliedMove);
	
										running = false;
										currentThinkingThread = null;
									}
	
								}
							);
					currentThinkingThread.setDaemon(true);
					currentThinkingThread.start();
					running = true;
				}
				else
				{
					running = true;
				}
			}
//		}
//		finally
//		{
//			context.getLock().unlock();
//		}
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
		final int mover = context.state().mover();
		final AI agent;

		if (ais == null || mover >= ais.size())
			agent = null;
		else
			agent = ais.get(context.state().playerToAgent(mover));

		lastStepAI = agent;

		if (agent != null)
		{
			currentThinkingThread =
					ThinkingThread.construct
					(
						agent,
						context.game(),
						agent.copyContext(context),
						maxSeconds[mover],
						maxIterations,
						maxSearchDepth,
						minSeconds,
						new Runnable()
						{

							@Override
							public void run()
							{
								// this code runs when AI finished selecting move
								Move move = currentThinkingThread.move();
								currentThinkingThread = null;
								
								move = checkMoveValid(checkMoveValid, context, move, moveMessageCallback);
								
								if (inPreAgentMoveCallback != null)
								{
									final long waitMillis = inPreAgentMoveCallback.call(move);
			
									if (waitMillis > 0L)
									{
										try
										{
											Thread.sleep(waitMillis);
										} 
										catch (final InterruptedException e)
										{
											e.printStackTrace();
										}
									}
								}
								
								final Move appliedMove = context.game().apply(context, move);
								context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1);
								lastStepMove = move;
								ready = true;

								if (inPostAgentMoveCallback != null)
									inPostAgentMoveCallback.call(appliedMove);

								running = false;
							}

						}
					);
			currentThinkingThread.setDaemon(true);
			currentThinkingThread.start();
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public List<AI> getLiveAIs()
	{
		final List<AI> ais = new ArrayList<>(1);

		if (currentThinkingThread != null)
			ais.add(currentThinkingThread.ai());

		return ais;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean verifyMoveLegal(final Context context, final Move move)
	{
		boolean validMove = false;
		final FastArrayList<Move> legal = new FastArrayList<Move>(context.game().moves(context).moves());

		final List<Action> moveActions = move.getActionsWithConsequences(context);
		
		for (final Move m : legal)
		{			
			if (movesEqual(move, moveActions, m, context))
			{
				validMove = true;
				break;
			}
		}

		if (legal.isEmpty() && move.isPass())
			validMove = true;

		return validMove;
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
		final Phase startPhase = game.rules().phases()[context.state().currentPhase(context.state().mover())];

		int numActionsApplied = 0;
		final Trial trial = context.trial();
		while
		(
			!trial.over()
			&&
			(maxNumPlayoutActions < 0 || maxNumPlayoutActions > numActionsApplied)
		)
		{
			final int mover = context.state().mover();
			final Phase currPhase = game.rules().phases()[context.state().currentPhase(mover)];

			if (currPhase != startPhase && currPhase.playout() != null)
			{
				// May have to switch over to new playout implementation
				return trial;
			}

			Move move = null;
			AI ai = null;

			if (ais != null)
				ai = ais.get(context.state().playerToAgent(mover));

			if (ai != null)
			{
				// Make AI move
				move = ai.selectAction(game, ai.copyContext(context), thinkingTime, -1, -1);
			}
			else
			{
				// Make (biased) random move
				final Moves legal = game.moves(context);
				
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
					final int r = random.nextInt(legal.moves().size());
					move = legal.moves().get(r);
				}
				else
				{
					// Let our playout move selector pick a move
					move = playoutMoveSelector.selectMove(context, legal.moves(), mover, (final Move m) -> {return true;});
				}
			}

			if (move == null)
			{
				System.out.println("Game.playout(): No move found.");
				break;
			}

			game.apply(context, move);
			++numActionsApplied;
		}
		
		return trial;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean callsGameMoves()
	{
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The specified move, else a random move if it's not valid.
	 */
	static Move checkMoveValid
	(
		final boolean checkMoveValid, final Context context, 
		final Move move, final MoveMessageCallback callBack
	) 
	{
		if (checkMoveValid && !context.model().verifyMoveLegal(context, move))
		{
			final FastArrayList<Move> legalMoves = context.game().moves(context).moves();
			final Move randomMove = legalMoves.get(ThreadLocalRandom.current().nextInt(legalMoves.size()));
			
			final String msg =  "illegal move detected: " + move.actions() + 
								", instead applying: " + randomMove;
			callBack.call(msg);
			System.out.println(msg);
			
			return randomMove;
		}
		return move;
	}

	//-------------------------------------------------------------------------

}
