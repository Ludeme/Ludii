package other;

import java.lang.ref.WeakReference;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import other.move.Move;

/**
 * Base abstract class for AI agents.
 * 
 * @author Dennis Soemers and cambolbro
 */
public abstract class AI
{
	//-------------------------------------------------------------------------
	
	/** A heuristic function */
	protected Heuristics heuristicFunction = null;
	
	/** Easily-readable, human-friendly name for AI */
	protected String friendlyName = "Unnamed";
	
	/** Set to true if the Ludii app would like this AI to interrupt any thinking */
	protected volatile boolean wantsInterrupt = false;
	
	/** Weak reference to the last game we've initialised the AI for */
	private WeakReference<Game> lastInitGame = new WeakReference<>(null);
	
	/** How often was start() called on the game object the last time we initialised AI for it? */
	private int lastInitGameStartCount = -1;
	
	/** Does our AI want to cheat with perfect knowledge of all RNG events? */
	protected boolean wantsCheatRNG = false;
	
	/** Functor we can use to create copies of contexts */
	protected ContextCopyInterface contextCopyer = STANDARD_CONTEXT_COPY;
	
	/** 
	 * Thinking time limit per move for this AI. Only used if no limit is
	 * otherwise explicitly defined. By default 1 second, but can be modified.
	 * Values below 0 mean no limit.
	 */
	protected double maxSecondsPerMove = 1.0;
	
	/** 
	 * Iteration count limit per move for this AI. Only used if no limit is
	 * otherwise explicitly defined. Values below 0 mean no limit. Default -1
	 * (= no limit). The meaning of an "iteration" can be different for different
	 * algorithms, and some may not even have a notion of iterations (which means
	 * that they can just ignore this)
	 */
	protected int maxIterationsPerMove = -1;
	
	/** 
	 * Search depth limit per move for this AI. Only used if no limit is
	 * otherwise explicitly defined. Values below 0 mean no limit. Default -1
	 * (= no limit). The meaning of "search depth" can be different for different
	 * algorithms, and some may just ignore this
	 */
	protected int maxSearchDepthPerMove = -1;

	/** 
	 * Leniency factor in range 0..1: 0 = no effect, 1 = 100% leniency. 
	 * Used to make the AI more lenient (i.e. more likely to play suboptimal 
	 * moves) to dynamically adjust its playing strength. 
	 */ 
	protected double leniency = 0;
	
	//-------------------------------------------------------------------------
	// Getters and setters
	
	/**
	 * @return Leniency factor in range 0..1: 0 = no effect, 1 = 100% leniency.
	 */
	public double leniency()
	{
		return leniency;
	}
	
	/**
	 * @param amount Leniency amound in range 0..1.
	 */
	public void setLeniency(final double amount)
	{
		leniency = amount;
	}
	
	/** 
	 * @return Thinking time limit per move for this AI. Only used if no limit is
	 * otherwise explicitly defined. By default 1 second, but can be modified.
	 * Values below 0 mean no limit.
	 */
	public double maxSecondsPerMove()
	{
		return maxSecondsPerMove;
	}
	
	/**
	 * Sets this AI's default time limit per move in seconds (use negative for no limit)
	 * @param newLimit
	 */
	public void setMaxSecondsPerMove(final double newLimit)
	{
		maxSecondsPerMove = newLimit;
	}
	
	/** 
	 * @return Iteration count limit per move for this AI. Only used if no limit is
	 * otherwise explicitly defined. Values below 0 mean no limit. Default -1
	 * (= no limit). The meaning of an "iteration" can be different for different
	 * algorithms, and some may not even have a notion of iterations (which means
	 * that they can just ignore this)
	 */
	protected int maxIterationsPerMove()
	{
		return maxIterationsPerMove;
	}
	
	/**
	 * Sets this AI's default iteration limit per move in seconds (use negative for no limit)
	 * @param newLimit
	 */
	public void setMaxIterationsPerMove(final int newLimit)
	{
		maxIterationsPerMove = newLimit;
	}
	
	/** 
	 * @return Search depth limit per move for this AI. Only used if no limit is
	 * otherwise explicitly defined. Values below 0 mean no limit. Default -1
	 * (= no limit). The meaning of "search depth" can be different for different
	 * algorithms, and some may just ignore this
	 */
	protected int maxSearchDepthPerMove()
	{
		return maxSearchDepthPerMove;
	}
	
	/**
	 * Sets this AI's default search depth limit per move in seconds (use negative for no limit)
	 * @param newLimit
	 */
	public void setMaxSearchDepthPerMove(final int newLimit)
	{
		maxSearchDepthPerMove = newLimit;
	}
	
	/**
	 * @return The friendly name.
	 */
	public String friendlyName()
	{
		return friendlyName;
	}
	
	/**
	 * Set the friendly name.
	 * @param fname The friendly name.
	 */
	public void setFriendlyName(final String fname)
	{
		friendlyName = new String(fname);
	}
	
	/**
	 * Sets heuristics to be used by MCTS (for instance to mix with backpropagation result).
	 * @param heuristics
	 */
	public void setHeuristics(final Heuristics heuristics)
	{
		heuristicFunction = heuristics;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Should be implemented to select and return an action to play.
	 * 
	 * @param game Reference to the game we're playing.
	 * @param context Copy of the context containing the current state of the game
	 * @param maxSeconds Max number of seconds before a move should be selected.
	 * Values less than 0 mean there is no time limit.
	 * @param maxIterations Max number of iterations before a move should be selected.
	 * Values less than 0 mean there is no iteration limit.
	 * @param maxDepth Max search depth before a move should be selected.
	 * Values less than 0 mean there is no search depth limit.
	 * @return Preferred move.
	 */
	public abstract Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	);
	
	/**
	 * Requests the AI object to select a move, using the AI object's own internal limits for
	 * search time, iterations, and/or search depth.
	 * 
	 * @param game
	 * @param context
	 * @return Preferred move.
	 */
	public Move selectAction(final Game game, final Context context)
	{
		return selectAction(game, context, maxSecondsPerMove, maxIterationsPerMove, maxSearchDepthPerMove);
	}
	
	/**
	 * Helper method to create copies of contexts. Changes behaviour depending
	 * on whether or not the AI wants to be able to cheat with perfect knowledge
	 * of RNG events.
	 * 
	 * @param other
	 * @return Copy of Context
	 */
	public final Context copyContext(final Context other)		// IMPORTANT for this to be final, prevents more cheating :P
	{
		return contextCopyer.copy(other);
	}

	/**
	 * @return Human-friendly name for this AI.
	 */
	public String name()
	{
		return friendlyName;
	}
	
	/**
	 * Allows an agent to perform any desired initialisation before starting 
	 * to play a game.
	 * 
	 * @param game The game that we'll be playing
	 * @param playerID The player ID (or index) for the AI in this game
	 */
	public void initAI(final Game game, final int playerID)
	{
		// Do nothing by default
	}
	
	/**
	 * Ludii may call this when it's fairly likely that this AI will no longer
	 * have to continue playing the game it was currently playing. This can then
	 * be used to free up any resources / memory if desired.
	 * 
	 * Ludii will generally call this on an AI right before switching over to
	 * a new type of AI, and also when restarting a game in the app or loading
	 * a new game. Note that it's not 100% guaranteed to always be called in 
	 * between subsequent initAI() calls.
	 */
	public void closeAI()
	{
		// Do nothing by default
	}
	
	/**
	 * Allows an agent to tell Ludii whether or not it can support playing
	 * any given game. AIs which do not override this method will, by default,
	 * tell Ludii that they support any game.
	 * @param game
	 * @return False if the AI cannot play the given game.
	 */
	public boolean supportsGame(final Game game)
	{
		return true;
	}
	
	/**
	 * Can be overridden by AIs to return a general value estimate in [-1, 1].
	 * Used only for visualisation purposes (e.g. smiley faces)
	 * 
	 * @return Value estimate in [-1, 1]
	 */
	public double estimateValue()
	{
		return 0.0;
	}
	
	/**
	 * Can be overridden by AIs to return a string to print in the Analysis tab
	 * of Ludii after making a move. 
	 * 
	 * Default implementation always returns null, which causes nothing to be printed.
	 * 
	 * @return String to print after making move, or null for no print.
	 */
	public String generateAnalysisReport()
	{
		return null;
	}
	
	/**
	 * Can be overridden by AIs to return data for visualisation of its
	 * thinking process.
	 * 
	 * @return Data for visualisations, null for no visualisations
	 */
	public AIVisualisationData aiVisualisationData()
	{
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sets whether the Ludii app wants this AI to interrupt any thinking
	 * @param val
	 */
	public void setWantsInterrupt(final boolean val)
	{
		wantsInterrupt = val;
	}
	
	/**
	 * Sets whether this AI wants to cheat with perfect knowledge of all RNG events
	 * @param wantsCheat
	 */
	public void setWantsCheatRNG(final boolean wantsCheat)
	{
		wantsCheatRNG = wantsCheat;
		
		if (wantsCheatRNG)
			contextCopyer = RNG_CHEAT_COPY;
		else
			contextCopyer = STANDARD_CONTEXT_COPY;
	}
	
	/**
	 * @return Does this AI want to cheat with perfect knowledge of all RNG events?
	 */
	public boolean wantsCheatRNG()
	{
		return wantsCheatRNG;
	}
	
	/**
	 * @param game
	 * @return Does this AI use spatial state-action features?
	 */
	public boolean usesFeatures(final Game game)
	{
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Calls initAI() only if it is needed (if the AI wasn't previously initialised
	 * for the same game + trial).
	 * @param game
	 * @param playerID
	 */
	public final void initIfNeeded(final Game game, final int playerID)
	{
		if 
		(
			lastInitGame.get() != null 
			&& 
			lastInitGame.get() == game 
			&& 
			lastInitGame.get().gameStartCount() == lastInitGameStartCount
		)
		{
			// we do not need to init AI
			return;
		}

		initAI(game, playerID);
		lastInitGame = new WeakReference<>(game);
		lastInitGameStartCount = game.gameStartCount();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper for data that AIs can return if they want to facilitate visualisations
	 * of their "thinking processes" in the Ludii app.
	 * 
	 * @author Dennis Soemers
	 */
	public static class AIVisualisationData
	{
		/** 
		 * Vector containing measures of "search effort" per move.
		 */
		private final FVector searchEffort;
		
		/**
		 * Vector containing value estimates per move (all expected
		 * to lie in [-1, 1]).
		 */
		private final FVector valueEstimates;
		
		/**
		 * List of moves for which we wish to draw visualisations, in
		 * the order that matches the search effort and value estimate vectors.
		 */
		private final FastArrayList<Move> moves;
		
		/**
		 * Constructor
		 * 
		 * @param searchEffort
		 * @param valueEstimates
		 * @param moves
		 */
		public AIVisualisationData
		(
			final FVector searchEffort, 
			final FVector valueEstimates, 
			final FastArrayList<Move> moves
		)
		{
			this.searchEffort = searchEffort;
			this.valueEstimates = valueEstimates;
			this.moves = moves;
		}
		
		/**
		 * @return Vector of "search effort" values
		 */
		public FVector searchEffort()
		{
			return searchEffort;
		}
		
		/**
		 * @return Vector of value estimates for moves
		 */
		public FVector valueEstimates()
		{
			return valueEstimates;
		}
		
		/**
		 * @return List of moves.
		 */
		public FastArrayList<Move> moves()
		{
			return moves;
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Interface for a functor that can create copies of Context objects
	 *
	 * @author Dennis Soemers
	 */
	public interface ContextCopyInterface
	{
		/**
		 * @param context
		 * @return A copy of the given context
		 */
		public Context copy(final Context context);
	}
	
	/** Functor that creates normal copies of context for normal use */
	public static final ContextCopyInterface STANDARD_CONTEXT_COPY = (final Context context) -> { return new Context(context); };
	
	/** Functor that creates copies of contexts including seed copying, for RNG cheats */
	public static final ContextCopyInterface RNG_CHEAT_COPY = (final Context context) -> { return Context.copyWithSeed(context); };
	
	//-------------------------------------------------------------------------
	
}
