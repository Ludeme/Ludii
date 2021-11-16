package other;

import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.TIntHashSet;
import main.Status;
import main.collections.FastTIntArrayList;
import other.state.owned.Owned;
import other.state.track.OnTrackIndices;
import other.state.zhash.HashedBitSet;

/**
 * Undo Data necessary to be able to undo a move.
 *
 * @author Eric.Piette
 */
public class UndoData
{
	//------------------------Data modified by end rules-------------------------------------
	
	/** Ranking of the players. */
	private final double[] ranking;
	
	/** Result of game (null if game is still in progress). */
	private final Status status;
	
	/** List of players who've already won */
	private final TIntArrayList winners;
	
	/** List of players who've already lost */
	private final TIntArrayList losers;
	
	/** For every player, a bit indicating whether they are active */
	private int active = 0;
	
	/** Scores per player. Game scores if this is a trial for just a game, match scores if it's a trial for a Match */
	private final int[] scores;
	
	/**
	 * Payoffs per player. Game payoff if this is a trial for just a game, match
	 * scores if it's a trial for a Match
	 */
	private final double[] payoffs;
	
	/**
	 * Data used during the computation of the ranking in case of multi results in
	 * the same turn. (we need a better name for that variable but I am too tired to
	 * find one ^^)			TODO officially I guess this should actually be in EvalContext?
	 */
	private int numLossesDecided = 0;
	
	/** Same as above, but for wins */		// TODO officially I guess this should actually be in EvalContext?
	private int numWinsDecided = 0;

	//-----------------------Data modified in game.apply()--------------------------------------
	
	/** The current phase of each player. */
	private final int[] phases;
	
	/** The pending values. */
	private final TIntHashSet pendingValues;
	
	/** The counter. */
	private final int counter;
	
	/** The previous state in the same turn. */
	private final TLongArrayList previousStateWithinATurn;

	/** The previous state in case of no repetition rule. */
	private final TLongArrayList previousState;

	/** The index of the previous player. */
	private final int prev;
	
	/** The index of the mover. */
	private final int mover;

	/** The index of the next player. */
	private final int next;
	
	/** The number of times the mover has been switched to a different player. */
	private final int numTurn;
	
	/** The number of turns played successively by the same player. */
	private final int numTurnSamePlayer;
	
	/** Number of consecutive pass moves. */
	private int numConsecutivePasses = 0;
	
	/** All the remaining dominoes. */
	private FastTIntArrayList remainingDominoes;

	/**
	 * BitSet used to store all the site already visited (from & to) by each move
	 * done by the player in a sequence of turns played by the same player.
	 */
	private HashedBitSet visited = null;

	/** In case of a sequence of capture to remove (e.g. some draughts games). */
	private TIntArrayList sitesToRemove = null;
	
	/** To access where are each type of piece on each track. */
	private OnTrackIndices onTrackIndices;
	
	/** To access where are each type of piece on each track. */
	private Owned owned;

	//-------------------------------------------------------------------------
	
	/**
	 * @param ranking          	       The ranking of the players.
	 * @param status           	       The status of the game.
	 * @param winners          	       The players who've already won.
	 * @param losers           	       The players who've already lost.
	 * @param active           	       For every player, a bit indicating whether they are active.
	 * @param scores           	       Scores per player.
	 * @param payoffs          	       Payoffs per player.
	 * @param numLossesDecided 	       Number of losses decided.
	 * @param numWinsDecided   	       Number of wins decided.
	 * @param phases           	       The phases of each player.
	 * @param pendingValues    	       The pending values.
	 * @param counter          	       The counter of the state.
	 * @param previousStateWithinATurn The previous state in the same turn.
	 * @param previousState		 	   The previous state in case of no repetition rule.
	 * @param prev		 	           The index of the previous player.
	 * @param mover		 	           The index of the mover.
	 * @param next		 	           The index of the next player.
	 * @param numTurn		 	       The number of turns.
	 * @param numTurnSamePlayer		   The number of moves played so far in the same turn.
	 * @param numConsecutivePasses	   Number of consecutive pass moves.
	 * @param remainingDominoes		   All the remainingDominoes.
	 * @param visited	  			   Sites visited during the same turn.
	 * @param sitesToRemove		   	   Sites to remove in case of a sequence of capture.
	 * @param onTrackIndices		   To access where are each type of piece on each track.
	 * @param owned		  			   Access to list of sites for each kind of component owned per player.
	 */
	public UndoData
	(
		final double[] ranking,	
		final Status status,	
		final TIntArrayList winners,	
		final TIntArrayList losers,	
		final int active,	
		final int[] scores,	
		final double[] payoffs,	
		final int numLossesDecided,
		final int numWinsDecided,
		final int[] phases,
		final TIntHashSet pendingValues,
		final int counter,
		final TLongArrayList previousStateWithinATurn,
		final TLongArrayList previousState,
		final int prev,
		final int mover,
		final int next,
		final int numTurn,
		final int numTurnSamePlayer,
		final int numConsecutivePasses,
		final FastTIntArrayList remainingDominoes,
		final HashedBitSet visited,
		final TIntArrayList sitesToRemove,
		final OnTrackIndices onTrackIndices,
		final Owned owned
	)
	{
		this.ranking = Arrays.copyOf(ranking, ranking.length);
		this.status = status == null ? null : new Status(status);
		this.winners = new TIntArrayList(winners);
		this.losers = new TIntArrayList(losers);
		this.active = active;
		this.scores = scores == null ? null : Arrays.copyOf(scores, scores.length);
		this.payoffs = payoffs == null ? null : Arrays.copyOf(payoffs, payoffs.length);
		this.numLossesDecided = numLossesDecided;
		this.numWinsDecided = numWinsDecided;
		this.phases = phases == null ? null : Arrays.copyOf(phases, phases.length);
		this.pendingValues = pendingValues == null ? null : new TIntHashSet(pendingValues);
		this.counter = counter;
		this.previousStateWithinATurn = new TLongArrayList(previousState);
		this.previousState = new TLongArrayList(previousState);
		this.prev = prev;
		this.mover = mover;
		this.next = next;
		this.numTurn = numTurn;
		this.numTurnSamePlayer = numTurnSamePlayer;
		this.numConsecutivePasses = numConsecutivePasses;
		this.remainingDominoes = remainingDominoes == null ? null : new FastTIntArrayList(remainingDominoes);
		this.visited = visited == null ? null : visited.clone();
		this.sitesToRemove = sitesToRemove == null ? null : new TIntArrayList(sitesToRemove);
		this.onTrackIndices = onTrackIndices == null ? null : new OnTrackIndices(onTrackIndices);
		this.owned = owned == null ? null : owned.copy();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return The ranking.
	 */
	public double[] ranking()
	{
		return ranking;
	}
	
	/**
	 * @return The status.
	 */
	public Status status()
	{
		return status;
	}
	
	/**
	 * @return The winners.
	 */
	public TIntArrayList winners()
	{
		return winners;
	}
	
	/**
	 * @return The losers.
	 */
	public TIntArrayList losers()
	{
		return losers;
	}
	
	/**
	 * @return For each player a bit to indicate each player is active.
	 */
	public int active()
	{
		return active;
	}
	
	/**
	 * @return The scores of each player.
	 */
	public int[] scores()
	{
		return scores;
	}
	
	/**
	 * @return The payoffs of each player.
	 */
	public double[] payoffs()
	{
		return payoffs;
	}
	
	/**
	 * @return The number of losses decided.
	 */
	public int numLossesDecided()
	{
		return numLossesDecided;
	}
	
	/**
	 * @return The number of wins decided.
	 */
	public int numWinsDecided()
	{
		return numWinsDecided;
	}
	
	/**
	 * @return The phase of each player.
	 */
	public int[] phases()
	{
		return phases;
	}
	
	/**
	 * @return The phase of each player.
	 */
	public TIntHashSet pendingValues()
	{
		return pendingValues;
	}
	
	/**
	 * @return The counter.
	 */
	public int counter()
	{
		return counter;
	}
	
	/**
	 * @return The previous state in the same turn.
	 */
	public TLongArrayList previousStateWithinATurn()
	{
		return previousStateWithinATurn;
	}
	
	/**
	 * @return The previous state in case of no repetition rule.
	 */
	public TLongArrayList previousState()
	{
		return previousState;
	}
	
	/**
	 * @return The index of the previous player.
	 */
	public int prev()
	{
		return prev;
	}
	
	/**
	 * @return The index of the mover.
	 */
	public int mover()
	{
		return mover;
	}
	
	/**
	 * @return The index of the next player.
	 */
	public int next()
	{
		return next;
	}
	
	/**
	 * @return The number of times the mover has been switched to a different player.
	 */
	public int numTurn()
	{
		return numTurn;
	}
	
	/**
	 * @return The number of turns played successively by the same player.
	 */
	public int numTurnSamePlayer()
	{
		return numTurnSamePlayer;
	}
	
	/**
	 * @return Number of consecutive pass moves.
	 */
	public int numConsecutivePasses()
	{
		return numConsecutivePasses;
	}
	
	/**
	 * @return All the remaining dominoes.
	 */
	public FastTIntArrayList remainingDominoes()
	{
		return remainingDominoes;
	}
	
	/**
	 * @return Sites visited during the same turn.
	 */
	public HashedBitSet visited()
	{
		return visited;
	}
	
	/**
	 * @return Sites to remove in case of a sequence of capture.
	 */
	public TIntArrayList sitesToRemove()
	{
		return sitesToRemove;
	}
	
	/**
	 * @return To access where are each type of piece on each track. 
	 */
	public OnTrackIndices onTrackIndices()
	{
		return onTrackIndices;
	}
	
	/**
	 * @return Owned sites per component
	 */
	public Owned owned()
	{
		return owned;
	}
}
