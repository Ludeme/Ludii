package other.model;

import java.util.Arrays;
import java.util.List;

import other.AI;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.playout.Playout;

//-----------------------------------------------------------------------------

/**
 * Model of game's control flow, can be used to step through a trial
 * correctly (i.e. alternating moves, simultaneous moves, etc.)
 * 
 * @author Dennis Soemers
 */
public abstract class Model implements Playout
{

	//-------------------------------------------------------------------------
	
	/**
	 * Applies a move selected by human
	 * @param context
	 * @param move
	 * @param player
	 * @return applied move
	 */
	public abstract Move applyHumanMove(final Context context, final Move move, final int player);
	
	/**
	 * @return A copy of the Model object
	 */
	public abstract Model copy();
	
	/**
	 * @return True if we expect human input
	 */
	public abstract boolean expectsHumanInput();
	
	/**
	 * @return List of AIs who have made a move in the last step
	 */
	public abstract List<AI> getLastStepAIs();
	
	/**
	 * @return List of moves selected by different players in the last step
	 */
	public abstract List<Move> getLastStepMoves();
	
	/**
	 * Interrupts any AIs that are currently running in separate threads
	 */
	public abstract void interruptAIs();
	
	/**
	 * @return True if and only if the last step that has been started has
	 * also finished processing.
	 */
	public abstract boolean isReady();
	
	/**
	 * @return True if and only if a step has been started and is properly
	 * running (ready to receive external moves if threaded)
	 */
	public abstract boolean isRunning();
	
	/**
	 * Completes the current step by taking random moves (for any players who
	 * do not currently have AIs thinking).
	 * @param context
	 * @param inPreAgentMoveCallback
	 * @param inPostAgentMoveCallback
	 */
	public abstract void randomStep
	(
		final Context context, 
		final AgentMoveCallback inPreAgentMoveCallback, 
		final AgentMoveCallback inPostAgentMoveCallback
	);
	
	/**
	 * @param context
	 * @param move
	 * @return True if given move is legal in given context, false otherwise.
	 */
	public abstract boolean verifyMoveLegal(final Context context, final Move move);
	
	//-------------------------------------------------------------------------

	/**
	 * Starts a new "time step" (a single move by a single player in Alternating-Move
	 * games, or a collection of moves by active players in Simultaneous-Move games).<br>
	 * <br>
	 * By default: <br>
	 * 	- A blocking call (sequencees not return until the step has been completed).<br>
	 * 	- Does not force running in a separate thread (so may run in the calling thread).<br>
	 * 	- Allows running AIs in different threads (simultaneous-move models will prefer to
	 * 	run AIs in multiple threads if allowed, whereas alternating-move models prefer to
	 * 	just run in a single thread). <br>
	 * 
	 * @param context Current context (containing current state/trial)
	 * @param ais List of AIs (0 index should be null)
	 * @param maxSeconds Maximum number of seconds that AIs are allowed to use
	 */
	public void startNewStep
	(
		final Context context, 
		final List<AI> ais,
		final double maxSeconds
	)
	{
		final double[] timeLimits = new double[context.game().players().count() + 1];
		Arrays.fill(timeLimits, maxSeconds);
		startNewStep(context, ais, timeLimits);
	}
	
	/**
	 * Similar to {@link #startNewStep(Context, List, double)}, but allows
	 * specifying an array with different thinking times for different agents.
	 * 
	 * @param context Current context (containing current state/trial)
	 * @param ais List of AIs (0 index should be null)
	 * @param maxSeconds For every AI, maximum number of seconds that that AI is 
	 * 	allowed to use. Index 0 is not used.
	 */
	public void startNewStep
	(
		final Context context, 
		final List<AI> ais,
		final double[] maxSeconds
	)
	{
		startNewStep(context, ais, maxSeconds, -1, -1, 0.0);
	}

	/**
	 * Similar to {@link #startNewStep(Context, List, double)}, but allows
	 * specifying max iterations, max search depth, and minimum search time
	 * for AIs (in addition to the maximum search time).
	 * 
	 * @param context Current context (containing current state/trial)
	 * @param ais List of AIs (0 index should be null)
	 * @param maxSeconds Maximum number of seconds that AIs are allowed to use
	 * @param maxIterations Maximum number of iterations for AIs (such as MCTS)
	 * @param maxSearchDepth Maximum search depth for AIs (such as Alpha-Beta)
	 * @param minSeconds Minimum number of seconds that the AI must spend per move
	 */
	public void startNewStep
	(
		final Context context, 
		final List<AI> ais,
		final double maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds
	)
	{
		final double[] timeLimits = new double[context.game().players().count() + 1];
		Arrays.fill(timeLimits, maxSeconds);
		startNewStep(context, ais, timeLimits, maxIterations, maxSearchDepth, minSeconds);
	}
	
	/**
	 * Similar to {@link #startNewStep(Context, List, double[])}, but allows
	 * specifying max iterations, max search depth, and minimum search time
	 * for AIs (in addition to the array of maximum search times).
	 * 
	 * @param context Current context (containing current state/trial)
	 * @param ais List of AIs (0 index should be null)
	 * @param maxSeconds For every AI, maximum number of seconds that that AI is 
	 * 	allowed to use. Index 0 is not used.
	 * @param maxIterations Maximum number of iterations for AIs (such as MCTS)
	 * @param maxSearchDepth Maximum search depth for AIs (such as Alpha-Beta)
	 * @param minSeconds Minimum number of seconds that the AI must spend per move
	 */
	public void startNewStep
	(
		final Context context, 
		final List<AI> ais,
		final double[] maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds
	)
	{
		startNewStep(context, ais, maxSeconds, maxIterations, maxSearchDepth, minSeconds, true, false, false, null, null);
	}
	
	/**
	 * Similar to {@link #startNewStep(Context, List, double, int, int, double)},
	 * but with additional parameters to control the threading/blocking behaviour
	 * of the call.
	 * 
	 * @param context Current context (containing current state/trial)
	 * @param ais List of AIs (0 index should be null)
	 * @param maxSeconds Maximum number of seconds that AIs are allowed to use
	 * @param maxIterations Maximum number of iterations for AIs (such as MCTS)
	 * @param maxSearchDepth Maximum search depth for AIs (such as Alpha-Beta)
	 * @param minSeconds Minimum number of seconds that the AI must spend per move
	 * @param block If true, this method will block and only return once a move has
	 * 	been applied to the current game state. If false, the method will return
	 * 	immediately while AIs may still spend time thinking about their moves in
	 * 	different threads.
	 * @param forceThreaded If true, this method is forced to run in a new thread.
	 * @param forceNotThreaded If true, this method may not create new threads and
	 * 	is forced to run entirely in the calling thread.
	 */
	public void startNewStep
	(
		final Context context, 
		final List<AI> ais,
		final double maxSeconds,
		final int maxIterations,
		final int maxSearchDepth,
		final double minSeconds,
		final boolean block,
		final boolean forceThreaded,
		final boolean forceNotThreaded
	)
	{
		final double[] timeLimits = new double[context.game().players().count() + 1];
		Arrays.fill(timeLimits, maxSeconds);
		startNewStep(context, ais, timeLimits, maxIterations, maxSearchDepth, minSeconds, block, forceThreaded, forceNotThreaded, null, null);
	}
	
	/**
	 * Similar to {@link #startNewStep(Context, List, double[], int, int, double)},
	 * but with additional parameters to control the threading/blocking behaviour
	 * of the call, and options to add extra callbacks to be called before and after
	 * agents select their moves.
	 * 
	 * @param context Current context (containing current state/trial)
	 * @param ais List of AIs (0 index should be null)
	 * @param maxSeconds Maximum number of seconds that AIs are allowed to use
	 * @param maxIterations Maximum number of iterations for AIs (such as MCTS)
	 * @param maxSearchDepth Maximum search depth for AIs (such as Alpha-Beta)
	 * @param minSeconds Minimum number of seconds that the AI must spend per move
	 * @param block If true, this method will block and only return once a move has
	 * 	been applied to the current game state. If false, the method will return
	 * 	immediately while AIs may still spend time thinking about their moves in
	 * 	different threads.
	 * @param forceThreaded If true, this method is forced to run in a new thread.
	 * @param forceNotThreaded If true, this method may not create new threads and
	 * 	is forced to run entirely in the calling thread.
	 * @param inPreAgentMoveCallback If not null, this callback will be called when
	 * 	an AI has picked its move, but before applying it.
	 * @param inPostAgentMoveCallback If not null, this callback will be called right
	 * 	after an AI's move has been applied.
	 */
	public abstract void startNewStep
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
	);
	
	/**
	 * Same as above, but with additional boolean param to check legal moves
	 * 
	 * @param context 
	 * @param ais 
	 * @param maxSeconds 
	 * @param maxIterations 
	 * @param maxSearchDepth 
	 * @param minSeconds 
	 * @param block 
	 * @param forceThreaded 
	 * @param forceNotThreaded 
	 * @param inPreAgentMoveCallback 
	 * @param inPostAgentMoveCallback 
	 * @param checkMoveValid 
	 * @param moveMessageCallback 
	 */
	public abstract void startNewStep
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
	);
	
	/**
	 * Notifies the model that agents are unpaused, and should now start running
	 * 
	 * @param context
	 * @param ais
	 * @param maxSeconds
	 * @param maxIterations
	 * @param maxSearchDepth
	 * @param minSeconds 
	 * @param inPreAgentMoveCallback
	 * @param inPostAgentMoveCallback
	 * @param checkMoveValid 
	 * @param moveMessageCallback 
	 */
	public abstract void unpauseAgents
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
	);

	//-------------------------------------------------------------------------
	
	/**
	 * @return List of AIs that are currently thinking
	 */
	public abstract List<AI> getLiveAIs();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Interface for callback functions that can be called before or after agents
	 * select/apply their moves.
	 * 
	 * @author Dennis Soemers
	 */
	public interface AgentMoveCallback
	{
		/**
		 * Callback function
		 * @param move The selected/applied move
		 * @return Number of milliseconds for which to sleep after calling this.
		 */
		long call(final Move move);
	}
	
	/**
	 * Interface for callback functions that can be called when moves are checked.
	 * 
	 * @author Matthew Stephenson
	 */
	public interface MoveMessageCallback
	{
		/**
		 * Callback function
		 * @param message The message to display on the status tab.
		 */
		void call(final String message);
	}

	/**
	 * @return The moves per player.
	 */
	@SuppressWarnings("static-method")
	public Move[] movesPerPlayer() 
	{
		return null;
	}
	
	//-------------------------------------------------------------------------
		
	/**
	 * @param m1 
	 * @param m2 
	 * @param context 
	 * @return if m1 is equivalent to m2
	 */
	public static boolean movesEqual(final Move m1, final Move m2, final Context context)
	{
		if (m1.from() != m2.from() || m1.to() != m2.to())
			return false;
		if (m1.then().isEmpty() && m2.then().isEmpty() && m1.actions().equals(m2.actions()))
			return true;
		
		return (m1.getActionsWithConsequences(context).equals(m2.getActionsWithConsequences(context)));
	}
	
	/**
	 * @param m1 
	 * @param m1Actions 
	 * @param m2 
	 * @param context 
	 * @return if m1 is equivalent to m2, with m1's actions already pre-calculated.
	 */
	public static boolean movesEqual(final Move m1, final List<Action> m1Actions, final Move m2, final Context context)
	{
		if (m1.from() != m2.from() || m1.to() != m2.to())
			return false;
		
		if (m1.then().isEmpty() && m2.then().isEmpty() && m1.actions().equals(m2.actions()))
			return true;
		
		return (m1Actions.equals(m2.getActionsWithConsequences(context)));
	}
	
	//-------------------------------------------------------------------------

}
