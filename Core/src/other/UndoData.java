package other;

import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import main.Status;

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
	
	/** The counter*/
	private final int counter;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param ranking          The ranking of the players.
	 * @param status           The status of the game.
	 * @param winners          The players who've already won.
	 * @param losers           The players who've already lost.
	 * @param active           For every player, a bit indicating whether they are active.
	 * @param scores           Scores per player.
	 * @param payoffs          Payoffs per player.
	 * @param numLossesDecided Number of losses decided.
	 * @param numWinsDecided   Number of wins decided.
	 * @param phases           The phases of each player.
	 * @param pendingValues    The pending values.
	 * @param counter          The counter of the state.
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
		final int counter
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
}
