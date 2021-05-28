package other;

import game.Game;
import other.context.Context;
import other.move.Move;

/**
 * This class can be used to let an AI player spend its thinking time
 * in a separate thread, and afterwards ask it what move it wants to
 * play.
 * 
 * @author Dennis Soemers
 */
public class ThinkingThread extends Thread
{

	//-------------------------------------------------------------------------
	
	/** Our runnable */
	protected final ThinkingThreadRunnable runnable;
	
	//-------------------------------------------------------------------------

	/**
	 * @param ai
	 * @param game
	 * @param context
	 * @param maxSeconds 
	 * @param maxIterations 
	 * @param maxDepth 
	 * @param minSeconds
	 * @param postThinking
	 * @return Constructs a thread for AI to think in
	 */
	public static ThinkingThread construct
	(
		final AI ai,
		final Game game,
		final Context context,
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth,
		final double minSeconds,
		final Runnable postThinking
	)
	{
		final ThinkingThreadRunnable runnable = 
				new ThinkingThreadRunnable
				(
					ai,
					game,
					context,
					maxSeconds,
					maxIterations,
					maxDepth,
					minSeconds,
					postThinking
				);
		
		//System.out.println("constructing thread for " + ai);
		//Global.stackTrace();
		
		return new ThinkingThread(runnable);
	}
	
	/**
	 * Constructor
	 * @param runnable
	 */
	protected ThinkingThread(final ThinkingThreadRunnable runnable)
	{
		super(runnable);
		this.runnable = runnable;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our AI object
	 */
	public AI ai()
	{
		return runnable.ai;
	}
	
	/**
	 * @return The chosen move (or null if not chosen a move yet)
	 */
	public Move move()
	{
		return runnable.chosenMove;
	}
	
	/**
	 * Tells the AI to interrupt its thinking process
	 * @return Reference to our AI
	 */
	public AI interruptAI()
	{
		runnable.postThinking = null;	// should not call the callback if we're interrupting
		runnable.ai.setWantsInterrupt(true);
		return runnable.ai;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runnable class for Thinking Thread
	 * 
	 * @author Dennis Soemers
	 */
	private static class ThinkingThreadRunnable implements Runnable
	{
		
		//---------------------------------------------------------------------
		
		/** AI */
		protected final AI ai;
		
		/** Game */
		protected final Game game;
		
		/** Context/state in which to think */
		protected final Context context;
		
		/** Max seconds */
		protected final double maxSeconds;
		
		/** Max iterations */
		protected final int maxIterations;
		
		/** Max search depth */
		protected final int maxDepth;
		
		/** Minimum number of seconds we should spend thinking */
		protected final double minSeconds;
		
		/** Runnable to run when we've finished thinking */
		protected Runnable postThinking;
		
		/** The move chosen by AI */
		protected Move chosenMove = null;
		
		//---------------------------------------------------------------------
		
		/**
		 * Constructor 
		 * @param ai
		 * @param game
		 * @param context
		 * @param maxSeconds 
		 * @param maxIterations 
		 * @param maxDepth 
		 * @param minSeconds
		 * @param postThinking 
		 */
		public ThinkingThreadRunnable
		(
			final AI ai,
			final Game game,
			final Context context,
			final double maxSeconds,
			final int maxIterations,
			final int maxDepth,
			final double minSeconds,
			final Runnable postThinking
		)
		{
			this.ai = ai;
			this.game = game;
			this.context = context;
			this.maxSeconds = maxSeconds;
			this.maxIterations = maxIterations;
			this.maxDepth = maxDepth;
			this.minSeconds = minSeconds;
			this.postThinking = postThinking;
		}
		
		//---------------------------------------------------------------------

		@Override
		public void run()
		{
			final long startTime = System.currentTimeMillis();
			
			chosenMove = 
					ai.selectAction
					(
						game, 
						ai.copyContext(context), 
						maxSeconds, 
						maxIterations,
						maxDepth
					);
			
			// make sure we don't play too fast
			if (System.currentTimeMillis() < startTime + 1000L * minSeconds)
			{
				try
				{
					Thread.sleep((long) (startTime + 1000L * minSeconds) - System.currentTimeMillis());
				} 
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			//System.out.println(Thread.currentThread() + " setting chosen move: " + chosenMove);
			
			if (postThinking != null)
				postThinking.run();
		}
		
	}
	
	//-------------------------------------------------------------------------
	
}
