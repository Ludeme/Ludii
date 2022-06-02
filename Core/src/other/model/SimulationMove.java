package other.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.Status;
import main.collections.FastArrayList;
import other.AI;
import other.ThinkingThread;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;

/**
 * Model for simulation games.
 *
 * @author Eric.Piette
 */
public final class SimulationMove extends Model
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
		return new SimulationMove();
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
					// do nothing
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
	public void randomStep(final Context context, final AgentMoveCallback inPreAgentMoveCallback,
			final AgentMoveCallback inPostAgentMoveCallback)
	{
		if (!ready && currentThinkingThread == null)
		{
			final FastArrayList<Move> legalMoves = new FastArrayList<Move>(context.game().moves(context).moves());
			if (!legalMoves.isEmpty())
				applyHumanMove(context, legalMoves.get(0), context.state().mover());

			ready = true;
			running = false;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public synchronized void startNewStep(final Context context, final List<AI> ais, final double[] maxSeconds,
			final int maxIterations, final int maxSearchDepth, final double minSeconds, final boolean block,
			final boolean forceThreaded, final boolean forceNotThreaded, final AgentMoveCallback inPreAgentMoveCallback,
			final AgentMoveCallback inPostAgentMoveCallback)
	{
		startNewStep(context,ais,maxSeconds,maxIterations,maxSearchDepth,minSeconds,block,forceThreaded,forceNotThreaded,inPreAgentMoveCallback,inPostAgentMoveCallback,false,null);
	}
	
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
		
		while (true)
		{
			final ThinkingThread thinkingThread = currentThinkingThread;
			
			if (thinkingThread == null || !thinkingThread.isAlive())
				break;
			
			// TODO uncomment below if we move to Java >= 9
			//Thread.onSpinWait();
		}
		
		final Game game = context.game();
		ready = false;
		running = true;
		final Trial trial = context.trial();
		if (!trial.over())
		{
			final FastArrayList<Move> legalMoves = new FastArrayList<Move>(context.game().moves(context).moves());
		    game.apply(context, legalMoves.get(0));
		}

		final FastArrayList<Move> legalMoves = new FastArrayList<Move>(context.game().moves(context).moves());
		if (legalMoves.isEmpty())
			context.trial().setStatus(new Status(0));

		running = false;
		ready = true;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void unpauseAgents(final Context context, final List<AI> ais, final double[] maxSeconds,
			final int maxIterations, final int maxSearchDepth, final double minSeconds,
			final AgentMoveCallback inPreAgentMoveCallback, final AgentMoveCallback inPostAgentMoveCallback, final boolean checkMoveValid, final MoveMessageCallback moveMessageCallback)
	{
		currentThinkingThread =
				ThinkingThread.construct
				(
					ais.get(0),
					context.game(),
					ais.get(0).copyContext(context),
					maxSeconds[0],
					maxIterations,
					maxSearchDepth,
					minSeconds,
					new Runnable()
					{
						@Override
						public void run()
						{
								final long startTime = System.currentTimeMillis();
								final long stopTime = (maxSeconds[0] > 0.0) ? startTime + (long) (maxSeconds[0] * 1000)
										: Long.MAX_VALUE;

								while (System.currentTimeMillis() < stopTime)
								{
									// Wait
								}

								final FastArrayList<Move> legalMoves = new FastArrayList<Move>(
										context.game().moves(context).moves());

								if (!legalMoves.isEmpty())
									applyHumanMove(context, legalMoves.get(0), context.state().mover());

							ready = true;
							running = false;
						}
					}
				);
		currentThinkingThread.setDaemon(true);
		currentThinkingThread.start();
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
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public Trial playout(final Context context, final List<AI> ais, final double thinkingTime,
			final PlayoutMoveSelector playoutMoveSelector, final int maxNumBiasedActions,
			final int maxNumPlayoutActions, final Random random)
	{
		final Game game = context.game();
		int numActionsApplied = 0;
		final Trial trial = context.trial();
		while (!trial.over() && (maxNumPlayoutActions < 0 || maxNumPlayoutActions > numActionsApplied))
		{
			final FastArrayList<Move> legalMoves = new FastArrayList<Move>(context.game().moves(context).moves());

			if (!legalMoves.isEmpty())
		        game.apply(context, legalMoves.get(0));
			else
				context.trial().setStatus(new Status(0));

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
}